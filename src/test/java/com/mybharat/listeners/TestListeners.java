package com.mybharat.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.mybharat.base.BaseTest;
import com.mybharat.utils.ExtentReportManager;

/**
 * TestListeners - TestNG ITestListener that integrates with ExtentReports for rich HTML reporting.
 *
 * Purpose: Captures test lifecycle events (start, pass, fail, skip) and logs them to
 *          ExtentReports with screenshots, timing, categorization, and color-coded labels.
 *
 * Flow:
 *   1. onTestStart   → creates ExtentTest entry, assigns category/author, logs metadata
 *   2. onTestSuccess → marks PASS with green label, logs duration
 *   3. onTestFailure → marks FAIL with red label, captures screenshot, logs exception
 *   4. onTestSkipped → marks SKIP with orange label (tracks for retry removal)
 *   5. onFinish      → flushes report to HTML file (reports/index.html)
 *
 * Key Methods:
 *   - onTestStart()    — initializes ExtentTest with description, category, author
 *   - onTestSuccess()  — logs pass status with execution time
 *   - onTestFailure()  — logs failure with screenshot and exception details
 *   - onTestSkipped()  — tracks skipped tests; removes them if retry passes
 *   - getExtentTest()  — static accessor for page classes to log intermediate steps
 *   - getModuleFromClass() — derives module category from test class name
 *
 * Retry Handling: When a test is retried (via Retry analyzer), the skipped entry is
 *                 removed from the report so only the final result appears.
 *
 * Dependencies: ExtentReports, BaseTest (for driver access), Selenium TakesScreenshot
 * Developer: Nishant Sharma (QA Team)
 *
 * @see ExtentReportManager
 * @see Retry
 * @see BaseTest
 */
public class TestListeners implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListeners.class);

    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final ExtentReports extent = ExtentReportManager.getReportObject();

    /** Track skipped tests so we can remove them if retry passes */
    private static final java.util.Map<String, ExtentTest> skippedTests = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String className = result.getTestClass().getRealClass().getSimpleName();

        // If this test was previously skipped (retry), remove the skipped entry
        String key = className + "." + testName;
        if (skippedTests.containsKey(key)) {
            extent.removeTest(skippedTests.get(key));
            skippedTests.remove(key);
        }

        // Create test with descriptive name: ClassName > methodName
        ExtentTest test = extent.createTest(className + " > " + testName);

        // Add description if available from @Test annotation
        if (description != null && !description.isEmpty()) {
            test.info("📋 Description: " + description);
        }

        // Assign categories based on module (not test groups like smoke/regression)
        test.assignCategory(getModuleFromClass(className));

        // Assign author
        test.assignAuthor("QA Team");

        // Log start info
        test.info("🚀 Test execution started at: " + getCurrentTime());
        test.info("📂 Module: " + getModuleFromClass(className));
        test.info("🖥 Environment: " + System.getProperty("env", "beta").toUpperCase());

        extentTest.set(test);
        log.info("▶ Test started: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        ExtentTest test = extentTest.get();

        test.log(Status.PASS, MarkupHelper.createLabel(
                "✅ TEST PASSED — " + result.getMethod().getMethodName(), ExtentColor.GREEN));
        test.info("⏱ Duration: " + formatDuration(duration));
        test.info("🏁 Completed at: " + getCurrentTime());

        log.info("✅ Test passed: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        ExtentTest test = extentTest.get();

        test.log(Status.FAIL, MarkupHelper.createLabel(
                "❌ TEST FAILED — " + result.getMethod().getMethodName(), ExtentColor.RED));
        test.info("⏱ Duration: " + formatDuration(duration));
        test.info("🏁 Failed at: " + getCurrentTime());

        // Log the error details
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            test.fail("💥 Error: " + throwable.getMessage());
            test.fail(throwable);
        }

        // Capture screenshot
        WebDriver driver = BaseTest.driverTL.get();
        if (driver != null) {
            try {
                String screenshotPath = captureScreenshot(driver, result.getMethod().getMethodName());
                test.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                test.info("📸 Screenshot captured: " + screenshotPath);
            } catch (Exception e) {
                test.warning("⚠ Could not capture screenshot: " + e.getMessage());
                log.warn("Could not capture screenshot (session may be invalid): {}", e.getMessage());
            }
        }

        log.error("❌ Test failed: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest test = extentTest.get();

        // Track this skipped test so it can be removed if retry passes
        String key = className + "." + testName;
        skippedTests.put(key, test);

        test.log(Status.SKIP, MarkupHelper.createLabel(
                "⏭ TEST SKIPPED — " + testName + " (will retry)", ExtentColor.ORANGE));

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            test.skip("Reason: " + throwable.getMessage());
        }

        log.warn("⏭ Test skipped: {}", testName);
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        log.info("Suite finished: {} | Passed={}, Failed={}, Skipped={}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Get the ExtentTest for the current thread (used by page classes to log steps).
     */
    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }

    private String captureScreenshot(WebDriver driver, String testName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path = System.getProperty("user.dir") + "/reports/" + testName + "_" + timestamp + ".png";
        FileUtils.copyFile(source, new File(path));
        return path;
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a").format(new Date());
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes > 0) {
            return minutes + " min " + seconds + " sec";
        }
        return seconds + " sec " + (millis % 1000) + " ms";
    }

    /**
     * Derive module name from test class name for categorization.
     */
    private String getModuleFromClass(String className) {
        if (className.contains("Registration")) return "Registration";
        if (className.contains("Login")) return "Login";
        if (className.contains("Logout")) return "Logout";
        if (className.contains("Profile")) return "Profile";
        if (className.contains("Certificate")) return "Certificate";
        if (className.contains("BasicInfo")) return "Basic Info";
        if (className.contains("Quiz")) return "Quiz";
        if (className.contains("ELP")) return "ELP";
        return "General";
    }
}
