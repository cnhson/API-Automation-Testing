package api.test.main.workers;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import api.test.entities.AccountEntity;
import api.test.entities.IndependentEntity;
import api.test.utilities.IndependentValUtil;
import api.test.utilities.TimeEslapseUtil;

@Listeners()
public class IndependentTestWorker extends IndependentValUtil {

	private JsonArray jsonArray = new JsonArray();
	private String fetchMode;
	private IndependentEntity iEntity;
	private AccountEntity accountEntity;
	private String errorMessage;
	private Boolean failCheck = false;
	private String timeEslapsed;
	private ITestResult report;

	public IndependentTestWorker(IndependentEntity iEntity, AccountEntity aEntity, Object[] group,
			String fetchMode) {
		this.fetchMode = fetchMode;
		this.iEntity = iEntity;
		this.accountEntity = aEntity;
	}

	@Test()
	public void positiveTest() {
		this.report = Reporter.getCurrentTestResult();
		Long functionTime = TimeEslapseUtil.getTime(() -> {
			JsonObject urlAndResult = new JsonObject();
			super.setAccountEntity(accountEntity);
			super.setIndependentEntity(iEntity);
			super.sendRequest();
			urlAndResult.addProperty("path", iEntity.getRequestURL());

			try {
				super.expectPositiveResult();
				super.captureAllAssert();
				urlAndResult.addProperty("result", "success");
				urlAndResult.addProperty("apiTime", super.getApiTimeEslapsed());
			}
			catch (AssertionError e) {
				urlAndResult.addProperty("result", "fail");
				urlAndResult.addProperty("apiTime", super.getApiTimeEslapsed());
				this.failCheck = true;
				this.errorMessage = e.getMessage();
			}
			jsonArray.add(urlAndResult);
		});

		this.timeEslapsed = String.valueOf(functionTime / 1000000);
		if (this.failCheck) {
			Throwable throwable = new Throwable(errorMessage);
			throwable.setStackTrace(new StackTraceElement[0]);
			report.setThrowable(throwable);
			report.setStatus(ITestResult.FAILURE);
		}
	}

	@AfterMethod
	public void afterTest(ITestContext context) {
		report.setTestName("Test_" + this.iEntity.getId());
		report.setAttribute("timeEslapsed", this.timeEslapsed);
		report.setAttribute("description", "");
		context.setAttribute("fetchModeChoosen", this.fetchMode);
		context.setAttribute("testType", "Chaining");
		this.report.setAttribute("resultList", new Gson().toJson(this.jsonArray));
	}
}
