package com.shinobi.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;

import static io.restassured.RestAssured.given;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class Main {

    private static Integer timeOut = 10000;
    private static Map<String, String> headerConfig = new HashMap<>();
    static private File sessionCookieFile = new File("src/test/resources/private/sessionCookie.txt");

    public static void setHeaderConfig() {
        headerConfig.put("Origin", "https://stalkuat.hanzo.finance");
        headerConfig.put("Accept-language", "en-US,en;q=0.5");
        headerConfig.put("Content-type", "application/json");
        headerConfig.put("Cookie", getFromFile());
    }

    public static Response postWithSession(String path, String payload) {
        return given()
                .config(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.socket.timeout", timeOut)
                                .setParam("http.connection.timeout", timeOut)))
                .headers(headerConfig)
                .accept(ContentType.ANY)
                .body(payload)
                .when()
                .post(path).peek();
    }

    public static String getFromFile() {
        try {
            String data = FileUtils.readFileToString(sessionCookieFile, "UTF-8").trim();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // Character CHAR = (char) 65;
        // System.out.println("\nHere: " + CHAR);
        setHeaderConfig();
        System.out.println("\nRunning\n");
        Response res = postWithSession("https://stalkuat.hanzo.finance/authenapi/userapi/getLoggedInfo", "");
        res.then().extract().asString();
        System.out.println("\n");
        // Response res2 = postLogin();
        // res2.then().log().body().log();
    }

}
