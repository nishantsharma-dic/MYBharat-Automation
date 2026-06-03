package com.mybharat.tests.org;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.org.CreateYouthClubPage;
import com.mybharat.pages.youth.LoginPage;
import com.mybharat.pages.youth.LogoutPage;
import com.mybharat.utils.ConfigReader;

/**
 * CreateYouthClubTest — Creates a Youth Club organization, then member accepts invite.
 *
 * Flow: Login → Navigate → About → Basic Info → Affiliation → Address → Infrastructure
 *       → Financial → Activities → Membership → Establishment → Declaration → Preview → Submit
 *       → Save to Partner Excel → Logout → Login as 2nd member → Accept popup → Logout
 */
@Listeners(TestListeners.class)
public class CreateYouthClubTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(CreateYouthClubTest.class);
    private LoginPage loginPage;
    private LogoutPage logoutPage;
    private CreateYouthClubPage createOrgPage;
    private String loginEmail;
    private String youthClubName;

    /** Store member emails for accept flow */
    private List<String> memberEmails = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
        createOrgPage = new CreateYouthClubPage(driver);

        // Read a RANDOM user from Youth_<env>.xlsx UserData sheet for login (OTP)
        ConfigReader cfg = new ConfigReader();
        String env = cfg.getEnv();
        String youthPath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";
        try (FileInputStream fis = new FileInputStream(youthPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("UserData");
            if (sheet == null) sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            // Pick a random row (skip header row 0)
            int randomRow = lastRow > 1
                    ? 1 + new java.util.Random().nextInt(lastRow)
                    : lastRow;
            Row row = sheet.getRow(randomRow);
            // Fallback to last row if random row is null
            if (row == null || row.getCell(0) == null) {
                row = sheet.getRow(lastRow);
            }
            loginEmail = row.getCell(0).getStringCellValue().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Youth_" + env + ".xlsx: " + e.getMessage(), e);
        }
        log.info("[SETUP] Login email (random): {}", loginEmail);
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
        youthClubName = "Youth Club Automation " + System.currentTimeMillis() % 10000;
        createOrgPage.enterName(youthClubName);
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

        // Read fresh members from Youth_<env>.xlsx "YouthClubMembers" sheet
        // Only pick the 6 most recently registered (highest rohank numbers)
        String youthPath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";

        try (FileInputStream fis = new FileInputStream(youthPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("YouthClubMembers");
            if (sheet != null) {
                // Collect all rohank emails with their numbers
                java.util.TreeMap<Integer, String> emailsByNumber = new java.util.TreeMap<>();
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) continue;
                    String email = row.getCell(0).getStringCellValue().trim();
                    if (email.startsWith("rohank") && email.contains("@")) {
                        try {
                            int num = Integer.parseInt(email.replace("rohank", "").split("@")[0]);
                            emailsByNumber.put(num, email);
                        } catch (NumberFormatException e) { /* skip */ }
                    }
                }

                // Take the last 6 (highest numbers = most recently registered)
                java.util.List<String> latest = new java.util.ArrayList<>(emailsByNumber.descendingMap().values());
                int count = Math.min(6, latest.size());
                for (int i = 0; i < count; i++) {
                    String email = latest.get(i);
                    if (!email.equals(loginEmail)) {
                        memberEmails.add(email);
                    }
                }
                log.info("Read {} fresh member emails (newest rohank numbers)", memberEmails.size());
            }
        } catch (Exception e) {
            log.warn("YouthClubMembers sheet not found: {}", e.getMessage());
        }

        // Ensure enough emails
        while (memberEmails.size() < 10) {
            memberEmails.add("rohank" + (memberEmails.size() + 100) + "@yopmail.com");
        }

        String[] emails = memberEmails.toArray(new String[0]);
        int addedCount = createOrgPage.addMembers(emails);
        Assert.assertTrue(addedCount >= 6, "Only " + addedCount + "/6 members added and verified");
    }

    @Test(priority = 13, dependsOnMethods = "step8_address", retryAnalyzer = Retry.class)
    public void step13_establishment() {
        createOrgPage.selectRegistered("No");
        createOrgPage.setDateOfEstablishment();
        createOrgPage.selectMoA("No");
    }

    @Test(priority = 14, dependsOnMethods = "step13_establishment", retryAnalyzer = Retry.class)
    public void step14_declarationAndPreview() {
        createOrgPage.clickDeclarationCheckbox();
        createOrgPage.clickPreview();
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

    // =========================================================================
    // STEP 17: Save creator info to Partner_<env>.xlsx
    // =========================================================================

    @Test(priority = 17, dependsOnMethods = "step16_goToProfile", retryAnalyzer = Retry.class)
    public void step17_saveToPartnerExcel() {
        ConfigReader cfg = new ConfigReader();
        String env = cfg.getEnv();
        String filePath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Partner_" + env + ".xlsx";

        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try {
            Workbook workbook;
            if (file.exists() && file.length() > 0) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
            }

            // Get or create sheet
            Sheet sheet = workbook.getSheet("YouthClubData");
            if (sheet == null) {
                sheet = workbook.createSheet("YouthClubData");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Youth Club Name");
                header.createCell(1).setCellValue("Creator Email");
            }

            // Append new entry
            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(youthClubName != null ? youthClubName : "Youth Club Automation");
            row.createCell(1).setCellValue(loginEmail);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            log.info("✅ Saved to Partner_{}.xlsx: {} | {}", env, youthClubName, loginEmail);
        } catch (Exception e) {
            log.error("Failed to save to Partner Excel: {}", e.getMessage());
        }
    }

    // =========================================================================
    // STEP 18: Logout after creating Youth Club
    // =========================================================================

    @Test(priority = 18, dependsOnMethods = "step17_saveToPartnerExcel", retryAnalyzer = Retry.class)
    public void step18_logoutAfterCreate() throws Exception {
        log.info("═══ Logging out after Youth Club creation ═══");
        Thread.sleep(2000);

        // Verify browser session is alive
        try {
            driver.getCurrentUrl();
        } catch (Exception e) {
            log.warn("Browser session appears invalid — refreshing driver from ThreadLocal");
            this.driver = getDriver();
        }

        // Navigate to organizations page where user-options logout works
        ConfigReader cfg = new ConfigReader();
        driver.get(cfg.getUrl() + "/mybharat_organizations");
        Thread.sleep(4000);
        loginPage.closePopupIfPresent();
        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

        // Click user circle icon
        WebElement userIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[@id='user-options']")));
        js.executeScript("arguments[0].click();", userIcon);
        Thread.sleep(1500);

        // Click "Log Out" button
        WebElement logoutBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@class,'firebase-profile-logout-btn')]")));
        js.executeScript("arguments[0].click();", logoutBtn);
        Thread.sleep(3000);

        log.info("✅ Logged out successfully");
    }

    // =========================================================================
    // STEP 19: SuperAdmin login and approve the Youth Club
    // =========================================================================

    @Test(priority = 19, dependsOnMethods = "step18_logoutAfterCreate", retryAnalyzer = Retry.class)
    public void step19_superAdminApprove() throws Exception {
        log.info("═══ SuperAdmin: Approve Youth Club ═══");

        com.mybharat.pages.superadmin.SuperAdminLoginPage superAdminLogin =
                new com.mybharat.pages.superadmin.SuperAdminLoginPage(driver);
        com.mybharat.pages.superadmin.OrgApprovalPage approvalPage =
                new com.mybharat.pages.superadmin.OrgApprovalPage(driver);

        // Step 1: Login as SuperAdmin
        superAdminLogin.loginAsSuperAdmin();
        Assert.assertTrue(superAdminLogin.isLoginSuccessful(), "SuperAdmin login failed");
        log.info("✅ SuperAdmin logged in");

        // Step 2: Approve the Youth Club created in this run
        approvalPage.approveYouthClub(youthClubName != null ? youthClubName : "Youth Club Automation");

        // Step 3: Verify approval
        Assert.assertTrue(approvalPage.isApprovalSuccessful(), "Youth Club approval failed");
        log.info("✅ Youth Club approved: {}", youthClubName);
    }
}
