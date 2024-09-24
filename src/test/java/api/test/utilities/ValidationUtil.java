package api.test.utilities;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class ValidationUtil {

	private Response response;
	private String requestUrl;
	private SoftAssert softAssert = new SoftAssert();
	private final Pattern escapeHtmlPattern = Pattern.compile("u[a-zA-Z0-9]+;");

	public ValidationUtil() {
	}

	public void setReponse(Response response) {
		this.response = response;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public void expectStatusCode(Integer expectedStatusCode) {
		softAssert.assertEquals(Integer.valueOf(this.response.getStatusCode()), expectedStatusCode,
				"Expected [statusCode]: " + expectedStatusCode + " but got " + this.response.getStatusCode()
						+ "");
	}

	public void expectEquals(String key, String actualValue, String expectedValue) {
		softAssert.assertEquals(actualValue, expectedValue, "[" + key + "]: ");
	}

	public void expectEqualKeySet(Set<String> actualValue, Set<String> expectedValue) {
		Set<String> missingSet = actualValue.stream().filter(e -> !expectedValue.contains(e))
				.collect(Collectors.toSet());
		softAssert.assertEquals(actualValue, expectedValue,
				" ".repeat(Math.max(0, 6)) + "Expected: " + expectedValue.size() + " keys\n"
						+ " ".repeat(Math.max(0, 6)) + "Actual: " + actualValue.size() + " keys\n"
						+ " ".repeat(Math.max(0, 6)) + "Missing: " + missingSet.toString());
	}

	public void isLoginNeed() {
		if (this.response.getStatusCode() == 302 && this.response.getHeader("LOCATION").contains("login")) {
			softAssert.fail("Login required to access this path: " + this.requestUrl);
		}
	}

	public void assertAll() {
		softAssert.assertAll();
	}

	public void assertFail(String message) {
		softAssert.fail(message);
	}

	public void expectNotNull(String key, String value) {
		softAssert.assertNotNull(value, "Expect [" + key + "] not null, but got null");
	}

	public void expectReponseNotNull() {
		Assert.assertNotNull(this.response, "Response is null");
	}

	public JsonObject parseJsonString(String stringResponse) {
		try {
			Matcher escapeHtmlMatcher = escapeHtmlPattern.matcher(stringResponse);
			if (escapeHtmlMatcher.find()) {
				stringResponse = StringEscapeUtils.unescapeHtml4(stringResponse);
				stringResponse = removeHtmlTags(stringResponse);
			}
			JsonObject parsedJson = JsonParser.parseString(stringResponse).getAsJsonObject();
			if (parsedJson.get("content").toString().contains("\\\"")) {
				String contentString = parsedJson.get("content").getAsString();
				JsonObject contentJson = JsonParser.parseString(contentString).getAsJsonObject();
				parsedJson.add("content", contentJson);
			}
			return parsedJson;
		}
		catch (Exception e) {
			System.err.println("\nValidationUtil (" + requestUrl
					+ ") Error occurred while parsing response to JSON: " + e.getMessage());
			return null;
		}
	}

	public String stringHtmlUnescape(String value) {
		try {
			Matcher escapeHtmlMatcher = escapeHtmlPattern.matcher(value);
			if (escapeHtmlMatcher.find()) {
				value = StringEscapeUtils.unescapeHtml4(value);
				// value = removeHtmlTags(value);
				value = value.replaceAll("\\\\*<^>*>", "");
			}
			return value;
		}
		catch (Exception e) {
			System.err.println("ValidationUtil Error while unescape html tags: " + requestUrl);
			return null;

		}
	}

	public String removeHtmlTags(String input) {
		try {
			StringBuilder result = new StringBuilder();
			boolean insideTag = false;

			for (int i = 0; i < input.length(); i++) {
				char currentChar = input.charAt(i);

				if (currentChar == '\\' && input.charAt(i + 1) == '<') {
					insideTag = true;
				} else if (currentChar == '>') {
					insideTag = false;
					continue;
				}
				if (!insideTag) {
					result.append(currentChar);
				}
			}
			return result.toString();
		}
		catch (Exception e) {
			System.err.println("[ValidationUtil] Error while unescape html tags: " + requestUrl);
			return null;
		}
	}
}
