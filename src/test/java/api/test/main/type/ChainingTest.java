package api.test.main.type;

import java.util.HashSet;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import api.utilities.ChainingValUtil;
import entities.AccountEntity;
import entities.ChainingEntity;

public class ChainingTest extends ChainingValUtil {

    private String fetchMode;
    private String requestUrl;
    private Boolean isFinalStep;
    private HashSet<String> verifyVarList;
    private HashSet<String> variableList;
    private JsonObject jsonVariableAndValue;

    public ChainingTest(ChainingEntity cEntity, AccountEntity aEntity) {
        super(cEntity.getPayload(), cEntity.getRequestURL(), cEntity.getAuthenRequire(), aEntity);
        this.fetchMode = cEntity.getFetchMode();
        this.requestUrl = cEntity.getRequestURL();
        this.isFinalStep = cEntity.getIsFinalStep();
        this.verifyVarList = cEntity.getVerifyVarList();
        this.variableList = cEntity.getVariableList();
        this.jsonVariableAndValue = cEntity.getJsonVariableAndValue();
    }

    @BeforeClass
    public void setUp() {
        if (super.checkIfVariableSlotInParam()) {
            super.insertVariableSlotValue(this.jsonVariableAndValue);
        }
        super.sendRequest();
    }

    @DataProvider(name = "API_CHAINING")
    @Test(groups = "API_CHAINING", priority = 2)
    public void positiveTest() {
        super.expectSuccessResult(this.isFinalStep, this.verifyVarList, this.variableList, this.jsonVariableAndValue);
        ITestResult report = Reporter.getCurrentTestResult();
        ITestContext context = report.getTestContext();
        context.setAttribute("fetchModeChoosen", fetchMode);
        report.getMethod().setDescription(this.requestUrl + "," + super.getTimeEslapsed());
        System.out.println("Positive test passed: " + jsonVariableAndValue);
        super.captureAllAssert();
    }

}
