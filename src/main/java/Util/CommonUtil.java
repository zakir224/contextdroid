package main.java.Util;

import soot.PatchingChain;
import soot.Unit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public static boolean deleteFileIfExists(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
