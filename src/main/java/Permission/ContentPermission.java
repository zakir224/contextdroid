package main.java.Permission;

public class ContentPermission extends Permission {

    private String contentUri;

    public ContentPermission(String packageName, String methodSignature, String returnType, String permission) {
        super(packageName, methodSignature, returnType, permission);
    }

    public ContentPermission(String packageName, String contentUri, String permission) {
        this(packageName, "", "", permission);
        this.contentUri = contentUri;
    }

    public String getContentUri() {
        return contentUri;
    }

    public void setContentUri(String contentUri) {
        this.contentUri = contentUri;
    }

    @Override
    public String toString() {
        return  getPermission() + " " + getPackageName() + " " + getContentUri();
    }
}
