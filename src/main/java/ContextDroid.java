package main.java;

import main.java.Permission.PSCoutPermissionMap;
import main.java.Permission.Permission;
import main.java.Util.CommonUtil;
import main.java.Util.ManifestUtil;
import main.java.Util.OutputUtil;
import main.java.Util.PermissionUtil;
import main.java.debug.Log;
import org.omg.CORBA.TIMEOUT;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
//import sun.plugin2.util.SystemUtil;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.*;


public class ContextDroid {


    private static Collection<Permission> permissionSet;
    private String datasetFile;
    private HashMap<String, Permission> permissionHashMap;
    private ParseCallGraph parseCallGraph;
    private ArrayList<SootMethod> listOfAppMethods;
    private HashMap<String, MethodContext> finalPermissionMapping;
    private HashMap<String, RequestMethodContext> finalRequestMapping;
    private HashMap<String, MethodContext> rationale;
    private FlowDroidCallGraph flowDroidCallGraph;
    private AppMetaData appMetaData;
    private long startTime;
    private long endTime;
    private ApkProcessingStatistic statistic;
    private HashMap<String, ArrayList<String>> permissionToRationale;
    private HashMap<String, String> serviceInitiator;

    public ContextDroid(String androidPlatform, String appToAnalyze, boolean restart) {
        Log.d(appToAnalyze,"Constructor: ContextDroid....", true);
        this.datasetFile = OutputUtil.getFolderPath(appToAnalyze);
        finalPermissionMapping = new HashMap<>();
        finalRequestMapping = new HashMap<>();
        permissionToRationale = new HashMap<>();
        serviceInitiator = new HashMap<>();

        initializeAnalyzer(androidPlatform, appToAnalyze, restart);
    }

    private void initializeAnalyzer(String androidPlatform, String appToAnalyze, boolean restart) {
        Log.d(appToAnalyze, "Initializing: ContextDroid...", true);
        permissionHashMap = PSCoutPermissionMap.getInstance().loadPermissionMapping("resources/pscout/pscout411.txt");
        permissionSet = permissionHashMap.values();
        flowDroidCallGraph = new FlowDroidCallGraph(androidPlatform, appToAnalyze);
        parseCallGraph = new ParseCallGraph();
        if(!restart) {
            OutputUtil.initOutputFiles(datasetFile);
            OutputUtil.intiStat(datasetFile);
        }
        Log.d(appToAnalyze, "Initialization done", true);
    }

