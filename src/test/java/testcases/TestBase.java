package testcases;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.github.javafaker.Faker;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.ConnectionType;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static drivers.DriverFactory.getNewInstance;
import static drivers.DriverHolder.getDriver;
import static drivers.DriverHolder.setDriver;
import static pages.PageBase.quitBrowser;

@Listeners(listeners.Listener.class)
public class TestBase {
    static WebDriver driver;
    //    static WebDriver driver;
    protected Faker faker = new Faker();
    // extend report
    protected static ExtentSparkReporter htmlReporter;
    protected static ExtentReports extent;
    protected static ExtentTest test;
    private static String PROJECT_NAME = null;
    private static String PROJECT_URL = null;

    // define Suite Elements
    static Properties prop;
    static FileInputStream readProperty;

    @BeforeSuite
    public void beforeSuite() throws Exception {
//        File folder = new File(System.getProperty("user.dir") + "\\recordings");
//        FileUtils.cleanDirectory(folder);
        // TODO: initialize the HtmlReporter
        htmlReporter = new ExtentSparkReporter(System.getProperty("user.dir") + "/testReport.html");

        //initialize ExtentReports and attach the HtmlReporter
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);

        setProjectDetails();

        // initialize test
        test = extent.createTest(PROJECT_NAME + " Test Automation Project");

        //configuration items to change the look and fee add content, manage tests etc
        htmlReporter.config().setDocumentTitle(PROJECT_NAME + " Test Automation Report");
        htmlReporter.config().setReportName(PROJECT_NAME + " Test Report");
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
    }

    private void setProjectDetails() throws IOException {
        // TODO: Step1: define object of properties file
        readProperty = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/properties/environment.properties");
        prop = new Properties();
        prop.load(readProperty);

        // define project name from properties file
        PROJECT_NAME = prop.getProperty("projectName");
        PROJECT_URL = prop.getProperty("url");
    }


    @Parameters("browserName")
    @BeforeTest
    public void setupDriver(String browserName) {
        setDriver(getNewInstance(browserName));

        driver = getDriver();

        slowNetwork();
        driver.get(PROJECT_URL);
    }

    @AfterTest
    public void tearDown() {
        quitBrowser(driver);
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        extent.flush();
    }

    @AfterMethod
    public void getResult(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, result.getName() + " failed with the following error: " + result.getThrowable());
            Reporter.log("Failed to perform " + result.getName(), 10, true);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, result.getName());
            Reporter.log("Successfully perform " + result.getName(), 10, true);
        } else {
            test.log(Status.SKIP, result.getName());
            Reporter.log("Skip " + result.getName(), 10, true);
        }
    }

    private void slowNetwork(){
        // Enable DevTools and simulate slow network conditions
        DevTools devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();

        // Simulate network conditions: slow 3G network (latency, bandwidth)
        devTools.send(Network.emulateNetworkConditions(
                false, // Offline (set to false for testing with network)
                300,  // Latency (ms)
                300,    // Download throughput (200 KB/s)
                200 * 1024,  // Upload throughput (200 KB/s)
                Optional.of(ConnectionType.CELLULAR2G)  // Correct enum reference for 3G connection
        ));
    }

}
