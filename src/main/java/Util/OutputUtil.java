package main.java.Util;

import main.java.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class OutputUtil {

    private static final String OUTPUT_USAGE = "output_usage.csv";
    private static final String OUTPUT_REQUEST = "output_request.csv";
    private static final String OUTPUT_PERMISSION = "permissions.txt";
    private static final String OUTPUT_STAT = "time_stats.txt";

    public static void writeRequestOutput(HashMap<String, RequestMethodContext> finalRequestMapping,
                                          AppMetaData appMetaData, String datasetFile) {
        for (RequestMethodContext methodContext :
                finalRequestMapping.values()) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String permission = methodContext.getPermission();
            String eventType = "";//methodContext.getEventType();
            String visibilityType = methodContext.getVisibilityType().toString();

            for (CallerMethod callerMethod :
                    methodContext.getCallerMethodList()) {
                eventType = eventType + callerMethod.getMethodName() + ", " + callerMethod.getVisibilityType() + ";";
            }
            String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName()
                    + "\t" + appMetaData.getVersionCode() + "\t" + appMetaData.getTargetSdk() + "\t" +
                    className + "\t" + methodName + "\t" + permission + "\t" + "" + "\t" +
                    eventType + "\t" + appMetaData.getSha256();
            try {
                CommonUtil.write(finalString, datasetFile + OUTPUT_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeUsageOutput(HashMap<String, MethodContext> finalPermissionMapping, AppMetaData appMetaData, String datasetFile) {
        for (MethodContext methodContext :
                finalPermissionMapping.values()) {
            String className = methodContext.getClassName();
            String methodName = methodContext.getMethodName();
            String permission = methodContext.getPermission().getPermission();
            String invokedMethod = methodContext.getPermission().getMethodSignature();
            String eventType = "";//methodContext.getEventType();
            String visibilityType = methodContext.getVisibilityType().toString();
            String onRequestPermissionResult = "false";
            for (CallerMethod callerMethod :
                    methodContext.getCallerMethodList()) {
                String sign = callerMethod.getMethodName();
                if (sign.contains("onRequestPermissionsResult") || sign.contains("(int,java.lang.String[],int[])")) {
                    onRequestPermissionResult = "true";
                }
                eventType = eventType + callerMethod.getMethodName() + ", " + callerMethod.getVisibilityType() + ";";

            }
            if (methodContext.toString().contains("onRequestPermissionsResult")
                    || methodContext.toString().contains("(int,java.lang.String[],int[])")) {
                onRequestPermissionResult = "true";
            }
            String finalString = appMetaData.getPackageName() + "\t" + appMetaData.getVersionName() + "\t" + appMetaData.getVersionCode() + "\t" + appMetaData.getTargetSdk() + "\t" +
                    className + "\t" + methodName + "\t" + permission + "\t" + invokedMethod + "\t" + visibilityType
                    + "\t" + onRequestPermissionResult + "\t" + eventType + "\t" + appMetaData.getSha256();
            try {
                CommonUtil.write(finalString, datasetFile + OUTPUT_USAGE);
            } catch (IOException e) {
                e.printStackTrace();
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

    public static boolean deleteFilesIfExists(String datasetFile) {
        deleteFile(datasetFile + OUTPUT_STAT);
        deleteFile(datasetFile + OUTPUT_PERMISSION);
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
}
