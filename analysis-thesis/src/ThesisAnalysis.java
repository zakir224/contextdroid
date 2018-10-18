import main.java.Util.OutputUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThesisAnalysis {

    private static final List<String> activityMethodList;
    private static final String[] serviceMethods;
    private static final List<String> serviceMethodList;
    private static final String[] fragmentMethods;
    private static final List<String> fragmentMethodList;
    private static final String[] broadcastMethods;
    private static final List<String> broadcastMethodList;
    private static final String[] activityMethods;
    private static HashMap<String, HashMap<String, Set<String>>> flaggedUsage = new HashMap<>();

    static {
        activityMethods = new String[]{"onCreate", "onDestroy", "onPause", "onRestart", "onResume", "onStart", "onStop", "onSaveInstanceState", "onRestoreInstanceState", "onCreateDescription", "onPostCreate", "onPostResume"};
        activityMethodList = Arrays.asList(activityMethods);
        serviceMethods = new String[]{"onCreate", "onDestroy", "onStart", "onStartCommand", "onBind", "onRebind", "onUnbind"};
        serviceMethodList = Arrays.asList(serviceMethods);
        fragmentMethods = new String[]{"onCreate", "onDestroy", "onPause", "onAttach", "onDestroyView", "onResume", "onStart", "onStop", "onCreateView", "onActivityCreated", "onViewStateRestored", "onDetach"};
        fragmentMethodList = Arrays.asList(fragmentMethods);
        broadcastMethods = new String[]{"onReceive"};
        broadcastMethodList = Arrays.asList(broadcastMethods);
    }

    public static void main(String[] args) {

        //format <app, hashmap of <permission, list of context>
        HashMap<String, HashMap<String, Set<String>>> usageContexts = new HashMap<>();
        HashMap<String, HashMap<String, Set<String>>> resuestContexts = new HashMap<>();

        ArrayList<String> usage = OutputUtil.getBufferedReader("/media/zakir/HDD2/final_download/result_64000/final/usage.csv");
        Set<String> apps = OutputUtil.getApps("/media/zakir/HDD2/final_download/result_64000/final/permissions.csv");
//        OutputUtil.readVT("/mnt/6a3e12f5-fa82-4667-be38-a46ad0e34f7c/android-sdk/latest.csv", apps);

        if(usage != null && usage.size() > 0) {
            for (String line: usage){
                Context context = new Context().getUsageContext(line.split("\t"));
                if(!context.getVisibilityType().equals("CUSTOM")) {
                        if(usageContexts.containsKey(context.getApp())) {
                            HashMap<String, Set<String>> contextHashMap = usageContexts.get(context.getApp());
                            if(contextHashMap.containsKey(context.getPermission())){
                                contextHashMap.get(context.getPermission()).add(context.getStringUsage());
                            } else {
                                contextHashMap.put(context.getPermission(), new HashSet<>());
                                contextHashMap.get(context.getPermission()).add(context.getStringUsage());
                            }
                        } else {
                            usageContexts.put(context.getApp(), new HashMap<>());
                            HashMap<String, Set<String>> contextHashMap = usageContexts.get(context.getApp());
                            if(contextHashMap.containsKey(context.getPermission())){
                                contextHashMap.get(context.getPermission()).add(context.getStringUsage());
                            } else {
                                contextHashMap.put(context.getPermission(), new HashSet<>());
                                contextHashMap.get(context.getPermission()).add(context.getStringUsage());
                            }
                        }
                }
            }
        }

        ArrayList<String> requests = OutputUtil.getBufferedReader("/media/zakir/HDD2/final_download/result_64000/final/request.csv");
        if(requests != null && requests.size() > 0) {
            for (String line: requests) {
                Context context = new Context().getRequestContext(line.split("\t"));
                if (!context.getVisibilityType().equals("CUSTOM")) {
                    if (resuestContexts.containsKey(context.getApp())) {
                        HashMap<String, Set<String>> contextHashMap = resuestContexts.get(context.getApp());
                        if (contextHashMap.containsKey(context.getPermission())) {
                            contextHashMap.get(context.getPermission()).add(context.getString());
                        } else {
                            contextHashMap.put(context.getPermission(), new HashSet<>());
                            contextHashMap.get(context.getPermission()).add(context.getString());
                        }
                    } else {
                        resuestContexts.put(context.getApp(), new HashMap<>());
                        HashMap<String, Set<String>> contextHashMap = resuestContexts.get(context.getApp());
                        if (contextHashMap.containsKey(context.getPermission())) {
                            contextHashMap.get(context.getPermission()).add(context.getString());
                        } else {
                            contextHashMap.put(context.getPermission(), new HashSet<>());
                            contextHashMap.get(context.getPermission()).add(context.getString());
                        }
                    }
                }
            }
        }
        HashMap<String, Integer> appScans = OutputUtil.readVTscan("vt_scan.txt",usageContexts, resuestContexts);

        int cor = 0;
        HashMap<String, Integer> flagged = new HashMap<>();
        HashMap<String, Integer> right = new HashMap<>();
        AtomicInteger mul = new AtomicInteger();
        for (String app: apps) {
//            if(appScans.containsKey(app)) {
                System.out.println(app);
                HashMap<String, Set<String>> appRequests = null;
                if(resuestContexts.containsKey(app)) {
                    appRequests = resuestContexts.get(app);
                }
                HashMap<String, Set<String>> appUsage = null;
                if(usageContexts.containsKey(app)) {
                    appUsage = usageContexts.get(app);
                }

                if(appUsage!=null) {
                    HashMap<String, Set<String>> finalAppRequests = appRequests;
                    appUsage.forEach((permission, context) -> {
                        System.out.println(permission);
                        int NumOfRequestContexts = 0;
                        try {
                            if(finalAppRequests.containsKey(permission)) {
                                NumOfRequestContexts = finalAppRequests.get(permission).size();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(context.size() == 1
                                && !context.contains("ACTIVITY")
                                && !context.contains("FRAGMENT")
                                && leakable(permission)) {
//                            flagged.put(app, appScans.get(app));
//                            flaggedUsage.put(app, usageContexts.get(app));
                            if(appScans.containsKey(app)) {
                                right.put(app, appScans.get(app));
                            }
                         }
                         if(context.size() > 1 && leakable(permission)) {
                            flagged.put(app, appScans.get(app));
                            flaggedUsage.put(app, usageContexts.get(app));
                            if(appScans.containsKey(app)) {
                                right.put(app, appScans.get(app));
                            }
                        } else if (context.size() == 1 && NumOfRequestContexts == 1) {
                            if((context.contains("ACTIVITY") || context.contains("FRAGMENT")) &&
                                    (finalAppRequests.get(permission).contains("SERVICE")
                                            || finalAppRequests.get(permission).contains("BROADCAST"))) {
                                System.out.println("Hello Im here..");
                            }
                        }


//                        for (String con: context) {
//                            System.out.println(con);
//                        }
                    });
                }

//                if(appRequests!=null) {
//                    appRequests.forEach((permission, context) -> {
//                        System.out.println(permission);
//
//                        for (String con: context) {
//                            System.out.println(con);
//                        }
//                    });
//                }
                System.out.println("\n");
//            }
        }

        System.out.println(mul);

    }

    private static boolean leakable(String permission) {
        return permission.contains("ACCESS_FINE_LOCATION") ||
                permission.contains("ACCESS_COARSE_LOCATION") ||
                permission.contains("RECEIVE_SMS") ||
                permission.contains("SEND_SMS") ||
                permission.contains("RECORD_AUDIO") ||
                permission.contains("READ_PHONE_STATE");
    }

    private static boolean isEntryPoint(String methodName, String visibilityType) {

        return false;
    }
}
