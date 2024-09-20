package api.test.entities;

import java.util.HashSet;

import com.google.gson.JsonObject;

public class FakeChainEntity {

    private String id;
    private String requestURL;
    private String payload;
    private String authenRequire;
    private Boolean isFinalStep;
    private HashSet<String> verifyVarList;
    private HashSet<String> variableList;

    public FakeChainEntity(String id, String requestURL, Boolean isFinalStep, String payload, String authenRequire,
            HashSet<String> variableList, HashSet<String> verifyVarList) {
        this.id = id;
        this.requestURL = requestURL;
        this.isFinalStep = isFinalStep;
        this.payload = payload;
        this.authenRequire = authenRequire;
        this.variableList = variableList;
        this.verifyVarList = verifyVarList;
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

    public Boolean getIsFinalStep() {
        return isFinalStep;
    }

    public String getAuthenRequire() {
        return authenRequire;
    }

    public HashSet<String> getVerifyVarList() {
        return verifyVarList;
    }

    public HashSet<String> getVariableList() {
        return variableList;
    }

}
