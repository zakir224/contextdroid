package main.java;

import main.java.Permission.Permission;

import java.util.ArrayList;
import java.util.List;

public class MethodContext {

    private Caller visibilityType;
    private String packageName;
    private String methodName;
    private String className;
    private Permission permission;
    private String eventType;
    private boolean isLibaryMethod;
    private List<CallerMethod> callerMethodList;
    private String permissionString;
    private List<String> callerList;
    private String callerClassType;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<CallerMethod> getCallerMethodList() {
        return callerMethodList;
    }

    public void setCallerMethodList(List<CallerMethod> callerMethodList) {
        this.callerMethodList = callerMethodList;
    }

    public boolean isLibaryMethod() {
        return isLibaryMethod;
    }

    public void setLibaryMethod(boolean libaryMethod) {
        isLibaryMethod = libaryMethod;
    }

    public void setPermissionString(String permission) {

        permissionString = permission;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setCallerList(List<String> callerList) {
        this.callerList = callerList;
    }

    public List<String> getCallerList() {
        return callerList;
    }

    public void setCallerClassType(String callerClassType) {
        this.callerClassType = callerClassType;
    }

    public String getCallerClassType() {
        return callerClassType;
    }

    enum Caller {
        ACTIVITY,
        FRAGMENT,
        DIALOG,
        SERVICE,
        BROADCAST_RECEIVER,
        ASYNC_TASK,
        CUSTOM
    }

    public MethodContext() {
        callerMethodList = new ArrayList<>();
    }

    public MethodContext(Caller visibilityType, String packageName, String methodName, String className, Permission permission) {
        this.visibilityType = visibilityType;
        this.packageName = packageName;
        this.methodName = methodName;
        this.className = className;
        this.permission = permission;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }


    @Override
    public String toString() {
        return permission + " " + visibilityType.toString() + " " + packageName + " " + className + " " + methodName;
    }


    public String toStringOutput() {
        String callers = findCallers();
        return permission.getMethodSignature() + " protected by " + permission.getPermission() + " was accessed from "
                + getVisibilityType() +" " + className + " " + methodName;
    }

    private String findCallers() {
        String canBeCalledFrom = "";
        for (CallerMethod callerMethod : callerMethodList) {
            if (callerMethod.getVisibilityType() != Caller.CUSTOM)
                canBeCalledFrom = canBeCalledFrom.concat(" ") +
                        callerMethod.getClassName() + " " + callerMethod.getVisibilityType().toString();
        }
        return "";
    }


    public Caller getVisibilityType() {
        return visibilityType;
    }

    public void setVisibilityType(Caller visibilityType) {
        this.visibilityType = visibilityType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getInvokedMethod() {
        return invokedMethod;
    }

    public void setInvokedMethod(String invokedMethod) {
        this.invokedMethod = invokedMethod;
    }

    private String invokedMethod;


}
