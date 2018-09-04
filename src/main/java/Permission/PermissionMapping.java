package main.java.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PermissionMapping {


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

    private static final String PERMISSION_MAPPING_FINAL = "resources/pscout/pscout411.txt";
    private static String[] tokens;


    public static HashMap<String, Permission>  loadPermissionMapping() {

        HashMap<String, Permission> permissionApiList = new HashMap<>();
        Permission permissionOdj;
        try {
            File file = new File(PERMISSION_MAPPING_FINAL);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;

                tokens = line.split("\t");
                permissionOdj = new Permission(tokens[1], tokens[3], tokens[2], tokens[0]);
                permissionApiList.put(permissionOdj.toString(), permissionOdj);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissionApiList;
    }

}
