package api.test.entities;

public class IndependentEntity {

    private String id;
    private String requestURL;
    private String payload;
    private String successResponse;
    private String failureResponse;
    private String authenRequire;

    public IndependentEntity(String id, String requestURL, String payload, String successResponse, String failureResponse,
            String authenRequire) {
        this.id = id;
        this.requestURL = requestURL;
        this.payload = payload;
        this.successResponse = successResponse;
        this.failureResponse = failureResponse;
        this.authenRequire = authenRequire;
    }

    public String getId() {
        return id;
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
