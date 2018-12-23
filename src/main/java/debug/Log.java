package main.java.debug;

import com.google.common.base.Strings;
import main.java.Util.CommonUtil;
import main.java.Util.OutputUtil;

import java.io.IOException;

public class Log {

    //  Prints out error messages
    public static void e(String apk, String error, boolean writeToFile) {
        System.out.println(error);
        if (writeToFile && !Strings.isNullOrEmpty(apk)) {
            try {
                CommonUtil.write(apk + ":\t" + error, OutputUtil.getFolderPath(apk) + "Error.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //  Prints out debug messages
    public static void d(String apk, String message, boolean writeToFile) {
        String app = apk != null ? apk : "Info: ";
        System.out.println(app + "\t" + message);
        if (writeToFile && !Strings.isNullOrEmpty(apk)) {
            try {
                CommonUtil.write(apk + ":\t" + message, OutputUtil.getFolderPath(apk) + "Log.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
