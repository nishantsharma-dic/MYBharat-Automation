package MYBharat.Test.TestComponents;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import MYBharat_ResourcesAndAbstractComponents.AbstractComponents.AbstractComponent;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * BaseTest provides WebDriver lifecycle management for all test classes.
 * Supports Chrome, Firefox, Edge, and headless Chrome via Maven -Dbrowser property.
 * Environment is selected via -Denv (qa | uat | prod), defaulting to qa.
 */
public class BaseTest extends AbstractComponent {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    /** WebDriver instance shared with subclasses and the Listeners. */
    protected WebDriver driver;

    /**
     * Thread-local driver store so Listeners can retrieve the active driver
     * for screenshot capture without needing to extend BaseTest.
     */
    public static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /** Constructor for dependency injection (used in tests that pass a driver). */
    public BaseTest(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    /** No-arg constructor required by TestNG. */
    public BaseTest() {
        super(null);
    }

    /**
     * Initialises the browser before any test in the class runs.
     * Browser is controlled by the Maven property {@code -Dbrowser} (default: firefox).
     */
    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws IOException {
        String browserName = System.getProperty("browser", "firefox");
        log.info("Initialising browser: {}", browserName);
        this.driver = initializeDriver(browserName);
        super.driver = this.driver;
        // Store driver in thread-local so Listeners can retrieve it for screenshots
        driverThreadLocal.set(this.driver);
        log.info("Browser started successfully");
    }

    /**
     * Quits the browser after all tests in the class have run.
     */
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (driver != null) {
            log.info("Quitting browser");
            driver.quit();
            driver = null;
            driverThreadLocal.remove();
        }
    }

    /**
     * Creates and returns a WebDriver instance for the requested browser.
     *
     * @param browserName chrome | firefox | edge | headless
     * @return configured WebDriver
     */
    public WebDriver initializeDriver(String browserName) {
        if (browserName == null) browserName = "chrome";

        switch (browserName.toLowerCase()) {

            case "chrome": {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                    "--remote-allow-origins=*",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-extensions",
                    "--no-sandbox",
                    "--disable-dev-shm-usage"
                );
                options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                driver = new ChromeDriver(options);
                break;
            }

            case "headless": {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                    "--headless=new",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-blink-features=AutomationControlled"
                );
                driver = new ChromeDriver(options);
                break;
            }

            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                driver = new FirefoxDriver(options);
                break;
            }

            case "firefox-headless": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                driver = new FirefoxDriver(options);
                break;
            }

            case "edge": {
                String localEdgeDriver = System.getProperty("user.dir") + "/msedgedriver/msedgedriver";
                File edgeDriverFile = new File(localEdgeDriver);
                if (edgeDriverFile.exists()) {
                    System.setProperty("webdriver.edge.driver", localEdgeDriver);
                } else {
                    WebDriverManager.edgedriver().setup();
                }
                driver = new EdgeDriver();
                break;
            }

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName
                    + ". Supported values: chrome, headless, firefox, firefox-headless, edge");
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        log.info("WebDriver initialised for browser: {}", browserName);
        return driver;
    }

    /**
     * Captures a screenshot and saves it under the reports/ directory.
     *
     * @param testCaseName name used for the screenshot file
     * @return absolute path to the saved screenshot
     * @throws IOException if the file cannot be written
     */
    public String getScreenshot(String testCaseName) throws IOException {
        TakesScreenshot scr = (TakesScreenshot) driver;
        File source = scr.getScreenshotAs(OutputType.FILE);
        String screenshotPath = System.getProperty("user.dir") + "/reports/" + testCaseName + ".png";
        FileUtils.copyFile(source, new File(screenshotPath));
        log.info("Screenshot saved: {}", screenshotPath);
        return screenshotPath;
    }
}
