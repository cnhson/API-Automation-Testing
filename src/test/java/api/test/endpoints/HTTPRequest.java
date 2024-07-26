package api.test.endpoints;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import api.utilities.SessionCookieUtil;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HTTPRequest {

    Routes routes = new Routes();
    private Integer timeOut = 5000;
    private String requestUrl;
    private String payload;
    private Map<String, String> headerConfig = new HashMap<>();

    public HTTPRequest(String requestUrl, String payload) {
        try {
            this.requestUrl = requestUrl;
            this.payload = payload;
            setHeaderConfig();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void setHeaderConfig() {
        headerConfig.put("origin", routes.getFullDomain());
        headerConfig.put("accept-language", "en-US,en;q=0.5");
    }

    public Response post() {
        return given()
                .contentType(ContentType.JSON)
                .config(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.socket.timeout", timeOut)
                                .setParam("http.connection.timeout", timeOut)))
                .headers(headerConfig)
                .accept(ContentType.JSON)
                .body(payload)
                .when()
                .post(routes.getFullDomain() + this.requestUrl);

    }

    public Response postWithSession() {
        return given()
                .contentType(ContentType.JSON)
                .config(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.socket.timeout", timeOut)
                                .setParam("http.connection.timeout", timeOut)))
                .headers(headerConfig)
                .accept(ContentType.JSON)
                .cookie(SessionCookieUtil.getFromFile())
                .body(payload)
                .when()
                .post(routes.getFullDomain() + this.requestUrl);

    }
}