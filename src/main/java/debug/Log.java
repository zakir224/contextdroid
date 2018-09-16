package main.java.debug;

import main.java.Util.CommonUtil;

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

    public static void d(String apk, String message, boolean writeToFile) {
        String app = apk != null ? apk : "Info:";
        System.out.println(app + "\t" + message);
        if (writeToFile) {
            try {
                CommonUtil.write(apk + "\t" + message, "Log.log");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
