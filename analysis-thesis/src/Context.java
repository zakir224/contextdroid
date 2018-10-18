import main.java.Util.StringUtil;

public class Context {

    private String className;
    private String methodName;
    private String permission;
    private String visibilityType;
    private String sha256;
    private String permissionApiIfUsage;
    private String app;

    public Context getUsageContext(String[] tokens) {
        if(tokens != null && tokens.length > 8) {
            this.app = tokens[0] + "_" + tokens[2];
            this.className = tokens[3];
            this.methodName = tokens[4];
            this.permission = tokens[5];
            this.permissionApiIfUsage = tokens[6];
            this.visibilityType = tokens[7];
            this.sha256 = tokens[8];
            return this;
        } else {
            return null;
        }
    }


    public Context getRequestContext(String[] tokens) {
        if(tokens != null && tokens.length > 7) {
            this.app = tokens[0] + "_" + tokens[2];
            this.className = tokens[3];
            this.methodName = tokens[4];
            this.permission = tokens[5];
            this.visibilityType = tokens[6];
            this.sha256 = tokens[7];
            return this;
        } else {
            return null;
        }
    }

    public String getVisibilityType() {
        return visibilityType;
    }

    public void setVisibilityType(String visibilityType) {
        this.visibilityType = visibilityType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermissionApiIfUsage() {
        return permissionApiIfUsage;
    }

    public void setPermissionApiIfUsage(String permissionApiIfUsage) {
        this.permissionApiIfUsage = permissionApiIfUsage;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public static String getAppName(String line) {
        if(line != null && line.length() >0) {
            return line.split("\t")[0];
        }
        return "";
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getString() {
        return this.className +":"+this.visibilityType;
    }

    public String getStringUsage() {
        return this.className +":"+this.visibilityType;
    }
}
