package main.java.Util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;

public class StringUtil {

    public static String extractSha256FromFilePath(String apkFilePath) {
        File file = new File(apkFilePath);

        if (file.exists()) {
            String[] fileNameToken = file.getName().split("\\.");
            if (fileNameToken.length == 2) { // its a hash
                return fileNameToken[0];
            } else {            // find the hash
                return toHex(Hash.SHA256.checksum(file));
            }
        } else {
            return null;
        }
    }

    private static String toHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
