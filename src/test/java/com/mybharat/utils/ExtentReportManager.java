package com.mybharat.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * ExtentReportManager - Creates and configures the HTML test report.
 * Report is saved to: reports/index.html
 */
public class ExtentReportManager {

    private ExtentReportManager() {
        // utility class
    }

    public static ExtentReports getReportObject() {
        String path = System.getProperty("user.dir") + "/reports/index.html";

        ExtentSparkReporter reporter = new ExtentSparkReporter(path);
        reporter.config().setReportName("MYBharat Automation Report");
        reporter.config().setDocumentTitle("Test Results");
        reporter.config().setTheme(Theme.DARK);

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(reporter);

        String env = System.getProperty("env", "beta").toUpperCase();
        extent.setSystemInfo("Environment", env);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Browser", System.getProperty("browser", "firefox"));

        return extent;
    }
}
