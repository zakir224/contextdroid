package main.java;

import main.java.Permission.PSCoutPermissionMap;
import main.java.Permission.Permission;
import main.java.Util.CommonUtil;
import main.java.Util.ManifestUtil;
import main.java.Util.OutputUtil;
import main.java.debug.Log;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
//import sun.plugin2.util.SystemUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ContextDroid {


    private static Collection<Permission> permissionSet;
    private String datasetFile;
    private HashMap<String, Permission> permissionHashMap;
    private ParseCallGraph parseCallGraph;
    private ArrayList<SootMethod> listOfAppMethods;
    private HashMap<String, MethodContext> finalPermissionMapping;
    private HashMap<String, RequestMethodContext> finalRequestMapping;
    private FlowDroidCallGraph flowDroidCallGraph;
    private AppMetaData appMetaData;
    private long startTime;
    private long endTime;
    private ApkProcessingStatistic statistic;

    public ContextDroid(String androidPlatform, String appToAnalyze) {
        Log.d(appToAnalyze,"Constructor: ContextAnalyzer....", true);
        this.datasetFile = OutputUtil.getFolderPath(appToAnalyze);
        finalPermissionMapping = new HashMap<>();
        finalRequestMapping = new HashMap<>();

        initializeAnalyzer(androidPlatform, appToAnalyze);
    }

    private void initializeAnalyzer(String androidPlatform, String appToAnalyze) {
        Log.d(appToAnalyze, "Initializing: ContextAnalyzer...", true);
        permissionHashMap = PSCoutPermissionMap.getInstance().loadPermissionMapping("resources/pscout/pscout411.txt");
        permissionSet = permissionHashMap.values();
        flowDroidCallGraph = new FlowDroidCallGraph(androidPlatform, appToAnalyze);
        parseCallGraph = new ParseCallGraph();
        OutputUtil.initOutputFiles(datasetFile);
        OutputUtil.intiStat(datasetFile);
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
        CallGraph callGraph = flowDroidCallGraph.getCallGraph(apkName);
        if (callGraph == null) {
            return false;
        }
        parseCallGraph.init().setCallGraph(callGraph);
        endTime = System.currentTimeMillis();
        statistic.setCallGraphGenerationTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
        parseCallGraph.setAppPackageName(appMetaData.getPackageName());
        Log.d(apkName,"CallGraph initialization " +
                "by FlowDroid took: " + statistic.getCallGraphGenerationTime() + " Seconds", true);
        return true;
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
        } else {
            Log.d(apkName,"Callgraph generation failed for: " + appMetaData.getPackageName(), true);
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
            extractPermissionRequest(method);
        }
    }

    private void checkPermission(SootMethod sootMethod) {

        try {
            String methodBody = sootMethod.getActiveBody().toString();

            for (Permission s : permissionSet) {
                //extractRationale(sootMethod);
                extractPermissionUsage(s, methodBody, sootMethod);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void extractRationale(SootMethod sootMethod) {
        parseCallGraph.extractRationale(sootMethod);
    }

    private void extractPermissionRequest(SootMethod sootMethod) {
        RequestMethodContext requestMethodContext = parseCallGraph.extractPermissionCheckAndRequest(sootMethod);
        if (requestMethodContext != null && !finalRequestMapping.containsKey(requestMethodContext.toString())) {
            finalRequestMapping.put(sootMethod.getSignature(), requestMethodContext);
        }
    }

    private void extractPermissionUsage(Permission p, String methodBody, SootMethod sootMethod) {
        if (methodBody.contains(p.getPackageName()) && methodBody.contains(p.getMethodSignature())) {
            MethodContext methodContext = parseCallGraph.extractMethodProperties(sootMethod, p);
            finalPermissionMapping.put(sootMethod.getSignature().concat(" " + p), methodContext);
        }
    }

}
