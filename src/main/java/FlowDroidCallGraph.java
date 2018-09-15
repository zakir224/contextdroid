package main.java;

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


//    public static FlowDroidCallGraph getInstance(String androidPlatform, String apkName) {
//
//        if (flowDroidCallGraph == null)
//            flowDroidCallGraph = new FlowDroidCallGraph(androidPlatform, apkName);
//
//        return flowDroidCallGraph;
//    }

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
        Scene.v().loadNecessaryClasses();
        System.out.println("Constructing call graph...");
        PackManager.v().runPacks();
        setupApplication.constructCallgraph();
        System.out.println("Call graph construction successful. Retrieving the callgraph..");
        return Scene.v().getCallGraph();
    }

    public CallGraph getCallGraph(String apkName) {
        try {
            G.reset();
            SetupApplication applicationAnalyser = getApplicationAnalyser(apkName);
            return constructCallGraph(applicationAnalyser, apkName);
        } catch (SootMethodRefImpl.ClassResolutionFailedException e) {
            System.out.println("Loading classes from the apk failed: " + e.getMessage());
            return null;
        } catch (NullPointerException e) {
            System.out.println("NullPointedException: " + e.getMessage());
            return null;
        }
    }

    private SetupApplication getApplicationAnalyser(String apkName) {

        SetupApplication applicationAnalyzer = new SetupApplication(androidPlatform, apkName);
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
}
