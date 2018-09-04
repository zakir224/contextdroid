import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipException;

public class FindVersion {

    private static ArrayList<String> apkList = new ArrayList<>();
    private static ProcessManifest processManifest;
    private static int targetSdk;
    private static Set<String> permissions;

    public static void main(String[] args) {
        readFinishedPermission();
        for (String app: apkList
             ) {
            extractManifestInfo(app);
        }
    }

    private void listFiles(String apkName) {
        apkList = new ArrayList<>();
        File[] files = new File("/media/zakir/HDD2/AndroidZoo").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".apk")) {
                    apkList.add(file.getAbsolutePath());
                }
            }
        }
    }

    private static boolean extractManifestInfo(String appToAnalyze) {
        try {
            System.out.println("Extracting manifest info\n\n");
            String packageName = null;
            int versionCode = 0;
            String versionName;
            try {
                processManifest = new ProcessManifest(appToAnalyze);
                packageName = processManifest.getPackageName();
                versionName = processManifest.getVersionName();
                versionCode = processManifest.getVersionCode();
                targetSdk = processManifest.targetSdkVersion();
                permissions = processManifest.getPermissions();
            } catch (ZipException | XmlPullParserException e) {
//
//                writeResultToFile(appToAnalyze + "\t" + packageName + "\t" + versionCode
//                        + "\t Zip file corrupted\t","failure.txt");
                return false;
            }

            System.out.println("Permission Count: " + permissions.size()+"\n");
            String permissionList = "";
            for (String per :
                    permissions) {
                permissionList = permissionList.concat(":").concat(per);
            }
            writeResultToFile(packageName.concat("\t"+versionName+"\t"+versionCode+"\t").concat(permissionList), "permissions");
            System.out.println("Permission List: " + permissionList+"\n");

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void writeResultToFile(String s, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(s).append("\n");
        writer.close();
    }


    public static void readFinishedPermission() {
        File file = new File("log");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int lineCount = 0;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\t");
                try {
                    apkList.add(split[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
