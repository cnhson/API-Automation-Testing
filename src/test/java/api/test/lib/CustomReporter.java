package api.test.lib;

import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestRunner;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.reporters.HtmlHelper;
import org.testng.reporters.TestHTMLReporter;

public class CustomReporter extends TestListenerAdapter implements IReporter {

    private void collectResultsByClass(Map<String, List<ITestResult>> resultsByClass, Collection<ITestResult> results) {
        for (ITestResult result : results) {
            String className = result.getTestClass().getName();
            if (!resultsByClass.containsKey(className)) {
                resultsByClass.put(className, new ArrayList<>());
            }
            resultsByClass.get(className).add(result);
        }
    }

    private static String[] getStatusDetail(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return new String[] { "SUCCESS", "GREEN", "🟢", "#5eff5e61" };
            case ITestResult.FAILURE:
                return new String[] { "FAILURE", "RED", "🔴", "#ff383861" };
            case ITestResult.SKIP:
                return new String[] { "SKIP", "YELLOW", "🟡", "#faff3861" };
            default:
                return new String[] { "UNKNOWN", "BLACK", "⚫", "#68686861" };
        }
    }

    private static final Comparator<ITestResult> NAME_COMPARATOR = new NameComparator();
    private static final Comparator<ITestResult> CONFIGURATION_COMPARATOR = new ConfigurationComparator();

    private ITestContext m_testContext = null;

    /////
    // implements ITestListener
    //
    @Override
    public void onStart(ITestContext context) {
        m_testContext = context;
    }

    @Override
    public void onFinish(ITestContext context) {

        Map<String, List<ITestResult>> resultsByClass = new HashMap<>();

        // Collect results from all test groups
        collectResultsByClass(resultsByClass, context.getFailedConfigurations().getAllResults());
        collectResultsByClass(resultsByClass, context.getSkippedConfigurations().getAllResults());
        collectResultsByClass(resultsByClass, context.getPassedTests().getAllResults());
        collectResultsByClass(resultsByClass, context.getFailedTests().getAllResults());
        collectResultsByClass(resultsByClass, context.getSkippedTests().getAllResults());
        collectResultsByClass(resultsByClass, context.getFailedButWithinSuccessPercentageTests().getAllResults());
        generateLog(
                m_testContext,
                null /* host */,
                m_testContext.getOutputDirectory(),
                resultsByClass);
    }
    //
    // implements ITestListener
    /////

    private static String getOutputFile(ITestContext context) {
        // return context.getName() + ".html";
        return "SuiteReport.html";

    }

    public static void generateTable(
            PrintWriter pw,
            String title,
            Collection<ITestResult> tests,
            String cssClass,
            Comparator<ITestResult> comparator) {

        String[] parts = title.split("\\.");
        String lastElement = parts[parts.length - 1];
        // api.test.main.IndependentTest
        // => IndependentTest
        pw.append("<table width='100%' border='0' style=\"font-size: 12px;\r\n" + //
                "font-weight: 600; border-collapse: collapse;\" class='")
                .append(lastElement + "-" + cssClass)
                .append("'>\n")
                .append("<tbody style=\"background: white;\"")
                .append("<tr><td colspan='5' align='center' style=\"background: rgb(0, 128, 96);\r\n" + //
                        "color: white;\r\n" + //
                        "height: 2rem;\r\n" + //
                        "border-top-left-radius: 5px;\r\n" + //
                        "border-top-right-radius: 5px;\"> <div style=\"display: flex; align-items: center;\"> ")
                .append("<div class=\"filter-section\" style=\"margin:10px 0 10px 25px;\">")
                .append("<b>Filter</b>")
                .append("<button style=\"margin-left: 10px;border-radius: 5px;border: 0px solid; padding: 5px 15px;\""
                        + "class=\"failed-filter-button\" onClick=\"filterFailed()\"><b>FAILED</b></button>")
                .append("<button style=\"margin-left: 10px;border-radius: 5px;border: 0px solid; padding: 5px 15px;\""
                        + "class=\"no-filter-button\"     onClick=\"resetFilter()\"><b>DEFAULT</b></button>")
                .append("</div>")
                .append("<div style=\"z-index: 2; position: absolute; width: fit-content; margin: 0 auto; font-size: 15px; left: 0; right: 0; \"><b>"
                        + lastElement + "</b></div>")
                .append("</div></td></tr>\n")
                .append("<tr style=\"border-bottom: 1px solid #ababab;\">")
                .append("<td width=\"4%\"></td>")
                .append("<td width=\"10%\" style=\"padding: 10px 0 5px 10px;\"><b>Method</b></td>\n")
                .append("<td width=\"30%\" style=\"padding: 10px 0 5px 10px;\"><b>API Path</b></td>\n")
                .append("<td width=\"auto\" style=\"padding: 10px 0 5px 10px;\"><b>Exception</b></td>\n")
                .append("<td width=\"5%\" style=\"padding: 10px 0 5px 10px;\"><b>Time</b></td>\n")
                .append("</tr>\n");

        if (tests instanceof List) {
            ((List<ITestResult>) tests).sort(comparator);
        }

        // User output?
        String id;
        Throwable tw;

        // Result in the front

        for (ITestResult tr : tests) {
            Integer statusCode = tr.getStatus();
            String[] statusDetail = getStatusDetail(statusCode);
            String statusIcon = statusDetail[2];
            String statusString = statusDetail[0];
            String statusBackgroundColor = statusDetail[3];

            pw.append("<tr class=\"" + statusString + "\" style=\"border-bottom: 1px solid #ababab; background: "
                    + statusBackgroundColor + "\">\n");
            pw.append("<td style=\"padding:10px 0 10px 20px;\">").append(statusIcon).append("</td>");

            // Test method
            ITestNGMethod method = tr.getMethod();

            // String name = method.getMethodName();
            // pw.append("<td title='")
            // .append(tr.getTestClass().getName())
            // .append(".")
            // .append(name)
            // .append("()'>");

            // Test class
            String name = method.getMethodName();
            String testClass = tr.getTestClass().getName();
            if (testClass != null) {
                // String testName = tr.getTestName();
                // if (testName != null) {
                // testName = "";
                // }
                pw.append("<td>").append(name).append("</td>");

            }

            // Method description (requestUrl) - "requestUrl,timeElapsed"
            if (!Utils.isStringEmpty(method.getDescription())) {
                String requestUrl = method.getDescription().split(",")[0];
                pw.append("<td>")
                        .append(requestUrl).append("</td>");
            }

            Object[] parameters = tr.getParameters();
            if (parameters != null && parameters.length > 0) {
                pw.append("<br>Parameters: ");
                for (int j = 0; j < parameters.length; j++) {
                    if (j > 0) {
                        pw.append(", ");
                    }
                    if (parameters[j] == null) {
                        pw.append("null");
                    } else {
                        String parameterToString;
                        try {
                            parameterToString = parameters[j].toString();
                        } catch (RuntimeException | Error e) {
                            log(e.toString());
                            // failover in case parameter toString() cannot be evaluated
                            parameterToString = parameters[j].getClass().getName() + "@"
                                    + System.identityHashCode(parameters[j]);
                        }
                        pw.append(parameterToString);
                    }
                }
            }
            // Output from the method, created by the user calling Reporter.log()
            {
                List<String> output = Reporter.getOutput(tr);
                if (!output.isEmpty()) {
                    pw.append("<br/>");
                    // Method name
                    String divId = "Output-" + tr.hashCode();
                    pw.append("\n<a href=\"#")
                            .append(divId)
                            .append("\"")
                            .append(" onClick='toggleBox(\"")
                            .append(divId)
                            .append("\", this, \"Show output\", \"Hide output\");'>")
                            .append("Show output</a>\n")
                            .append("\n<a href=\"#")
                            .append(divId)
                            .append("\"")
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

            // Custom attributes
            CustomAttribute[] attributes = tr.getMethod().getAttributes();
            if (attributes != null && attributes.length > 0) {
                pw.append("<td>");
                String divId = "attributes-" + tr.hashCode();
                pw.append("\n<a href=\"#")
                        .append(divId)
                        .append("\"")
                        .append(" onClick='toggleBox(\"")
                        .append(divId)
                        .append("\", this, \"Show attributes\", \"Hide attributes\");'>")
                        .append("Show attributes</a>\n")
                        .append("\n<a href=\"#")
                        .append(divId)
                        .append("\"></a>");
                pw.append("<div class='log' id=\"").append(divId).append("\">\n");
                Arrays.stream(attributes)
                        .map(
                                attribute -> "name: " + attribute.name() + ", value:"
                                        + Arrays.toString(attribute.values()))
                        .forEach(
                                line -> {
                                    pw.append(Utils.escapeHtml(line));
                                    pw.append("<br>");
                                });
                pw.append("</div>");
                pw.append("</td>");
            }

            // Exception
            tw = tr.getThrowable();
            // String stackTrace;
            String fullStackTrace;

            id = "stack-trace" + tr.hashCode();
            pw.append("<td style=\"overflow-y: auto; max-width: 40rem; max-height: 10rem;\">");

            if (null != tw) {
                fullStackTrace = Utils.longStackTrace(tw, true);
                pw.append("<a href='#' style=\"text-decoration: none;\r\n" + //
                        "font-weight: 700;\" onClick='toggleBox(\"")
                        .append(id)
                        .append(
                                "\", this, \"Click to show all stack frames\", \"Click to hide stack frames\")'>")
                        .append("Click to show all stack frames")
                        .append("</a>\n")
                        .append("<div class='stack-trace' id='")
                        .append(id)
                        .append("' style=\"max-height: 20rem;\">")
                        .append("<pre>")
                        .append(fullStackTrace)
                        .append("</pre>")
                        .append("</div>");
            }

            pw.append("</td>\n");

            // Method description (timeElapsed) - "requestUrl,timeElapsed"
            if (!Utils.isStringEmpty(method.getDescription())) {
                String timeEsapsed = method.getDescription().split(",")[1];
                pw.append("<td style=\"padding-left: 10px;\">")
                        .append(timeEsapsed + " ms").append("</td>");
            }

            // Time
            // long time = (tr.getEndMillis() - tr.getStartMillis()) / 1000;
            // String strTime = Long.toString(time);
            // pw.append("<td style=\"padding-left: 10px;\">").append(strTime + "
            // ms").append("</td>\n");
            // pw.append("</tr>\n");
        }

        pw.append("</tbody>\n").append("</table>\n");
    }

    private static String arrayToString(String[] array) {
        StringBuilder result = new StringBuilder();
        for (String element : array) {
            result.append(element).append(" ");
        }

        return result.toString();
    }

    public static void generateLog(
            ITestContext testContext,
            String host,
            String outputDirectory,
            Map<String, List<ITestResult>> resultsByClass) {
        try (PrintWriter writer = new PrintWriter(Utils.openWriter(outputDirectory, getOutputFile(testContext)))) {

            writer
                    .append("<html>\n<head>\n")
                    .append("<title>TestNG:  ")
                    .append(testContext.getName())
                    .append("</title>\n")
                    .append(HtmlHelper.getCssString())
                    .append(HEAD)
                    .append("</head>\n")
                    .append("<body>\n");

            String fetchModeChoosen = testContext.getAttribute("fetchModeChoosen") == null ? ""
                    : testContext.getAttribute("fetchModeChoosen").toString();
            Date startDate = testContext.getStartDate();
            Date endDate = testContext.getEndDate();
            long duration = (endDate.getTime() - startDate.getTime()) / 1000;
            int passed = testContext.getPassedTests().size()
                    + testContext.getFailedButWithinSuccessPercentageTests().size();
            int failed = testContext.getFailedTests().size();
            int skipped = testContext.getSkippedTests().size();
            int totalTestCases = passed + failed + skipped;
            String hostLine = Utils.isStringEmpty(host) ? "" : "<tr><td>Remote host:</td><td>" + host + "</td>\n</tr>";

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");

            writer
                    .append("<h2 align='center' style=\"font-family: Bahnschrift;\r\n" + //
                            "padding: 0.425rem 0px;\r\n" + //
                            "background: rgb(0, 128, 96);\r\n" + //
                            "color: white;\">")
                    // .append(testContext.getName())
                    .append("Report")
                    .append("</h2>")
                    .append("<table border='1' align=\"center\">\n")
                    .append("<tr><td>Mode</td><td>")
                    .append(fetchModeChoosen)
                    .append("</td></tr>\n")
                    .append("<tr><td>Total</td><td>")
                    .append(Integer.toString(totalTestCases))
                    .append("</td></tr>\n")
                    .append("<tr><td>Passed/Failed/Skipped </td><td>")
                    .append(Integer.toString(passed))
                    .append("/")
                    .append(Integer.toString(failed))
                    .append("/")
                    .append(Integer.toString(skipped))
                    .append("</td></tr>\n")
                    .append("<tr><td>Started on</td><td>")
                    .append(dateFormat.format(testContext.getStartDate()))
                    .append("</td></tr>\n")
                    .append("<tr><td>Ended on</td><td>")
                    .append(dateFormat.format(testContext.getEndDate()))
                    .append("</td></tr>\n")
                    .append(hostLine)
                    .append("<tr><td>Total time</td><td>")
                    .append(Long.toString(duration))
                    .append(" seconds (")
                    .append(Long.toString(endDate.getTime() - startDate.getTime()))
                    .append(" ms)</td>\n")
                    // .append("</tr><tr>\n")
                    // .append("<td>Included groups:</td><td>")
                    // .append(arrayToString(testContext.getIncludedGroups()))
                    // .append("</td>\n")
                    // .append("</tr><tr>\n")
                    // .append("<td>Excluded groups:</td><td>")
                    // .append(arrayToString(testContext.getExcludedGroups()))
                    // .append("</td>\n")
                    // .append("</tr>\n")
                    .append("</table><p/>\n");

            // if (!percentageTests.isEmpty()) {
            // generateTable(
            // writer,
            // "FAILED TESTS BUT WITHIN SUCCESS PERCENTAGE",
            // percentageTests,
            // "percent",
            // NAME_COMPARATOR);
            // }

            // if (!failedTests.isEmpty()) {
            // generateTable(writer, "FAILED TESTS", failedTests, "failed",
            // NAME_COMPARATOR);
            // }

            // if (!passedTests.isEmpty()) {
            // generateTable(writer, "PASSED TESTS", passedTests, "passed",
            // NAME_COMPARATOR);
            // }
            // if (!skippedTests.isEmpty()) {
            // generateTable(writer, "SKIPPED TESTS", skippedTests, "skipped",
            // NAME_COMPARATOR);
            // }
            for (Map.Entry<String, List<ITestResult>> entry : resultsByClass.entrySet()) {
                String className = entry.getKey();
                List<ITestResult> classResults = entry.getValue();
                generateTable(writer, className, classResults, "results", NAME_COMPARATOR);
            }

            writer.append("</body>\n</html>");
        } catch (IOException e) {
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

    private static class ConfigurationComparator implements Comparator<ITestResult> {

        @Override
        public int compare(ITestResult o1, ITestResult o2) {
            ITestNGMethod tm1 = o1.getMethod();
            ITestNGMethod tm2 = o2.getMethod();
            return annotationValue(tm2) - annotationValue(tm1);
        }

        private static int annotationValue(ITestNGMethod method) {
            if (method.isBeforeSuiteConfiguration()) {
                return 10;
            }
            if (method.isBeforeTestConfiguration()) {
                return 9;
            }
            if (method.isBeforeClassConfiguration()) {
                return 8;
            }
            if (method.isBeforeGroupsConfiguration()) {
                return 7;
            }
            if (method.isBeforeMethodConfiguration()) {
                return 6;
            }
            if (method.isAfterMethodConfiguration()) {
                return 5;
            }
            if (method.isAfterGroupsConfiguration()) {
                return 4;
            }
            if (method.isAfterClassConfiguration()) {
                return 3;
            }
            if (method.isAfterTestConfiguration()) {
                return 2;
            }
            if (method.isAfterSuiteConfiguration()) {
                return 1;
            }

            return 0;
        }
    }

    private static final String HEAD = "\n<style type=\"text/css\">\n" +
            ".log { display: none;} \n" +
            ".stack-trace { display: none;} \n" +
            "</style>\n" +
            "<link href=\"https://fonts.googleapis.com/css2?family=Quicksand:wght@500;600;700;800&display=swap\" rel=\"stylesheet\">\n"
            +
            "    <style>\n" + //
            "        * { \n" +
            "           font-family: 'Quicksand', sans-serif; \n" +
            "        } \n" +
            "        body {\n" + //
            "            font-weight: 500;\n" +
            "            background-color: #e9e9e9;\n" +
            "        }\n" + //
            "    </style>\n" +
            "<script type=\"text/javascript\">\n"
            // + "<!--\n"
            +
            "function filterFailed() {" +
            "    var rows = document.getElementsByTagName('tr');" +
            "    for (var i = 0; i < rows.length; i++) {" +
            "        if (rows[i].classList.contains('SUCCESS')) {" +
            "            rows[i].style.display = 'none';" +
            "        } else if (rows[i].classList.contains('FAILURE')) {" +
            "            rows[i].style.display = '';" +
            "        }" +
            "    }" +
            "}" +
            "function resetFilter() {" +
            "    var rows = document.getElementsByTagName('tr');" +
            "    for (var i = 0; i < rows.length; i++) {" +
            "        rows[i].style.display = '';" +
            "    }" +
            "}" + "function flip(e) {\n" + "  current = e.style.display;\n" +
            "  if (current == 'block') {\n" +
            "    e.style.display = 'none';\n" +
            "    return 0;\n" +
            "  }\n" +
            "  else {\n" +
            "    e.style.display = 'block';\n" +
            "    return 1;\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "function toggleBox(szDivId, elem, msg1, msg2)\n" +
            "{\n" +
            "  var res = -1;" +
            "  if (document.getElementById) {\n" +
            "    res = flip(document.getElementById(szDivId));\n" +
            "  }\n" +
            "  else if (document.all) {\n" +
            "    // this is the way old msie versions work\n" +
            "    res = flip(document.all[szDivId]);\n" +
            "  }\n" +
            "  if(elem) {\n" +
            "    if(res == 0) elem.innerHTML = msg1; else elem.innerHTML = msg2;\n" +
            "  }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "function toggleAllBoxes() {\n" +
            "  if (document.getElementsByTagName) {\n" +
            "    d = document.getElementsByTagName('div');\n" +
            "    for (i = 0; i < d.length; i++) {\n" +
            "      if (d[i].className == 'log') {\n" +
            "        flip(d[i]);\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            // + "// -->n"+
            "</script>\n";
}
