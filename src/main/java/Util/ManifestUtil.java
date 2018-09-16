package main.java.Util;

import main.java.AppMetaData;
import main.java.debug.Log;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.IOException;
import java.util.ArrayList;

public class ManifestUtil {

    public static AppMetaData extractManifestInfo(String apkFilePath) {

        if (apkFilePath == null || !apkFilePath.contains(".apk")) {
            return null;
        }

        ProcessManifest processManifest;
        AppMetaData appMetaData = new AppMetaData();
        ArrayList<String> permissions = new ArrayList<>();
        try {
            processManifest = new ProcessManifest(apkFilePath);
        } catch (XmlPullParserException | IOException e) {
            Log.e(apkFilePath, e.getMessage(), true);
            return null;
        }

        appMetaData.setPackageName(processManifest.getPackageName());
        appMetaData.setVersionCode(processManifest.getVersionCode());
        appMetaData.setVersionName(processManifest.getVersionName());
        appMetaData.setTargetSdk(processManifest.targetSdkVersion());
        appMetaData.setMinSdk(processManifest.getMinSdkVersion());
        appMetaData.setSha256(StringUtil.extractSha256FromFilePath(apkFilePath));
        appMetaData.getPermissions().addAll(processManifest.getPermissions());

        return appMetaData;
    }
}
