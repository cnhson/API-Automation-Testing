package api.test.utilities;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.endpoints.HTTPRequest;
import api.test.entities.AccountEntity;
import api.test.entities.ChainingEntity;
import io.restassured.response.Response;

public class ChainingValUtil extends ValidationUtil {

	private JsonObject jsonPayLoad;
	private JsonObject jsonVariableAndValue = new JsonObject(); // Stored variable and value as JSON
	private String requestUrl;
	private String authenRequire;
	private String username;
	private String password;
	private JsonObject jsonResponse;
	private HashSet<String> varList = new HashSet<String>(); // Temporary variable list ( for each API, new variable
																// list will be imported )
	private String apiTimeEslapsed;

	public ChainingValUtil() {
	}

	public void setChainingEntity(ChainingEntity chainingEntity) {
		this.requestUrl = chainingEntity.getRequestURL();
		this.authenRequire = chainingEntity.getAuthenRequire();
		this.jsonPayLoad = JsonParser.parseString(chainingEntity.getPayload()).getAsJsonObject();
	}

	public void setAccountEntity(AccountEntity accountEntity) {
		this.username = accountEntity.getUsername();
		this.password = accountEntity.getPassword();
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
			AuthenUtil au = new AuthenUtil();
			Instant start;
			HTTPRequest request = new HTTPRequest(this.requestUrl, this.jsonPayLoad.toString());
			if (this.authenRequire.equals("NO")) {
				start = Instant.now();
				res = request.post();
			} else {
				au.isLoggedIn(this.username, this.password);
				start = Instant.now();
				res = request.postWithSession();
			}
			Instant finish = Instant.now();
			this.apiTimeEslapsed = String.valueOf(Duration.between(start, finish).toMillis());
			super.setRequestUrl(requestUrl);
			super.setReponse(res);
			super.expectReponseNotNull();
			this.jsonResponse = super.parseJsonString(res.then().extract().asString());

		}
		catch (Exception e) {
			// System.out.println("\n[Failed] Json Payload: " + this.jsonPayLoad.toString() + "\n");
			System.err.println("\n[ChainingValUtil] Error while trying to send request: " + e.getMessage());
		}
	}

	public String getApiTimeEslapsed() {
		return this.apiTimeEslapsed;
	}

	// Checking variable slot in 1 single String line
	// For example: aa: ${key}
	public Boolean isVariableSlot(String value) {
		try {
			if (value.startsWith("${"))
				return true;
			else
				return false;
		}
		catch (Exception e) {
			System.err.println(
					"\n[ChainingValUtil] Error while trying to check variable slot: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Checking if there is variable slot in String paragraph. Formula {@code{key}}
	 * <p>
	 * For example: {"aaa: ${data[0].name}, bbb: "bbb", ccc: "1"}
	 * </p>
	 **/
	public Boolean isVariableSlotInString(String longString) {
		try {
			if (longString.contains("${"))
				return true;
			else
				return false;
		}
		catch (Exception e) {
			System.err.println(
					"\n[ChainingValUtil] Error while trying to check variable slot: " + e.getMessage());
			return false;
		}
	}

	/** Main function for looping with callback function **/
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
	public void iterateJsonExtractKeyValue(JsonObject jsonObject, HashSet<String> tempHashSet) {
		try {
			this.iterateJson(jsonObject, "", (entry, newPath) -> {
				Optional<String> foundVar = this.varList.stream().filter(newPath::contains).findFirst();
				if (foundVar.isPresent()) {
					// String value = stringHtmlUnescape(entry.getValue().toString());
					this.jsonVariableAndValue.add(foundVar.get(),
							JsonParser.parseString(entry.getValue().toString()));
					if (tempHashSet != null) {
						tempHashSet.add(foundVar.get());
					}
				}
			});
		}
		catch (Exception e) {
			System.err.println(
					"[ChainingValUtil] Error while trying to extracting key value: " + e.getMessage());
		}
	}

	public void iterateJsonInsertValue(JsonObject jsonObject, JsonObject importJsonVariableAndValue) {
		this.iterateJson(jsonObject, "", (entry, newPath) -> {
			if (this.isVariableSlot(entry.getValue().getAsString())) {
				String currentVar = entry.getValue().getAsString().split("\\$\\{")[1].split("\\}")[0];
				entry.setValue(importJsonVariableAndValue.get(currentVar));
			}
		});
	}

	/**
	 * Verify value stored from previous steps. For example:
	 * <p>
	 * + Change account's password from {@code${oldPassword}} to {@code${newPassword}}, store {@code${newPassword}} into
	 * jsonKeyAndValue which is passed to next steps.
	 * </p>
	 * <p>
	 * + In final step: We want to verify if password is changed by getting {@code${newPassword}} from jsonKeyAndValue and
	 * compare with current password from Response
	 * </p>
	 **/
	public void verifyReponse(HashSet<String> verifyVarList, JsonObject importJsonVariableAndValue) {
		// Loop comparing key & value
		this.iterateJson(this.jsonResponse, "", (entry, newPath) -> {
			Optional<String> foundVar = verifyVarList.stream().filter(newPath::contains).findFirst();
			if (foundVar.isPresent() && importJsonVariableAndValue.get(foundVar.get()) != null) {
				// System.out.println("?? " + importJsonVariableAndValue);
				super.expectEquals(entry.getKey(), entry.getValue().toString(),
						importJsonVariableAndValue.get(foundVar.get()).toString());
			}
		});
	}

	// Get variable & value from response or jsonPayLoad
	public void getVariableValue() {
		HashSet<String> notFoundSet = new HashSet<>();
		iterateJsonExtractKeyValue(this.jsonResponse, notFoundSet);
		// Take variable & value from jsonPayLoad if response doesn't contain them
		if (notFoundSet.size() > 0) {
			iterateJsonExtractKeyValue(this.jsonPayLoad, null);
		}
	}

	// Insert variable value to jsonPayLoad's slot - ${value}
	public void insertVariableSlotValue(JsonObject imporJsonObject) {
		try {
			if (!imporJsonObject.isEmpty())
				iterateJsonInsertValue(this.jsonPayLoad, imporJsonObject);
		}
		catch (Exception e) {
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
		}
		catch (Exception e) {
			System.err.println(
					"\n[ChainingUtil] Error while checking if variable slot in param: " + e.getMessage());
			return false;
		}
	}

	public void expectedValueEqualsString(String key, String expectedValue) {
		JsonElement valueElement = this.jsonResponse.get(key);
		super.expectEquals(key, valueElement.getAsString(), expectedValue);
	}

	public void expectSuccessResult(Boolean isFinalStep, HashSet<String> verifyVarList,
			HashSet<String> variableList, JsonObject importJsonVariableAndValue) {
		try {
			long startTime = System.nanoTime();
			super.isLoginNeed();
			if (!isFinalStep) {

				super.expectStatusCode(200);

				this.expectedValueEqualsString("result", "success");
				// Set variableList from Excel to ChainingUtil

				this.setVariableList(variableList);

				// Get key, value from res or jsonPayLoad according to variableList above
				this.getVariableValue();

				// Store variable and value list for next step
				this.exportVariableAndValue(importJsonVariableAndValue);

			} else {
				// System.out.println("Final step, verifying " + verifyVarList);
				this.verifyReponse(verifyVarList, importJsonVariableAndValue);
			}

			long endTime = System.nanoTime() - startTime;
			System.out.println("\nReq: " + this.requestUrl + ", time: " + endTime);
		}
		catch (Exception e) {
			System.err.println("\n[ChainingValUtil] Error trying to validate positive result in path ["
					+ requestUrl + "] :" + e.getMessage());
		}
	}

	public void exportVariableAndValue(JsonObject target) {
		for (String key : this.jsonVariableAndValue.keySet()) {
			target.add(key, this.jsonVariableAndValue.get(key));
		}
		// System.out.println("\nExtracted: " + this.jsonVariableAndValue + "\nExported: " + target + "\n");
	}

	public void captureAllAssert() {
		super.assertAll();
	}

	public void assertFail(String message) {
		super.assertFail(message);
		captureAllAssert();
	}
}
