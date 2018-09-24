package main.java;

public class CallerMethod {

    private CallerType visibilityType;
    private String packageName;
    private String methodName;
    private String className;
    private String eventType;


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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public CallerType getVisibilityType() {
        return visibilityType;
    }

    public void setVisibilityType(CallerType visibilityType) {
        this.visibilityType = visibilityType;
    }
}
