package main.java;

import java.util.ArrayList;

public class AppMetaData {

    private String packageName;
    private int versionCode;
    private String versionName;
    private int targetSdk;
    private int minSdk;
    private String sha256;
    private ArrayList<String> permissions;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName != null ? versionName : "N/A";
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public ArrayList<String> getPermissions() {
        if(permissions == null) {
            permissions = new ArrayList<>();
        }
        return permissions;
    }

    public void setPermissions(ArrayList<String> permissions) {
        this.permissions = permissions;
    }


    public int getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }
}
