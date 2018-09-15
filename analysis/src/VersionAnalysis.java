import main.java.MethodContext;
import main.java.RequestMethodContext;
//import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointConstants;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VersionAnalysis {

    private static String[] dangerousPermission = {
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            "android.permission.CAMERA",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_CALL_LOG",
            "android.permission.ADD_VOICEMAIL",
            "android.permission.USE_SIP",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_WAP_PUSH",
            "android.permission.RECEIVE_MMS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"

    };


    private static void initPermissionGroup() {
        permissionGroup.put("android.permission.READ_CALENDAR", "CALENDER");
        permissionGroup.put("android.permission.WRITE_CALENDAR", "CALENDER");
        permissionGroup.put("android.permission.CAMERA", "CAMERA");
        permissionGroup.put("android.permission.READ_CONTACTS", "CONTACTS");
        permissionGroup.put("android.permission.WRITE_CONTACTS", "CONTACTS");
        permissionGroup.put("android.permission.GET_ACCOUNTS", "CONTACTS");
        permissionGroup.put("android.permission.ACCESS_FINE_LOCATION", "LOCATION");
        permissionGroup.put("android.permission.ACCESS_COARSE_LOCATION", "LOCATION");
        permissionGroup.put("android.permission.RECORD_AUDIO", "MICROPHONE");
        permissionGroup.put("android.permission.READ_PHONE_STATE", "PHONE");
        permissionGroup.put("android.permission.READ_CALL_LOG", "PHONE");
        permissionGroup.put("android.permission.WRITE_CALL_LOG", "PHONE");
        permissionGroup.put("android.permission.ADD_VOICEMAIL", "PHONE");
        permissionGroup.put("android.permission.USE_SIP", "PHONE");
        permissionGroup.put("android.permission.SEND_SMS", "SMS");
        permissionGroup.put("android.permission.RECEIVE_SMS", "SMS");
        permissionGroup.put("android.permission.READ_SMS", "SMS");
        permissionGroup.put("android.permission.RECEIVE_WAP_PUSH", "SMS");
        permissionGroup.put("android.permission.RECEIVE_MMS", "SMS");
        permissionGroup.put("android.permission.READ_EXTERNAL_STORAGE", "STORAGE");
        permissionGroup.put("android.permission.WRITE_EXTERNAL_STORAGE", "STORAGE");
    }


    private static HashMap<String, ArrayList<String>> appPermissions;
    // format: app, hashmap of permission -> callers
    private static HashMap<String, HashMap<String, HashMap<String, String>>> permissionRequests = new HashMap<>();
    private static HashMap<String, HashMap<String, HashMap<String, String>>> permissionUsage = new HashMap<>();
    private static HashMap<String, ArrayList<RequestMethodContext>> appPermissionRequests;
    private static HashMap<String, ArrayList<MethodContext>> appPermissionUsage;
    private static HashMap<String, Integer> permissionRequestToContextCount;
    private static HashMap<String, Integer> permissionUsageToContextCount;
    private static Set<String> appList;
    private static Set<String> foundAppList;
    private static Set<String> dissimarContextAppList = new HashSet<>();

    private static List<String> activityLifecycleMethods = new ArrayList<>();
    private static List<String> fragmentLifecycleMethods = new ArrayList<>();
    private static List<String> serviceLifecycleMethods = new ArrayList<>();
    private static List<String> broadCastLifeCycleMethods = new ArrayList<>();
    private static HashMap<String, Integer> permissionUseInBackGroundAndForegound;
    private static HashMap<String, Integer> permissionUseInBackGround;
    private static HashMap<String, Integer> permissionUseForegound;
    private static HashMap<String, String> permissionGroup;
    private static HashMap<String, Integer> dissimilarContextGroup = new HashMap<>();
    private static HashMap<String, Integer> dissimilarContextPermission = new HashMap<>();
    private static HashMap<String, Integer> dissimilarContextCategory = new HashMap<>();
    private static HashMap<String, ContextScore> contextScoreHashMap = new HashMap<>();

    public static void main(String[] args) {
//        activityLifecycleMethods = AndroidEntryPointConstants.getActivityLifecycleMethods();
//        fragmentLifecycleMethods = AndroidEntryPointConstants.getFragmentLifecycleMethods();
//        serviceLifecycleMethods = AndroidEntryPointConstants.getServiceLifecycleMethods();
//        broadCastLifeCycleMethods = AndroidEntryPointConstants.getBroadcastLifecycleMethods();
        appPermissions = new HashMap<>();
        appPermissionRequests = new HashMap<>();
        appPermissionUsage = new HashMap<>();
        permissionGroup = new HashMap<>();
        foundAppList = new HashSet<>();
        permissionRequestToContextCount = new HashMap<>();
        permissionUsageToContextCount = new HashMap<>();
        permissionUseInBackGroundAndForegound = new HashMap<>();
        permissionUseInBackGround = new HashMap<>();
        permissionUseForegound = new HashMap<>();

        initPermissionGroup();
//        deleteOutputFileIfExists();
        openPermissionFile();
        readRequests();
        readUsage();
        appList = appPermissions.keySet();
        getPermissionRequests();
        getPermissionUsage();
        int numberOfApps = 0;
        int numberOfAppsDissimilarContext = 0;
        AtomicInteger numberOfRequestingPermissionInMoreThanOneContext = new AtomicInteger();
        AtomicInteger totalRequest = new AtomicInteger();
        AtomicInteger numberOfUsagePermissionInMoreThanOneContext = new AtomicInteger();
        AtomicInteger totalUsage = new AtomicInteger();
        AtomicInteger appRequestingInMoreThanOneContext = new AtomicInteger();
        AtomicInteger appUsingInMoreThanOneContext = new AtomicInteger();

        deleteOutputFileIfExists("raw_data/" + "failure.txt");
        deleteOutputFileIfExists("raw_data/" + "comparison.csv");
        deleteOutputFileIfExists("raw_data/" + "contextdiffer.csv");
        deleteOutputFileIfExists("raw_data/" + "dissimilar.csv");
        deleteOutputFileIfExists("raw_data/" + "raw_data/dissimilar_service.csv");
        deleteOutputFileIfExists("raw_data/" + "raw_data/dissimilar_camera_audio.csv");
        for (String app : appList
                ) {
            AtomicBoolean isMoreRequestContext = new AtomicBoolean(false);
            AtomicBoolean isMoreUsageContext = new AtomicBoolean(false);
            ContextScore contextScore = new ContextScore();
            HashMap<String, HashMap<String, String>> hashMapRequest = permissionRequests.get(app);
            HashMap<String, HashMap<String, String>> hashMapUsage = permissionUsage.get(app);

            if (hashMapRequest.size() == 0 || hashMapUsage.size() == 0) {
                try {
                    writeResultToFile(app + printDangerous(appPermissions.get(app)), "raw_data/" + "failure.txt");
                    getdangerousPermissionGroup(appPermissions.get(app));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                boolean dissimilar = false;
                boolean requusefound = false;
                if(hashMapRequest.size() > 0 && hashMapUsage.size() > 0) {

                    try {
                        writeResultToFile("install", "raw_data/install.txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (String permission :
                            permissionGroup.keySet()) {
                        HashMap<String, String> requestContext = hashMapRequest.get(permission);
                        HashMap<String, String> usageContext = hashMapUsage.get(permission);
                        if(requestContext != null)
                            contextScore.increaseRequestContextScore(requestContext.size()-1);
                        if(usageContext != null)
                            contextScore.increaseUsageContextScore(usageContext.size()-1);

                        if(usageContext != null)
                        usageContext.forEach((key, value) -> {
                            try {
                                if (value.contains("SERVICE")) {
                                    contextScore.setBackgroundContext();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        if(usageContext != null && usageContext.size() > 1 && usageContext.containsValue("SERVICE")) {
                            contextScore.setBackgroundForegroundContext();
                        }

                        if (hashMapRequest.containsKey(permission) && hashMapUsage.containsKey(permission)) {
                            try {
                                writeResultToFile("request usage found", "raw_data/requsefound.txt");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            HashMap<String, String> requestContext = hashMapRequest.get(permission);
//                            HashMap<String, String> usageContext = hashMapUsage.get(permission);
                            if(requestContext.size()  != usageContext.size()) {
//                                if(usageContext.containsValue("SERVICE") && (permission.contains("CAMERA")
//                                        || permission.contains("AUDIO"))) {
//                                    try {
//                                        writeResultToFile(permission, "raw_data/dissimilar_camera_audio.csv");
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                                addDissimilarPermission(permission);
                                addDissimilarPermissionGroup(permission);
                                int difference = requestContext.size() - usageContext.size();
                                try {
                                    writeResultToFile(String.valueOf(Math.abs(difference)), "raw_data/dissimilar.csv");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dissimilar = true;
                                contextScore.increaseRequestUsageContextContextScore(Math.abs(difference));
                            }
                        }
                        if(contextScore.getFinalScore() > 0)
                            contextScoreHashMap.put(app, contextScore);
                    }
                }
                if(dissimilar)
                    dissimarContextAppList.add(app);
                numberOfApps++;
                foundAppList.add(app);

            }
            if(hashMapRequest.size() > 0 || hashMapUsage.size() > 0) {
                System.out.println("\nApplication: " + app);
                try {
                    writeResultToFile("\nApplication: " + app, "raw_data/" + "comparison.csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (hashMapRequest.size() > 0) {
                System.out.println("Permission Requests");
                try {
                    writeResultToFile("Permission Requests", "raw_data/" + "comparison.csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hashMapRequest.forEach((key, value) -> {
                    if (hashMapRequest.get(key).size() > 1) {
                        if (permissionRequestToContextCount.containsKey(key)) {
                            Integer integer = permissionRequestToContextCount.get(key);
                            integer = integer + 1;
                            permissionRequestToContextCount.put(key, integer);
                        } else {
                            permissionRequestToContextCount.put(key, 1);
                        }
                        isMoreRequestContext.set(true);
                        numberOfRequestingPermissionInMoreThanOneContext.getAndIncrement();
                    }
                    totalRequest.getAndIncrement();
                    System.out.println(key + " " + value);
                    try {
                        writeResultToFile(key + "\t" + value, "raw_data/" + "comparison.csv");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (hashMapUsage.size() > 0) {
                System.out.println("Permission Usage");
                try {
                    writeResultToFile("Permission Usage", "raw_data/" + "comparison.csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hashMapUsage.forEach((key, value) -> {
                    HashMap<String, String> map = hashMapUsage.get(key);
                    if ((value.containsValue("FRAGMENT") || value.containsValue("ACTIVITY")) && value.containsValue("SERVICE")) {
                        if (permissionUseInBackGroundAndForegound.containsKey(key)) {
                            int num = permissionUseInBackGroundAndForegound.get(key);
                            num++;
                            permissionUseInBackGroundAndForegound.put(key, num);
                        } else {
                            permissionUseInBackGroundAndForegound.put(key, 1);
                        }
                    }
                    map.forEach((permission, context) -> {
                        if (context.contains("ACTIVITY") || context.contains("FRAGMENT")) {
                            if (permissionUseForegound.containsKey(key)) {
                                Integer count = permissionUseForegound.get(key);
                                count++;
                                permissionUseForegound.put(key, count);
                            } else {
                                permissionUseForegound.put(key, 1);
                            }
                        } else if (context.contains("SERVICE") || context.contains("ASYNCTASK") || context.contains("ASYNCTASK")) {
                            if (permissionUseInBackGround.containsKey(key)) {
                                Integer count = permissionUseInBackGround.get(key);
                                count++;
                                permissionUseInBackGround.put(key, count);
                            } else {
                                permissionUseInBackGround.put(key, 1);
                            }
                        }
                    });

                    if (hashMapUsage.get(key).size() > 1) {
                        if (permissionUsageToContextCount.containsKey(key)) {
                            Integer integer = permissionUsageToContextCount.get(key);
                            integer = integer + 1;
                            permissionUsageToContextCount.put(key, integer);
                        } else {
                            permissionUsageToContextCount.put(key, 1);
                        }
                        isMoreUsageContext.set(true);
                        numberOfUsagePermissionInMoreThanOneContext.getAndIncrement();
                    }
                    totalUsage.getAndIncrement();
                    System.out.println(key + " " + value);
                    try {
                        writeResultToFile(key + "\t" + value, "raw_data/" + "comparison.csv");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (isMoreRequestContext.get()) {
                appRequestingInMoreThanOneContext.getAndIncrement();
            }
            if (isMoreUsageContext.get()) {
                appUsingInMoreThanOneContext.getAndIncrement();
            }
        }

//        try {
//            writeResultToFile("permission usage", "requestcontext.csv");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        deleteOutputFileIfExists("raw_data/" + "requestcontext.csv");
        permissionRequestToContextCount.forEach((key, value) -> {
            try {
                findRequestContextForGroup(key, value);
                writeResultToFile(key + "\t" + value, "raw_data/" + "requestcontext.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

//        try {
//            writeResultToFile("permission request", "usagecontext.csv");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        deleteOutputFileIfExists("raw_data/" + "usagecontext.csv");

        permissionUsageToContextCount.forEach((key, value) -> {
            try {
                findUsageContextForGroup(key, value);
                writeResultToFile(key + "\t" + value, "raw_data/" + "usagecontext.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteOutputFileIfExists("raw_data/" + "foreground_background.csv");
        permissionUseInBackGroundAndForegound.forEach((key, value) -> {
            try {
                writeResultToFile(key + "\t" + value, "raw_data/" + "foreground_background.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteOutputFileIfExists("raw_data/" + "foreground.csv");
        permissionUseForegound.forEach((key, value) -> {
            try {
                writeResultToFile(key + "\t" + value, "raw_data/" + "foreground.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteOutputFileIfExists("raw_data/" + "background.csv");
        permissionUseInBackGround.forEach((key, value) -> {
            try {
                writeResultToFile(key + "\t" + value, "raw_data/" + "background.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        deleteOutputFileIfExists("raw_data/" + "request_group.csv");
        permissionGroupRequestContextCount.forEach((key, value) -> {
            try {
                writeResultToFile(key + "\t" + value, "raw_data/" + "request_group.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteOutputFileIfExists("raw_data/" + "usage_group.csv");
        permissionGroupUsageContextCount.forEach((key, value) -> {
            try {
                writeResultToFile(key + "\t" + value, "raw_data/" + "usage_group.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


//        readCategories();
//        readDissimilarCategories();
//        deleteOutputFileIfExists("raw_data/" + "app_stats.csv");
//        categoryStats.forEach((key, value) -> {
//            try {
//                writeResultToFile(key + "\t" + value, "raw_data/" + "app_stats.csv");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        deleteOutputFileIfExists("raw_data/" + "dissimilar_app_stats.csv");
//        dissimilarCategoryStats.forEach((key, value) -> {
//            try {
//                writeResultToFile(key + "\t" + value, "raw_data/" + "dissimilar_app_stats.csv");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        deleteOutputFileIfExists( "raw_data/" + "dissimilarContextGroup.csv");
//        dissimilarContextGroup.forEach((key, value) -> {
//            try {
//                writeResultToFile(key + "\t" + value, "raw_data/" + "dissimilarContextGroup.csv");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        deleteOutputFileIfExists( "raw_data/" + "dissimilarContextPermission.csv");
//        dissimilarContextPermission.forEach((key, value) -> {
//            try {
//                writeResultToFile(key + "\t" + value, "raw_data/" + "dissimilarContextPermission.csv");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        System.out.println("Num" + numberOfApps
                + " " + numberOfRequestingPermissionInMoreThanOneContext
                + " " + totalRequest);
    }

    private static void readDissimilarCategories() {
        try {
            File file = new File("app_categories.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] token;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    token = line.split("\t");
                    if(dissimarContextAppList.contains(token[0]) && dissimilarCategoryStats.containsKey(token[4])) {
                        int categoryCount = dissimilarCategoryStats.get(token[4]);
                        dissimilarCategoryStats.put(token[4], ++categoryCount);
                    } else if(dissimarContextAppList.contains(token[0])) {
                        dissimilarCategoryStats.put(token[4], 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void addDissimilarPermissionGroup(String permission) {
        if(dissimilarContextGroup.containsKey(permissionGroup.get(permission))) {
            int count = dissimilarContextGroup.get(permissionGroup.get(permission));
            count++;
            dissimilarContextGroup.put(permissionGroup.get(permission), count);
        } else {
            dissimilarContextGroup.put(permissionGroup.get(permission), 1);
        }
    }

    private static void addDissimilarPermission(String permission) {
        if(dissimilarContextPermission.containsKey(permission)) {
            int count = dissimilarContextPermission.get(permission);
            count++;
            dissimilarContextPermission.put(permission, count);
        } else {
            dissimilarContextPermission.put(permission, 1);
        }
    }

    static HashMap<String, Integer> permissionGroupRequestContextCount = new HashMap<>();

    private static void findRequestContextForGroup(String permission, Integer contextCount) {
        if(permissionGroupRequestContextCount.containsKey(permissionGroup.get(permission))) {
            int count = permissionGroupRequestContextCount.get(permissionGroup.get(permission));
            count = count + contextCount;
            permissionGroupRequestContextCount.put(permissionGroup.get(permission), count);
        } else {
            permissionGroupRequestContextCount.put(permissionGroup.get(permission), contextCount);
        }
    }


    static HashMap<String, Integer> permissionGroupUsageContextCount = new HashMap<>();

    private static void findUsageContextForGroup(String permission, Integer contextCount) {
        if(permissionGroupUsageContextCount.containsKey(permissionGroup.get(permission))) {
            int count = permissionGroupUsageContextCount.get(permissionGroup.get(permission));
            count = count + contextCount;
            permissionGroupUsageContextCount.put(permissionGroup.get(permission), count);
        } else {
            permissionGroupUsageContextCount.put(permissionGroup.get(permission), contextCount);
        }
    }

    private static String printDangerous(ArrayList<String> strings) {
        AtomicReference<String> s = new AtomicReference<>("");
        strings.forEach((value) -> {
            for (int i = 0; i < dangerousPermission.length; i++) {
                if (value.contains(dangerousPermission[i])) {
                    s.set(s.get().concat(";").concat(dangerousPermission[i]));
                }
            }
        });
        return s.toString();
    }

    private static void deleteOutputFileIfExists(String fileName) {
        File f = new File( fileName);
        if (f.delete()) {
            System.out.println("File" + fileName +" deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }
    }

    private static void getPermissionUsage() {
        for (String app : appList) {

//            ArrayList<RequestMethodContext> requestMethodContexts = appPermissionRequests.get(app);
            ArrayList<MethodContext> requestMethodContexts = appPermissionUsage.get(app);
            System.out.println("\nApplication: " + app);
            HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();

            if (requestMethodContexts != null && !requestMethodContexts.isEmpty()) {
                for (String permission :
                        appPermissions.get(app)) {
                    boolean invoked = false;
                    if (!permission.isEmpty() && isDangerous(permission)) {
                        System.out.println(permission);
                        for (MethodContext context :
                                requestMethodContexts) {
                            if (context.getPermissionString().contains(permission)) {
                                invoked = true;
                                if (context.getCallerList() != null && !context.getCallerList().isEmpty()) {

                                    for (String s : context.getCallerList()) {
                                        if (!s.contains("CUSTOM")) {
                                            if (!s.contains("CUSTOM")) {
                                                String callerClass = getClassName(s);
                                                if (stringHashMapHashMap.containsKey(permission)) {
                                                    HashMap<String, String> hashMap = stringHashMapHashMap.get(permission);
                                                    if (!hashMap.containsKey(callerClass)) {
                                                        hashMap.put(callerClass, getClassType(s));
                                                    }
                                                    stringHashMapHashMap.put(permission, hashMap);
                                                } else {
                                                    HashMap<String, String> hashMap = new HashMap<>();
                                                    hashMap.put(callerClass, getClassType(s));
                                                    stringHashMapHashMap.put(permission, hashMap);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (stringHashMapHashMap.containsKey(permission)) {
                                        HashMap<String, String> hashMap = stringHashMapHashMap.get(permission);
                                        if (!hashMap.containsKey(context.getClassName())) {
                                            hashMap.put(context.getClassName(), context.getCallerClassType());
                                        }
                                        stringHashMapHashMap.put(permission, hashMap);
                                    } else {
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put(context.getClassName(), context.getCallerClassType());
                                        stringHashMapHashMap.put(permission, hashMap);
                                    }
                                }
                            }
                        }
                    }
//                    if (!permission.isEmpty() && !invoked) {
//                        System.out.println("Not Invoked");
//                    }
                }
            }
            permissionUsage.put(app, stringHashMapHashMap);
        }
    }

    private static void getPermissionRequests() {
        for (String app : appList) {

            ArrayList<RequestMethodContext> requestMethodContexts = appPermissionRequests.get(app);
            System.out.println("\nApplication: " + app);
            HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();

            if (requestMethodContexts != null && !requestMethodContexts.isEmpty()) {
                for (String permission :
                        appPermissions.get(app)) {
                    boolean invoked = false;
                    if (!permission.isEmpty() && isDangerous(permission)) {
                        System.out.println("\nPermission: " + permission);
                        for (RequestMethodContext context :
                                requestMethodContexts) {
                            if (context.getPermission().contains(permission)) {
                                invoked = true;
                                if (context.getCallerList() != null && !context.getCallerList().isEmpty()) {
                                    for (String s : context.getCallerList()) {
                                        if (!s.contains("CUSTOM")) {
                                            String callerClass = getClassName(s);
                                            if (stringHashMapHashMap.containsKey(permission)) {
                                                HashMap<String, String> hashMap = stringHashMapHashMap.get(permission);
                                                if (!hashMap.containsKey(callerClass)) {
                                                    hashMap.put(callerClass, getClassType(s));
                                                }
                                                stringHashMapHashMap.put(permission, hashMap);
                                            } else {
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                hashMap.put(callerClass, getClassType(s));
                                                stringHashMapHashMap.put(permission, hashMap);
                                            }
                                        }
                                    }
                                } else {
                                    if (stringHashMapHashMap.containsKey(permission)) {
                                        HashMap<String, String> hashMap = stringHashMapHashMap.get(permission);
                                        if (!hashMap.containsKey(context.getClassName())) {
                                            hashMap.put(context.getClassName(), context.getCallerClassType());
                                        }
                                        stringHashMapHashMap.put(permission, hashMap);
                                    } else {
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put(context.getClassName(), context.getCallerClassType());
                                        stringHashMapHashMap.put(permission, hashMap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            permissionRequests.put(app, stringHashMapHashMap);
        }
    }

    private static void writeResultToFile(String s, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(s).append("\n");
        writer.close();
    }

    private static String getClassType(String s) {
        if (s.contains("ACTIVITY")) {
            return "ACTIVITY";
        } else if (s.contains("FRAGMENT")) {
            return "FRAGMENT";
        } else if (s.contains("SERVICE")) {
            return "SERVICE";
        } else if (s.contains("BROADCAST_RECEIVER")) {
            return "BROADCAST_RECEIVER";
        } else if (s.contains("ASYNC_TASK")) {
            return "ASYNC_TASK";
        }
        return null;
    }

    private static String getClassName(String s) {
        return s.split(":")[0];
    }

    private static boolean isDangerous(String permission) {
        for (String perm :
                permissionGroup.keySet()) {
            if (perm.contains(permission))
                return true;
        }
        return false;
    }

    private static void readRequests() {
        try {
            File file = new File("raw_data/" + "aspirin_request.csv");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] tokens;
            String[] token2;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    RequestMethodContext methodContext = new RequestMethodContext();
                    tokens = line.split("\t");
                    methodContext.setClassName(tokens[1]);
                    methodContext.setPermission(tokens[3]);
                    methodContext.setMethodName(tokens[2]);
                    methodContext.setCallerClassType(tokens[4]);
                    List<String> callerList = new ArrayList<>();
                    if (tokens[5] != null && !tokens[5].isEmpty()) {
                        token2 = tokens[5].split(";");
                        callerList.addAll(Arrays.asList(token2));
                    }
                    methodContext.setCallerList(callerList);
                    if (appPermissionRequests.containsKey(tokens[0])) {
                        appPermissionRequests.get(tokens[0]).add(methodContext);
                    } else {
                        ArrayList<RequestMethodContext> methodContexts = new ArrayList<>();
                        methodContexts.add(methodContext);
                        appPermissionRequests.put(tokens[0], methodContexts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readUsage() {
        try {
            File file = new File("raw_data/" + "aspirin_usage.csv");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] tokens;
            String[] token2;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    MethodContext methodContext = new MethodContext();
                    tokens = line.split("\t");
                    methodContext.setClassName(tokens[1]);
                    methodContext.setPermissionString(tokens[3]);
                    methodContext.setMethodName(tokens[2]);
                    methodContext.setCallerClassType(tokens[5]);
                    List<String> callerList = new ArrayList<>();
                    if (tokens[7] != null && !tokens[7].isEmpty()) {
                        token2 = tokens[7].split(";");
                        callerList.addAll(Arrays.asList(token2));
                    }
                    methodContext.setCallerList(callerList);
                    if (appPermissionUsage.containsKey(tokens[0])) {
                        appPermissionUsage.get(tokens[0]).add(methodContext);
                    } else {
                        ArrayList<MethodContext> methodContexts = new ArrayList<>();
                        methodContexts.add(methodContext);
                        appPermissionUsage.put(tokens[0], methodContexts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openPermissionFile() {
        try {
            File file = new File("raw_data/" + "aspirin_permissions");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] token;
            String[] token2;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    ArrayList<String> permList = new ArrayList<>();
                    token = line.split("\t");
                    String perm = token[1].replace(":", "\t");
                    token2 = perm.split("\t");
                    permList.addAll(Arrays.asList(token2));
                    // getdangerousPermissionGroup(token2);
//                    getdangerousPermissionGroup(permList);
                    if (appPermissions.containsKey(token[0])) {
                        writeResultToFile(token[0] + appPermissions.get(token[0]).toArray().toString(), "duplicate.txt");
                    }
                    if (getDangerousPermissionGroup(token2))
                        appPermissions.put(token[0], permList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    static HashMap<String, String> category = new HashMap<>();
    static HashMap<String, Integer> categoryStats = new HashMap<>();
    static HashMap<String, Integer> dissimilarCategoryStats = new HashMap<>();
    private static void readCategories() {
        try {
            File file = new File("app_categories.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] token;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    token = line.split("\t");
                    if(foundAppList.contains(token[0]) && categoryStats.containsKey(token[4])) {
                        int categoryCount = categoryStats.get(token[4]);
                        categoryStats.put(token[4], ++categoryCount);
                    } else if(foundAppList.contains(token[0])) {
                        categoryStats.put(token[4], 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static boolean isGame(String appName) {
        return category.containsKey(appName) && category.get(appName).contains("GAME_");
    }

    public static boolean getDangerousPermissionGroup(String[] permissionList) {
        HashMap<String, Integer> groups = new HashMap<>();
        int dangerousCount = 0;
        ArrayList<String> dan = new ArrayList<>();
        for (int i = 0; i < permissionList.length; i++) {
            if (permissionGroup.containsKey(permissionList[i])) {  // means this permission is dangerous.
                groups.put(permissionGroup.get(permissionList[i]), 1);
                dan.add(permissionList[i]);
                dangerousCount++;
            }
        }
        if(groups.size() == 2 && (groups.containsKey("STORAGE") || groups.containsKey("LOCATION")))
            return false;
        else if (groups.size() <= 1) {
            return false;
        } else {
            try {
                writeResultToFile(String.valueOf(dangerousCount), "acceptedapps.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    static String[] rarePermissions = {"android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.ACCESS_COARSE_LOCATION"
            , "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.READ_EXTERNAL_STORAGE"
    };

    private static boolean rarePermissionsDominated(ArrayList<String> dan) {
        if (dan.size() == 2
                && dan.contains("android.permission.WRITE_EXTERNAL_STORAGE") && dan.contains("android.permission.READ_EXTERNAL_STORAGE"))
            return true;
        else
            return false;
    }

    public static boolean getdangerousPermissionGroup(ArrayList<String> permissionList) {
        HashMap<String, Integer> groups = new HashMap<>();
        int dangerousCount = 0;
        ArrayList<String> dan = new ArrayList<>();
        for (String aPermissionList : permissionList) {
            if (permissionGroup.containsKey(aPermissionList)) {  // means this permission is dangerous.
                groups.put(permissionGroup.get(aPermissionList), 1);
                dan.add(aPermissionList);
                dangerousCount++;
            }
        }
        if (dangerousCount > 1) {
            String str = "";
            try {
                for (String s :
                        dan) {
                    str = str + "\t" + s;
                }
                writeResultToFile(dangerousCount + "\t" +str, "total");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dangerousCount > 2 && groups.size() > 1) {
            return true;
        } else {
            return false;
        }
    }
}