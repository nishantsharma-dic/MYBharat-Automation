package com.mybharat.tests.organisation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;
import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.organisation.CreateOrganisationPage;

@Listeners(TestListeners.class)
public class CreateOrganisationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(CreateOrganisationTest.class);
    private CreateOrganisationPage page;
    private final Faker faker = new Faker();

    private String orgName;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        page = new CreateOrganisationPage(driver);
        orgName = "Test Org " + faker.company().name();
    }

    @Test(priority = 1, groups = {"smoke", "organisation"})
    public void navigateToCreateOrganisation() {
        log.info("Step 1: Navigate to Create Organisation");
        page.navigateToCreateOrg();
        Assert.assertTrue(page.isCreateOrgPageLoaded(), "Create Org page should load");
        log.info("✅ Page loaded");
    }

    @Test(priority = 2, groups = {"smoke", "organisation"}, dependsOnMethods = "navigateToCreateOrganisation")
    public void uploadImages() {
        log.info("Step 2: Upload Banner and Logo");
        page.uploadBanner();
        page.uploadLogo();
        log.info("✅ Images uploaded");
    }

    @Test(priority = 3, groups = {"smoke", "organisation"}, dependsOnMethods = "navigateToCreateOrganisation")
    public void fillAboutSection() {
        log.info("Step 3: Fill About section");
        page.enterOrgName(orgName);
        page.enterAboutText("Government Organisation for " + faker.company().catchPhrase() + ". " + faker.lorem().sentence(10));
        log.info("✅ About filled");
    }

    @Test(priority = 4, groups = {"smoke", "organisation"}, dependsOnMethods = "fillAboutSection")
    public void clickNext() {
        log.info("Step 4: Click Next");
        page.clickNext();
        log.info("✅ Basic Info visible");
    }

    @Test(priority = 5, groups = {"smoke", "organisation"}, dependsOnMethods = "clickNext")
    public void selectCategoryAndSubCategory() {
        log.info("Step 5: Select Category and SubCategory");
        page.selectCategory("Government");
        page.selectSubCategory("Police");
        log.info("✅ Category/SubCategory selected");
    }

    @Test(priority = 6, groups = {"smoke", "organisation"}, dependsOnMethods = "clickNext")
    public void fillOrgDetails() {
        log.info("Step 6: Fill Org Details");
        page.enterName(orgName);
        page.enterAbbreviation(faker.lorem().characters(3, 5).toUpperCase());
        page.selectGroup("State");
        page.selectSpecialization("Awareness");
        page.enterNodalDesignation(faker.job().title());
        log.info("✅ Org details filled");
    }

    @Test(priority = 7, groups = {"smoke", "organisation"}, dependsOnMethods = "clickNext")
    public void fillAddress() {
        log.info("Step 7: Fill Address");
        page.enterAddress1(faker.address().streetAddress());
        page.enterAddress2(faker.address().secondaryAddress());
        page.selectState("Delhi");
        page.selectDistrict();
        page.selectAreaUrban();
        page.selectLocalBody();
        page.enterPincode("110001");
        page.enterCity(faker.address().city());
        log.info("✅ Address filled");
    }

    @Test(priority = 8, groups = {"smoke", "organisation"}, dependsOnMethods = "clickNext")
    public void validateContact() {
        log.info("Step 8: Validate Contact");
        Assert.assertTrue(page.validateContactSection());
        log.info("✅ Contact validated");
    }

    @Test(priority = 9, groups = {"smoke", "organisation"}, dependsOnMethods = "clickNext")
    public void uploadDoc() {
        log.info("Step 9: Upload Document");
        page.uploadDocument();
        log.info("✅ Document uploaded");
    }

    @Test(priority = 10, groups = {"smoke", "organisation"}, dependsOnMethods = "uploadDoc")
    public void previewAndSubmit() {
        log.info("Step 10: Preview and Submit");
        page.clickPreview();
        page.agreeToTerms();
        page.downloadPDF();
        page.finalSubmit();
        Assert.assertTrue(page.isSubmissionSuccessful());
        log.info("✅ Organisation submitted");
    }
}
