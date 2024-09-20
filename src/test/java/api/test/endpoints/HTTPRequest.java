package api.test.endpoints;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import api.test.utilities.SessionCookieUtil;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HTTPRequest {

	Routes routes = new Routes();
	private Integer timeOut = 4000;
	private String requestUrl;
	private String payload;
	private Map<String, String> headersConfig = new HashMap<>();

	public HTTPRequest(String requestUrl, String payload) {
		try {
			this.requestUrl = requestUrl;
			this.payload = payload;
			setDefaultHeadersConfig();
		}
		catch (Exception e) {
			System.err.println("Error while creating HTTP request: " + e.getMessage());
		}
	}

	public void setDefaultHeadersConfig() {
		this.headersConfig.put("Origin", routes.getFullDomain());
		this.headersConfig.put("Accept-language", "en-US,en;q=0.5");
		this.headersConfig.put("Content-type", "application/json");
	}

	public Response post() {
		this.headersConfig.put("Cookie", "");
		return given()
				.config(RestAssuredConfig.config()
						.httpClient(HttpClientConfig.httpClientConfig()
								.setParam("http.socket.timeout", this.timeOut)
								.setParam("http.connection.timeout", this.timeOut)))
				.headers(this.headersConfig).accept(ContentType.ANY).body(this.payload).when()
				.post(routes.getFullDomain() + this.requestUrl);

	}

	public Response postWithSession() {
		this.headersConfig.put("Cookie", SessionCookieUtil.getFromFile());
		return given()
				.config(RestAssuredConfig.config()
						.httpClient(HttpClientConfig.httpClientConfig()
								.setParam("http.socket.timeout", this.timeOut)
								.setParam("http.connection.timeout", this.timeOut)))
				.headers(this.headersConfig).accept(ContentType.ANY).body(this.payload).when()
				.post(routes.getFullDomain() + this.requestUrl);
	}
}