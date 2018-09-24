package main.java;


import java.util.ArrayList;
import java.util.List;

public class RequestMethodContext {

    private CallerType visibilityType;
    private String callerClassType;
    private String packageName;
    private String methodName;
    private String className;
    private String permission;
    private String eventType;
    private boolean isLibaryMethod;
    private List<CallerMethod> callerMethodList;
    private List<String> callerList;

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

//    enum Caller {
//        ACTIVITY,
//        FRAGMENT,
//        DIALOG,
//        SERVICE,
//        BROADCAST_RECEIVER,
//        ASYNC_TASK,
//        CUSTOM
//    }

    public RequestMethodContext() {
        callerMethodList = new ArrayList<>();
    }

    public RequestMethodContext(CallerType visibilityType, String packageName, String methodName, String className, String permission) {
        this.visibilityType = visibilityType;
        this.packageName = packageName;
        this.methodName = methodName;
        this.className = className;
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }


    public CallerType getVisibilityType() {
        return visibilityType;
    }

    public void setVisibilityType(CallerType visibilityType) {
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


    public List<String> getCallerList() {
        return callerList;
    }

    public void setCallerList(List<String> callerList) {
        this.callerList = callerList;
    }

    public String getCallerClassType() {
        return callerClassType;
    }

    public void setCallerClassType(String callerClassType) {
        this.callerClassType = callerClassType;
    }
}
