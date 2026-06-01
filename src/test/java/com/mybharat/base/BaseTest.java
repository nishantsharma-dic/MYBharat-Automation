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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.mybharat.utils.ConfigReader;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * BaseTest - All test classes extend this.
 * 
 * Browser lifecycle is per TestNG &lt;test&gt; block:
 *   - Each &lt;test&gt; in the XML gets its own browser instance.
 *   - All classes within the SAME &lt;test&gt; share the same browser session.
 *   - This supports parallel="tests" where each &lt;test&gt; runs in its own thread.
 * 
 * Usage:
 *   mvn test -Denv=beta -Dbrowser=chrome
 *   mvn test -Denv=prod -Dbrowser=firefox
 */
public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    /** Thread-local driver — each <test> block gets its own driver instance */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    protected static ConfigReader config;

    /** Accessible driver for child classes — populated from ThreadLocal */
    protected WebDriver driver;

    /** Public accessor for listeners/utilities to get the current thread's driver */
    public static final ThreadLocal<WebDriver> driverTL = driverThreadLocal;

    @BeforeTest(alwaysRun = true)
    public void setUp() {
        config = new ConfigReader();
        String browserName = System.getProperty("browser", config.getProperty("browser"));
        log.info("Starting browser: {} | Environment: {} | Thread: {}",
                browserName, config.getEnv(), Thread.currentThread().getName());

        WebDriver newDriver = createDriver(browserName);
        newDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        newDriver.manage().window().maximize();
        driverThreadLocal.set(newDriver);
        this.driver = newDriver;
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        boolean closeBrowser = Boolean.parseBoolean(System.getProperty("closeBrowser", "true"));
        WebDriver currentDriver = driverThreadLocal.get();
        if (currentDriver != null && closeBrowser) {
            log.info("Closing browser | Thread: {}", Thread.currentThread().getName());
            currentDriver.quit();
            driverThreadLocal.remove();
            this.driver = null;
        } else {
            log.info("Browser left open (closeBrowser=false)");
        }
    }

    /**
     * Sync the driver field from ThreadLocal for each class in the test block.
     * This runs before child class @BeforeClass methods.
     */
    @org.testng.annotations.BeforeClass(alwaysRun = true)
    public void syncDriver() {
        if (this.driver == null) {
            this.driver = driverThreadLocal.get();
            if (this.driver != null) {
                log.info("Driver synced from ThreadLocal for: {} | Thread: {}",
                        this.getClass().getSimpleName(), Thread.currentThread().getName());
            } else {
                log.error("Driver is NULL in ThreadLocal for: {} | Thread: {}",
                        this.getClass().getSimpleName(), Thread.currentThread().getName());
            }
        }
    }

    /**
     * Ensure the driver field is set from ThreadLocal for classes that initialize
     * after the @BeforeTest has already run (all classes in the same <test> block).
     */
    protected WebDriver getDriver() {
        if (this.driver == null) {
            this.driver = driverThreadLocal.get();
        }
        return this.driver;
    }

    /**
     * Navigate to the application URL (from config).
     */
    protected void openApp() {
        String url = config.getProperty("url");
        log.info("Opening URL: {}", url);
        getDriver().get(url);
    }

    /**
     * Take a screenshot and return the file path.
     */
    public String takeScreenshot(String testName) throws IOException {
        WebDriver d = getDriver();
        if (d == null) return null;
        File source = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
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
