package com.mybharat.listeners;

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
import com.mybharat.base.BaseTest;
import com.mybharat.utils.ExtentReportManager;

/**
 * TestListeners - Integrates TestNG with ExtentReports.
 * Captures screenshots on failure automatically.
 */
public class TestListeners implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListeners.class);

    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private final ExtentReports extent = ExtentReportManager.getReportObject();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        extentTest.set(test);
        log.info("▶ Test started: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Test Passed");
        log.info("✅ Test passed: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        extentTest.get().fail(result.getThrowable());
        log.error("❌ Test failed: {}", result.getMethod().getMethodName());

        // Capture screenshot — safely handle invalid/dead sessions
        WebDriver driver = BaseTest.driverThreadLocal.get();
        if (driver != null) {
            try {
                String path = captureScreenshot(driver, result.getMethod().getMethodName());
                extentTest.get().addScreenCaptureFromPath(path);
            } catch (Exception e) {
                log.warn("Could not capture screenshot (session may be invalid): {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().log(Status.SKIP, "Test Skipped");
        log.warn("⏭ Test skipped: {}", result.getMethod().getMethodName());
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

    private String captureScreenshot(WebDriver driver, String testName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String path = System.getProperty("user.dir") + "/reports/" + testName + ".png";
        FileUtils.copyFile(source, new File(path));
        return path;
    }
}
