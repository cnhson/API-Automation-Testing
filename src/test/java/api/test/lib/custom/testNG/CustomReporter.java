package api.test.lib.custom.testNG;

import org.apache.commons.io.IOUtils;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;

import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestRunner;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.reporters.HtmlHelper;
import org.testng.reporters.TestHTMLReporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.test.entities.SuiteDataDBEntity;
import api.test.entities.SuiteInfoDBEntity;
import api.test.utilities.DatabaseUtil;

public class CustomReporter extends TestListenerAdapter implements IReporter {

	private void collectResultsByClass(Map<String, List<ITestResult>> resultsByClass,
			Collection<ITestResult> results) {
		for (ITestResult result : results) {
			String className = result.getTestClass().getName();
			// System.out.println("\nclassName: " + result.getTestClass().getName());
			// System.out.println("className1: " + result.getName());
			// System.out.println("here " + result.getAttribute("resultList"));
			if (!resultsByClass.containsKey(className)) {
				resultsByClass.put(className, new ArrayList<>());
			}
			resultsByClass.get(className).add(result);
		}
	}

	private static String[] getStatusDetail(int status) {
		switch (status) {
		case ITestResult.SUCCESS:
			return new String[] {"SUCCESS", "GREEN", "🟢", "#5eff5e61"};
		case ITestResult.FAILURE:
			return new String[] {"FAILURE", "RED", "🔴", "#ff383861"};
		case ITestResult.SKIP:
			return new String[] {"SKIP", "YELLOW", "🟡", "#faff3861"};
		default:
			return new String[] {"UNKNOWN", "BLACK", "⚫", "#68686861"};
		}
	}

	private static String getChainGroupStatus(String result) {
		switch (result) {
		case "success":
			return "🟩";
		case "fail":
			return "🟥";
		case "skip":
			return "🟨";
		default:
			return "";
		}
	}

	private static final Comparator<ITestResult> NAME_COMPARATOR = new NameComparator();
	private static final Comparator<ITestResult> INDEX_COMPARATOR = new IndexComparator();

	// private static final Comparator<ITestResult> CONFIGURATION_COMPARATOR = new
	// ConfigurationComparator();

	private ITestContext m_testContext = null;

	//
	// implements ITestListener
	//
	@Override
	public void onStart(ITestContext context) {
		DatabaseUtil.getInstance();
		m_testContext = context;
		System.out.println("[CustomReporter] Start generating report...");
	}

	@Override
	public void onFinish(ITestContext context) {

		Map<String, List<ITestResult>> resultsByClass = new LinkedHashMap<>();

		// Collect results from all test groups
		// collectResultsByClass(resultsByClass, context.getFailedConfigurations().getAllResults());
		// collectResultsByClass(resultsByClass, context.getSkippedConfigurations().getAllResults());

		collectResultsByClass(resultsByClass, context.getPassedTests().getAllResults());
		collectResultsByClass(resultsByClass, context.getFailedTests().getAllResults());
		collectResultsByClass(resultsByClass, context.getSkippedTests().getAllResults());

		// collectResultsByClass(resultsByClass,
		// context.getFailedButWithinSuccessPercentageTests().getAllResults());
		// for (Map.Entry<String, List<ITestResult>> entry : resultsByClass.entrySet()) {
		// System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
		// }

		generateLog(m_testContext, null /* host */, m_testContext.getOutputDirectory(), resultsByClass);
		DatabaseUtil.getInstance().closeConnection();
	}
	//
	// implements ITestListener
	//

	private static String getOutputFile(ITestContext context) {
		return context.getName() + ".html";
	}

