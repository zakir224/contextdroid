package main.java;

import com.google.common.base.Strings;
import main.java.Util.CommonUtil;
import main.java.Util.OutputUtil;
import main.java.debug.Log;
import java.util.*;

public class ContextExtractionManager {

    private String apkFolder;
    private String androidPlatform;
    private ContextDroid contextDroid;
    private ArrayList<String> apkList;

    public ContextExtractionManager(String apkFolder, String androidPlatform, boolean restart) {
        setApkFolder(apkFolder);
        setAndroidPlatform(androidPlatform);
        if(setApkList(apkFolder, restart)) {
            setContextDroid(restart);
        }
    }

    public void start() {

        if(contextDroid == null) {
            System.out.println("Error 100: Failed to initialize ContextDroid. Please check if you have entered the folder path correctly");
            return;
        }

        if (apkList.size() == 0) {
            Log.e(apkFolder,"Error 102: No APKs found in the directory", true);
            return;
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < apkList.size(); i++) {
            String apk = apkList.get(i);
            contextDroid.start(apk);
            Log.d(apkFolder,OutputUtil.getFolderPath(apk) + "\t" + apk + "\t" + i, true);
            OutputUtil.moveAnalyzedApk(apk);
        }
        long end = System.currentTimeMillis();

        Log.d(apkFolder, "Analyzed: "
                + apkList.size() + " apps in "
                + CommonUtil.getTimeDifferenceInSeconds(start, end) + " seconds", true);
    }

    public String getApkFolder() {
        return apkFolder;
    }

    public void setApkFolder(String apkFolder) {
        this.apkFolder = apkFolder;
    }

    public String getAndroidPlatform() {
        return androidPlatform;
    }

    public ArrayList<String> getApkList() {
        return apkList;
    }

    public boolean setApkList(String apkFolder, boolean restart) {
        if (Strings.isNullOrEmpty(apkFolder)) {
            Log.e("FolderNotFound","Error 101: Empty APK folder or Android platform path", false);
            return false;
        }

        this.apkList = CommonUtil.listApkFiles(apkFolder, restart);
        return true;
    }

    public void setAndroidPlatform(String androidPlatform) {
        this.androidPlatform = androidPlatform;
    }

    public void setContextDroid(boolean restart) {
        if (apkList != null && apkList.size() > 0) {
            contextDroid = new ContextDroid(androidPlatform, apkList.get(0), restart);
        }
    }
}
