package main.java;

import main.java.Util.CommonUtil;
import main.java.Util.ManifestUtil;
import main.java.debug.Log;
import soot.*;
import soot.dexpler.DexResolver;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

import java.util.Collections;

public class FlowDroidCallGraph {


//    private SetupApplication applicationAnalyzer;

//    public static FlowDroidCallGraph flowDroidCallGraph;
    private String androidPlatform;
    private SetupApplication anayzer;


    public FlowDroidCallGraph(String androidPlatform, String apkName) {
        init(androidPlatform, apkName);
    }

    private void init(String androidPlatform, String apkName) {
        this.androidPlatform = androidPlatform;
    }

    private CallGraph constructCallGraph (SetupApplication setupApplication, String apkName)
            throws SootMethodRefImpl.ClassResolutionFailedException,NullPointerException {
        PackManager.v().getPack("cg");
        PackManager.v().getPack("jb");
        PackManager.v().getPack("wjap.cgg");
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apkName));
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
        try {
            Scene.v().loadNecessaryClasses();
        } catch (AndroidPlatformException e) {
            Log.e(apkName,"AndroidPlatformException: " + apkName + e.getMessage(), true);
            return null;
        }
        Log.d(apkName,"Constructing call graph...", true);
        try {
            PackManager.v().runPacks();
            setupApplication.constructCallgraph();
            Log.d(apkName,"Call graph construction successful. Retrieving the callgraph..", true);
            return Scene.v().getCallGraph();
        } catch (ResolutionFailedException e) {
            Log.e(apkName,"ResolutionFailedException: " + apkName  + "\t" + e.getMessage(), true);
            return null;
        } catch (ClassCastException e) {
            Log.e(apkName,"ClassCastException: " + apkName  + "\t" + e.getMessage(), true);
            return null;
        } catch (RuntimeException e) {
            Log.e(apkName,"RuntimeException: " + apkName  + "\t" + e.getMessage(), true);
            return null;
        }
    }

    public CallGraph getCallGraph(String apkName) {
        try {
            G.reset();
            SetupApplication applicationAnalyser = getApplicationAnalyser(apkName);
            return constructCallGraph(applicationAnalyser, apkName);
        } catch (SootMethodRefImpl.ClassResolutionFailedException e) {
            Log.e(apkName, "Loading classes from the apk failed: " + e.getMessage(), true);
            return null;
        } catch (NullPointerException e) {
            Log.e(apkName,"NullPointedException: " + e.getMessage(), true);
            return null;
        }
    }

    private SetupApplication getApplicationAnalyser(String apkName) {

        SetupApplication applicationAnalyzer = new SetupApplication(androidPlatform, apkName);
        anayzer = applicationAnalyzer;
        applicationAnalyzer.getConfig().setEnableStaticFieldTracking(true);
        applicationAnalyzer.getConfig().setFlowSensitiveAliasing(true); // alias flowin
        applicationAnalyzer.getConfig().setTaintAnalysisEnabled(false);
        applicationAnalyzer.getConfig().setMergeDexFiles(true);
        applicationAnalyzer.setCallbackFile("resources/AndroidCallbacks.txt");

        return applicationAnalyzer;
    }

    public Chain<SootClass> getApplicationClasses() {
        return Scene.v().getApplicationClasses();
    }

    public void reset() {
        G.reset();
        Scene.v().setDoneResolving();
        anayzer.abortAnalysis();
        anayzer = null;
    }
}
