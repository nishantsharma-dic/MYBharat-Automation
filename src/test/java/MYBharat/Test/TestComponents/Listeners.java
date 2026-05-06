package MYBharat.Test.TestComponents;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import MYBharat_ResourcesAndAbstractComponents.resources.ExtentReporterNG;

/**
 * TestNG listener that integrates with ExtentReports.
 * Captures pass/fail/skip status and attaches screenshots on failure.
 * Thread-safe via ThreadLocal so it works correctly in parallel execution.
 *
 * <p>This class does NOT extend BaseTest to avoid driver lifecycle conflicts.
 * The WebDriver is retrieved from the TestNG ITestContext (set by BaseTest.setUpClass).
 */
public class Listeners implements ITestListener {

    private static final Logger log = LogManager.getLogger(Listeners.class);

    /** Thread-local ExtentTest so parallel tests don't share the same node. */
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    /** Single ExtentReports instance for the whole suite. */
    private final ExtentReports extent = ExtentReporterNG.getReportObject();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        extentTest.set(test);
        log.info("Test started: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Test Passed");
        log.info("Test passed: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        extentTest.get().fail(result.getThrowable());
        log.error("Test failed: {} - {}", result.getMethod().getMethodName(),
            result.getThrowable().getMessage());

        // Retrieve driver from thread-local (set in BaseTest.setUpClass)
        WebDriver driver = BaseTest.driverThreadLocal.get();
        if (driver != null) {
            try {
                String filePath = captureScreenshot(driver, result.getMethod().getMethodName());
                extentTest.get().addScreenCaptureFromPath(filePath, result.getMethod().getMethodName());
            } catch (IOException e) {
                log.error("Failed to capture screenshot for test: {}", result.getMethod().getMethodName(), e);
            }
        } else {
            log.warn("WebDriver not found in context for test: {}", result.getMethod().getMethodName());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().log(Status.SKIP, "Test Skipped");
        log.warn("Test skipped: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        extentTest.get().log(Status.WARNING, "Test failed but within success percentage");
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        log.info("Test suite finished: {}. Passed={}, Failed={}, Skipped={}",
            context.getName(),
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size());
    }

    /**
     * Captures a screenshot and saves it to the reports/ directory.
     *
     * @param driver       active WebDriver instance
     * @param testCaseName used as the file name
     * @return absolute path to the saved screenshot
     */
    private String captureScreenshot(WebDriver driver, String testCaseName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String screenshotPath = System.getProperty("user.dir") + "/reports/" + testCaseName + ".png";
        FileUtils.copyFile(source, new File(screenshotPath));
        log.info("Screenshot saved: {}", screenshotPath);
        return screenshotPath;
    }
}