	public static void generateTable(PrintWriter pw, String title, Collection<ITestResult> tests,
			String cssClass, Comparator<ITestResult> comparator, Integer suiteId) {

		String[] parts = title.split("\\.");
		String lastElement = parts[parts.length - 1];
		String tableName = lastElement + "-" + cssClass;

		// api.test.main.IndependentTest
		// => IndependentTest
		pw.append("<table width='100%' border='0' style=\"font-size: 12px; margin-bottom: 50px;\r\n" + //
				"font-weight: 600; border-collapse: collapse;\" class='").append(lastElement + "-" + cssClass)
				.append("'>\n").append("<tbody style=\"background: white;\"")
				.append("<tr><td colspan='7' align='center' class=\"table-header\"> <div style=\"display: flex; align-items: center;\"> ")
				.append("<div class=\"filter-section\" style=\"margin:10px 0 10px 25px;\">")
				.append("<b>Filter</b>")
				.append("<button class=\"failed-filter-button\" onClick=\"tableFilter(\'" + tableName
						+ "\',\'fail\')\"><b>FAILED</b></button>")
				.append("<button class=\"no-filter-button\"     onClick=\"tableFilter(\'" + tableName
						+ "\',\'default\')\"><b>DEFAULT</b></button>")
				.append("</div>")
				.append("<div style=\"z-index: 2; position: absolute; width: fit-content; margin: 0 auto; font-size: 15px; left: 0; right: 0; \"><b>"
						+ lastElement + "</b></div>")
				.append("</div></td></tr>\n").append("<tr style=\"border-bottom: 1px solid #ababab;\">")
				.append("<td width=\"4%\"></td>")
				.append("<td width=\"8%\" style=\"padding: 10px 0 5px 0px;\"><b>ID</b></td>\n")
				.append("<td width=\"30%\" style=\"padding: 10px 0 5px 0px;\"><b>Description</b></td>\n")

				.append("<td width=\"10%\" style=\"padding: 10px 0 5px 0px;\"><b>Method</b></td>\n")

				.append("<td width=\"auto\" style=\"padding: 10px 0 5px 0px;\"><b>API Path | Response Time</b></td>\n")
				.append("<td width=\"10%\" style=\"padding: 10px 0 5px 0px;\"><b>Exception</b></td>\n")
				.append("<td width=\"5%\" style=\"padding: 10px 0 5px 0px;\"><b>Time</b></td>\n")
				.append("</tr>\n");

		if (tests instanceof List) {
			((List<ITestResult>) tests).sort(comparator);
		}

		// User output?
		String id;
		Throwable tw;

		// Result in the front
		for (ITestResult tr : tests) {
			ITestNGMethod method = tr.getMethod();
			SuiteDataDBEntity sdbEntity = new SuiteDataDBEntity();
			List<SuiteDataDBEntity> sdbEntityList = new ArrayList<SuiteDataDBEntity>();
			Integer statusCode = tr.getStatus();
			String[] statusDetail = getStatusDetail(statusCode);
			String statusIcon = statusDetail[2];
			String statusString = statusDetail[0];
			String statusBackgroundColor = statusDetail[3];
			String testName = tr.getName();
			String description = "";
			String methodName = method.getMethodName();
			JsonArray jsonArrayResult = new JsonArray();
			String stackTrace = "";
			String timeEslapsed = "";

			// Getting data from ITestResult
			if (tr.getAttribute("timeEslapsed") != null) {
				timeEslapsed = (String) tr.getAttribute("timeEslapsed");
			}
			if (tr.getAttribute("description") != null) {
				description = (String) tr.getAttribute("description");
			}
			tw = tr.getThrowable();
			id = "stack-trace" + tr.hashCode();
			if (null != tw) {
				stackTrace = Utils.shortStackTrace(tw, true).split("at ")[0];
			}
			/***********************************************************/

			// @Row background color
			pw.append("<tr class=\"" + statusString
					+ "\" style=\"border-bottom: 1px solid #ababab; background: " + statusBackgroundColor
					+ "\">\n");

			// @Column 1
			// Status icon
			pw.append("<td style=\"padding:10px 0 10px 20px;\">").append(statusIcon).append("</td>");

			// @Column 2
			// Group name
			pw.append("<td style=\"padding:10px 0 10px 0px;\">").append(testName).append("</td>");

			// @Column 3
			// Description
			pw.append("<td style=\"padding: 10px 10px 10px 0px;\r\n" + //
					"    word-break: auto-phrase;\">").append(description).append("</td>");

			// @Column 4
			// Test method
			pw.append("<td>").append(methodName).append("</td>");

			// @Column 5
			// API path
			try {
				Boolean isGroupCheck = false;
				jsonArrayResult = JsonParser.parseString(tr.getAttribute("resultList").toString())
						.getAsJsonArray();
				pw.append("<td colspan='1' style=\"padding:10px 0 10px 0px;\">")
						.append("<ul id=\"myUL\"><li>");
				if (jsonArrayResult.size() > 1) {
					isGroupCheck = true;
					pw.append("<a class=\"caret\"> View </a>").append("<ul class=\"nested-api-group\">");
				}
				pw.append(
						" <table class=\"nested-api-table\" colspan='3'><tr><td width='1%'/><td width='auto'/><td width='16%'/></tr>");

				for (JsonElement jsonElement : jsonArrayResult) {
					JsonObject jsonObject = jsonElement.getAsJsonObject();
					String apiPathInGroup = jsonObject.get("path").toString().replace("\"", "");
					String apiResultInGroup = jsonObject.get("result").toString().replace("\"", "");
					String apiEslapsedInGroup = jsonObject.get("apiTime").toString().replace("\"", "");

					pw.append("<tr>");
					pw.append("<td style=\"position: relative\"> "
							+ (isGroupCheck ? "<div class=\"row-line\"/>" : "") + "</td>");
					pw.append("<td>" + (isGroupCheck ? getChainGroupStatus(apiResultInGroup) : ""));
					pw.append(" " + apiPathInGroup + "</td>");
					pw.append("<td style=\"text-align: right;\"> " + apiEslapsedInGroup + " ms</td></tr>");

					sdbEntity.setType(tableName);
					sdbEntity.setSuiteId(suiteId);
					sdbEntity.setTestName(testName);
					sdbEntity.setMethod(methodName);
					sdbEntity.setResultList(jsonObject.toString());
					sdbEntity.setStatus(statusString);
					sdbEntity.setTimeElapsed(timeEslapsed);
					sdbEntity.setException(stackTrace);
					sdbEntity.setDescription(description);
					sdbEntityList.add(sdbEntity);
				}

				pw.append("</table>");
				if (isGroupCheck)
					pw.append("</ul></li></ul>");
				pw.append("</td>");
			}
			catch (Exception e) {
				System.err.println("[CustomReporter] Error while getting result list: " + e.getMessage());
			}

			// Output from the method, created by the user calling Reporter.log()
			{
				List<String> output = Reporter.getOutput(tr);
				if (!output.isEmpty()) {
					pw.append("<br/>");
					// Method name
					String divId = "Output-" + tr.hashCode();
					pw.append("\n<a href=\"#").append(divId).append("\"").append(" onClick='toggleBox(\"")
							.append(divId).append("\", this, \"Show output\", \"Hide output\");'>")
							.append("Show output</a>\n").append("\n<a href=\"#").append(divId).append("\"")
							.append(" onClick=\"toggleAllBoxes();\">Show all outputs</a>\n");

					// Method output
					pw.append("<div class='log' id=\"").append(divId).append("\">\n");
					for (String s : output) {
						pw.append(s).append("<br/>\n");
					}
					pw.append("</div>\n");
				}
			}

			pw.append("</td>\n");

			// @Column 6
			// Exception
			pw.append("<td style=\"overflow-y: auto; max-width: 40rem; max-height: 10rem;\">");
			if (null != tw) {
				pw.append("<button class=\"border-button\" style=\"text-decoration: none;\r\n" + //
						"font-weight: 700;\" onClick=\"toggleBox(\'").append(id).append("\')\">")
						.append("Logs 👁‍🗨").append("</button>")
						.append("<div class='stack-trace-modal' id='").append(id).append("'>")
						.append("<div class=\"stack-trace-content\">")
						.append("<div class=\"stack-trace-header\">")
						.append("<span onClick=\"closeModalButton(\'").append(id)
						.append("\')\" class=\"close\">&times;</span>").append("</div>").append("<pre>")
						.append(stackTrace).append("</pre></div></div>");
			}

			pw.append("</td>\n");

			// @Column 7
			// Method description (timeElapsed) - "requestUrl,timeElapsed"
			pw.append("<td style=\"padding:10px 0 10px 0px;\">").append(timeEslapsed + " ms").append("</td>");

			// tableName, suiteId, testName, name, jsonArrayResult.toString(), statusString, timeEslapsed, stackTrace, description

			DatabaseUtil.getInstance().insertSuiteDataList(sdbEntityList);
		}
		pw.append("</tbody>\n").append("</table>\n");
	}

