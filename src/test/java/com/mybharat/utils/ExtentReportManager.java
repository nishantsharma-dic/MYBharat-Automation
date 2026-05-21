package com.mybharat.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * ExtentReportManager - Creates and configures the HTML test report.
 * Uses singleton pattern so all test suites write to the SAME report.
 * Report is saved to: reports/index.html
 */
public class ExtentReportManager {

    private static ExtentReports extent;

    private ExtentReportManager() {
        // utility class
    }

    public static synchronized ExtentReports getReportObject() {
        if (extent == null) {
            String path = System.getProperty("user.dir") + "/reports/index.html";

            ExtentSparkReporter reporter = new ExtentSparkReporter(path);
            reporter.config().setReportName("MY Bharat - QA Automation Report");
            reporter.config().setDocumentTitle("MY Bharat | Test Execution Report");
            reporter.config().setTheme(Theme.DARK);
            reporter.config().setTimelineEnabled(true);

            extent = new ExtentReports();
            extent.attachReporter(reporter);

            // Project & Team Info
            extent.setSystemInfo("Project", "MY Bharat");
            extent.setSystemInfo("Team", "QA Team");
            extent.setSystemInfo("Tester", "Nishant Sharma");

            // Environment Info
            String env = System.getProperty("env", "beta").toUpperCase();
            extent.setSystemInfo("Environment", env);
            extent.setSystemInfo("Base URL", env.equals("PROD")
                    ? "https://mybharat.gov.in"
                    : "https://beta.mybharat.gov.in");
            extent.setSystemInfo("Browser", System.getProperty("browser", "chrome"));
            extent.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
        return extent;
    }
}
