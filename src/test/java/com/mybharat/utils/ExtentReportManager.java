package com.mybharat.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * ExtentReportManager - Singleton factory for the ExtentReports HTML report engine.
 *
 * Purpose: Creates and configures a single shared ExtentReports instance used by all
 *          test suites. Generates a DARK-themed interactive HTML report with project
 *          metadata, environment info, and timeline visualization.
 *
 * Report Output: reports/index.html (in the project root)
 *
 * Key Methods:
 *   - getReportObject() — returns the singleton ExtentReports instance (thread-safe)
 *
 * Report Configuration:
 *   - Theme: DARK
 *   - Timeline: enabled (shows test execution order)
 *   - System info: Project name, Team, Tester, Environment, Base URL, Browser, OS, Java version
 *
 * Thread Safety: Uses synchronized initialization (double-checked locking pattern via
 *                synchronized method) to ensure only one report instance exists.
 *
 * Dependencies: ExtentReports (aventstack), ExtentSparkReporter
 * Developer: Nishant Sharma (QA Team)
 *
 * @see TestListeners
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
