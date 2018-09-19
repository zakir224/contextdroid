package main.java;

import com.google.common.base.Strings;
import main.java.Util.CommonUtil;
import main.java.Util.OutputUtil;
import main.java.debug.Log;

import java.io.*;
import java.util.*;

public class ContextExtractionManager {

    private final String apkFolder;
    private final String androidPlatform;
    ContextDroid contextDroid;
    private ArrayList<String> apkList;

    public ContextExtractionManager(String apkFolder, String androidPlatform) {
        this.apkFolder = apkFolder;
        this.androidPlatform = androidPlatform;
        apkList = CommonUtil.listApkFiles(apkFolder);
        initPermissionGroup();
        if (apkList != null && apkList.size() > 0) {
            contextDroid = new ContextDroid(androidPlatform, apkList.get(0));
        }
    }

    public void start() {
        if (Strings.isNullOrEmpty(apkFolder) || Strings.isNullOrEmpty(androidPlatform)) {
            System.out.println("Error 404: empty APK, folder or platform path ");
            return;
        }

        if (apkList.size() == 0) {
            System.out.println("Error 444: no APKs found in the directory");
            return;
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < apkList.size(); i++) {
            String apk = apkList.get(i);
            contextDroid.start(apk);
            Log.d(apkFolder,OutputUtil.getFolderPath(apk) + "\t" + apk + "\t" + i, true);
        }
        long end = System.currentTimeMillis();

        Log.d(apkFolder, "Analyzed: "
                + apkList.size() + "apps in "
                + CommonUtil.getTimeDifferenceInSeconds(start, end) + " seconds", true);
    }

    private boolean rarePermissionsDominated(Set<String> dan) {
        if (dan.size() == 2
                && dan.contains("android.permission.WRITE_EXTERNAL_STORAGE") && dan.contains("android.permission.READ_EXTERNAL_STORAGE"))
            return true;
        else
            return false;
    }

    public boolean rareDominated(Set<String> permissionList) {
        HashMap<String, Integer> groups = new HashMap<>();
        int dangerousCount = 0;
        Set<String> dan = new HashSet<>();
        for (String aPermissionList : permissionList) {
            if (permissionGroup.containsKey(aPermissionList)) {  // means this permission is dangerous.
                groups.put(permissionGroup.get(aPermissionList), 1);
                dan.add(aPermissionList);
                dangerousCount++;
            }
        }
        if (rarePermissionsDominated(dan)) {
            return true;
        } else {
            return false;
        }
    }

    private void initPermissionGroup() {
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

    private HashMap<String, String> permissionGroup = new HashMap<>();
}
