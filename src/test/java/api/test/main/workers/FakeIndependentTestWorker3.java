package api.test.main.workers;

import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import api.test.entities.FakeChainEntity;

// @Listeners({api.test.lib.CustomMethodInterceptor.class})
public class FakeIndependentTestWorker3 {

	private String fetchMode;
	private List<FakeChainEntity> fakeEntity;
	private String testId;
	private Integer instanceId;
	private SoftAssert softAssert = new SoftAssert();
	private String errorMessage;
	// private LinkedHashMap<String, String> urlAndResult = new LinkedHashMap<>();
	private JsonArray jsonArray = new JsonArray();
	private ITestResult report;
	private Boolean failCheck = false;
	private String description;

	// create me json having keys [url, status]

	public FakeIndependentTestWorker3(List<FakeChainEntity> fEntity, Object[] groupId) {
		this.fakeEntity = fEntity;
		this.instanceId = hashCode();
		this.testId = groupId[0].toString();
		this.description = groupId[1].toString();
		System.out.println("Information: " + groupId[1]);

		// System.out.println("Instance: " + this.toString() + ", groupId: " + groupId);
	}

	public String getTestId() {
		return this.testId;

	}

	// @BeforeMethod()
	// public void setUp() {
	// System.out.println("Thread after " + this.instanceId + ": " + Thread.currentThread().getId());
	// }

	@Test()
	public void positiveTest() {
		// System.out.println("Thread test1:" + this.instanceId + ": " + Thread.currentThread().getId());
		// ITestResult report = Reporter.getCurrentTestResult();
		this.report = Reporter.getCurrentTestResult();
		ITestContext context = report.getTestContext();
		for (FakeChainEntity fEntity : this.fakeEntity) {
			// super.getTimeEslapsed());

			JsonObject urlAndResult = new JsonObject();
			urlAndResult.addProperty("path", fEntity.getRequestURL());
			if (!this.failCheck) {
				try {
					if (fEntity.getRequestURL().contains("9")) {
						softAssert.assertNotNull(null);
						softAssert.assertAll();
					} else {
						softAssert.assertNotNull(fEntity.getRequestURL());
						softAssert.assertAll();
					}
					// urlAndResult.put(fEntity.getRequestURL(), "success");
					urlAndResult.addProperty("result", "success");
					urlAndResult.addProperty("apiTime", "0.01");
				}
				catch (AssertionError e) {
					urlAndResult.addProperty("result", "fail");
					urlAndResult.addProperty("apiTime", "0.05");
					this.failCheck = true;
					this.errorMessage = e.getMessage();
				}
			} else {
				urlAndResult.addProperty("result", "skip");
				urlAndResult.addProperty("apiTime", "0");
			}

			jsonArray.add(urlAndResult);
		}
		if (this.failCheck) {
			Throwable throwable = new Throwable(errorMessage);
			throwable.setStackTrace(new StackTraceElement[0]);
			report.setThrowable(throwable);
			report.setStatus(ITestResult.FAILURE);

		}
	}

	@AfterMethod
	public void exportToAttribute(ITestContext context) {
		Gson gson = new Gson();
		// System.out.println(gson.toJson(this.jsonArray));
		report.setTestName("FakeTest_Group_" + this.testId);
		report.setAttribute("description", this.description);
		// report.getMethod().setDescription("View,");
		context.setAttribute("fetchModeChoosen", "Online");
		context.setAttribute("testType", "Chaining");
		// ITestResult report = Reporter.getCurrentTestResult();
		this.report.setAttribute("resultList", gson.toJson(this.jsonArray));
	}
}
