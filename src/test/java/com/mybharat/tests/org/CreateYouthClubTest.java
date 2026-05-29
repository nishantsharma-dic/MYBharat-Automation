package com.mybharat.tests.org;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.org.CreateYouthClubPage;
import com.mybharat.pages.youth.LoginPage;
import com.mybharat.utils.ConfigReader;

/**
 * CreateYouthClubTest — Creates a Youth Club organization.
 *
 * Flow: Login → Navigate → About (Banner+Logo+Name+About+Next)
 *       → Basic Info (Category+SubCategory+Name+Abbreviation+NodalDesignation)
 *       → Affiliation → Address → Infrastructure → Financial
 *       → Activities → Membership → Establishment → Declaration → Preview → Submit
 */
@Listeners(TestListeners.class)
public class CreateYouthClubTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(CreateYouthClubTest.class);
    private LoginPage loginPage;
    private CreateYouthClubPage createOrgPage;
    private String loginEmail;
    private String loginPassword;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        createOrgPage = new CreateYouthClubPage(driver);

        // Always read last user from Youth_prod.xlsx for login (OTP)
        ConfigReader cfg = new ConfigReader();
        String env = cfg.getEnv();
        String youthPath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";
        try (FileInputStream fis = new FileInputStream(youthPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("UserData");
            if (sheet == null) sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(sheet.getLastRowNum());
            loginEmail = row.getCell(0).getStringCellValue().trim();
            loginPassword = "";
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Youth_" + env + ".xlsx: " + e.getMessage(), e);
        }
        log.info("[SETUP] Login email: {}", loginEmail);
    }

    @Test(priority = 1, retryAnalyzer = Retry.class)
    public void step1_login() throws Exception {
        loginPage.login(loginEmail, null);
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed");
        log.info("✅ Login passed: {}", loginEmail);
    }

    @Test(priority = 2, dependsOnMethods = "step1_login", retryAnalyzer = Retry.class)
    public void step2_navigateToCreateOrg() {
        createOrgPage.navigateToCreateOrg();
        Assert.assertTrue(createOrgPage.isPageLoaded(), "Create Org page not loaded");
    }

    @Test(priority = 3, dependsOnMethods = "step2_navigateToCreateOrg", retryAnalyzer = Retry.class)
    public void step3_aboutSection() {
        createOrgPage.uploadBanner();
        createOrgPage.uploadLogo();
        createOrgPage.enterAboutText("This Youth Club engages youth in community development, sports, culture, and skill training.");
        createOrgPage.clickNext();
    }

    @Test(priority = 4, dependsOnMethods = "step3_aboutSection", retryAnalyzer = Retry.class)
    public void step4_selectCategory() {
        createOrgPage.selectCategory("Not For Profit");
    }

    @Test(priority = 5, dependsOnMethods = "step4_selectCategory", retryAnalyzer = Retry.class)
    public void step5_selectSubCategory() {
        createOrgPage.selectSubCategory("Youth Club");
    }

    @Test(priority = 6, dependsOnMethods = "step5_selectSubCategory", retryAnalyzer = Retry.class)
    public void step6_fillBasicInfo() {
        createOrgPage.enterName("Youth Club Automation " + System.currentTimeMillis() % 10000);
        createOrgPage.enterAbbreviation("YCA");
        createOrgPage.selectNodalDesignation("President");
    }

    @Test(priority = 7, dependsOnMethods = "step5_selectSubCategory", retryAnalyzer = Retry.class)
    public void step7_affiliation() {
        createOrgPage.selectAffiliation("No");
        createOrgPage.clickAgreeCheckbox();
    }

    @Test(priority = 8, dependsOnMethods = "step6_fillBasicInfo", retryAnalyzer = Retry.class)
    public void step8_address() {
        createOrgPage.enterAddress1("123 Youth Club Building, Sector 10");
        createOrgPage.enterAddress2("Near Community Center, Block A");
        createOrgPage.selectState("DELHI");
        createOrgPage.selectDistrict();
        createOrgPage.selectAreaUrban();
        createOrgPage.selectLocalBody();
        createOrgPage.enterPincode("110001");
    }

    @Test(priority = 9, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step9_infrastructure() {
        createOrgPage.selectPhysicalOfficeNo();
    }

    @Test(priority = 10, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step10_financial() {
        createOrgPage.selectFinancialAssistance("None");
        createOrgPage.selectBankAccount("No");
    }

    @Test(priority = 11, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step11_activities() {
        createOrgPage.selectActivities("Arts, Culture & Heritage", "Community Service & Social Action");
        createOrgPage.selectSubActivities("Craft Workshops", "Cleanliness Drives");
    }

    @Test(priority = 12, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step12_membership() {
        ConfigReader cfg = new ConfigReader();
        String env = cfg.getEnv();
        String youthPath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";
        java.util.List<String> allEmails = new java.util.ArrayList<>();
        try (FileInputStream fis = new FileInputStream(youthPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("UserData");
            if (sheet == null) sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String email = row.getCell(0).getStringCellValue().trim();
                    if (!email.isEmpty() && !email.equals(loginEmail)) {
                        allEmails.add(email);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read Youth Excel: {}", e.getMessage());
        }
        // Ensure enough emails (provide extras in case some are already invited)
        while (allEmails.size() < 10) {
            allEmails.add("member" + (allEmails.size() + 1) + "@yopmail.com");
        }
        // Take last 10 (extras for retry if member already invited)
        int start = Math.max(0, allEmails.size() - 10);
        String[] emails = new String[10];
        for (int i = 0; i < 10; i++) emails[i] = allEmails.get(start + i);
        createOrgPage.addMembers(emails);
    }

    @Test(priority = 13, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step13_establishment() {
        createOrgPage.selectRegistered("No");
        createOrgPage.setDateOfEstablishment();
        createOrgPage.selectMoA("No");
    }

    @Test(priority = 14, dependsOnMethods = "step13_establishment", retryAnalyzer = Retry.class)
    public void step14_declarationAndPreview() {
        // Click declaration checkbox: //ion-checkbox[@formcontrolname='declarationAccepted']
        createOrgPage.clickDeclarationCheckbox();
        createOrgPage.clickPreview();
    }

    private void clickDeclarationCheckbox() {
        // This is handled by createOrgPage.clickAgreeCheckbox() which uses AGREE_CHECKBOX locator
    }

    @Test(priority = 15, dependsOnMethods = "step14_declarationAndPreview", retryAnalyzer = Retry.class)
    public void step15_submit() {
        createOrgPage.finalSubmit();
        Assert.assertTrue(createOrgPage.isSubmissionSuccessful(), "Submission failed");
    }

    @Test(priority = 16, dependsOnMethods = "step15_submit", retryAnalyzer = Retry.class)
    public void step16_goToProfile() {
        createOrgPage.clickGoToProfile();
    }
}
