package main.java.Util;

import main.java.AppMetaData;
import main.java.debug.Log;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class ManifestUtil {

    public static AppMetaData extractManifestInfo(String apkFilePath) {

        if (apkFilePath == null || !apkFilePath.contains(".apk")) {
            return null;
        }

        ProcessManifest processManifest;
        AppMetaData appMetaData = new AppMetaData();
        try {
            processManifest = new ProcessManifest(apkFilePath);
            appMetaData.setPackageName(processManifest.getPackageName());
            appMetaData.setVersionCode(processManifest.getVersionCode());
            appMetaData.setVersionName(processManifest.getVersionName());
            appMetaData.setTargetSdk(processManifest.targetSdkVersion());
            appMetaData.setMinSdk(processManifest.getMinSdkVersion());
            appMetaData.setSha256(StringUtil.extractSha256FromFilePath(apkFilePath));
            appMetaData.setMainActivity(getMainActivity(processManifest));
            appMetaData.getPermissions().addAll(processManifest.getPermissions());
            appMetaData.setServices(getServices(processManifest));
//            processManifest.getActivities()getActivities
        } catch (XmlPullParserException | IOException | ClassCastException e) {
            Log.e(apkFilePath, e.getMessage(), true);
            return null;
        }

        return appMetaData;
    }

    private static ArrayList<String> getServices(ProcessManifest processManifest) {
        ArrayList<String> services = new ArrayList<>();
        for (AXmlNode service : processManifest.getServices()) {
            services.add(service.getAttribute("name").getValue().toString());
        }
        return services;
    }

    private static String getMainActivity(ProcessManifest processManifest) throws NullPointerException {
        Set<AXmlNode> manifest = processManifest.getLaunchableActivities();

        for (AXmlNode applicationTags : manifest) {
            return applicationTags.getAttribute("name").getValue().toString();
        }

        return "NF";
    }
}
