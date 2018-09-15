package main.java;

public class ApkProcessingStatistic {

    private int callGraphGenerationTime;
    private int listingMethodsTime;
    private int contextExtractionTime;
    private int numberOfClasses;
    private String app;
    private int numberOfMethods;
    private float fileSize;

    public int getCallGraphGenerationTime() {
        return callGraphGenerationTime;
    }

    public void setCallGraphGenerationTime(int callGraphGenerationTime) {
        this.callGraphGenerationTime = callGraphGenerationTime;
    }

    public int getListingMethodsTime() {
        return listingMethodsTime;
    }

    public void setListingMethodsTime(int listingMethodsTime) {
        this.listingMethodsTime = listingMethodsTime;
    }

    public int getContextExtractionTime() {
        return contextExtractionTime;
    }

    public void setContextExtractionTime(int contextExtractionTime) {
        this.contextExtractionTime = contextExtractionTime;
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int numberOfMethods) {
        this.numberOfMethods = numberOfMethods;
    }

    public float getFileSize() {
        return fileSize;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return app + "\t" + fileSize + "\t" + callGraphGenerationTime+ "\t" + listingMethodsTime + "\t" +contextExtractionTime + "\t" + numberOfClasses + "\t" + numberOfMethods;
    }
}
