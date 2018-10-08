package main.java.Util;

import main.java.Permission.Permission;
import soot.PatchingChain;
import soot.Unit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CommonUtil {

    public static void write(String s, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(s).append("\n");
        writer.close();
    }


    public static int getTimeDifferenceInSeconds(long startTime, long endTime) {
        return (int) ((endTime-startTime)/1000);
    }

    public static float getFileSize(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            return ((file.length()/1000)/1000);
        } else {
            return 0;
        }
    }

    public static ArrayList<String> listApkFiles(String apkFolder, boolean restart) {
        ArrayList<String> completedApkList = new ArrayList<>();
        if(restart) {
            completedApkList = OutputUtil.getCompletedApkList(apkFolder);
        }

        ArrayList<String> apkList = new ArrayList<>();
        File[] files = new File(apkFolder).listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.contains(".apk")) {
                    if(completedApkList != null && completedApkList.size() > 0
                            && completedApkList.contains(StringUtil.extractSha256FromFileName(fileName))) {

                    } else {
                        apkList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return apkList;
    }
}
