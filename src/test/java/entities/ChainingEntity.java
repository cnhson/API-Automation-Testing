package entities;

import java.util.HashSet;

import com.google.gson.JsonObject;

public class ChainingEntity {

    private String fetchMode;
    private String requestURL;
    private String payload;
    private String authenRequire;
    private Boolean isFinalStep;
    private HashSet<String> verifyVarList;
    private HashSet<String> variableList;
    private JsonObject jsonVariableAndValue;

    public ChainingEntity(String fetchMode, String requestURL, Boolean isFinalStep, String payload,
            String authenRequire, HashSet<String> variableList,
            HashSet<String> verifyVarList, JsonObject jsonVariableAndValue) {
        this.fetchMode = fetchMode;
        this.requestURL = requestURL;
        this.isFinalStep = isFinalStep;
        this.payload = payload;
        this.authenRequire = authenRequire;
        this.variableList = variableList;
        this.verifyVarList = verifyVarList;
        this.jsonVariableAndValue = jsonVariableAndValue;
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

    public JsonObject getJsonVariableAndValue() {
        return jsonVariableAndValue;
    }

}
