package api.test.main.workers;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import api.test.entities.AccountEntity;
import api.test.entities.IndependentEntity;
import api.test.utilities.IndependentValUtil;

// @Listeners({api.test.lib.CustomMethodInterceptor.class})
public class FakeIndependentTestWorker extends IndependentValUtil {

	private String fetchMode;
	private IndependentEntity independentEntity;
	private AccountEntity accountEntity;
	private String testId;

	public FakeIndependentTestWorker(IndependentEntity iEntity, AccountEntity aEntity, Object[] group,
			String fetchMode) {
		this.fetchMode = fetchMode;
		this.independentEntity = iEntity;
		this.accountEntity = aEntity;
		this.testId = iEntity.getId();
	}

	public String getTestId() {
		return this.testId;
	}

	@BeforeMethod
	public void setUp(ITestContext context) {
		context.setAttribute("testId", this.testId);
	}

	@Test()

	public void positiveTest() {
		ITestResult report = Reporter.getCurrentTestResult();
		ITestContext context = report.getTestContext();
		report.setTestName("FakeTest_" + getTestId());
		context.setAttribute("fetchModeChoosen", fetchMode);
		report.getMethod()
				.setDescription(independentEntity.getRequestURL() + "," + super.getApiTimeEslapsed());
		super.captureAllAssert();
	}

}
