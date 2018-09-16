package main.java;


public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            int startingPoint = 0;
            int endPoint = 2;
//            String apkName = args[0];
//            String androidPlatform = args[1];
//            String folder = args[2];
            ContextDroid contextDroid = new ContextDroid("/media/zakir/HDD2/final_download/5"
                    , "/mnt/6a3e12f5-fa82-4667-be38-a46ad0e34f7c/android-studio/sdk/platforms");
                    contextDroid.start();
        } else {
            System.out.println("Not enough arguments");
            return;
        }
    }
}
