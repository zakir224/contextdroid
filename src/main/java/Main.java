package main.java;


public class Main {

    public static void main(String[] args) {
        if (args.length > 1) {
            String androidPlatform = args[0];
            String apkFolder = args[1];
            System.out.println(androidPlatform + "\t" + apkFolder);
            ContextExtractionManager contextDroid = new ContextExtractionManager(apkFolder
                    , androidPlatform);
                    contextDroid.start();
        } else {
            System.out.println("Not enough arguments. Follow the guidelines below");
            System.out.println("java -cp contextdroid.jar:/* main.java.Main <path to Android platforms> <path to APK directory>");
        }
    }
}
