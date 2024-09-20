package api.test.main.workers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import api.test.entities.AccountEntity;
import api.test.entities.ChainingEntity;
import api.test.utilities.ChainingValUtil;
import api.test.utilities.TimeEslapseUtil;

public class ChainingTestWorker extends ChainingValUtil {

	private JsonObject jsonVariableAndValue = new JsonObject();
	private JsonArray jsonArray = new JsonArray();
	private String fetchMode;
	private List<ChainingEntity> cEntityList;
	private AccountEntity aEntity;
	private String testId;
	// private Integer instanceId;
	private String errorMessage;
	private ITestResult report;
	private Boolean failCheck = false;
	private String timeEslapsed;
	private String description;

	public ChainingTestWorker(List<ChainingEntity> cEntityList, AccountEntity aEntity, Object[] group,
			String fetchMode) {

		this.fetchMode = fetchMode;
		this.cEntityList = cEntityList;
		this.aEntity = aEntity;
		this.testId = String.valueOf(group[0]);
		this.description = String.valueOf(group[1]);
	}

	@Test()
	public void positiveTest() {
		this.report = Reporter.getCurrentTestResult();
		Long startTime = System.nanoTime();
		Instant start = Instant.now();
		for (ChainingEntity cEntity : this.cEntityList) {
			JsonObject urlAndResult = new JsonObject();

			if (!this.failCheck) {
				super.setChainingEntity(cEntity);
				super.setAccountEntity(this.aEntity);
				if (super.checkIfVariableSlotInParam()) {

					super.insertVariableSlotValue(this.jsonVariableAndValue);
				}
				super.sendRequest();

				urlAndResult.addProperty("path", cEntity.getRequestURL());

				Long start1 = TimeEslapseUtil.getTime(() -> {
					try {
						super.expectSuccessResult(cEntity.getIsFinalStep(), cEntity.getVerifyVarList(),
								cEntity.getVariableList(), this.jsonVariableAndValue);
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
				});
				System.out.println("\nMiliTime [" + testId + "]: " + (start1));

			} else {
				urlAndResult.addProperty("result", "skip");
				urlAndResult.addProperty("apiTime", "0");

			}
			jsonArray.add(urlAndResult);
		}
		Instant finish = Instant.now();
		System.out.println("\nNanoTime [" + testId + "]: " + (System.nanoTime() - startTime));
		this.timeEslapsed = String.valueOf(Duration.between(start, finish).toMillis());
		if (this.failCheck) {
			Throwable throwable = new Throwable(errorMessage);
			throwable.setStackTrace(new StackTraceElement[0]);
			report.setThrowable(throwable);
			report.setStatus(ITestResult.FAILURE);
		}

	}

	@AfterMethod
	public void afterTest(ITestContext context) {
		Gson gson = new Gson();
		report.setTestName("Group_" + this.testId);
		report.setAttribute("timeEslapsed", this.timeEslapsed);
		report.setAttribute("description", this.description);
		context.setAttribute("fetchModeChoosen", this.fetchMode);
		context.setAttribute("testType", "Chaining");
		this.report.setAttribute("resultList", gson.toJson(this.jsonArray));
	}
}