	// private static String arrayToString(String[] array) {
	// StringBuilder result = new StringBuilder();
	// for (String element : array) {
	// result.append(element).append(" ");
	// }
	// return result.toString();
	// }

	public static void generateLog(ITestContext testContext, String host, String outputDirectory,
			Map<String, List<ITestResult>> resultsByClass) {
		try (PrintWriter writer = new PrintWriter(
				Utils.openWriter(outputDirectory, getOutputFile(testContext)))) {
			String htmlFilePath = "src/test/java/api/test/lib/custom/testNG/html/";

			// Read headContent.html as string
			FileInputStream headContentfis = new FileInputStream(htmlFilePath + "headContent.html");
			FileInputStream bodyContentfis = new FileInputStream(htmlFilePath + "bodyContent.html");
			String headHtmlContent = IOUtils.toString(headContentfis, StandardCharsets.UTF_8);
			String bodyHtmlContent = IOUtils.toString(bodyContentfis, StandardCharsets.UTF_8);
			//
			writer.append("<html>\n<head>\n").append("<title>TestNG:  ").append(testContext.getName())
					.append("</title>\n")
					// .append(HtmlHelper.getCssString())
					.append(headHtmlContent).append("</head>\n").append("<body>\n");
			// 2022-12-31 23.59.59
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
			String fetchModeChoosen = String.valueOf(testContext.getAttribute("fetchModeChoosen"));
			// Map<String, String> resultList = testContext.getAttribute("resultList");

			Integer suiteId = (int) (long) (testContext.getStartDate().getTime() / 1000);
			String startDate = dateFormat.format(testContext.getStartDate());
			String endDate = dateFormat.format(testContext.getEndDate());
			long duration = (testContext.getEndDate().getTime() - testContext.getStartDate().getTime());
			int passed = testContext.getPassedTests().size()
					+ testContext.getFailedButWithinSuccessPercentageTests().size();
			int failed = testContext.getFailedTests().size();
			int skipped = testContext.getSkippedTests().size();
			int totalTestCases = passed + failed + skipped;
			String hostLine = Utils.isStringEmpty(host) ? ""
					: "<tr><td>Remote host:</td><td>" + host + "</td>\n</tr>";

			// Insert into SuiteInfo table
			SuiteInfoDBEntity siEntity = new SuiteInfoDBEntity();
			siEntity.setId(suiteId);
			siEntity.setMode(fetchModeChoosen);
			siEntity.setPassedCount(passed);
			siEntity.setSkippedCount(skipped);
			siEntity.setFailedCount(failed);
			siEntity.setStartDate(startDate);
			siEntity.setEndDate(endDate);
			siEntity.setTotalTime(String.valueOf(duration));
			siEntity.setSuiteName(testContext.getName());
			DatabaseUtil.getInstance().insertSuiteInfo(siEntity);
			writer.append("<h2 align='center' style=\"font-family: Bahnschrift;\r\n" + //
					"padding: 0.425rem 0px;\r\n" + //
					"background: rgb(0, 128, 96);\r\n" + //
					"color: white;\">")
					// .append(testContext.getName())
					.append("Report").append("</h2>")
					.append("<table border='0' align=\"center\" class=\"summary-table\">\n")
					.append("<tr><td class=\"row-head\">Mode</td><td class=\"row-content\">")
					.append(fetchModeChoosen).append("</td></tr>\n")
					.append("<tr><td class=\"row-head\">Total</td>").append("<td class=\"row-content\">")
					.append(Integer.toString(totalTestCases)).append("</td></tr>\n")
					.append("<tr><td class=\"row-head\">Passed/Failed/Skipped </td>")
					.append("<td class=\"row-content\">").append(Integer.toString(passed)).append("/")
					.append(Integer.toString(failed)).append("/").append(Integer.toString(skipped))
					.append("</td></tr>\n").append("<tr><td class=\"row-head\">Started on</td>")
					.append("<td class=\"row-content\">").append(startDate).append("</td></tr>\n")
					.append("<tr><td class=\"row-head\">Ended on</td>").append("<td class=\"row-content\">")
					.append(endDate).append("</td></tr>\n").append(hostLine)
					.append("<tr><td class=\"row-head\">Total time</td>").append("<td class=\"row-content\">")
					.append(Long.toString(duration / 1000)).append(" seconds (")
					.append(Long.toString(duration)).append(" ms)</td>\n").append("</table><p/>\n");
			for (Map.Entry<String, List<ITestResult>> entry : resultsByClass.entrySet()) {
				String className = entry.getKey();
				List<ITestResult> classResults = entry.getValue();
				generateTable(writer, className, classResults, "results", INDEX_COMPARATOR, suiteId);
			}
			writer.append(bodyHtmlContent);
			writer.append("</body>\n</html>");
		}
		catch (IOException e) {
			if (TestRunner.getVerbose() > 1) {
				Logger.getLogger(TestRunner.class).error(e.getMessage(), e);
			} else {
				log(e.getMessage());
			}
		}
	}

	private static void log(String s) {
		Logger.getLogger(TestHTMLReporter.class).info("[TestHTMLReporter] " + s);
	}

	private static class NameComparator implements Comparator<ITestResult> {
		@Override
		public int compare(ITestResult o1, ITestResult o2) {
			String c1 = o1.getMethod().getMethodName();
			String c2 = o2.getMethod().getMethodName();
			return c1.compareTo(c2);
		}
	}

	private static class IndexComparator implements Comparator<ITestResult> {
		@Override
		public int compare(ITestResult result1, ITestResult result2) {
			try {
				String name1 = result1.getName().split("_")[1];
				String name2 = result2.getName().split("_")[1];
				return name1.compareTo(name2);
			}
			catch (Exception e) {
				String c1 = result1.getMethod().getMethodName();
				String c2 = result2.getMethod().getMethodName();
				return c1.compareTo(c2);
			}
		}
	}
}
