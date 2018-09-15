package main.java;


public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            int startingPoint = 0;
            int endPoint = 2;
//            String apkName = args[0];
//            String androidPlatform = args[1];
//            String folder = args[2];
            ContextDroid contextDroid = new ContextDroid("/Users/zakir/Desktop/problem apk/"
                    , "/Users/zakir/Library/Android/sdk/platforms", "mirror", 0,4);
            contextDroid.start();
        } else {
            System.out.println("Not enough arguments");
            return;
        }
    }
}
