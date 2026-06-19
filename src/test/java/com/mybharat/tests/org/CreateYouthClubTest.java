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

        // Read last user from Youth_<env>.xlsx UserData sheet for login (OTP)
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
        // Use members registered in THIS run (from RegisterMembersForYouthClubTest)
        List<String> freshEmails = RegisterMembersForYouthClubTest.getRegisteredEmails();
        if (!freshEmails.isEmpty()) {
            memberEmails.clear();
            memberEmails.addAll(freshEmails);
            log.info("Using {} members from current run registration", memberEmails.size());
        } else {
            // Fallback: read from Excel (only yc format)
            log.warn("No emails from current run — reading from Excel");
            ConfigReader cfg = new ConfigReader();
            String env = cfg.getEnv();
            String youthPath = System.getProperty("user.dir") + File.separator
                    + "resources" + File.separator + "Youth_" + env + ".xlsx";

            try (FileInputStream fis = new FileInputStream(youthPath);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheet("YouthClubMembers");
                if (sheet != null) {
                    java.util.TreeMap<Integer, String> emailsByNumber = new java.util.TreeMap<>();
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null || row.getCell(0) == null) continue;
                        String email = row.getCell(0).getStringCellValue().trim();
                        if (email.startsWith("yc") && email.contains("@")) {
                            try {
                                int num = Integer.parseInt(email.replace("yc", "").split("@")[0]);
                                emailsByNumber.put(num, email);
                            } catch (NumberFormatException e) { /* skip */ }
                        }
                    }
                    java.util.List<String> latest = new java.util.ArrayList<>(emailsByNumber.descendingMap().values());
                    int count = Math.min(6, latest.size());
                    for (int i = 0; i < count; i++) {
                        String email = latest.get(i);
                        if (!email.equals(loginEmail)) {
                            memberEmails.add(email);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("YouthClubMembers sheet not found: {}", e.getMessage());
            }
        }

        // Ensure enough emails
        while (memberEmails.size() < 10) {
            memberEmails.add("yc" + String.format("%06d", memberEmails.size() + 100) + "@maildrop.cc");
        }

        log.info("Members to add: {}", memberEmails.subList(0, Math.min(6, memberEmails.size())));
        String[] emails = memberEmails.toArray(new String[0]);
        int addedCount = createOrgPage.addMembers(emails);
        Assert.assertTrue(addedCount >= 6, "Only " + addedCount + "/6 members added (5 OTP verified + 1 pending)");
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
    // STEP 19: Login as Member 6 → Accept Youth Club invitation → Logout
    // =========================================================================

    @Test(priority = 19, dependsOnMethods = "step18_logoutAfterCreate", retryAnalyzer = Retry.class)
    public void step19_member6AcceptInvite() throws Exception {
        log.info("═══ Member 6: Login (Maildrop OTP) and Accept Youth Club Invitation ═══");

        // Wait for backend to process the Youth Club invitation (race condition on fast servers)
        log.info("  Waiting 10s for backend to process invitation...");
        Thread.sleep(10000);

        // Get the 6th member email (last one added — the one without OTP verify)
        String member6Email = null;
        if (memberEmails.size() >= 6) {
            member6Email = memberEmails.get(5);
        } else if (memberEmails.size() > 0) {
            member6Email = memberEmails.get(memberEmails.size() - 1);
        }
        Assert.assertNotNull(member6Email, "Member 6 email not found");
        log.info("Member 6 email: {}", member6Email);

        ConfigReader cfg = new ConfigReader();
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String mailbox = member6Email.split("@")[0];

        // Step A: Navigate and open login modal
        driver.get(cfg.getUrl());
        Thread.sleep(5000);
        loginPage.closePopupIfPresent();
        Thread.sleep(1000);

        // Check if still logged in (previous logout might have failed on server)
        boolean alreadyLoggedIn = false;
        try {
            WebElement userMenu = driver.findElement(By.xpath(
                    "//a[@id='user-options'] | //button[contains(@class,'rounded-full')]"));
            if (userMenu.isDisplayed()) {
                alreadyLoggedIn = true;
                log.warn("Still logged in — logging out first");
                js.executeScript("arguments[0].click();", userMenu);
                Thread.sleep(1500);
                WebElement logoutLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[contains(@class,'firebase-profile-logout-btn')] | //a[contains(text(),'Log Out')]")));
                js.executeScript("arguments[0].click();", logoutLink);
                Thread.sleep(3000);
                driver.get(cfg.getUrl());
                Thread.sleep(5000);
                loginPage.closePopupIfPresent();
                Thread.sleep(1000);
            }
        } catch (Exception e) { /* not logged in — good */ }

        // Click Sign In
        WebElement signIn = null;
        try {
            signIn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[normalize-space()='Sign In']")));
        } catch (Exception e) {
            // Fallback: try alternative Sign In locators
            signIn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@class,'sign-in')] | //a[contains(text(),'Sign In')] | //*[normalize-space()='Sign In']")));
        }
        try { signIn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", signIn); }
        Thread.sleep(1500);

        // Step B: Enter email
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='otp_login_header']")));
        emailInput.clear();
        emailInput.sendKeys(member6Email);
        Thread.sleep(500);

        // Step C: Check consent
        try {
            WebElement consent = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#consentCheck1")));
            if (!consent.isSelected()) { try { consent.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", consent); } }
        } catch (Exception e) { /* skip */ }

        // Step D: Capture prevCount BEFORE clicking Login
        int prevCount = 0;
        try (org.apache.hc.client5.http.impl.classic.CloseableHttpClient preClient =
                org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {
            org.apache.hc.client5.http.classic.methods.HttpPost preReq =
                    new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
            preReq.setHeader("Content-Type", "application/json");
            preReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                    "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id } }\"}"));
            String preResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                    preClient.execute(preReq).getEntity());
            prevCount = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(preResp).path("data").path("inbox").size();
        } catch (Exception e) { /* default 0 */ }
        log.info("  prevCount={} for {}", prevCount, mailbox);

        // Step E: Click Login button (same as LoginPage uses)
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.firebase-user-sentOtp-btn")));
        try { loginBtn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", loginBtn); }
        log.info("  Login button clicked — OTP requested");

        // Step F: Wait and fetch OTP from Maildrop (SAME as addMembers pattern)
        Thread.sleep(5000);
        String otp = "";
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (int otpAttempt = 1; otpAttempt <= 8; otpAttempt++) {
            try (org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient =
                    org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {

                // Get inbox
                org.apache.hc.client5.http.classic.methods.HttpPost listReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                listReq.setHeader("Content-Type", "application/json");
                listReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id subject date } }\"}"));
                String listResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        httpClient.execute(listReq).getEntity());

                com.fasterxml.jackson.databind.JsonNode inbox = mapper.readTree(listResp).path("data").path("inbox");

                if (inbox.size() <= prevCount) {
                    log.info("  No NEW email yet (attempt {}/8, count={})", otpAttempt, inbox.size());
                    Thread.sleep(3000);
                    continue;
                }

                // Get the NEWEST message (index 0)
                String msgId = inbox.get(0).get("id").asText();
                org.apache.hc.client5.http.classic.methods.HttpPost msgReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                msgReq.setHeader("Content-Type", "application/json");
                msgReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ message(mailbox:\\\"" + mailbox + "\\\", id:\\\"" + msgId + "\\\") { id html data } }\"}"));
                String msgResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        httpClient.execute(msgReq).getEntity());

                com.fasterxml.jackson.databind.JsonNode msg = mapper.readTree(msgResp).path("data").path("message");
                String body = msg.has("html") && !msg.get("html").isNull()
                        ? msg.get("html").asText() : msg.has("data") ? msg.get("data").asText() : "";

                if (!body.isEmpty()) {
                    // Pattern 1: <strong>XXXXXX</strong>
                    java.util.regex.Matcher mStrong = java.util.regex.Pattern.compile("<strong>(\\d{6})</strong>").matcher(body);
                    if (mStrong.find()) { otp = mStrong.group(1); break; }
                    // Pattern 2: Your OTP: XXXXXX
                    java.util.regex.Matcher mYourOtp = java.util.regex.Pattern.compile("Your OTP:\\s*(\\d{6})").matcher(body);
                    if (mYourOtp.find()) { otp = mYourOtp.group(1); break; }
                    // Pattern 3: OTP is XXXXXX
                    java.util.regex.Matcher mIs = java.util.regex.Pattern.compile("(?:OTP|is)\\s+(?:<[^>]+>)*(\\d{6})").matcher(body);
                    if (mIs.find()) { otp = mIs.group(1); break; }
                }
                Thread.sleep(3000);
            } catch (Exception apiEx) {
                log.warn("  Maildrop API error (attempt {}/8): {}", otpAttempt, apiEx.getMessage());
                Thread.sleep(3000);
            }
        }
        Assert.assertFalse(otp.isEmpty(), "Could not fetch OTP from Maildrop for Member 6 login");
        log.info("  ✅ OTP fetched: {}", otp);

        // Step G: Enter OTP and verify (login form — uses #otp-field-3 and #btn-otp-verify-header)
        WebElement otpField = null;
        try {
            otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#otp-field-3")));
        } catch (Exception e) {
            otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id,'otp-field')] | //input[contains(@class,'otp')]")));
        }
        otpField.clear();
        otpField.sendKeys(otp);
        Thread.sleep(500);

        WebElement verifyBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='btn-otp-verify-header']")));
        try { verifyBtn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", verifyBtn); }
        Thread.sleep(3000);

        Assert.assertTrue(loginPage.isLoginSuccessful(), "Member 6 login failed after OTP");
        log.info("✅ Member 6 logged in: {}", member6Email);

        // Step H: Handle "Update the newly introduced fields" popup (Caste, PwD, Aapda Mitra)
        // This popup appears after login — just click Submit (fields are pre-filled)
        Thread.sleep(3000);
        try {
            WebElement submitPopup = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Submit']")));
            js.executeScript("arguments[0].click();", submitPopup);
            log.info("✅ 'Update newly introduced fields' popup submitted");
            Thread.sleep(3000);
        } catch (Exception e) {
            log.info("No 'Update fields' popup — continuing");
        }

        // After Update fields popup, navigate to profile to trigger the Accept popup
        driver.get(cfg.getUrl() + "/reports/public_profile");
        Thread.sleep(5000);
        loginPage.closePopupIfPresent();
        Thread.sleep(2000);

        // Step I: Wait for "Confirm your participation" popup and click Accept
        Thread.sleep(3000);
        try {
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Accept')]")));
            js.executeScript("arguments[0].click();", acceptBtn);
            log.info("✅ Clicked Accept — invitation accepted");
            Thread.sleep(2000);
        } catch (Exception e) {
            // Try refreshing the profile page — popup might appear on reload
            log.warn("Accept popup not found — refreshing profile page");
            driver.navigate().refresh();
            Thread.sleep(5000);
            try {
                WebElement acceptAlt = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Accept')] | //button[normalize-space()='Accept']")));
                js.executeScript("arguments[0].click();", acceptAlt);
                log.info("✅ Accept clicked after refresh");
                Thread.sleep(2000);
            } catch (Exception e2) {
                log.error("❌ Accept button not found even after refresh: {}", e2.getMessage());
                throw e2;
            }
        }

        // Step J: Logout Member 6
        Thread.sleep(2000);
        driver.get(cfg.getUrl() + "/mybharat_organizations");
        Thread.sleep(4000);
        loginPage.closePopupIfPresent();
        Thread.sleep(1000);

        WebElement userIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[@id='user-options']")));
        js.executeScript("arguments[0].click();", userIcon);
        Thread.sleep(1500);

        WebElement logoutBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@class,'firebase-profile-logout-btn')]")));
        js.executeScript("arguments[0].click();", logoutBtn);
        Thread.sleep(3000);

        log.info("✅ Member 6 logged out after accepting invitation");
    }

    // =========================================================================
    // STEP 20: SuperAdmin login and approve the Youth Club
    // =========================================================================

    @Test(priority = 20, dependsOnMethods = "step19_member6AcceptInvite", retryAnalyzer = Retry.class)
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
