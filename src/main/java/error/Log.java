package main.java.error;

import main.java.Util.CommonUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {

    public static void e(String apk, String error, boolean writeToFile) {
        System.out.println(error);
        if (writeToFile) {
            try {
                CommonUtil.write(apk + "\t" + error, "Error.log");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
