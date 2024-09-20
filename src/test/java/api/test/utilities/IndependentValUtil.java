package api.test.utilities;

import java.time.Duration;
import java.time.Instant;
// import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.endpoints.HTTPRequest;
import api.test.entities.AccountEntity;
import api.test.entities.IndependentEntity;
import io.restassured.response.Response;

public class IndependentValUtil extends ValidationUtil {

	private String payload;
	private String authenRequire;
	private String requestUrl;
	private String username;
	private String password;
	private String expectedResponse;
	private JsonObject jsonResponse;
	private JsonObject jsonExpectedResponse;
	private String timeEslapsed;

	public IndependentValUtil() {

	}

	public void setIndependentEntity(IndependentEntity independentEntity) {
		this.requestUrl = independentEntity.getRequestURL();
		this.payload = independentEntity.getPayload();
		this.authenRequire = independentEntity.getAuthenRequire();
		this.expectedResponse = independentEntity.getSuccessResponse();
	}

	public void setAccountEntity(AccountEntity accountEntity) {
		this.username = accountEntity.getUsername();
		this.password = accountEntity.getPassword();
	}

	public void sendRequest() {
		try {
			Response res = null;
			Integer attempts = 0;
			AuthenUtil au = new AuthenUtil();
			HTTPRequest request = new HTTPRequest(this.requestUrl, this.payload);
			Instant start;
			if (this.authenRequire.equals("NO")) {
				start = Instant.now();
				res = request.post();
			} else {
				while (attempts < 2) {
					if (au.isLoggedIn()) {
						break;
					} else {
						au.authorize("SYSTEM", this.username, this.password);
						attempts++;
					}
				}
				start = Instant.now();
				res = request.postWithSession();
			}
			Instant finish = Instant.now();
			this.timeEslapsed = String.valueOf(Duration.between(start, finish).toMillis());
			super.setReponse(res);
			super.setRequestUrl(requestUrl);
			this.jsonResponse = super.parseJsonStringToJson(res.then().extract().asString());
			this.jsonExpectedResponse = super.parseJsonStringToJson(this.expectedResponse);

		}
		catch (Exception e) {
			System.err.println(
					"\n[IndependentValUtil] Error while trying to send request in : " + e.getMessage());
		}
	}

	public String getApiTimeEslapsed() {
		return this.timeEslapsed;
	}

	public void expectedValueEqualsString(String key, String expectedValue) {
		JsonElement valueElement = this.jsonResponse.get(key);
		super.expectEquals(key, valueElement.getAsString(), expectedValue);
	}

	public void expectedContentKeys() {
		Set<String> exactKeys = new HashSet<String>();
		Set<String> expectedKeys = new HashSet<String>();
		// System.out.println("\nactuResponse: " + this.jsonResponse);
		// System.out.println("\nexpeResponse: " + this.jsonExpectedResponse);
		try {
			if (!this.jsonResponse.isEmpty()) {
				getKeysRecursive(this.jsonResponse, exactKeys);
				// System.out.println("\nexactKeys: " + exactKeys);
			}
			getKeysRecursive(this.jsonExpectedResponse, expectedKeys);
			// System.out.println("\nexpectedKeys: " + expectedKeys);
			super.expectEqualKeySet(exactKeys, expectedKeys);
		}
		catch (Exception e) {
			System.err
					.println("[IndepdentUtil] Error while checking exected content keys: " + e.getMessage());
		}

	}

	public void iterateJson(JsonObject jsonObject, Boolean arraySearchAll, String currentPath,
			BiConsumer<Map.Entry<String, JsonElement>, String> innerFunction) {
		try {
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
							iterateJson(jsonArray.get(i).getAsJsonObject(), arraySearchAll,
									newPath + "[" + i + "]", innerFunction);
						}
					} else
						iterateJson(jsonArray.get(0).getAsJsonObject(), arraySearchAll,
								newPath + "[" + 0 + "]", innerFunction);
				}
			}
		}
		catch (Exception e) {
			System.err.println("[IndependentValUtil] Error while iterating json: " + e.getMessage());
		}
	}

	public void getKeysRecursive(JsonObject jsonObject, Set<String> keys) {
		this.iterateJson(jsonObject, false, "", (entry, path) -> {
			keys.add(path);
		});
	}

	public void expectPositiveResult() {
		super.isLoginNeed();
		super.expectReponseNotNull();
		super.expectStatusCode(200);
		this.expectedValueEqualsString("result", "success");
		this.expectedContentKeys();

	}

	public void captureAllAssert() {
		super.assertAll();
	}

	public void assertFail(String message) {
		super.assertFail(message);
		captureAllAssert();
	}
}
