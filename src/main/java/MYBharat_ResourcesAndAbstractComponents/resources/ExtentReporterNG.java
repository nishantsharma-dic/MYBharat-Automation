package MYBharat_ResourcesAndAbstractComponents.resources;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Factory class that creates and configures the ExtentReports instance.
 * Report is written to reports/index.html under the project root.
 *
 * Tester name and environment are read from system properties so they can be
 * overridden at runtime without touching source code:
 *   -Dtester="Jane Doe"  -Denv=qa
 */
public class ExtentReporterNG {

    private ExtentReporterNG() {
        // utility class – no instances
    }

    /**
     * Builds and returns a fully configured {@link ExtentReports} instance.
     *
     * @return ready-to-use ExtentReports
     */
    public static ExtentReports getReportObject() {
        String path = System.getProperty("user.dir") + "/reports/index.html";

        ExtentSparkReporter reporter = new ExtentSparkReporter(path);
        reporter.config().setReportName("MYBharat Automation Report");
        reporter.config().setDocumentTitle("Test Results");
        reporter.config().setTheme(Theme.DARK);

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(reporter);

        // Read tester and environment from system properties; fall back to sensible defaults
        String tester = System.getProperty("tester", "QA Team");
        String env    = System.getProperty("env", "qa").toUpperCase();

        extent.setSystemInfo("Tester", tester);
        extent.setSystemInfo("Environment", env);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));

        return extent;
    }
}
