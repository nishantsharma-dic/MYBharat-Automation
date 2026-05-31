package com.mybharat.tests.megaevent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;
import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.megaevent.MegaEventPage;
import com.mybharat.utils.ExcelUtils;

/**
 * MegaEventTest - Creates and publishes a Mega Event end-to-end.
 *
 * Single test method: publishMegaEvent
 * Flow: Login (Partner) → Navigate to Org → Mega Event menu → Create form
 *       → Upload images → Fill details/dates → Save → Publish → Verify → Save to Excel
 *
 * Run standalone:
 *   mvn test "-Denv=prod" "-Dbrowser=chrome" "-Dsurefire.suiteXmlFiles=testSuites/testng-megaevent.xml"
 */
@Listeners(TestListeners.class)
public class MegaEventTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(MegaEventTest.class);
    private MegaEventPage megaEventPage;
    private final Faker faker = new Faker();

    // Excel paths — environment-aware
    private static final String ENV            = System.getProperty("env", "beta");
    private static final String USER_EXCEL     = "resources/Partner_" + ENV + ".xlsx";
    private static final String EVENT_OUT_EXCEL = "resources/Create_MegaEvent_" + capitalize(ENV) + ".xlsx";
    private static final String SHEET_NAME     = "MegaEvents";

    private static String capitalize(String s) {
        return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String loginEmail;
    private String loginPassword;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);

        List<String> emails = ExcelUtils.readColumn(USER_EXCEL, SHEET_NAME, 0);
        Assert.assertFalse(emails.isEmpty(), USER_EXCEL + " must have at least one email");
        loginEmail = emails.get(emails.size() - 1);

        List<String> passwords = ExcelUtils.readColumn(USER_EXCEL, SHEET_NAME, 1);
        loginPassword = (passwords.size() >= emails.size()) ? passwords.get(passwords.size() - 1) : null;

        log.info("[SETUP] Login email: {}, hasPassword: {}", loginEmail, loginPassword != null);
    }

    /**
     * Single test: Create and publish a new Mega Event.
     * Appears as one test case in ExtentReport.
     */
    @Test(groups = {"smoke", "megaevent"},
          description = "Create and publish a new Mega Event (Partner user)")
    public void publishMegaEvent() throws Exception {
        String eventName = "Seeva See Seekhen";

        // Step 1: Login as Partner
        log.info("Step 1: Login — {}", loginEmail);
        com.mybharat.pages.youth.LoginPage loginPage = new com.mybharat.pages.youth.LoginPage(driver);
        loginPage.login(loginEmail, loginPassword);
        log.info("Logged in as: {}", loginEmail);

        // Step 2: Navigate to Organization
        log.info("Step 2: Navigate to Organization");
        megaEventPage.navigateToProfile();
        megaEventPage.clickViewOrganization();
        megaEventPage.selectOrganization(null);

        // Step 3: Navigate to Mega Event and Create
        log.info("Step 3: Navigate to Mega Event → Create");
        megaEventPage.clickMegaEventMenu();
        megaEventPage.clickCreateMegaEvent();

        // Step 4: Upload banner and logo
        log.info("Step 4: Upload banner and logo");
        megaEventPage.uploadBanner();
        megaEventPage.uploadLogo();

        // Step 5: Fill event name and about
        log.info("Step 5: Fill event details — '{}'", eventName);
        megaEventPage.enterEventName(eventName);
        megaEventPage.enterAbout("This mega event is created via automation testing. " + faker.lorem().sentence(10));

        // Step 6: Fill event dates (past dates)
        log.info("Step 6: Fill event dates");
        LocalDate startDate = LocalDate.now().minusDays(14);
        LocalDate endDate   = LocalDate.now().minusDays(7);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        megaEventPage.fillEventDates(startDate.format(fmt), "09:00", endDate.format(fmt), "18:00");

        // Step 7: Fill inclusion dates
        log.info("Step 7: Fill inclusion dates");
        megaEventPage.fillInclusionDates(startDate.format(fmt), "09:00", endDate.format(fmt), "18:00");

        // Step 8: Select checkboxes
        log.info("Step 8: Select checkboxes");
        megaEventPage.checkVolunteerOpportunity();
        megaEventPage.checkExperientialLearning();

        // Step 9: Select dropdowns
        log.info("Step 9: Select dropdowns");
        megaEventPage.selectSpecialization();
        megaEventPage.selectMedium();
        megaEventPage.selectFunctionalCategory();

        // Step 10: Select location
        log.info("Step 10: Select location");
        megaEventPage.selectState();
        megaEventPage.selectDistrict();

        // Step 11: Save
        log.info("Step 11: Save Mega Event");
        megaEventPage.clickSave();

        // Step 12: Publish
        log.info("Step 12: Publish Mega Event");
        megaEventPage.publishMegaEvent();

        // Step 13: Verify in Past tab
        log.info("Step 13: Verify event in Past tab");
        boolean isActive = megaEventPage.isEventInActiveTab();
        Assert.assertTrue(isActive, "Published event should appear in listing");

        // Save event name to Excel
        ExcelUtils.appendValue(EVENT_OUT_EXCEL, SHEET_NAME, eventName);
        log.info("✅ publishMegaEvent PASSED — Event '{}' created and saved to Excel", eventName);
    }
}
