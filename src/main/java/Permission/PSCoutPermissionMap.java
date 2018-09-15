package main.java.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PSCoutPermissionMap implements PermissionMap {



    private static final String PERMISSION_MAPPING_FINAL = "resources/pscout/pscout411.txt";
    private static String[] tokens;

    public static PSCoutPermissionMap permissionMap;


    public static PSCoutPermissionMap getInstance() {
        if(permissionMap == null) {
            permissionMap = new PSCoutPermissionMap();
        }
        return permissionMap;
    }

    @Override
    public HashMap<String, Permission> loadPermissionMapping(String filePath) {

        HashMap<String, Permission> permissionApiList = new HashMap<>();
        Permission permissionOdj;
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;

                tokens = line.split("\t");
                permissionOdj = new Permission(tokens[1], tokens[3], tokens[2], tokens[0]);
                permissionApiList.put(permissionOdj.toString(), permissionOdj);
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissionApiList;
    }
}
