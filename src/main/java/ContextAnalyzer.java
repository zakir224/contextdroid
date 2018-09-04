package main.java;

import main.java.Permission.PermissionMapping;
import main.java.Permission.Permission;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
//import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.*;
import java.util.*;
import java.util.zip.ZipException;

public class ContextAnalyzer {


    private Collection<Permission> permissionSet;
    private final String androidPlatform;
    private final String appToAnalyze;
    private String datasetFile;
    private HashMap<String, Permission> permissionHashMap;
    private SetupApplication analyzer;
    private ParseCallGraph parseCallGraph;
    private ArrayList<SootMethod> listOfAppMethods;
    private HashMap<String, MethodContext> finalPermissionMapping;
    private HashMap<String, RequestMethodContext> finalRequestMapping;
    private String packageName;
    private ProcessManifest processManifest = null;
    private PermissionMethodParser permissionMethodParser;
    private Set<String> permissions;
    private String versionName;
    private int versionCode;
    private int targetSdk;


    public ContextAnalyzer(String androidPlatform, String appToAnalyze, String datasetFile) {
        System.out.println("Constructor: ContextAnalyzer\n\n");
        this.androidPlatform = androidPlatform;
        this.appToAnalyze = appToAnalyze;
        this.datasetFile = datasetFile;
        finalPermissionMapping = new HashMap<>();
        finalRequestMapping = new HashMap<>();

        initializeAnalyzer();
    }

    private void initializeAnalyzer() {
        System.out.println("Initializing: ContextAnalyzer\n\n");
        permissionHashMap = PermissionMapping.loadPermissionMapping();
        permissionSet = permissionHashMap.values();
        if (!extractManifestInfo())
            return;
        soot.G.reset();
        analyzer = new SetupApplication(androidPlatform, appToAnalyze);
        analyzer.getConfig().setEnableStaticFieldTracking(true); //no static field tracking --nostatic
        analyzer.getConfig().setAccessPathLength(5); // specify access path length
        analyzer.getConfig().setFlowSensitiveAliasing(true); // alias flowin
        analyzer.getConfig().setTaintAnalysisEnabled(false);
        analyzer.getConfig().setMergeDexFiles(true);
        analyzer.setCallbackFile("resources/AndroidCallbacks.txt");

        PackManager.v().getPack("cg");
        PackManager.v().getPack("jb");
        PackManager.v().getPack("wjap.cgg");
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(appToAnalyze));
        Options.v().set_android_jars(androidPlatform);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_app(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().setPhaseOption("cg", "safe-newinstance:true");
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("wjap.cgg", "show-lib-meths:true");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Scene.v().loadNecessaryClasses();
        System.out.println("Initialization done");
    }

    private boolean extractManifestInfo() {
        try {
            System.out.println("Extracting manifest info\n\n");
            try {
                processManifest = new ProcessManifest(appToAnalyze);
            } catch (ZipException | XmlPullParserException e) {

                writeResultToFile(appToAnalyze + "\t" + packageName + "\t" + versionCode
                        + "\t Zip file corrupted\t","failure.txt");
                return false;
            }
            packageName = processManifest.getPackageName();
            versionName = processManifest.getVersionName();
            versionCode = processManifest.getVersionCode();
            targetSdk = processManifest.targetSdkVersion();
            permissions = processManifest.getPermissions();
            System.out.println("Permission Count: " + permissions.size()+"\n");
            String permissionList = "";
            for (String per :
                    permissions) {
                permissionList = permissionList.concat(":").concat(per);
            }
            writeResultToFile(packageName.concat("\t").concat(permissionList).concat("\t" + appToAnalyze), datasetFile + "_permissions");
            System.out.println("Permission List: " + permissionList+"\n");

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean startSootAnalysis(boolean shouldRunInfoFlow) {
        System.out.println("Starting soot analysis..\n\n");
        try {
            PackManager.v().runPacks();
            analyzer.constructCallgraph();
        } catch (Exception e) {
            try {
                writeResultToFile(appToAnalyze + "\t" + packageName + "\t" + versionCode
                        + "\t Failed to construct call graph\t","failure.txt");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
//        if(shouldRunInfoFlow) {
//            try {
//                analyzer.runInfoflow(new HashSet<>(), new HashSet<>());
//            } catch (IOException | XmlPullParserException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
        return true;
    }

    private void startCallGraphAnalysis() {
        System.out.println("Starting call graph analysis..\n\n");
        try {
            parseCallGraph = new ParseCallGraph(Scene.v().getCallGraph());
        } catch (RuntimeException e) {
            try {
                writeResultToFile(appToAnalyze + "\t" + packageName + "\t" + versionCode
                        + "\t Failed to construct call graph\t","failure.txt");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        parseCallGraph.setClassList(Scene.v().getApplicationClasses());
        parseCallGraph.setAppPackageName(packageName);
        listOfAppMethods = parseCallGraph.listMethods(true);
    }

    public void startAnalysis(boolean shouldRunInfoFlow) {
        System.out.println("Starting analysis..\n\n");
        if(startSootAnalysis(shouldRunInfoFlow)) {
            startCallGraphAnalysis();
            extractPermissionUsageContext();
            writeUsageOutput();
            writeRequestOutput();
            clearMemory();
            listOfAppMethods.clear();
        }
    }

    private void clearMemory() {
        System.gc();
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
                eventType = eventType + callerMethod.getMethodName() +", " + callerMethod.getVisibilityType() + ";";
            }
            String finalString = packageName + "\t" + versionName + "\t" + versionCode + "\t" + targetSdk + "\t" +
                    className + "\t" + methodName + "\t" + permission + "\t" + visibilityType + "\t" + eventType + "\t" + appToAnalyze;
            try {
                writeResultToFile(finalString, datasetFile + "_output_request.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finalPermissionMapping.clear();
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
                if(sign.contains("onRequestPermissionsResult") || sign.contains("(int,java.lang.String[],int[])")) {
                    onRequestPermissionResult = "true";
                }
                eventType = eventType + callerMethod.getMethodName() +", " + callerMethod.getVisibilityType() + ";";

            }
            if(methodContext.toString().contains("onRequestPermissionsResult")
                    || methodContext.toString().contains("(int,java.lang.String[],int[])")) {
                onRequestPermissionResult = "true";
            }
            String finalString = packageName + "\t" + versionName + "\t" + versionCode + "\t" + targetSdk + "\t" +
                    className + "\t" +methodName + "\t" +permission + "\t" +invokedMethod + "\t" + visibilityType
                    + "\t" +onRequestPermissionResult + "\t" + eventType + "\t" + appToAnalyze;
            try {
                writeResultToFile(finalString, datasetFile + "_output_usage.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finalPermissionMapping.clear();
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
        if(requestMethodContext != null && !finalRequestMapping.containsKey(requestMethodContext.toString())) {
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
