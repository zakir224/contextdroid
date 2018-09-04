
public class Category {

    private String appName;
    private int minInstall;
    private int MaxInstall;
    private String category;

    public Category(String appName, int minInstall, int maxInstall, String category) {
        this.appName = appName;
        this.minInstall = minInstall;
        MaxInstall = maxInstall;
        this.category = category;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMaxInstall() {
        return MaxInstall;
    }

    public void setMaxInstall(int maxInstall) {
        MaxInstall = maxInstall;
    }

    public int getMinInstall() {
        return minInstall;
    }

    public void setMinInstall(int minInstall) {
        this.minInstall = minInstall;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
