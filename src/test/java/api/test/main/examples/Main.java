package api.test.main.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.testng.asserts.SoftAssert;

import com.google.common.html.HtmlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.endpoints.HTTPRequest;
import api.test.utilities.AuthenUtil;
import api.test.utilities.DatabaseUtil;
import api.test.utilities.PropertyUtil;
import groovy.json.JsonException;
import io.restassured.response.Response;

public class Main {

	public static void main(String[] args) {
		String fileName = "src/test/java/api/test/main/examples/testResponse4.txt"; // Replace with your file name
		String stringRes = readFileToString(fileName);
		String escapedString = StringEscapeUtils.escapeJava(stringRes);
		System.out.println("\nBefore: " + StringEscapeUtils.unescapeJava(escapedString));
		System.out.println("\nAfter:  " + escapedString);

	}

	public static void main1(String[] args) {
		try {
			String fileName = "src/test/java/api/test/main/examples/testResponse.txt"; // Replace with your file name
			String stringRes = readFileToString(fileName);
			// String stringRes = res.then().extract().body().asPrettyString();
			// System.out.println("\nBefore: " + stringRes);
			Pattern pattern = Pattern.compile("\\\"");

			Gson gson = new Gson();
			// Pattern escapeHtmlPattern = Pattern.compile("&[^;]+;");
			Pattern pattern2 = Pattern.compile(
					"(?<=[{}:,\\\\[\\\\]\\\\])\\\\+\\\"|(?<!\\\\\\\")\\\\+\\\"(?=[{}:,\\\\[\\\\]\\\\]+)");
			Pattern pattern3 = Pattern.compile("(?<=[{}:,\\[\\]])\\\\+\"|\\\\+\"(?=[{}:,\\[\\]]+)");
			// Matcher escapeHtmlMatcher = escapeHtmlPattern.matcher(stringRes);

			// StringEscapeUtils seu;
			String baseStringRes = StringEscapeUtils.unescapeHtml4(stringRes);

			System.out.println("\n\nBefore remove: " + baseStringRes);

			long start_1 = System.nanoTime();
			String testRes_1 = baseStringRes.replaceAll("\\\\*<[^>]*>", "");
			long end_1 = System.nanoTime() - start_1;
			System.out.println("\nAfter time_1: " + end_1);
			System.out.println("\nAfter remove: " + testRes_1);

			long start0 = System.nanoTime();
			String testRes = removeHtmlTags(baseStringRes);
			long end0 = System.nanoTime() - start0;
			System.out.println("\nAfter time 0: " + end0);
			System.out.println("\nremoveHtmlTags: " + testRes);

			long start = System.nanoTime();
			baseStringRes = RegExUtils.replaceAll(baseStringRes, "\\\\*<[^>]*>", "");
			long end = System.nanoTime() - start;

			System.out.println("\nAfter time -: " + end);
			System.out.println("\nAfter remove: " + baseStringRes);
			// String res = StringUtils.replace(baseStringRes, "\\\"", "");
			System.out.println(
					"\n\nRegex: (?<=[{}:,\\\\[\\\\]\\\\])\\\\+\\\"|(?<!\\\\\\\")\\\\+\\\"(?=[{}:,\\\\[\\\\]\\\\]+)");

			long start3 = System.nanoTime();

			String res3 = pattern3.matcher(baseStringRes).replaceAll("\"");
			long end3 = System.nanoTime() - start3;
			System.out.println("\n\nAfter 3: " + end3);
			System.out.println("\nRes 3: " + res3);

			JsonObject parsedJson = JsonParser.parseString(baseStringRes).getAsJsonObject();
			Matcher matcher = pattern.matcher(parsedJson.toString());
			// Extract the "content" field as a string and unescape it

			// System.out.println("First parsed: " + parsedJson);
			// Check if any key's value is stringJson then parse
			long start4 = System.nanoTime();
			// if (matcher.find()) {
			// Set<Map.Entry<String, JsonElement>> responseMap = parsedJson.entrySet();
			// for (Map.Entry<String, JsonElement> entry : responseMap) {
			// // System.out.println("\ntoString(): " + entry.getValue().toString());
			// // System.out.println("\nasString(): " + entry.getValue().getAsString());
			// if (entry.getValue().toString().contains("\\\"")) {
			// entry.setValue(JsonParser.parseString(entry.getValue().getAsString()));
			// }
			// }
			// }
			if (parsedJson.get("content").toString().contains("\\\"")) {
				String contentString = parsedJson.get("content").getAsString();

				// Now parse the unescaped "content" string
				JsonObject contentJson = JsonParser.parseString(contentString).getAsJsonObject();

				parsedJson.add("content", contentJson);
			}
			long end4 = System.nanoTime() - (start4);
			System.out.println("\n\nAfter 4: " + end4);
			System.out.println("\nRes 4: " + parsedJson);

			// long start3 = System.nanoTime();
			// JsonObject parsedJson2 = JsonParser.parseString(baseStringRes).getAsJsonObject();

			// // Check if any key's value is stringJson then parse
			// Matcher matcher2 = pattern.matcher(baseStringRes);
			// if (matcher2.find()) {
			// Set<Map.Entry<String, JsonElement>> responseMap = parsedJson2.entrySet();
			// for (Map.Entry<String, JsonElement> entry : responseMap) {
			// String valueAsString = entry.getValue().getAsString();
			// if (entry.getValue().getAsString().contains("\":")) {
			// entry.setValue(
			// gson.fromJson(StringEscapeUtils.unescapeJson(valueAsString), JsonObject.class)

			// );
			// }
			// }
			// }
			// long d3 = System.nanoTime() - (start3);

			// long start4 = System.nanoTime();
			// stringRes2 = JsonStringParse(stringRes2);
			// JsonObject jsonObject4 = gson.fromJson(stringRes, JsonObject.class);
			// long d4 = System.nanoTime() - (start4);

			// System.out.println("\n\nAfter: " + parsedJson);

		}
		catch (Exception e) {
			System.err.println("Error in main " + e.getMessage());
		}

	}

	public static String JsonStringParse(String jsonString) {
		StringBuilder result = new StringBuilder(jsonString.length());

		for (int i = 0; i < jsonString.length(); i++) {
			char currentChar = jsonString.charAt(i);

			// Detect the start of an escaped sequence
			if (currentChar == '\"' && i + 1 < jsonString.length()) {
				if (jsonString.charAt(i + 1) == '{') {
					// Skip the backslash and just append '{'
					result.append('{');
					i++; // Skip the next character as it's part of the escaped sequence
				} else if (jsonString.charAt(i + 1) == '}') {
					// Skip the backslash and just append '}'
					result.append('}');
					i++; // Skip the next character as it's part of the escaped sequence
				} else {
					// For any other character, add both current and next char
					result.append(currentChar);
				}
			} else {
				// Append the character as is
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String removeHtmlTags(String input) {
		StringBuilder result = new StringBuilder();
		boolean insideTag = false;

		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);

			if (currentChar == '\\' && input.charAt(i + 1) == '<') {
				insideTag = true;
			} else if (currentChar == '>') {
				insideTag = false;
				continue; // Skip the closing '>'
			}

			if (!insideTag) {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String readFileToString(String fileName) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}
}
