package main.java.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ContentPermissionMap implements PermissionMap{

    public static ContentPermissionMap permissionMap;


    public static ContentPermissionMap getInstance() {
        if(permissionMap == null) {
            permissionMap = new ContentPermissionMap();
        }
        return permissionMap;
    }

    @Override
    public HashMap<String, Permission> loadPermissionMapping(String filePath) {
        HashMap<String, Permission> permissionApiList = new HashMap<>();
        String[] tokens;
        Permission permissionOdj;
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;

                tokens = line.split(";");
                permissionOdj = new ContentPermission(tokens[0], tokens[1], tokens[2]);
                permissionApiList.put(permissionOdj.toString(), permissionOdj);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissionApiList;
    }
}
