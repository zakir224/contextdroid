package main.java;

import main.java.Permission.PSCoutPermissionMap;
import main.java.Permission.Permission;
import main.java.Util.CommonUtil;
import main.java.Util.ManifestUtil;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import sun.plugin2.util.SystemUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ContextAnalyzer2 {


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

    public ContextAnalyzer2(String androidPlatform, String appToAnalyze, String datasetFile) {
        System.out.println("Constructor: ContextAnalyzer\n\n");
        this.datasetFile = datasetFile;
        finalPermissionMapping = new HashMap<>();
        finalRequestMapping = new HashMap<>();

        initializeAnalyzer(androidPlatform, appToAnalyze);
    }

    private void initializeAnalyzer(String androidPlatform, String appToAnalyze) {
        System.out.println("Initializing: ContextAnalyzer\n\n");
        permissionHashMap = PSCoutPermissionMap.getInstance().loadPermissionMapping("resources/pscout/pscout411.txt");
        permissionSet = permissionHashMap.values();
        flowDroidCallGraph = new FlowDroidCallGraph(androidPlatform, appToAnalyze);
        parseCallGraph = new ParseCallGraph();
        CommonUtil.deleteFileIfExists("time_stats.txt");
        try {
            CommonUtil.write("app\tfileSize\tcallGraphGenerationTime\tlistingMethodsTime" +
                    "\tcontextExtractionTime\tnumberOfClasses\tnumberOfMethods", "time_stats.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Initialization done");
    }

    private boolean processCallGraph(String apkName) {
        if (flowDroidCallGraph != null) {
            System.out.println("Starting Flowdroid...");

            startTime = System.currentTimeMillis();
            CallGraph callGraph = flowDroidCallGraph.getCallGraph(apkName);
            if (callGraph == null) {
                return false;
            }
            parseCallGraph.init().setCallGraph(callGraph);
            endTime = System.currentTimeMillis();
            statistic.setCallGraphGenerationTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
            System.out.println("CallGraph initialization " +
                    "by FlowDroid took: " + statistic.getCallGraphGenerationTime() + " Seconds");

            parseCallGraph.setAppPackageName(appMetaData.getPackageName());

            System.out.println("Listing methods inside the callgraph...");
            startTime = System.currentTimeMillis();
            listOfAppMethods = parseCallGraph.listMethods();
            endTime = System.currentTimeMillis();
            statistic.setNumberOfMethods(listOfAppMethods.size());
            statistic.setListingMethodsTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
            System.out.println("CallGraph initialization took: " + statistic.getListingMethodsTime() + " Seconds");

            return true;
        } else {
            return false;
        }
    }


    public void start(String apkName) {
        System.out.println("Starting analysis..");
        if (updateAppMetadata(apkName) && processCallGraph(apkName)) {
            System.out.println("Extracting contexts...");
            startTime = System.currentTimeMillis();
            extractPermissionUsageContext();
            endTime = System.currentTimeMillis();
            statistic.setContextExtractionTime(CommonUtil.getTimeDifferenceInSeconds(startTime, endTime));
            System.out.println("Context Extraction took: " + statistic.getContextExtractionTime() + " Seconds");
            writeUsageOutput();
            writeRequestOutput();
            writeTimeStat();
        } else {
            System.out.println("Callgraph generation failed for: " + appMetaData.getPackageName());
        }
    }

    private void writeTimeStat() {
        try {
            CommonUtil.write(statistic.toString(), "time_stats.txt");
        } catch (IOException e) {
            e.printStackTrace();
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

    private void writeRequestOutput() {
        Iterable<RequestMethodContext> values = finalRequestMapping.values();
        for (RequestMethodContext methodContext :
                values) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String permission = methodContext.getPermission();
            String eventType = "";//methodContext.getEventType();
            String visibilityType = methodContext.getVisibilityType().toString();

            for (CallerMethod callerMethod :
                    methodContext.getCallerMethodList()) {
                eventType = eventType + callerMethod.getMethodName() + ", " + callerMethod.getVisibilityType() + ";";
            }
            String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t" + appMetaData.getTargetSdk() + "\t" +
                    className + "\t" + methodName + "\t" + permission + "\t" + visibilityType + "\t" + eventType + "\t" + appMetaData.getSha256();
            try {
                writeResultToFile(finalString, datasetFile + "_output_request.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeUsageOutput() {
        Iterable<MethodContext> values = finalPermissionMapping.values();
        for (MethodContext methodContext :
                values) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String permission = methodContext.getPermission().getPermission();
            String invokedMethod = methodContext.getPermission().getMethodSignature();
            String eventType = "";//methodContext.getEventType();
            String visibilityType = methodContext.getVisibilityType().toString();
            String onRequestPermissionResult = "false";
            for (CallerMethod callerMethod :
                    methodContext.getCallerMethodList()) {
                String sign = callerMethod.getMethodName();
                if (sign.contains("onRequestPermissionsResult") || sign.contains("(int,java.lang.String[],int[])")) {
                    onRequestPermissionResult = "true";
                }
                eventType = eventType + callerMethod.getMethodName() + ", " + callerMethod.getVisibilityType() + ";";

            }
            if (methodContext.toString().contains("onRequestPermissionsResult")
                    || methodContext.toString().contains("(int,java.lang.String[],int[])")) {
                onRequestPermissionResult = "true";
            }
            String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t" + appMetaData.getTargetSdk() + "\t" +
                    className + "\t" + methodName + "\t" + permission + "\t" + invokedMethod + "\t" + visibilityType
                    + "\t" + onRequestPermissionResult + "\t" + eventType + "\t" + appMetaData.getSha256();
            try {
                writeResultToFile(finalString, datasetFile + "_output_usage.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractPermissionUsageContext() {
        for (SootMethod method :
                listOfAppMethods) {
            checkPermission(method);
        }
    }

    private void checkPermission(SootMethod sootMethod) {

        try {
            String methodBody = sootMethod.getActiveBody().toString();

            for (Permission s : permissionSet) {
                extractRationale(sootMethod);
                extractPermissionUsage(s, methodBody, sootMethod);
                extractPermissionRequest(sootMethod);
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


    private void writeResultToFile(String s, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(s).append("\n");
        writer.close();
    }

}
