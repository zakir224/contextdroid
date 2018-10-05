package main.java.Util;

import main.java.debug.Log;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PermissionUtil {

    public static String getPermissionString(String app, String unitString) {

        String permissionString = "";
        if (unitString.contains("android.permission")) {
            int i = unitString.indexOf("android.permission");
            try {
                String[] tokens = unitString.substring(i, unitString.length() - 1).split(" ");
                permissionString = tokens[0].trim();
            } catch (Exception e) {
                Log.e(app, e.getMessage(), true);
            }
        }

        return permissionString;
    }

    public static boolean findPermissionRequest(String unitString) {
        return (
                (
                        unitString.contains("android.support.v4.app.ActivityCompat")
                                || unitString.contains("android.support.v4.app.Activity")
                                || unitString.contains("android.support.v13.app.FragmentCompat")
                                || unitString.contains("android.support.v4.app.Fragment")
                                || unitString.contains("android.content.Context")
                                || unitString.contains("android.content.pm.PackageManager")
                                || unitString.contains("android.support.v4.app.FragmentHostCallback")
                                || unitString.contains("android.support.v4.app.ActivityCompat")
                                || unitString.contains("Activity")
                                || unitString.contains("Fragment")
                                || unitString.contains("android.support")
                )
                        &&
                        (
                                (unitString.contains("android.content.Context") && unitString.contains("java.lang.String[]")) ||
                                        (
                                                unitString.contains("requestPermissions")
                                                        || (unitString.contains("onRequestPermissionsFromFragment"))
                                                        || (unitString.contains("requestPermission"))
                                        )

                        )
        );
    }

    public static String removeDuplicatePermission(String s) {
        return Arrays.stream(s.split(",")).distinct().collect(Collectors.joining(","));
    }
}
