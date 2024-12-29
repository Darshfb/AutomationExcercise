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
import static utilities.ExtentReporter.openReports;

@Listeners(listeners.Listener.class)
public class TestBase {
    static WebDriver driver;
    //    static WebDriver driver;
    protected Faker faker = new Faker();

    private static String PROJECT_NAME = null;
    private static String PROJECT_URL = null;

    // define Suite Elements
    static Properties prop;
    static FileInputStream readProperty;

    @BeforeSuite
    public void beforeSuite() throws Exception {
//        File folder = new File(System.getProperty("user.dir") + "\\recordings");
//        FileUtils.cleanDirectory(folder);
        setProjectDetails();

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
        driver.get(PROJECT_URL);
    }

    @AfterTest
    public void tearDown() {
        quitBrowser(driver);
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        openReports();
    }


}
