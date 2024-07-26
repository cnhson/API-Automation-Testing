package api.utilities;

import java.util.HashSet;
import java.util.Iterator;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.endpoints.HTTPRequest;
import entities.AccountEntity;
import io.restassured.response.Response;

public class ChainingValUtil extends ValidationUtil {

    private JsonObject jsonPayLoad;
    private JsonObject jsonVariableAndValue = new JsonObject(); // Stored variable and value as JSON
    private Response response;
    private String requestUrl;
    private String authenRequire;
    private String username;
    private String password;
    private HashSet<String> arrayKeys = new HashSet<>(Arrays.asList("data", "listString"));
    private HashSet<String> varList = new HashSet<String>(); // Temporary variable list ( for each API, new variable
                                                             // list will be imported )
    private String timeEslapsed;

    public ChainingValUtil(String payload, String requestUrl, String authenRequire, AccountEntity accountInfo) {
        super(requestUrl);
        this.requestUrl = requestUrl;
        this.authenRequire = authenRequire;
        this.username = accountInfo.getUsername();
        this.password = accountInfo.getPassword();
        this.jsonPayLoad = JsonParser.parseString(payload).getAsJsonObject();
    }

    public void setVariableList(HashSet<String> variableList) {
        this.varList = variableList;
    }

    public String getInsertedjsonPayLoadAsString() {
        return this.jsonPayLoad.toString();
    }

    public void sendRequest() {
        try {
            Response res = null;
            Integer attempts = 0;
            AuthenUtil au = new AuthenUtil();
            HTTPRequest request = new HTTPRequest(this.requestUrl, this.jsonPayLoad.toString());
            if (this.authenRequire.equals("NO")) {
                res = request.post();
            } else {
                while (attempts < 2) {
                    if (au.isLoggedIn(res)) {
                        attempts = 2;
                        break;
                    } else {
                        au.authorize("SYSTEM", this.username, this.password);
                        attempts++;
                    }
                    res = request.postWithSession();
                }
            }
            super.setReponse(res);
            this.response = res;
            System.out.println(
                    "\n[Success] Json Payload: " + this.jsonPayLoad.toString() + "\n");
        } catch (Exception e) {
            System.out.println(
                    "\n[Failed] Json Payload: " + this.jsonPayLoad.toString() + "\n");
            System.err.println("[ChainingValUtil] Error while trying to send request: " + e.getMessage());
        }
    }

    public String getTimeEslapsed() {
        return this.timeEslapsed;
    }

