package api.test.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

public class SessionCookieUtil {
    static private File sessionCookieFile = new File("src/test/resources/private/sessionCookie.txt");

    public static void storeToFile(String cookieString) {
        String cookieSession = cookieString.split(";Domain")[0];
        try (PrintWriter writer = new PrintWriter(new FileWriter(sessionCookieFile.getPath()))) {
            writer.println(cookieSession.trim());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFromFile() {
        try {
            String data = FileUtils.readFileToString(sessionCookieFile, "UTF-8").trim();
            return data;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}