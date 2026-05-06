package MYBharat_ResourcesAndAbstractComponents.AbstractComponents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * AbstractComponent provides common utility methods and property loading for all
 * Selenium page objects and test base classes.
 *
 * <p>Design notes:
 * <ul>
 *   <li>The {@code driver} field is <em>instance</em>-scoped (not static) so that
 *       parallel test runs with separate driver instances work correctly.</li>
 *   <li>Configuration is loaded from {@code config.<env>.properties} on the
 *       classpath, where {@code env} defaults to {@code qa} and can be overridden
 *       with {@code -Denv=uat} or {@code -Denv=prod}.</li>
 * </ul>
 */
public class AbstractComponent {

    private static final Logger log = LogManager.getLogger(AbstractComponent.class);

    /** Default explicit-wait timeout in seconds. */
    private static final int DEFAULT_WAIT_SECONDS = 25;

    /**
     * WebDriver instance.
     * Declared {@code protected} (not static) to support parallel execution.
     */
    protected WebDriver driver;

    private final Properties properties = new Properties();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Initialises the component with the given driver and loads the environment
     * properties file from the classpath.
     *
     * @param driver WebDriver instance (may be {@code null} during Listeners init)
     */
    public AbstractComponent(WebDriver driver) {
        this.driver = driver;
        loadProperties();
    }

    private void loadProperties() {
        String env = System.getProperty("env", "qa");
        String propertiesFile = "config." + env + ".properties";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input != null) {
                properties.load(input);
                log.debug("Loaded properties from {}", propertiesFile);
            } else {
                log.error("Properties file not found on classpath: {}", propertiesFile);
            }
        } catch (IOException e) {
            log.error("Failed to load {}: {}", propertiesFile, e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Property access
    // -------------------------------------------------------------------------

    /**
     * Returns the value for {@code key} from the loaded properties file,
     * or {@code null} if the key is absent.
     */
    protected String getProperty(String key) {
        return properties.getProperty(key);
    }

    // -------------------------------------------------------------------------
    // Wait helpers
    // -------------------------------------------------------------------------

    /**
     * Waits up to {@value #DEFAULT_WAIT_SECONDS} seconds for {@code element} to be visible.
     */
    public void waitForWebElementToVisible(WebElement element) {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS))
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits up to {@code timeoutSeconds} for the element identified by {@code locator} to be visible.
     *
     * @return the visible WebElement
     */
    public WebElement waitForWebElementToVisible(By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits up to {@code timeoutSeconds} for {@code element} to be visible.
     *
     * @return the visible WebElement
     */
    public WebElement waitForWebElementToVisible(WebElement element, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits up to {@value #DEFAULT_WAIT_SECONDS} seconds for {@code element} to be clickable.
     */
    public void waitForWebElementToClickable(WebElement element) {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS))
            .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits up to {@value #DEFAULT_WAIT_SECONDS} seconds for the loader identified by
     * {@code locator} to become invisible.
     */
    public void waitForLoaderToDisappear(By locator) {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS))
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits for the OTP input field to contain exactly 6 characters.
     * Uses a generous 500-second timeout to allow manual OTP entry.
     */
    public void waitForOTP() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(500));
        WebElement otpField = driver.findElement(By.xpath("//input[@id='otp']"));
        wait.until(d -> {
            String val = otpField.getAttribute("value");
            return val != null && val.length() == 6;
        });
    }

    // -------------------------------------------------------------------------
    // Scroll helpers
    // -------------------------------------------------------------------------

    /**
     * Scrolls the page vertically by {@code pixels}.
     */
    public void scrollPage(int pixels) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + pixels + ")");
    }

    /**
     * Scrolls {@code element} into the centre of the viewport.
     */
    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({block:'center',behavior:'smooth'});", element);
    }

    // -------------------------------------------------------------------------
    // Browser / cache helpers
    // -------------------------------------------------------------------------

    /**
     * Clears localStorage, sessionStorage, and reloads the page.
     */
    public void clearBrowserCache() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.localStorage.clear();");
        js.executeScript("window.sessionStorage.clear();");
        js.executeScript("window.location.reload();");
    }

    // -------------------------------------------------------------------------
    // File helpers
    // -------------------------------------------------------------------------

    /**
     * Deletes all regular files inside {@code folderPath}.
     * Silently skips if the folder does not exist.
     */
    public void cleanFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Excel helper
    // -------------------------------------------------------------------------

    /**
     * Appends {@code data} to the "Data" sheet of the given workbook and writes
     * the workbook to {@code excelFilePath}.
     */
    public void writeToExcel(String data, Workbook workbook, String excelFilePath) {
        Sheet sheet = workbook.getSheet("Data");
        if (sheet == null) {
            sheet = workbook.createSheet("Data");
        }
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(data);
        try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            log.error("Failed to write to Excel file {}: {}", excelFilePath, e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Random data helpers
    // -------------------------------------------------------------------------

    /**
     * Generates a random 10-digit Indian mobile number starting with '9'.
     *
     * @return 10-character numeric string
     */
    public static String get10DigitNumber() {
        Random random = new Random();
        StringBuilder mobileNumber = new StringBuilder("9");
        for (int i = 1; i < 10; i++) {
            mobileNumber.append(random.nextInt(10));
        }
        return mobileNumber.toString();
    }

    // -------------------------------------------------------------------------
    // Selenium interaction helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link Select} wrapper for the given dropdown element.
     */
    public static Select select(WebElement dropdownElement) {
        return new Select(dropdownElement);
    }

    /**
     * Returns an {@link Actions} builder bound to the current driver.
     */
    public Actions action() {
        return new Actions(driver);
    }
}
