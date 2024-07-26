package api.utilities;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import api.test.endpoints.HTTPRequest;
import io.restassured.http.Cookies;
import io.restassured.response.Response;

public class AuthenUtil {

    HashUtil hu = new HashUtil();
    SessionCookieUtil cu = new SessionCookieUtil();
    PropertyUtil pu = new PropertyUtil();

    public AuthenUtil() {

    }

    public Boolean isSessionCookieExist() {
        try {
            if (!SessionCookieUtil.getFromFile().isBlank() && !SessionCookieUtil.getFromFile().isEmpty()) {
                return true;
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public void authorize(String pathRole, String username, String password) {
        try {

            String hashedPass = hu.getHashedValue(password);
            String loginUrlWithRole = pu.getPropAsString(pathRole);

            JsonObject payload = new JsonObject();
            payload.addProperty("username", username);
            payload.addProperty("password", hashedPass);
            payload.addProperty("secureinfo", "");

            HTTPRequest request = new HTTPRequest(loginUrlWithRole, payload.toString());
            Response res = request.post();

            String resBody = res.then().extract().asString();
            if (!resBody.isBlank()) {
                if (resBody.contains("fail")) {
                    System.out.println("› Wrong password or username");
                } else if (resBody.contains("success")) {
                    Cookies resCookie = res.then().extract().detailedCookies();
                    CompletableFuture<Void> storeToFileFuture = CompletableFuture.runAsync(() -> {
                        SessionCookieUtil.storeToFile(resCookie.toString());
                    });
                    storeToFileFuture.get(); // Wait for completion before continuing
                    System.out.println("› Login successful with username: " + username);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while trying to authorize: " + e.getMessage());
        }
    }
}