    // Checking variable slot in 1 single String line
    // For example: aa: {key}
    public Boolean isVariableSlot(String value) {
        try {
            // Example: {key}, {name}, {id}
            Pattern pattern = Pattern.compile("\\$\\{.+\\}$");
            Matcher matcher = pattern.matcher(value);

            return matcher.find();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Checking variable slot in String paragraph
    // For example: /"aaa: {key}, bbb: "bbb", ccc: "1"/"
    public Boolean isVariableSlotInString(String longString) {
        try {
            // Example: aa {key} aa, cc {name} cc, bb {id} bb
            Pattern pattern = Pattern.compile(".*\\$\\{.+\\}$");
            Matcher matcher = pattern.matcher(longString);

            return matcher.find();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Main function for looping with custom child function inside
    public void iterateJson(JsonObject jsonObject, String currentPath,
            BiConsumer<Map.Entry<String, JsonElement>, String> innerFunction) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
            if (!entry.getValue().isJsonArray()) {
                innerFunction.accept(entry, newPath);
            }
            if (entry.getValue().isJsonObject()) {
                iterateJson(entry.getValue().getAsJsonObject(), newPath, innerFunction);
            } else if (entry.getValue().isJsonArray()) {
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement element = jsonArray.get(i);
                    iterateJson(element.getAsJsonObject(), newPath + "[" + i + "]", innerFunction);
                }
            }
        }
    }

    // Extracting key & value according to imported variable list
    // And then store them into jsonVariableAndValue
    public void iterateJsonExtractKeyValue(JsonObject jsonObject) {
        this.iterateJson(jsonObject, "", (entry, newPath) -> {
            Iterator<String> iterator = varList.iterator();
            while (iterator.hasNext()) {
                String var = iterator.next();
                if (newPath.contains(var)) {
                    jsonVariableAndValue.add(var, entry.getValue());
                    iterator.remove();
                }
            }
        });
    }

    // Loop inserting value in variable slot
    public void iterateJsonInsertValue(JsonObject jsonObject, JsonObject jsonVariableAndValue) {
        this.iterateJson(jsonObject, "", (entry, newPath) -> {
            if (this.isVariableSlot(entry.getValue().getAsString())) {
                String currentVar = entry.getValue().getAsString().split("\\$\\{")[1].split("\\}")[0];
                entry.setValue(jsonVariableAndValue.get(currentVar));
            }
        });
    }

    // Loop comparing key & value
    public void iterateJsonCompare(HashSet<String> verifyVarList, JsonObject jsonObject,
            JsonObject jsonVariableAndValue) {
        this.iterateJson(jsonObject, "", (entry, newPath) -> {
            for (String variable : verifyVarList) {
                if (newPath.contains(variable)) {
                    if (entry.getValue().toString().equals(jsonVariableAndValue.get(variable).toString())) {
                        super.expectEquals(entry.getValue().toString(),
                                jsonVariableAndValue.get(variable).toString() + "1");
                        System.out.println("+ " + entry.getKey() + " : " + entry.getValue() + " == "
                                + jsonVariableAndValue.get(variable).toString());
                    }
                }
            }
        });
    }

    // Comparing variable & value taken in previous step to final one
    // For example: change account password from ${oldPassword} to ${newPassword}
    // In final step: get password from reponse and compare with ${newPassword}
    // stored
    public void verifyReponse(HashSet<String> verifyVarList, JsonObject jsonImportVariableAndValue) {

        String resBody = this.response.then().extract().asString();
        JsonObject jsonResponse = null;
        if (!resBody.isBlank()) {
            jsonResponse = JsonParser.parseString(resBody).getAsJsonObject();
            if (isJsonContentObjectType(jsonResponse)) {
                String rawContent = jsonResponse.get("content").getAsString();
                if (verifyVarList.contains("content")) {
                    super.expectEquals(rawContent, "success");
                } else {
                    JsonElement parsedContent = JsonParser.parseString(rawContent);
                    jsonResponse.remove("content");
                    jsonResponse.add("content", parsedContent);
                    iterateJsonCompare(verifyVarList, jsonResponse, jsonImportVariableAndValue);
                }
            }
        }
    }

    // Get variable & value from response or jsonPayLoad
    public void getVariableValue() {
        String resBody = this.response.then().extract().asString();
        Boolean responseFlag = false;
        JsonObject jsonResponse = null;
        // High priority in getting variable & value from response
        if (!resBody.isBlank()) {
            jsonResponse = JsonParser.parseString(resBody).getAsJsonObject();
            if (isJsonContentObjectType(jsonResponse)) {
                String rawContent = jsonResponse.get("content").getAsString();
                JsonElement parsedContent = JsonParser.parseString(rawContent);
                jsonResponse.remove("content");
                jsonResponse.add("content", parsedContent);
                iterateJsonExtractKeyValue(jsonResponse);
                responseFlag = true;
            }
        }
        // Take variable & value from jsonPayLoad if response doesn't contain them
        if (responseFlag == false) {
            iterateJsonExtractKeyValue(this.jsonPayLoad);
        }
    }

    // Insert variable value to jsonPayLoad's slot - ${value}
    public void insertVariableSlotValue(JsonObject imporJsonObject) {
        try {
            // System.out.println("imporJsonObject: " + imporJsonObject);
            if (!imporJsonObject.isEmpty())
                iterateJsonInsertValue(this.jsonPayLoad, imporJsonObject);
        } catch (Exception e) {
            System.err.println("Insert variable slot value failed: " + e.getMessage());
        }
    }

    // Check if there is any variable slot in jsonPayLoad
    public Boolean checkIfVariableSlotInParam() {
        try {
            if (isVariableSlotInString(this.getInsertedjsonPayLoadAsString()))
                return true;
            else
                return false;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Check if Json value is content type
    public Boolean isJsonContentObjectType(JsonObject jsonObject) {
        try {
            String subString = jsonObject.get("content").getAsString().split("\":")[0];
            for (String keyWord : arrayKeys) {
                if (subString.contains(keyWord))
                    return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Checking content type failed: " + e.getMessage());
            return false;
        }
    }

    public void expectSuccessResult(Boolean isFinalStep, HashSet<String> verifyVarList, HashSet<String> variableList,
            JsonObject jsonVariableAndValue) {
        try {
            Instant start = Instant.now();
            super.isLoginNeed();
            if (!isFinalStep) {
                super.expectStatusCode(200);
                // Set variableList from Excel to ChainingUtil
                this.setVariableList(variableList);
                // Get key, value from res or jsonPayLoad according to variableList above
                this.getVariableValue();
                // Store variable and value list for next step
                this.exportVariableAndValue(jsonVariableAndValue);
                System.out.println("Current stored: " + jsonVariableAndValue);
            } else {
                System.out.println("Final step, verifying " + verifyVarList);
                this.verifyReponse(verifyVarList, jsonVariableAndValue);
            }
            Instant finish = Instant.now();
            this.timeEslapsed = String.valueOf(Duration.between(start, finish).toMillis());
        } catch (Exception e) {
            System.err
                    .println("[ChainingValUtil] Error trying to validate positive result in path [" + requestUrl + "] :"
                            + e.getMessage());
        }
    }

    public void exportVariableAndValue(JsonObject target) {
        for (String key : this.jsonVariableAndValue.keySet()) {
            target.add(key, this.jsonVariableAndValue.get(key));
        }
    }

    public void captureAllAssert() {
        super.assertAll();
    }

    public void assertFail(String message) {
        super.assertFail(message);
        captureAllAssert();
    }
}
