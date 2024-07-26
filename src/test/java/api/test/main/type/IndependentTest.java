package api.test.main.type;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import api.utilities.IndependentValUtil;
import entities.AccountEntity;
import entities.IndependentEntity;

public class IndependentTest extends IndependentValUtil {

    private String fetchMode;
    private String requestUrl;

    public IndependentTest(IndependentEntity iEntity, AccountEntity aEntity) {
        super(iEntity.getPayload(), iEntity.getRequestURL(), iEntity.getAuthenRequire(), iEntity.getSuccessResponse(),
                aEntity);
        this.fetchMode = iEntity.getFetchMode();
        this.requestUrl = iEntity.getRequestURL();
    }

    @BeforeClass
    public void setUp() {
        super.sendRequest();
    }

    @DataProvider(name = "API_INDEPENDENT")
    @Test(groups = "API_INDEPENDENT", priority = 1)
    public void positiveTest() {
        super.expectPositiveResult();
        ITestResult report = Reporter.getCurrentTestResult();
        ITestContext context = report.getTestContext();
        context.setAttribute("fetchModeChoosen", fetchMode);
        report.getMethod().setDescription(this.requestUrl + "," + super.getTimeEslapsed());
        super.captureAllAssert();
    }

}
