package utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static drivers.DriverHolder.getDriver;

public class ExtentReporter implements ITestListener {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testNode = new ThreadLocal<>();
    private static final Map<String, ExtentTest> testSuiteMap = new ConcurrentHashMap<>();
    private static String reportName;

    public synchronized static ExtentReports getExtentInstance() {
        if (extent == null) {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            reportName = "Extent-Report-" + timeStamp + ".html";
            String reportPath = System.getProperty("user.dir") + "\\reports\\" + reportName;

            // The reporter path and name
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            // title of report
            sparkReporter.config().setDocumentTitle("Altoro Website Report");
            // name of report
            sparkReporter.config().setReportName("Altoro Test Execution Summary");
            // theme of report
            sparkReporter.config().setTheme(Theme.DARK);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            // app name
            extent.setSystemInfo("Host Name", "Localhost");
            // module name
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("User", System.getProperty("user.name"));
        }
        return extent;
    }

    @Override
    public void onStart(ITestContext context) {
        String suiteName = context.getSuite().getName();
        ExtentTest suiteNode = getExtentInstance().createTest(suiteName);
        testSuiteMap.put(context.getName(), suiteNode);

        suiteNode.info("Suite started: " + suiteName);
        suiteNode.assignCategory("Suite: " + suiteName);

        getExtentInstance().setSystemInfo("OS", context.getCurrentXmlTest().getParameter("os"));
        getExtentInstance().setSystemInfo("Browser", context.getCurrentXmlTest().getParameter("browser"));
        List<String> includedGroups = context.getCurrentXmlTest().getIncludedGroups();
        if (!includedGroups.isEmpty()) {
            getExtentInstance().setSystemInfo("Groups", includedGroups.toString());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest suiteNode = testSuiteMap.get(result.getTestContext().getName());
        ExtentTest methodNode = suiteNode.createNode(result.getMethod().getMethodName());
        methodNode.assignCategory(result.getTestContext().getName());
        testNode.set(methodNode);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = testNode.get();
        test.log(Status.PASS, result.getName() + " passed.");
        test.log(Status.PASS, result.getMethod().getMethodName() + " passed.");
        test.info("Execution Time: " + getExecutionTime(result) + " ms");
        test.assignCategory(result.getMethod().getGroups());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testNode.get();
        test.log(Status.FAIL, result.getName() + " failed.");
//        test.log(Status.FAIL, result.getMethod().getMethodName() + " failed.");
        test.assignCategory(result.getMethod().getGroups());
        test.log(Status.FAIL, result.getThrowable());

        try {
            String screenshotPath = captureScreenshot(result.getName());
            test.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
        } catch (IOException e) {
            e.printStackTrace();
        }
        test.info("Execution Time: " + getExecutionTime(result) + " ms");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = testNode.get();
        test.log(Status.SKIP, result.getName() + " skipped.");
        test.log(Status.SKIP, result.getMethod().getMethodName() + " skipped.");
        test.info("Reason: " + result.getThrowable());
        test.info("Execution Time: " + getExecutionTime(result) + " ms");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentTest suiteNode = testSuiteMap.get(context.getName());
        suiteNode.info("Suite finished: " + context.getSuite().getName());
        suiteNode.info("Passed: " + context.getPassedTests().size());
        suiteNode.info("Failed: " + context.getFailedTests().size());
        suiteNode.info("Skipped: " + context.getSkippedTests().size());

        getExtentInstance().flush();

    }

    static public void openExtentReport() {
        System.out.println("nameOpenReport" + reportName);
        try {
            File reportFile = new File(System.getProperty("user.dir") + "\\reports\\" + reportName);
            Desktop.getDesktop().browse(reportFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static public void openAllureReport() throws IOException {
        // Use the full path to the allure command (adjust this based on your installation location)
        String allurePath = "C:/allure-2.32.0/bin/allure.bat";  // For Windows
        // String allurePath = "/path/to/allure/bin/allure";  // For macOS/Linux

        // Allure command to generate the report
        String command = allurePath + " generate --single-file target/allure-results --clean -o target/allure-report";

        // Running the command using ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.inheritIO(); // This makes the output of the command show in the console
        Process process = processBuilder.start();

        // Wait for the process to finish
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Allure report generated successfully.");
            } else {
                System.err.println("Error generating Allure report. Exit code: " + exitCode);
            }
            try {
                File reportFile = new File(System.getProperty("user.dir") + "/target/allure-report/index.html");
                Desktop.getDesktop().browse(reportFile.toURI());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openReports() {
        try {
            openExtentReport();
            openAllureReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String captureScreenshot(String testName) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String screenshotPath = System.getProperty("user.dir") + "\\screenshots\\" + testName + "_" + timestamp + ".png";
        File sourceFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        File targetFile = new File(screenshotPath);
        sourceFile.renameTo(targetFile);
        return screenshotPath;
    }

    private long getExecutionTime(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }

}