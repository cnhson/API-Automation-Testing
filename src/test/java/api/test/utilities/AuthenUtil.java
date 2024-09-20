package api.test.utilities;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import api.test.endpoints.HTTPRequest;
import io.restassured.http.Cookies;
import io.restassured.response.Response;

public class AuthenUtil {

	HashUtil hu = new HashUtil();
	SessionCookieUtil cu = new SessionCookieUtil();
	PropertyUtil pu = new PropertyUtil();
	String loginCheckUrl = pu.getPropAsString("LOGIN_CHECK");

	public AuthenUtil() {

	}

	public Boolean isSessionCookieExist() {
		try {
			if (!SessionCookieUtil.getFromFile().isBlank() && !SessionCookieUtil.getFromFile().isEmpty()) {
				return true;
			} else
				return false;
		}
		catch (Exception e) {
			System.err.println("Error while checking session cookie: " + e.getMessage());
			return false;
		}
	}

	public Boolean isLoggedIn() {
		try {
			Boolean isLogin = false;
			if (!isSessionCookieExist()) {
				// System.out.println("› Stored session cookie not exist");
				return isLogin;
			} else {
				HTTPRequest request = new HTTPRequest(this.loginCheckUrl, "");
				Response res = request.postWithSession();
				String resBody = res.then().extract().asString();

				if (!resBody.isBlank()) {
					if (resBody.contains("success")) {
						// System.out.println("› Already logged in");
						isLogin = true;
					} else
						System.out.println("› Not logged in yet");

				}
				return isLogin;
			}
		}
		catch (Exception e) {
			System.err.println("Error while checking login status: " + e.getMessage());
			return false;
		}
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
		}
		catch (Exception e) {
			System.err.println("Error while trying to authorize: " + e.getMessage());
		}
	}
}
