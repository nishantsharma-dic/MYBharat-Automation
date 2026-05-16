package com.mybharat.tests.megaevent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;
import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.megaevent.MegaEventPage;

/**
 * MegaEventTest - Creates a Mega Event end-to-end.
 *
 * Flow: Login → Profile → View Org → Select Org → Mega Event → Create → Fill Form → Save
 *
 * Environment: Beta (yuva-beta.mybharats.in)
 * User: createorg_01@yopmail.com (OTP login)
 * Organization: dasasd
 */
@Listeners(TestListeners.class)
public class MegaEventTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(MegaEventTest.class);
    private MegaEventPage megaEventPage;
    private final Faker faker = new Faker();

    private static final String LOGIN_EMAIL = "createorg_01@yopmail.com";
    private static final String ORG_NAME = "dasasd";

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "megaevent"})
    public void loginToApplication() {
        log.info("Step 1: Login with OTP");
        megaEventPage.loginWithOTP(LOGIN_EMAIL);
        log.info("✅ Login successful");
    }

    @Test(priority = 2, groups = {"smoke", "megaevent"}, dependsOnMethods = "loginToApplication")
    public void navigateToOrganization() {
        log.info("Step 2-4: Navigate to Organization");
        megaEventPage.navigateToProfile();
        megaEventPage.clickViewOrganization();
        megaEventPage.selectOrganization(ORG_NAME);
        log.info("✅ Organization entered");
    }

    @Test(priority = 3, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToOrganization")
    public void navigateToMegaEvent() {
        log.info("Step 5-6: Navigate to Mega Event and Create");
        megaEventPage.clickMegaEventMenu();
        megaEventPage.clickCreateMegaEvent();
        log.info("✅ Create Mega Event form loaded");
    }

    @Test(priority = 4, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void uploadImages() {
        log.info("Step 7a: Upload Banner and Logo");
        megaEventPage.uploadBanner();
        megaEventPage.uploadLogo();
        log.info("✅ Images uploaded");
    }

    @Test(priority = 5, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void fillEventDetails() {
        log.info("Step 7b: Fill Event Name and About");
        String eventName = "Automation Mega Event " + faker.number().numberBetween(1000, 9999);
        String aboutText = "This mega event is created via automation testing. " + faker.lorem().sentence(10);

        megaEventPage.enterEventName(eventName);
        megaEventPage.enterAbout(aboutText);
        log.info("✅ Event details filled: {}", eventName);
    }

    @Test(priority = 6, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void fillEventDates() {
        log.info("Step 7c: Fill Event Dates");
        // Future dates
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(14);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        megaEventPage.fillEventDates(
                startDate.format(fmt), "09:00",
                endDate.format(fmt), "18:00"
        );
        log.info("✅ Event dates filled");
    }

    @Test(priority = 7, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void fillInclusionDates() {
        log.info("Step 7d: Fill Inclusion Dates");
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(14);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        megaEventPage.fillInclusionDates(
                startDate.format(fmt), "09:00",
                endDate.format(fmt), "18:00"
        );
        log.info("✅ Inclusion dates filled");
    }

    @Test(priority = 8, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void selectCheckboxes() {
        log.info("Step 7e: Select checkboxes");
        megaEventPage.checkVolunteerOpportunity();
        megaEventPage.checkExperientialLearning();
        log.info("✅ Checkboxes selected");
    }

    @Test(priority = 9, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void selectDropdowns() {
        log.info("Step 7f: Select dropdowns");
        megaEventPage.selectSpecialization();
        megaEventPage.selectMedium();
        megaEventPage.selectFunctionalCategory();
        log.info("✅ Dropdowns selected");
    }

    @Test(priority = 10, groups = {"smoke", "megaevent"}, dependsOnMethods = "navigateToMegaEvent")
    public void selectLocation() {
        log.info("Step 7g: Select Event Location");
        megaEventPage.selectState();
        megaEventPage.selectDistrict();
        log.info("✅ Location selected");
    }

    @Test(priority = 11, groups = {"smoke", "megaevent"}, dependsOnMethods = "selectLocation")
    public void saveEvent() {
        log.info("Step 8: Save Mega Event");
        megaEventPage.clickSave();
        log.info("✅ Mega Event saved — on preview page");
    }

    @Test(priority = 12, groups = {"smoke", "megaevent"}, dependsOnMethods = "saveEvent")
    public void publishEvent() {
        log.info("Step 9: Publish Mega Event (green tick)");
        megaEventPage.publishMegaEvent();
        log.info("✅ Mega Event published");
    }

    @Test(priority = 13, groups = {"smoke", "megaevent"}, dependsOnMethods = "publishEvent")
    public void verifyActiveTab() {
        log.info("Step 10: Verify event in Active tab");
        boolean isActive = megaEventPage.isEventInActiveTab();
        org.testng.Assert.assertTrue(isActive, "Published event should appear in Active tab");
        log.info("✅ Event verified in Active tab");
    }
}
