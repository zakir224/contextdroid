package main.java.Util;

import soot.PatchingChain;
import soot.Unit;

public class ContextUtil {

    public static String getPermissionUnit(PatchingChain<Unit> activeBody) {
        String permission = "";
        for (Unit unit: activeBody
                ) {
            String unitString = unit.toString();

            if (unitString.contains("android.permission")) {
//                System.out.println(unitString);
                permission = permission + " " + unitString.substring(unitString.indexOf("android.permission"), unitString.length());
            }
        }
        return permission;
    }

}
