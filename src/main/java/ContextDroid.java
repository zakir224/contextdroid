package main.java;

import com.google.common.base.Strings;
import main.java.ContextAnalyzer;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.*;
import java.util.*;

public class ContextDroid {

    public static final int RANGE = 10000;
    private final String apkName;
    private final String androidPlatform;
    private final String folder;
    private int startingPoint;
    private int endPoint;
    ContextAnalyzer contextAnalyzer;
    private ArrayList<String> apkList;

    public ContextDroid(String apkName, String androidPlatform, String folder, int startingPoint, int endPoint) {
        this.apkName = apkName;
        this.androidPlatform = androidPlatform;
        this.folder = folder;
        this.startingPoint = startingPoint;
        this.endPoint = endPoint;
        listFiles(apkName);
        initPermissionGroup();
    }

    public void start() {
        if (Strings.isNullOrEmpty(apkName) || Strings.isNullOrEmpty(folder) || Strings.isNullOrEmpty(androidPlatform)) {
            System.out.println("Error 404: empty apk, folder or platform path ");
            return;
        }

        if (apkList.size() == 0) {
            System.out.println("Error 444: no apks found in the directory");
            return;
        }
        for (int i = startingPoint; i < apkList.size(); i++) {
            contextAnalyzer = new ContextAnalyzer(androidPlatform, apkList.get(i), folder);
            contextAnalyzer.startAnalysis(false);
            contextAnalyzer = null;
            writeResultToFile(folder+ "\t" + apkList.get(i) + "\t" + i);
        }
    }

    private void listFiles(String apkName) {
        apkList = new ArrayList<>();
        File[] files = new File(apkName).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".apk")) {
                    apkList.add(file.getAbsolutePath());
                }
            }
        }
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
//        permissionGroup.put("android.permission.BODY_SENSORS" , "SENSORS");
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
