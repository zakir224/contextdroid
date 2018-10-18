package main.java.Util;

import main.java.*;
import main.java.debug.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OutputUtil {

    private static final String OUTPUT_USAGE = "output_usage.csv";
    private static final String OUTPUT_REQUEST = "output_request.csv";
    private static final String OUTPUT_PERMISSION = "permissions.txt";
    private static final String OUTPUT_RATIONALE = "rationale.txt";
    private static final String OUTPUT_SERVICE_INITIATOR = "service_initiator.txt";
    private static final String OUTPUT_STAT = "time_stats.txt";

    public static void writeRequestOutput(HashMap<String, RequestMethodContext> finalRequestMapping,
                                          AppMetaData appMetaData, String datasetFile) {
        for (RequestMethodContext methodContext :
                finalRequestMapping.values()) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String perm= methodContext.getPermission();
            String visibilityType = methodContext.getVisibilityType().toString();
            perm = PermissionUtil.removeDuplicatePermission(perm.replace("\"",""));
            String[] multiplePermission = perm.split(",");

            for (String permission: multiplePermission) {

                String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName()
                        + "\t" + appMetaData.getVersionCode() + "\t" + className + "\t" + methodName + "\t" + permission + "\t"
                        + visibilityType + "\t" + appMetaData.getSha256();

                try {
                    CommonUtil.write(finalString, datasetFile + OUTPUT_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (CallerMethod callerMethod :
                        methodContext.getCallerMethodList()) {
                    finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t" +
                            callerMethod.getClassName() + "\t" + callerMethod.getMethodName() + "\t" + permission + "\t" +
                            callerMethod.getVisibilityType() + "\t" + appMetaData.getSha256();
                    try {
                        CommonUtil.write(finalString, datasetFile + OUTPUT_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    public static void writePermissions(AppMetaData appMetaData, String datasetFile) {

        StringBuilder permissions = new StringBuilder();
        for(String permission: appMetaData.getPermissions()) {
            permissions.insert(0, permission + ";");
        }

        try {
            String finalString = appMetaData.getPackageName() + "\t" +
                    appMetaData.getVersionName() + "\t" +
                    appMetaData.getVersionCode() + "\t" +
                    permissions.toString() + "\t" +
                    appMetaData.getMainActivity() + "\t" +
                    appMetaData.getSha256();
            CommonUtil.write(finalString, datasetFile + OUTPUT_PERMISSION);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static ArrayList<String> getCompletedApkList(String datasetFile) {

        ArrayList<String> completedApkHashList = new ArrayList<>();
        try {
            File file = new File(datasetFile + OUTPUT_PERMISSION);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
                String[] tokens = line.split("\t");
                try {
                    completedApkHashList.add(tokens[5]);
                } catch (IndexOutOfBoundsException e) {
                   Log.e(datasetFile, e.getMessage(), true);
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return completedApkHashList;
    }

    public static void writeRationale(AppMetaData appMetaData, HashMap<String, ArrayList<String>> rationale, String datasetFile) {


        rationale.forEach((permission, rationaleContexts) ->{
            for (String context : rationaleContexts) {
                String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t"
                        + permission + "\t" + context;
                try {
                    CommonUtil.write(finalString, datasetFile + OUTPUT_RATIONALE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void moveAnalyzedApk(String sourceApk) {
        File file = new File(sourceApk);
        String newFolderPath = OutputUtil.getFolderPath(sourceApk) + "/done/";
        boolean moveDirectoryCreated = new File(newFolderPath).exists();
        if(!moveDirectoryCreated) {
            moveDirectoryCreated = new File(newFolderPath).mkdirs();
        }

        if(moveDirectoryCreated) {
            // renaming the file and moving it to a new location
            if(file.renameTo
                    (new File(newFolderPath + file.getName())))
            {
                // if file copied successfully then delete the original file
                if(file.delete()) {
                    Log.d(sourceApk, "File moved successfully.", true);
                } else {
                    Log.e(sourceApk, "Failed to move file.", true);
                }
            }
            else
            {
                Log.e(sourceApk, "Failed to move file.", true);
            }
        }
    }

    public static void writeServiceInitiator(AppMetaData appMetaData, HashMap<String, String> serviceInitiator, String datasetFile) {


        serviceInitiator.forEach((service, initiator) ->{
                String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t"
                        + service + "\t" + initiator;
                try {
                    CommonUtil.write(finalString, datasetFile + OUTPUT_SERVICE_INITIATOR);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

    }

    public static void writeUsageOutput(HashMap<String, MethodContext> finalPermissionMapping, AppMetaData appMetaData, String datasetFile) {
        for (MethodContext methodContext :
                finalPermissionMapping.values()) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String permission = methodContext.getPermission().getPermission();
            String invokedMethod = methodContext.getPermission().getMethodSignature();
            String visibilityType = methodContext.getVisibilityType().toString();
            String onRequestPermissionResult = "false";


            String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t"
                    + className + "\t" + methodName + "\t" + permission + "\t" + invokedMethod
                    + "\t" + visibilityType  + "\t" + appMetaData.getSha256();
            try {
                CommonUtil.write(finalString, datasetFile + OUTPUT_USAGE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (CallerMethod callerMethod :
                    methodContext.getCallerMethodList()) {
                finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t"
                        + callerMethod.getClassName() + "\t" + callerMethod.getMethodName() + "\t" + permission + "\t" + invokedMethod
                        + "\t" + callerMethod.getVisibilityType() + "\t" + appMetaData.getSha256();
                try {
                    CommonUtil.write(finalString, datasetFile + OUTPUT_USAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeTimeStat(ApkProcessingStatistic stats, String datasetFile) {
        try {
            CommonUtil.write(stats.toString(), datasetFile + OUTPUT_STAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initOutputFiles(String datasetFile) {
        deleteFilesIfExists(datasetFile);
    }

    private static boolean deleteFilesIfExists(String datasetFile) {
        deleteFile(datasetFile + OUTPUT_STAT);
        deleteFile(datasetFile + OUTPUT_PERMISSION);
        deleteFile(datasetFile + OUTPUT_RATIONALE);
        deleteFile(datasetFile + OUTPUT_SERVICE_INITIATOR);
        deleteFile(datasetFile + OUTPUT_USAGE);
        deleteFile(datasetFile + OUTPUT_REQUEST);
        return true;
    }

    private static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void intiStat(String datasetFile) {
        try {
            CommonUtil.write("app\tfileSize\tcallGraphGenerationTime\tlistingMethodsTime" +
                    "\tcontextExtractionTime\tnumberOfClasses\tnumberOfMethods", datasetFile + OUTPUT_STAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFolderPath(String appToAnalyze) {
        File f = new File(appToAnalyze);
        if(f.exists()) {
            return f.getParent() + "/";
        } else {
            return null;
        }
    }

    public static void moveOldStatIfExists(String sourceApk) {
        File file = new File(sourceApk);
        String newFolderPath = OutputUtil.getFolderPath(sourceApk) + "/old_stat/";
        boolean moveDirectoryCreated = new File(newFolderPath).exists();
        if(!moveDirectoryCreated) {
            moveDirectoryCreated = new File(newFolderPath).mkdirs();
        }

        if(moveDirectoryCreated) {
            // renaming the file and moving it to a new location
            if(file.renameTo
                    (new File(newFolderPath + file.getName())))
            {
                // if file copied successfully then delete the original file
                if(file.delete()) {
                    Log.d(sourceApk, "File moved successfully.", true);
                } else {
                    Log.e(sourceApk, "Failed to move file.", true);
                }
            }
            else
            {
                Log.e(sourceApk, "Failed to move file.", true);
            }
        }
    }


    public static ArrayList<String> getBufferedReader(String filePath) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            fileReader.close();

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Set<String> getApps(String fileName) {
        try {
            Set<String> lines = new HashSet<>();
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    String[] tokens = line.split("\t");
                    lines.add(tokens[0] + "_" +tokens[2]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //e.printStackTrace();
                    lineCount ++;
                }
            }
            fileReader.close();

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static HashMap<String, Integer> getVTscan(String fileName, ArrayList<String> apps) {
        try {
            HashMap<String, Integer> lines = new HashMap<>();
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if(lineCount > 0 && !tokens[7].equals("") && !tokens[7].equals("0") ) {
                    try {
                        String app = tokens[5].replace("\"", "") + "_" + tokens[6];
                        if(apps.contains(app)) {
                            lines.put(app, Integer.valueOf(tokens[7]));
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                lineCount++;
            }
            fileReader.close();

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static HashMap<String, Integer> readVTscan(String fileName, HashMap<String, HashMap<String, Set<String>>> usageContexts, HashMap<String, HashMap<String, Set<String>>> resuestContexts) {
        try {
            HashMap<String, Integer> lines = new HashMap<>();
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split("\t");
                if(tokens.length > 1  &&
                        !tokens[1].equals("") && !tokens[1].equals("0") ) {
                    try {
                        String app = tokens[0];
                        if(Integer.valueOf(tokens[1]) > 0)
                            lines.put(app, Integer.valueOf(tokens[1]));
                    } catch (NumberFormatException e) {

                    }
                }
                lineCount++;
            }
            fileReader.close();

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static HashMap<String, Integer> readVT(String fileName, Set<String> apps) {
        try {
            HashMap<String, Integer> lines = new HashMap<>();
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if(lineCount > 0) {
                    try {
                        String app = tokens[5].replace("\"", "") + "_" + tokens[6];
                        if(apps.contains(app)) {
                            CommonUtil.write(app +"\t"+ tokens[7], "vt_scan.txt");
//                            lines.put(app, Integer.valueOf(tokens[7]));
                        }
                    } catch (IOException e) {
                        CommonUtil.write(line, "error.txt");

                    }
                }
                lineCount++;
            }
            fileReader.close();

            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
