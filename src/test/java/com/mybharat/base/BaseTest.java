package com.mybharat.base;

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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.mybharat.utils.ConfigReader;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * BaseTest - All test classes extend this.
 * 
 * Browser starts ONCE at suite level and is shared across all test classes.
 * This allows sequential test classes to run on the SAME browser session.
 * 
 * Usage:
 *   mvn test -Denv=beta -Dbrowser=chrome
 *   mvn test -Denv=prod -Dbrowser=firefox
 */
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    /** Shared driver across all test classes in the suite */
    protected static WebDriver driver;
    protected static ConfigReader config;

    /** Thread-local driver so Listeners can access it for screenshots */
    public static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        config = new ConfigReader();
        String browserName = System.getProperty("browser", config.getProperty("browser"));
        log.info("Starting browser: {} | Environment: {}", browserName, config.getEnv());

        driver = createDriver(browserName);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driverThreadLocal.set(driver);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {
        boolean closeBrowser = Boolean.parseBoolean(System.getProperty("closeBrowser", "true"));
        if (driver != null && closeBrowser) {
            log.info("Closing browser");
            driver.quit();
            driver = null;
            driverThreadLocal.remove();
        } else {
            log.info("Browser left open (closeBrowser=false)");
        }
    }

    /**
     * Navigate to the application URL (from config).
     */
    protected void openApp() {
        String url = config.getProperty("url");
        log.info("Opening URL: {}", url);
        driver.get(url);
    }

    /**
     * Take a screenshot and return the file path.
     */
    public String takeScreenshot(String testName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String path = System.getProperty("user.dir") + "/reports/" + testName + ".png";
        FileUtils.copyFile(source, new File(path));
        return path;
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private WebDriver createDriver(String browserName) {
        if (browserName == null) browserName = "chrome";

        switch (browserName.toLowerCase()) {
            case "chrome": {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                    "--remote-allow-origins=*",
                    "--disable-blink-features=AutomationControlled",
                    "--no-sandbox",
                    "--disable-dev-shm-usage"
                );
                options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

                // Set download directory to project-local folder
                String downloadDir = System.getProperty("user.dir") + File.separator + "downloads";
                new File(downloadDir).mkdirs();
                java.util.HashMap<String, Object> prefs = new java.util.HashMap<>();
                prefs.put("download.default_directory", downloadDir);
                prefs.put("download.prompt_for_download", false);
                prefs.put("plugins.always_open_pdf_externally", true);
                options.setExperimentalOption("prefs", prefs);

                return new ChromeDriver(options);
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
                    "--remote-allow-origins=*"
                );

                // Set download directory for headless mode
                String headlessDownloadDir = System.getProperty("user.dir") + File.separator + "downloads";
                new File(headlessDownloadDir).mkdirs();
                java.util.HashMap<String, Object> headlessPrefs = new java.util.HashMap<>();
                headlessPrefs.put("download.default_directory", headlessDownloadDir);
                headlessPrefs.put("download.prompt_for_download", false);
                headlessPrefs.put("plugins.always_open_pdf_externally", true);
                options.setExperimentalOption("prefs", headlessPrefs);

                return new ChromeDriver(options);
            }
            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            }
            case "firefox-headless": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                return new FirefoxDriver(options);
            }
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName
                    + ". Use: chrome, headless, firefox, firefox-headless");
        }
    }
}
