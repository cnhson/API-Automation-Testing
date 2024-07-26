package entities;

public class IndependentEntity {

    private String fetchMode;
    private String requestURL;
    private String payload;
    private String successResponse;
    private String failureResponse;
    private String authenRequire;

    public IndependentEntity(String fetchMode, String requestURL, String payload, String successResponse,
            String failureResponse, String authenRequire) {
        this.fetchMode = fetchMode;
        this.requestURL = requestURL;
        this.payload = payload;
        this.successResponse = successResponse;
        this.failureResponse = failureResponse;
        this.authenRequire = authenRequire;
    }

    public String getFetchMode() {
        return fetchMode;
    }

    public void setFetchMode(String fetchMode) {
        this.fetchMode = fetchMode;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public String getPayload() {
        return payload;
    }

    public String getSuccessResponse() {
        return successResponse;
    }

    public String getFailureResponse() {
        return failureResponse;
    }

    public String getAuthenRequire() {
        return authenRequire;
    }
}
