package api.utilities;

import java.util.Set;
import java.util.stream.Collectors;

// import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import io.restassured.response.Response;

public class ValidationUtil {

    private Response response;
    private String requestUrl;
    private SoftAssert softAssert = new SoftAssert();

    public ValidationUtil(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setReponse(Response response) {
        this.response = response;
    }

    public void expectStatusCode(Integer expectedStatusCode) {
        softAssert.assertEquals(Integer.valueOf(this.response.getStatusCode()), expectedStatusCode,
                "Expected [" + expectedStatusCode + "] but got [" + this.response.getStatusCode() + "] in "
                        + this.requestUrl);
    }

    public void expectEquals(String actualValue, String expectedValue) {
        softAssert.assertEquals(actualValue, expectedValue);
    }

    public void expectEquals(Set<String> actualValue, Set<String> expectedValue) {
        Set<String> missingSet = actualValue.stream().filter(e -> !expectedValue.contains(e))
                .collect(Collectors.toSet());
        softAssert.assertEquals(actualValue, expectedValue, "Expected: " + expectedValue.toString() + "\n"
                + " ".repeat(Math.max(0, 24)) + "Actual: "
                + actualValue.toString() + "\n" + " ".repeat(Math.max(0, 24)) + "Missing: " + missingSet.toString());
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
}
