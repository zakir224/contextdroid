package main.java.Permission;

public class Permission {

    public Permission(String packageName, String methodSignature, String returnType, String permission) {
        this.packageName = packageName;
        this.methodSignature = methodSignature;
        this.returnType = returnType;
        this.permission = permission;
    }

    private String packageName;
    private String methodSignature;
    private String returnType;
    private String permission;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return  permission + " " + packageName + " " + returnType + " " + methodSignature;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
