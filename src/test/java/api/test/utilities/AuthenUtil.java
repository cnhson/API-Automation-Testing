package api.test.utilities;

import com.google.gson.JsonObject;

import api.test.endpoints.HTTPRequest;
import io.restassured.http.Cookies;
import io.restassured.response.Response;

public class AuthenUtil {

	private HashUtil hu = new HashUtil();
	private PropertyUtil pu = new PropertyUtil();
	private String loginCheckUrl = pu.getPropAsString("LOGIN_CHECK");
	private static boolean isAuthorized = false;

	public AuthenUtil() {
	}

	public static boolean getSessionStatus() {
		return isAuthorized;
	}

	public static Boolean isSessionCookieExist() {
		try {
			if (!SessionCookieUtil.getFromFile().isBlank() && !SessionCookieUtil.getFromFile().isEmpty()) {
				return true;
			} else
				return false;
		}
		catch (Exception e) {
			System.err.println("[AuthenUtil] Error while checking session cookie: " + e.getMessage());
			return false;
		}
	}

	// Run before suite test
	public void sessionValidPreCheck() {
		try {
			if (!isSessionCookieExist()) {
				System.out.println("› Stored session cookie not exist");
			}
			HTTPRequest request = new HTTPRequest(this.loginCheckUrl, "");
			Response res = request.postWithSession();
			String resBody = res.then().extract().asString();
			if (!resBody.isBlank() && resBody.contains("success")) {
				System.out.println("› Already logged in");
				isAuthorized = true;
			}
		}
		catch (

		Exception e) {
			System.err.println("[AuthenUtil] Error while checking login status: " + e.getMessage());
		}
	}

	public void isLoggedIn(String username, String password) {
		if (!getSessionStatus()) {
			synchronized (this) {
				this.authorize("SYSTEM", username, password);
			}
		}

	}

	public synchronized void authorize(String pathRole, String username, String password) {
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
			if (!resBody.isBlank() && resBody.contains("success")) {
				Cookies resCookie = res.then().extract().detailedCookies();
				SessionCookieUtil.storeToFile(resCookie.toString());
				System.out.println("› Login successful with username: " + username);
			} else {
				System.out.println("› Wrong password or username");
			}

		}
		catch (

		Exception e) {
			System.err.println("[AuthenUtil] Error while trying to authorize: " + e.getMessage());
		}
	}
}
