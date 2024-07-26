package api.utilities;

import java.time.Duration;
import java.time.Instant;
// import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.endpoints.HTTPRequest;
import entities.AccountEntity;
import io.restassured.response.Response;

public class IndependentValUtil extends ValidationUtil {

    private String payload;
    private String authenRequire;
    private String requestUrl;
    private String username;
    private String password;
    private JsonObject jsonResponse;
    private JsonObject jsonExpectedRes;
    private String timeEslapsed;

    public IndependentValUtil(String payload, String requestUrl, String authenRequire, String expectedResponse,
            AccountEntity accountInfo) {
        super(requestUrl);
        this.requestUrl = requestUrl;
        this.payload = payload;
        this.authenRequire = authenRequire;
        this.username = accountInfo.getUsername();
        this.password = accountInfo.getPassword();
        this.jsonExpectedRes = convertToJson(expectedResponse);
    }

    public JsonObject convertToJson(String jsonString) {
        try {
            return JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (Exception e) {
            System.err
                    .println("(" + requestUrl + ") Error occurred during parsing to JSON type: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("null")
    public void sendRequest() {
        try {
            Response res = null;
            Integer attempts = 0;
            AuthenUtil au = new AuthenUtil();
            HTTPRequest request = new HTTPRequest(this.requestUrl, this.payload);
            if (this.authenRequire.equals("NO")) {
                res = request.post();
            } else {

                while (attempts < 2) {
                    res = request.postWithSession();

                    if (au.isLoggedIn(res)) {
                        attempts = 2;
                    } else {
                        au.authorize("SYSTEM", this.username, this.password);
                        attempts++;
                    }
                }
            }
            super.setReponse(res);
            this.jsonResponse = convertToJson(res.then().extract().asString());
        } catch (Exception e) {
            System.err.println("Error while trying to send request in [IndependentValUtil]: " + e.getMessage());
        }
    }

    public String getTimeEslapsed() {
        return this.timeEslapsed;
    }

    public void expectedValueEqualsString(String expectedKey, String expectedValue) {
        JsonElement valueElement = this.jsonResponse.get(expectedKey);
        super.expectEquals(valueElement.getAsString(), expectedValue);
    }

    public void expectedContentKeys() {
        Set<String> exactKeys = new HashSet<String>();
        Set<String> expectedKeys = new HashSet<String>();
        // System.out.println("\nactuResponse: " + this.jsonResponse);
        // System.out.println("\nexpeResponse: " + this.jsonExpectedRes);
        try {
            if (!this.jsonResponse.isEmpty()) {
                String rawContent = this.jsonResponse.get("content").getAsString();
                if (rawContent.contains("data")) {
                    JsonElement parsedContent = JsonParser.parseString(rawContent);
                    this.jsonResponse.remove("content");
                    this.jsonResponse.add("content", parsedContent);
                }
                getKeysRecursive(this.jsonResponse, exactKeys);
                // System.out.println("\nexactKeys: " + exactKeys);
            }
            getKeysRecursive(this.jsonExpectedRes, expectedKeys);
            // System.out.println("\nexpectedKeys: " + expectedKeys);
            super.expectEquals(exactKeys, expectedKeys);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public void iterateJson(JsonObject jsonObject, Boolean arraySearchAll, String currentPath,
            BiConsumer<Map.Entry<String, JsonElement>, String> innerFunction) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
            if (!entry.getValue().isJsonArray()) {
                innerFunction.accept(entry, newPath);
            }
            if (entry.getValue().isJsonObject()) {
                iterateJson(entry.getValue().getAsJsonObject(), arraySearchAll, newPath, innerFunction);
            } else if (entry.getValue().isJsonArray()) {
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                if (arraySearchAll) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        iterateJson(jsonArray.get(i).getAsJsonObject(), arraySearchAll, newPath + "[" + i + "]",
                                innerFunction);
                    }
                } else
                    iterateJson(jsonArray.get(0).getAsJsonObject(), arraySearchAll, newPath + "[" + 0 + "]",
                            innerFunction);
            }
        }
    }

    public void getKeysRecursive(JsonObject jsonObject, Set<String> keys) {
        this.iterateJson(jsonObject, false, "", (entry, path) -> {
            keys.add(path);
        });
    }

    public void expectPositiveResult() {
        Instant start = Instant.now();
        super.isLoginNeed();
        super.expectStatusCode(200);
        this.expectedValueEqualsString("result", "success");
        this.expectedContentKeys();
        Instant finish = Instant.now();
        this.timeEslapsed = String.valueOf(Duration.between(start, finish).toMillis());
    }

    public void captureAllAssert() {
        super.assertAll();
    }

    public void assertFail(String message) {
        super.assertFail(message);
        captureAllAssert();
    }
}