    private boolean processCallGraph(String apkName) {
        if (flowDroidCallGraph != null) {
            Log.d(apkName,"Starting FlowDroid...", true);

            if(initCallGraph(apkName)) {
                extractMethodList(apkName);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void extractMethodList(String apkName) {
        Log.d(apkName,"Listing methods inside the callgraph...", true);
        startTime = System.currentTimeMillis();
        listOfAppMethods = parseCallGraph.listMethods();
        endTime = System.currentTimeMillis();
        statistic.setNumberOfMethods(listOfAppMethods.size());
        statistic.setListingMethodsTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
        Log.d(apkName, "CallGraph initialization took: " + statistic.getListingMethodsTime() + " Seconds"
                , true);
    }

    private boolean initCallGraph(String apkName) {
        startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<CallGraph> future = null;
        Callable<CallGraph> task = () -> flowDroidCallGraph.getCallGraph(apkName);

        future = executor.submit(task);
        try {
            CallGraph callGraph = future.get(10, TimeUnit.MINUTES);
            parseCallGraph.init().setCallGraph(callGraph);
            endTime = System.currentTimeMillis();
            statistic.setCallGraphGenerationTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
            parseCallGraph.setAppPackageName(appMetaData.getPackageName());
            Log.d(apkName,"CallGraph initialization " +
                    "by FlowDroid took: " + statistic.getCallGraphGenerationTime() + " Seconds", true);
            return callGraph != null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            future.cancel(true);
            executor.shutdownNow();
            flowDroidCallGraph.reset();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            future.cancel(true);
            executor.shutdownNow();
            Log.e(apkName,"Timeout:\t" + apkName, true);
            flowDroidCallGraph.reset();
            return false;
        } catch (Exception e) {
            return false;
        }

    }


    public void start(String apkName) {
        Log.d(apkName,"Starting analysis..", true);
        if (updateAppMetadata(apkName) && processCallGraph(apkName)) {
            Log.d(apkName,"Extracting contexts.....", true);
            startTime = System.currentTimeMillis();
            extractPermissionUsageContext();
            endTime = System.currentTimeMillis();
            statistic.setContextExtractionTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
            Log.d(apkName,"Context Extraction took: " + statistic.getContextExtractionTime() + " Seconds", true);
            OutputUtil.writeUsageOutput(finalPermissionMapping, appMetaData, datasetFile);
            OutputUtil.writeRequestOutput(finalRequestMapping, appMetaData, datasetFile);
            OutputUtil.writeTimeStat(statistic, datasetFile);
            OutputUtil.writePermissions(appMetaData, datasetFile);
            OutputUtil.writeRationale(appMetaData, permissionToRationale, datasetFile);
            OutputUtil.writeServiceInitiator(appMetaData, serviceInitiator, datasetFile);
            finalPermissionMapping.clear();
            finalRequestMapping.clear();
            serviceInitiator.clear();
            permissionToRationale.clear();
            appMetaData = null;
        } else {
            try {
                Log.d(apkName,"CallGraph generation failed for: " + appMetaData.getPackageName(), true);
            } catch (NullPointerException e) {
                Log.d(apkName,"CallGraph generation failed for: " + apkName, true);
            }
        }
    }

    private boolean updateAppMetadata(String apkName) {

        try {
            appMetaData = ManifestUtil.extractManifestInfo(apkName);
            statistic = new ApkProcessingStatistic();
            statistic.setApp(appMetaData.getPackageName());
            statistic.setFileSize(CommonUtil.getFileSize(apkName));
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void extractPermissionUsageContext() {
        for (SootMethod method :
                listOfAppMethods) {
            checkPermission(method);
            getServiceInitiator(method);
            extractPermissionRequest(method);
        }
    }

    private void checkPermission(SootMethod sootMethod) {

        try {
            String methodBody = sootMethod.getActiveBody().toString();

            for (Permission s : permissionSet) {
                extractPermissionUsage(s, methodBody, sootMethod);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void extractRationale(SootMethod sootMethod, String perm) {
        perm = PermissionUtil.removeDuplicatePermission(perm.replace("\"",""));
        String[] multiplePermission = perm.split(",");
        for (String permission: multiplePermission) {
            if(parseCallGraph.extractRationale(sootMethod)) {
                System.out.println("Check rationale found in :" + sootMethod.getSignature() + " " + permission);
                if(permissionToRationale.containsKey(permission)) {
                    permissionToRationale.get(permission).add(sootMethod.getSignature());
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(sootMethod.getDeclaringClass() + ": " + sootMethod.getName());
                    permissionToRationale.put(permission, list);
                }
            }
        }
    }

    private void extractPermissionRequest(SootMethod sootMethod) {
        RequestMethodContext requestMethodContext = parseCallGraph.extractPermissionCheckAndRequest(sootMethod);
        if (requestMethodContext != null && !finalRequestMapping.containsKey(requestMethodContext.toString())) {
            finalRequestMapping.put(sootMethod.getSignature(), requestMethodContext);
            extractRationale(sootMethod,requestMethodContext.getPermission());
        }
    }

    private void extractPermissionUsage(Permission p, String methodBody, SootMethod sootMethod) {
        if (methodBody.contains(p.getPackageName()) && methodBody.contains(p.getMethodSignature())) {
            MethodContext methodContext = parseCallGraph.extractMethodProperties(sootMethod, p);
            finalPermissionMapping.put(sootMethod.getSignature().concat(" " + p), methodContext);
        }
    }

    private void getServiceInitiator(SootMethod sootMethod) {
        String s = sootMethod.getActiveBody().toString();
        if(s.contains("startService(android.content.Intent)")) {
            s = s.replace("/",".");
            for (String service : appMetaData.getServices()) {
                if(s.contains(service)) {
                    serviceInitiator.put(service, sootMethod.getSignature());
                }
            }
        }

    }

}
