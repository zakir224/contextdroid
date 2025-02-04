package main.java;

import com.google.common.base.Strings;
import main.java.Permission.Permission;
import main.java.Util.PermissionUtil;
import main.java.debug.Log;
import soot.*;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.toolkits.graph.DirectedGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ParseCallGraph {

    private HashMap<Object, Object> permissionCheckerMethods;
    private CallGraph callGraph;
    private HashMap<String, Integer> eventCallBlackClasses;
    private MethodContext methodContext;
    private RequestMethodContext requestMethodContext;
    private String appPackageName;

    private ParseCallGraph parseCallGraph;

//    public ParseCallGraph getInstance() {
//        if (parseCallGraph == null) {
//            parseCallGraph = new ParseCallGraph();
//        }
//        return parseCallGraph;
//    }

    public ParseCallGraph() {

    }

    public ParseCallGraph init() {
        eventCallBlackClasses = new HashMap<>();
        permissionCheckerMethods = new HashMap<>();
        loadCallbacks();

        return this;
    }

    public void setCallGraph(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    /*
        isSelf tells whether this is the original method. if yes it's not added to the called methods list.
         */
    public void retrieveCallers(SootMethod sootMethod, MethodContext methodContext, boolean isSelf) {
        Iterator sources = new Sources(callGraph.edgesInto(sootMethod));

        if (sources.hasNext()) {
            try {
                if (!isSelf && !sootMethod.getSignature().contains("dummyMain")) {
                    CallerMethod callerInfo = getCallerInfo(sootMethod);
                    if (callerInfo.getVisibilityType() != CallerType.CUSTOM) {
                        methodContext.getCallerMethodList().add(callerInfo);
                    }
                }
                retrieveCallers((SootMethod) sources.next(), methodContext, false);
            } catch (StackOverflowError e) {
                writeResultToFile(e.getMessage());
                return;
            }

        } else {
            if (!isSelf && !sootMethod.getSignature().contains("dummyMain"))
                methodContext.getCallerMethodList().add(getCallerInfo(sootMethod));
        }
    }

    private void retrieveRequestCallers(SootMethod sootMethod, RequestMethodContext methodContext, boolean isSelf) {
        Iterator sources = new Sources(callGraph.edgesInto(sootMethod));

        if (sources.hasNext()) {
            if (!isSelf && !sootMethod.getSignature().contains("dummyMain")) {
                CallerMethod callerInfo = getCallerInfo(sootMethod);
                if (callerInfo.getVisibilityType() != CallerType.CUSTOM) {
                    methodContext.getCallerMethodList().add(callerInfo);
                }
            }
            retrieveRequestCallers((SootMethod) sources.next(), methodContext, false);

        } else {
            if (!isSelf && !sootMethod.getSignature().contains("dummyMain"))
                methodContext.getCallerMethodList().add(getCallerInfo(sootMethod));
        }
    }

    private boolean callerExists(CallerMethod callerInfo, List<CallerMethod> callerMethodList) {
        for (CallerMethod callerMethod :
                callerMethodList) {
            if (callerMethod.getPackageName().equals(callerInfo.getPackageName()) && callerMethod.getClassName().equals(callerInfo.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private CallerMethod getCallerInfo(SootMethod sootMethod) {
        CallerMethod callerMethod = null;
        try {
            //String eventType = extractEventInfo(sootMethod);
            String className = sootMethod.getDeclaringClass().toString();
            String methodName = sootMethod.getName();
            String packageName = sootMethod.getDeclaringClass().getPackageName();
            CallerType visibilityType = getComponentType(sootMethod.getDeclaringClass());
            if (visibilityType == CallerType.CUSTOM) {
                visibilityType = checkIfButtonClick(sootMethod);
            }


            callerMethod = new CallerMethod();
            callerMethod.setClassName(className);
            callerMethod.setMethodName(methodName);
            callerMethod.setPackageName(packageName);
            callerMethod.setVisibilityType(visibilityType);
        } catch (Exception e) {
            writeResultToFile(appPackageName + "\t" + e.getMessage());
        }

        return callerMethod;
    }

    public ArrayList<SootMethod> listMethods() {
        ArrayList<SootMethod> listOfMethods = new ArrayList<>();
        listMethodsFromCallGraph(listOfMethods);
        listOfMethods = listSupportFragments(listOfMethods);
        return listOfMethods;
    }

    private ArrayList<SootMethod> listSupportFragments(ArrayList<SootMethod> listOfMethods) {
        ArrayList<SootMethod> newList = new ArrayList<>();
        for (SootMethod sootMethod :
                listOfMethods) {
            SootClass superclass = null;
            try {
                superclass = sootMethod.getDeclaringClass().getSuperclass();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if (superclass != null) {
                if (superclass.getPackageName().contains("android.support")) {
                    if (checkIfFragment(sootMethod.getDeclaringClass().getMethods())) {
                        List<SootMethod> list = sootMethod.getDeclaringClass().getMethods();
                        for (SootMethod method :
                                list) {
                            try {
                                Body body = method.retrieveActiveBody();
                                method.setActiveBody(body);
                                newList.add(method);
                            } catch (Exception e) {
                                //e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        listOfMethods.addAll(newList);
        return listOfMethods;
    }

    private boolean checkIfFragment(List<SootMethod> methods) {
        boolean onCreateViewSign = false;
        for (SootMethod method :
                methods) {
            String returnType = method.getReturnType().toString();
            if (method.getSignature().contains("android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle")
                    ) {
                onCreateViewSign = true;
            }
        }
        return onCreateViewSign;
    }

    private void listMethodsFromCallGraph(ArrayList<SootMethod> listOfMethods) {
        Iterator<MethodOrMethodContext> tr;
        try {
            tr = callGraph.sourceMethods();
        } catch (Exception e) {
            Log.e(getAppPackageName(), "Callgraph null while listing methods", true);
            return;
        }
        HashMap<String, Integer> listOfVisitedMethods = new HashMap<>();

        while (tr.hasNext()) {
            SootMethod mainSourceMethod = (SootMethod) tr.next();
            if (!listOfVisitedMethods.containsKey(mainSourceMethod.getSignature())) {
                listOfVisitedMethods.put(mainSourceMethod.getSignature(), 1);
                listOfMethods.add(mainSourceMethod);
            }

            listParentMethods(listOfMethods, mainSourceMethod, listOfVisitedMethods);
            listChildMethods(listOfMethods, mainSourceMethod, listOfVisitedMethods);
        }
    }

    private void listChildMethods(ArrayList<SootMethod> listOfMethods, SootMethod mainSourceMethod, HashMap<String, Integer> listOfVisitedMethods) {
        Iterator sources = new Sources(callGraph.edgesOutOf(mainSourceMethod));
        while (sources.hasNext()) {
            SootMethod callerMethod = (SootMethod) sources.next();
            if (!listOfVisitedMethods.containsKey(callerMethod.getSignature())) {
                listOfVisitedMethods.put(callerMethod.getSignature(), 1);
                listOfMethods.add(callerMethod);
            }
        }
    }

    private void listParentMethods(ArrayList<SootMethod> listOfMethods,
                                   SootMethod mainSourceMethod, HashMap<String, Integer> listOfVisitedMethods) {
        Iterator sources = new Sources(callGraph.edgesInto(mainSourceMethod));

        while (sources.hasNext()) {
            SootMethod callerMethod = (SootMethod) sources.next();
            try {
                if (!listOfVisitedMethods.containsKey(callerMethod.getSignature())) {
                    listOfVisitedMethods.put(callerMethod.getSignature(), 1);
                    listOfMethods.add(callerMethod);
                }
            } catch (RuntimeException e) {
                Log.e("", e.getMessage(), true);
            }
        }
    }

    /*
     **  Method that identifies whether an api call is result of an event.
     */
    public String extractEventInfo(SootMethod sootMethod) {
        String typeFromMethodSign = retrieveCallerEventTypeFromMethodSign(sootMethod);
        if (Strings.isNullOrEmpty(typeFromMethodSign)) {
            return retrieveCallerEventType(sootMethod);
        } else {
            return typeFromMethodSign;
        }
    }

    private String retrieveCallerEventTypeFromMethodSign(SootMethod sootMethod) {
        String signature = sootMethod.getSignature();
        for (String callBack : eventCallBlackClasses.keySet()) {
            if (signature.contains(callBack)) {
                return callBack;
            }
        }
        return "";
    }

    private String retrieveCallerEventType(SootMethod sootMethod) {

        String eventType = getEventType(sootMethod.getDeclaringClass());

        if (!Strings.isNullOrEmpty(eventType)) {
            return eventType;
        } else {
            Iterator sources = new Sources(callGraph.edgesInto(sootMethod));

            if (sources.hasNext()) {
                retrieveCallerEventType((SootMethod) sources.next());
            }

            return "";
        }
    }

    private String getEventType(SootClass mClass) {
        String eventType;
        try {
            eventType = getEvent(mClass);
            while (eventType == null && mClass.hasSuperclass()) {
                mClass = mClass.getSuperclass();
                eventType = getEvent(mClass);
            }

            if (eventType == null) {
                eventType = "";
            }

        } catch (StackOverflowError e) {
            //e.printStackTrace();
            return "";
        }

        return eventType;
    }

    private String getEvent(SootClass mClass) {
        String mClassName = mClass.getName();
        String mClassPackageName = mClass.getPackageName();
        for (String callBack : eventCallBlackClasses.keySet()) {
            if (mClassName.contains(callBack) || mClassPackageName.contains(callBack)) {
                return callBack;
            }
        }
        return null;
    }

    public MethodContext extractMethodProperties(SootMethod sootMethod, Permission permission) {

        boolean isLibraryMethod = false;
        SootClass declaringClass = sootMethod.getDeclaringClass();

        CallerType classType = getComponentType(declaringClass);
        if (classType == CallerType.CUSTOM) {
            classType = checkIfButtonClick(sootMethod);
        }
        String packageName = declaringClass.getPackageName();
        String name = sootMethod.getName();
        String className = declaringClass.getName();
        //String eventType = extractEventInfo(sootMethod);
        if (!declaringClass.getPackageName().contains(appPackageName)) {
            isLibraryMethod = true;
        }

        MethodContext methodContext = new MethodContext();
        methodContext.setVisibilityType(classType);
        methodContext.setPackageName(packageName);
        methodContext.setClassName(className);
        methodContext.setMethodName(name);
        methodContext.setLibaryMethod(isLibraryMethod);
        methodContext.setPermission(permission);

        retrieveCallers(sootMethod, methodContext, true);
        return methodContext;
    }


    private RequestMethodContext extractRequestMethodProperties(SootMethod sootMethod, String permission) {

        try {
            boolean isLibraryMethod = false;
            SootClass declaringClass = sootMethod.getDeclaringClass();

            CallerType classType = getComponentType(declaringClass);
            if (classType == CallerType.CUSTOM) {
                classType = checkIfButtonClick(sootMethod);
            }
            String packageName = declaringClass.getPackageName();
            String name = sootMethod.getName();
            String className = declaringClass.getName();
            //String eventType = extractEventInfo(sootMethod);
            if (!declaringClass.getPackageName().contains(appPackageName)) {
                isLibraryMethod = true;
            }

            requestMethodContext = new RequestMethodContext();
            requestMethodContext.setVisibilityType(classType);
            requestMethodContext.setPackageName(packageName);
            requestMethodContext.setClassName(className);
            requestMethodContext.setMethodName(name);
            //requestMethodContext.setEventType(eventType);
            requestMethodContext.setLibaryMethod(isLibraryMethod);
            requestMethodContext.setPermission(permission);

            retrieveRequestCallers(sootMethod, requestMethodContext, true);
        } catch (StackOverflowError e) {
            System.out.println(e.getMessage());
        }
        return requestMethodContext;
    }

    private CallerType getComponentType(SootClass mClass) {
        CallerType classType;
        classType = getClassType(mClass);
        while (classType == null && mClass.hasSuperclass()) {
            mClass = mClass.getSuperclass();
            classType = getClassType(mClass);
        }

        if (classType == null) {
            classType = CallerType.CUSTOM;
        }

        return classType;
    }

    private CallerType checkIfButtonClick(SootMethod method) {
        if (method.getSignature().contains("void onClick(android.view.View)")) {
            return CallerType.ON_CLICK;
        } else {
            return CallerType.CUSTOM;
        }
    }


    private CallerType getClassType(SootClass sootClass) {
        if (sootClass != null && sootClass.getName() != null) {
            if (sootClass.getName().contains("Activity")) {
                return CallerType.ACTIVITY;
            } else if (sootClass.getName().contains("Fragment")) {
                return CallerType.FRAGMENT;
            } else if (sootClass.getName().contains("DialogFragment")) {
                return CallerType.DIALOG;
            } else if (sootClass.getName().contains("android.app.Service")) {
                return CallerType.SERVICE;
            } else if (sootClass.getName().contains("BroadcastReceiver")) {
                return CallerType.BROADCAST_RECEIVER;
            } else if (sootClass.getName().contains("AsyncTask")) {
                return CallerType.ASYNC_TASK;
            } else if (checkIfFragment(sootClass.getMethods())) {
                return CallerType.FRAGMENT;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private HashMap<String, String> loadCallbacks() {

        HashMap<String, String> permissionApiList = new HashMap<>();

        try {
            File file = new File("resources/AndroidCallbacks.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                eventCallBlackClasses.put(line, 1);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissionApiList;
    }

    public RequestMethodContext extractPermissionCheckAndRequest(SootMethod sootMethod) {
        RequestMethodContext requestMethodContext = null;

        PatchingChain<Unit> activeBody = sootMethod.getActiveBody().getUnits();

        if (!permissionCheckerMethods.containsKey(sootMethod.getSignature())) {
            permissionCheckerMethods.put(sootMethod.getSignature(), 1);
            String permission = returnPermissionStringIfPresent(activeBody);
            if (!Strings.isNullOrEmpty(permission)) {
                boolean present = verifyPermissionRequestOrCheck(sootMethod, permission);
                if (present) {
                    requestMethodContext = extractRequestMethodProperties(sootMethod, permission);
                }
            }
        }
        return requestMethodContext;
    }

    private String returnPermissionStringIfPresent(PatchingChain<Unit> activeBody) {
        String permission = "";
        for (Unit unit : activeBody) {
            try {
                String unitString = unit.toString();
                String newPerm = PermissionUtil.getPermissionString(appPackageName, unitString);

                if (permission.isEmpty()) {
                    permission = newPerm;
                } else if (!Strings.isNullOrEmpty(newPerm)) {
                    permission = permission.concat(",").concat(newPerm);
                }
            } catch (StackOverflowError e) {
                writeResultToFile(appPackageName + "\t" + e.getMessage());
                return "";
            }
        }
        return permission;
    }


    private boolean findPermissionCheck(String unitString) {
        return (unitString.contains("android.support.v4.content.ContextCompat")
                || unitString.contains("android.support.v4.content.PermissionChecker")
                || unitString.contains("android.content.Context")
                || unitString.contains("android.content.pm.PackageManager")
                || unitString.contains("android.support.v13.app.FragmentCompat")
                || unitString.contains("android.support.v4.app.ActivityCompat")
                || unitString.contains("ActivityCompat")
                || unitString.contains("ContextCompat")
                || unitString.contains("Activity")
                || unitString.contains("Fragment")
                || unitString.contains("android.support")
        )
                &&
                unitString.contains("checkSelfPermission") ||
                (unitString.contains("checkPermission"));
    }


    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }


    private boolean verifyPermissionRequestOrCheck(SootMethod targetMethod, String permission) {
        InfoflowCFG icfg = new InfoflowCFG();
        DirectedGraph<Unit> ug = icfg.getOrCreateUnitGraph(targetMethod);
        Iterator<Unit> uit = ug.iterator();
        Iterable<SootMethod> checkOrRequestPermissionCallee;
        boolean isPermissionRequested = false;
        String methodActiveBody = targetMethod.getActiveBody().toString();
        if (PermissionUtil.findPermissionRequest(methodActiveBody)) {
            isPermissionRequested = true;
        } else {
            while (uit.hasNext()) {
                Unit u = uit.next();
                if (u.branches()) {
                    List<Unit> list = icfg.getSuccsOf(u);
                    for (Unit unit :
                            list) {
                        isPermissionRequested = getCheckRequest(unit, icfg, permission, targetMethod.getSignature());
                    }
                } else if (icfg.isCallStmt(u)) {
                    isPermissionRequested = getCheckRequest(u, icfg, permission, targetMethod.getSignature());
                }
            }
        }
        return isPermissionRequested;
    }

    private void extendFragmentCallGraph(SootMethod targetMethod) {
        InfoflowCFG icfg = new InfoflowCFG();
//        if(targetMethod.getActiveBody() == null)
        targetMethod.retrieveActiveBody();
        DirectedGraph<Unit> ug = icfg.getOrCreateUnitGraph(targetMethod);
        Iterator<Unit> uit = ug.iterator();
        Iterable<SootMethod> checkOrRequestPermissionCallee;

        String methodActiveBody = targetMethod.getActiveBody().toString();

        while (uit.hasNext()) {
            Unit u = uit.next();
            if (u.branches()) {
                List<Unit> list = icfg.getSuccsOf(u);
                for (Unit unit :
                        list) {
                    //isPermissionRequested = getCheckRequest(unit, icfg, permission, targetMethod.getSignature());
                    findCalleesFragment(unit, icfg);
                }
            } else if (icfg.isCallStmt(u)) {
                findCalleesFragment(u, icfg);
            }
        }

    }

    private boolean getCheckRequest(Unit u, InfoflowCFG icfg, String permission, String methodSignature) {
        Iterable<SootMethod> calleesOfCallAt = icfg.getCalleesOfCallAt(u);
        for (SootMethod sootMethod :
                calleesOfCallAt) {
            String activeBody = null;
            try {
                activeBody = sootMethod.getActiveBody().toString();
                if (PermissionUtil.findPermissionRequest(activeBody)) {
                    return true;
                }
            } catch (Exception e) {
                writeResultToFile(appPackageName + "\t" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Method not used in the analysis. may be I can use it later on if call graph needs to be extended.
     *
     * @param u
     * @param icfg
     * @return
     */
    private boolean findCalleesFragment(Unit u, InfoflowCFG icfg) {
        if (u.toString().contains("myapplication.a")) {
            int i = 0;
            List<ValueBox> useAndDefBoxes = u.getUseAndDefBoxes();
            List<ValueBox> useDefBoxes = u.getDefBoxes();
            Value value = useAndDefBoxes.get(0).getValue();
            List<ValueBox> useBoxes = value.getUseBoxes();
            List<ValueBox> value1 = useBoxes;
        }
        SootMethod calleesOfCallAt = icfg.getMethodOf(u);
        try {
            System.out.println(calleesOfCallAt.getSignature());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        for (SootMethod sootMethod :
//                calleesOfCallAt) {
//            String activeBody = null;
//            try {
//                activeBody = sootMethod.getActiveBody().toString();
//
//            } catch (Exception e) {
//                writeResultToFile(appPackageName + "\t" + e.getMessage());
//            }
//        }
        return false;
    }

    private void writeResultToFile(String s) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("log", true));
            writer.append(s).append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean extractRationale(SootMethod method) {

        String[] methodTokens = method.getActiveBody().toString().split("\n");
        for (String line :
                methodTokens) {
            if (checksRationale(line)) {
                if(showsRationale(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checksRationale(String methodActiveBody) {
        return methodActiveBody.contains("shouldShowRequestPermissionRationale")
                || (methodActiveBody.contains("staticinvoke")
                && methodActiveBody.contains("android.support.v4.app")
                && methodActiveBody.contains("boolean")
                && methodActiveBody.contains("(android.app.Activity,java.lang.String)"));
    }

    private boolean showsRationale(SootMethod sootMethod) {
        String activeBody = sootMethod.getActiveBody().toString();
        if(activeBody.contains("void show()") || activeBody.contains("makeToast"))
            return true;
        else {
            Iterator sources = new Sources(callGraph.edgesOutOf(sootMethod));
            while (sources.hasNext()) {
                SootMethod callerMethod = (SootMethod) sources.next();
                activeBody = callerMethod.getActiveBody().toString();
                if(activeBody.contains("void show()") || activeBody.contains("makeToast"))
                    return true;
            }
        }

        return false;
    }

}
