import main.java.MethodContext;
import main.java.Permission.Permission;
import main.java.RequestMethodContext;

import java.io.*;
import java.util.*;

public class AnalysisUsage {

    private static String[] dangerousPermission = {
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            "android.permission.CAMERA",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.GET_ACCOUNTS",
            "android.permission.AUTHENTICATE_ACCOUNTS",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_PHONE_NUMBERS",
            "android.permission.CALL_PHONE",
            "android.permission.ANSWER_PHONE_CALLS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.ADD_VOICEMAIL",
            "android.permission.USE_SIP",
            "android.permission.PROCESS_OUTGOING_CALLS",
            "android.permission.BODY_SENSORS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_WAP_PUSH",
            "android.permission.RECEIVE_MMS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"

    };

    private static HashMap<String, ArrayList<String>> appPermissions;
    // format: app, hashmap of permission -> callers
    private static HashMap<String, HashMap<String, HashMap<String, String>>> permissionUsage = new HashMap<>();
    private static HashMap<String, ArrayList<RequestMethodContext>> appPermissionRequests;
    private static HashMap<String, ArrayList<MethodContext>> appPermissionUsage;
    private static Set<String> appList;
    private static List<String> activityLifecycleMethods = new ArrayList<>();
    private static List<String> fragmentLifecycleMethods = new ArrayList<>();
    private static List<String> serviceLifecycleMethods = new ArrayList<>();
    private static List<String> broadCastLifeCycleMethods = new ArrayList<>();
    private static final String PERMISSION_MAPPING_FILE = "mappings/pscout/mapping_old.txt";
    private static final String PERMISSION_MAPPING_FILE_5_DEC = "resources/pscout/pscout411.txt";
    private static final String PERMISSION_MAPPING_FILE_XPLORER = "mappings/axplorer/axplorer_mapping.txt";
    private static String[] tokens;
    private static String returnType;
    private static String permission;
    static HashMap<String, Integer> dangerousPermissions = new HashMap<>();
    private static HashMap<String, Permission> permissionApiList = new HashMap<>();


    public static void main(String[] args) {
        for (String s : dangerousPermission
                ) {
            dangerousPermissions.put(s, 1);
        }

//        loadDangerousPermissionMapping();
//        loadPscoutPermissionMapping();
        loadXplorerPermissionMapping();
        permissionApiList.forEach((k, value) -> {
            Permission permission = value;
            try {
                writeResultToFile(permission.getPermission() + "\t" +permission.getPackageName()
                        + "\t" +permission.getReturnType() + "\t" + permission.getMethodSignature(), "xplorer_final.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        int i = 0;
    }

    public static HashMap<String, Permission> loadDangerousPermissionMapping() {
        Permission permissionOdj;
        try {
            File file = new File(PERMISSION_MAPPING_FILE);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
//                    String api = line.split(" ")[0];
                    tokens = line.split(" ");
                    returnType = tokens[1];
                    String methodSign = tokens[2];
                    String packageName = tokens[0].replace(":", "");
                    String permission = tokens[tokens.length - 1];
                    permissionOdj = new Permission(packageName, methodSign, returnType, permission);
                    if(dangerousPermissions.containsKey(permission) && !permissionApiList.containsKey(permissionOdj.toString()))
                        permissionApiList.put(permissionOdj.toString(), permissionOdj);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return permissionApiList;
    }

    public static HashMap<String, Permission> loadXplorerPermissionMapping() {
        Permission permissionOdj;
        try {
            File file = new File(PERMISSION_MAPPING_FILE_XPLORER);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
                if (line.contains("LocationManager")) {
                    int jhjh = 99;
                }
                String api = line.split("  ::  ")[0];
                tokens = api.split("\\.");
                String methodSign = "";
                String packageSign = "";
                int methodTokenIndex = 5000;
                for (int i = 0; i < tokens.length; i++) {
                    String s = tokens[i];
                    if (i < tokens.length - 1) {
                        s = s.concat(".");
                    }
                    if (s.contains("(")) {
                        methodTokenIndex = i;
                    }
                    if (i >= methodTokenIndex) {
                        methodSign = methodSign + s;
                    } else {
                        packageSign = packageSign + s;
                    }
                }
                packageSign = packageSign.substring(0, packageSign.length() - 1);
                tokens = methodSign.split("\\)");
//                methodSign = packageSign + ": " + tokens[1] + " " + tokens[0] + ")";
                methodSign = tokens[0].concat(")");
                permission = line.split("  ::  ")[1];
                returnType = tokens[1];
                permissionOdj = new Permission(packageSign, methodSign, returnType, permission);
                if(dangerousPermissions.containsKey(permission) && !permissionApiList.containsKey(permissionOdj.toString()))
                    permissionApiList.put(permissionOdj.toString(), permissionOdj);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return permissionApiList;
    }

    public static HashMap<String, Permission>  loadPscoutPermissionMapping() {
        Permission permissionOdj;
        try {
            File file = new File(PERMISSION_MAPPING_FILE_5_DEC);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;

                tokens = line.split("\t");
                permissionOdj = new Permission(tokens[1], tokens[3], tokens[2], tokens[0]);
                if(dangerousPermissions.containsKey(tokens[0]) && !permissionApiList.containsKey(permissionOdj.toString()))
                    permissionApiList.put(permissionOdj.toString(), permissionOdj);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return permissionApiList;
    }

    private static void writeResultToFile(String s, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(s).append("\n");
        writer.close();
    }

}

