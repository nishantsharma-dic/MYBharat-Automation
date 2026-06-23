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
 * Reports as 2 test cases in Extent Report:
 *   1. step15_submit — Full creation flow (Login → Create → Submit → Member6 Accept → Logout)
 *   2. step19_superAdminApprove — SuperAdmin login and approve
 *
 * If any internal step fails, the error message shows WHICH step failed.
 */
@Listeners(TestListeners.class)
public class CreateYouthClubTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(CreateYouthClubTest.class);
    private LoginPage loginPage;
    private LogoutPage logoutPage;
    private CreateYouthClubPage createOrgPage;
    private String loginEmail;
    private String youthClubName;
    private List<String> memberEmails = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
        createOrgPage = new CreateYouthClubPage(driver);

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

    // =========================================================================
    // TEST CASE 1: Create Youth Club (full flow)
    // =========================================================================

    @Test(priority = 1, retryAnalyzer = Retry.class)
    public void step15_submit() throws Exception {
        log.info("═══ CREATE YOUTH CLUB — Full Flow ═══");

        // Step 1: Login
        log.info("▶ Step 1: Login");
        loginPage.login(loginEmail, null);
        Assert.assertTrue(loginPage.isLoginSuccessful(), "[Step 1] Login failed");
        log.info("✅ Login passed: {}", loginEmail);

        // Step 2: Navigate to Create Org
        log.info("▶ Step 2: Navigate to Create Org");
        createOrgPage.navigateToCreateOrg();
        Assert.assertTrue(createOrgPage.isPageLoaded(), "[Step 2] Create Org page not loaded");

        // Step 3: About Section
        log.info("▶ Step 3: About Section");
        createOrgPage.uploadBanner();
        createOrgPage.uploadLogo();
        createOrgPage.enterAboutText("We are a community-driven youth organization focused on empowering young people through sports, cultural activities, skill development workshops, and social service initiatives. Our club organizes regular events to promote leadership, teamwork, and civic responsibility among the youth of our community.");
        createOrgPage.clickNext();

        // Step 4-5: Category
        log.info("▶ Step 4-5: Category Selection");
        createOrgPage.selectCategory("Not For Profit");
        createOrgPage.selectSubCategory("Youth Club");

        // Step 6: Basic Info
        log.info("▶ Step 6: Basic Info");
        // Generate unique realistic Youth Club name (different every run)
        String[] clubNames = {
            "Yuva Shakti", "Nai Disha", "Pragati", "Jagriti", "Udaan", "Tarun Bharat",
            "Navjyoti", "Sahyog", "Umang", "Sankalp", "Prerana", "Vikas"
        };
        String[] areas = {"Nagar", "Colony", "Vihar", "Puram", "Enclave", "Block", "Sector", "Ward", "Mohalla", "Basti"};
        String[] suffixes = {"Alpha", "Beta", "Delta", "Sigma", "Nova", "Apex", "Prime", "Elite", "Core", "Rise"};
        java.util.Random rnd = new java.util.Random();
        youthClubName = clubNames[rnd.nextInt(clubNames.length)] + " " 
                + areas[rnd.nextInt(areas.length)] + " " 
                + suffixes[rnd.nextInt(suffixes.length)] + " Youth Club";
        createOrgPage.enterName(youthClubName);
        createOrgPage.enterAbbreviation("YSYC");
        createOrgPage.selectNodalDesignation("President");

        // Step 7: Affiliation
        log.info("▶ Step 7: Affiliation");
        createOrgPage.selectAffiliation("No");
        createOrgPage.clickAgreeCheckbox();

        // Step 8: Address
        log.info("▶ Step 8: Address");
        createOrgPage.enterAddress1("Ward No 12, Community Hall Building");
        createOrgPage.enterAddress2("Near Government School, Main Road");
        createOrgPage.selectState("DELHI");
        createOrgPage.selectDistrict();
        createOrgPage.selectAreaUrban();
        createOrgPage.selectLocalBody();
        createOrgPage.enterPincode("110001");

        // Step 9: Infrastructure
        log.info("▶ Step 9: Infrastructure");
        createOrgPage.selectPhysicalOfficeNo();

        // Step 10: Financial
        log.info("▶ Step 10: Financial");
        createOrgPage.selectFinancialAssistance("None");
        createOrgPage.selectBankAccount("No");

        // Step 11: Activities
        log.info("▶ Step 11: Activities");
        createOrgPage.selectActivities("Arts, Culture & Heritage", "Community Service & Social Action");
        createOrgPage.selectSubActivities("Craft Workshops", "Cleanliness Drives");

        // Step 12: Membership
        log.info("▶ Step 12: Membership — Add 6 Members");
        loadMemberEmails();
        Assert.assertTrue(memberEmails.size() >= 6,
                "[Step 12] Not enough registered members: got " + memberEmails.size() + " but need 6");
        String[] emails = memberEmails.toArray(new String[0]);
        int addedCount = createOrgPage.addMembers(emails);
        Assert.assertTrue(addedCount >= 6, "[Step 12] Only " + addedCount + "/6 members added");
        // Step 13: Establishment
        log.info("▶ Step 13: Establishment");
        createOrgPage.selectRegistered("No");
        createOrgPage.setDateOfEstablishment();
        createOrgPage.selectMoA("No");

        // Step 14: Declaration + Preview
        log.info("▶ Step 14: Declaration & Preview");
        createOrgPage.clickDeclarationCheckbox();
        createOrgPage.clickPreview();

        // Step 15: Submit
        log.info("▶ Step 15: SUBMIT");
        createOrgPage.finalSubmit();
        Assert.assertTrue(createOrgPage.isSubmissionSuccessful(), "[Step 15] Submission failed");

        // Save Youth Club name to file for email report
        try {
            File reportsDir = new File(System.getProperty("user.dir") + File.separator + "reports");
            reportsDir.mkdirs();
            java.io.FileWriter fw = new java.io.FileWriter(reportsDir + File.separator + "youth_club_name.txt");
            fw.write(youthClubName);
            fw.close();
            log.info("Saved youth club name to reports/youth_club_name.txt: {}", youthClubName);
        } catch (Exception e) { log.warn("Could not save youth club name: {}", e.getMessage()); }

        // Mark used members in Excel as "Picked" with Youth Club name
        markMembersAsPicked();

        // Step 16: Go to Profile
        log.info("▶ Step 16: Go to Profile");
        createOrgPage.clickGoToProfile();

        // Step 17: Save to Partner Excel
        log.info("▶ Step 17: Save to Excel");
        saveToPartnerExcel();

        // Step 18: Logout
        log.info("▶ Step 18: Logout Creator");
        performLogout();

        // Step 19: Member 6 Accept Invite
        log.info("▶ Step 19: Member 6 Login + Accept Invite");
        member6AcceptInvite();

        log.info("═══ ✅ CREATE YOUTH CLUB — ALL STEPS PASSED ═══");
    }

    // =========================================================================
    // TEST CASE 2: SuperAdmin Approve
    // =========================================================================

    @Test(priority = 2, dependsOnMethods = "step15_submit", retryAnalyzer = Retry.class)
    public void step19_superAdminApprove() throws Exception {
        log.info("═══ SuperAdmin: Approve Youth Club ═══");

        com.mybharat.pages.superadmin.SuperAdminLoginPage superAdminLogin =
                new com.mybharat.pages.superadmin.SuperAdminLoginPage(driver);
        com.mybharat.pages.superadmin.OrgApprovalPage approvalPage =
                new com.mybharat.pages.superadmin.OrgApprovalPage(driver);

        superAdminLogin.loginAsSuperAdmin();
        Assert.assertTrue(superAdminLogin.isLoginSuccessful(), "SuperAdmin login failed");
        log.info("✅ SuperAdmin logged in");

        approvalPage.approveYouthClub(youthClubName != null ? youthClubName : "Youth Club Automation");
        Assert.assertTrue(approvalPage.isApprovalSuccessful(), "Youth Club approval failed");
        log.info("✅ Youth Club approved: {}", youthClubName);
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private void loadMemberEmails() {
        List<String> freshEmails = RegisterMembersForYouthClubTest.getRegisteredEmails();
        if (!freshEmails.isEmpty()) {
            memberEmails.clear();
            memberEmails.addAll(freshEmails);
            log.info("Using {} members from current run (static list)", memberEmails.size());
        } else {
            // Fallback: read from Excel BUT only pick emails from THIS run's startNumber range
            log.warn("Static list empty — reading from Excel (filtering by current run range)");
            int runStart = RegisterMembersForYouthClubTest.getStartNumber();
            int runEnd = runStart + 7; // Up to 8 registered (MEMBER_COUNT)
            log.info("Current run range: yco{} to yco{}", String.format("%05d", runStart), String.format("%05d", runEnd));

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
                        if (email.startsWith("yco") && email.contains("@")) {
                            try {
                                int num = Integer.parseInt(email.replace("yco", "").split("@")[0]);
                                // Only include emails from THIS run's range
                                if (num >= runStart && num <= runEnd) {
                                    emailsByNumber.put(num, email);
                                }
                            } catch (NumberFormatException e) { /* skip */ }
                        }
                    }
                    java.util.List<String> thisRun = new java.util.ArrayList<>(emailsByNumber.values());
                    for (int i = 0; i < Math.min(8, thisRun.size()); i++) {
                        if (!thisRun.get(i).equals(loginEmail)) memberEmails.add(thisRun.get(i));
                    }
                    log.info("Found {} emails from current run range in Excel", memberEmails.size());
                }
            } catch (Exception e) { log.warn("Excel read failed: {}", e.getMessage()); }
        }
    }

    private void markMembersAsPicked() {
        // Update YouthClubMembers sheet — mark used members with Youth Club name + "Picked"
        ConfigReader cfg = new ConfigReader();
        String env = cfg.getEnv();
        String filePath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            FileInputStream fis = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(fis);
            fis.close();

            Sheet sheet = wb.getSheet("YouthClubMembers");
            if (sheet == null) return;

            // Get the first 6 member emails that were actually used
            List<String> usedEmails = memberEmails.subList(0, Math.min(6, memberEmails.size()));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;
                String email = row.getCell(0).getStringCellValue().trim();
                if (usedEmails.contains(email)) {
                    // Column 2: Youth Club Name
                    if (row.getCell(1) == null) row.createCell(1);
                    row.getCell(1).setCellValue(youthClubName);
                    // Column 3: Status = Picked
                    if (row.getCell(2) == null) row.createCell(2);
                    row.getCell(2).setCellValue("Picked");
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
            fos.close();
            wb.close();
            log.info("✅ Marked {} members as 'Picked' for: {}", usedEmails.size(), youthClubName);
        } catch (Exception e) {
            log.warn("Failed to mark members in Excel: {}", e.getMessage());
        }
    }

    private void saveToPartnerExcel() {
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
            Sheet sheet = workbook.getSheet("YouthClubData");
            if (sheet == null) {
                sheet = workbook.createSheet("YouthClubData");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Youth Club Name");
                header.createCell(1).setCellValue("Creator Email");
            }
            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(youthClubName != null ? youthClubName : "Youth Club Automation");
            row.createCell(1).setCellValue(loginEmail);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            log.info("✅ Saved to Partner_{}.xlsx", env);
        } catch (Exception e) { log.error("Failed to save: {}", e.getMessage()); }
    }

    private void performLogout() {
        safeSleep(1000);
        try { driver.getCurrentUrl(); } catch (Exception e) { this.driver = getDriver(); }
        ConfigReader cfg = new ConfigReader();
        driver.get(cfg.getUrl() + "/mybharat_organizations");
        safeSleep(3000);
        loginPage.closePopupIfPresent();
        safeSleep(500);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        WebElement userIcon = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='user-options']")));
        js.executeScript("arguments[0].click();", userIcon);
        safeSleep(1000);
        WebElement logoutBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'firebase-profile-logout-btn')]")));
        js.executeScript("arguments[0].click();", logoutBtn);
        safeSleep(2000);
        log.info("✅ Logged out");
    }

    private void member6AcceptInvite() throws Exception {
        safeSleep(10000); // Wait for backend to process invitation

        String member6Email = memberEmails.size() >= 6 ? memberEmails.get(5) : memberEmails.get(memberEmails.size() - 1);
        Assert.assertNotNull(member6Email, "[Step 19] Member 6 email not found");
        log.info("Member 6: {}", member6Email);

        ConfigReader cfg = new ConfigReader();
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String mailbox = member6Email.split("@")[0];

        // Navigate and check if logged out
        driver.get(cfg.getUrl());
        safeSleep(4000);
        loginPage.closePopupIfPresent();
        safeSleep(500);

        // Force logout if still logged in
        try {
            WebElement userMenu = driver.findElement(By.xpath("//a[@id='user-options'] | //button[contains(@class,'rounded-full')]"));
            if (userMenu.isDisplayed()) {
                js.executeScript("arguments[0].click();", userMenu);
                safeSleep(1500);
                WebElement logoutLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'firebase-profile-logout-btn')] | //a[contains(text(),'Log Out')]")));
                js.executeScript("arguments[0].click();", logoutLink);
                safeSleep(2000);
                driver.get(cfg.getUrl());
                safeSleep(4000);
                loginPage.closePopupIfPresent();
                safeSleep(500);
            }
        } catch (Exception e) { /* not logged in */ }

        // Sign In - try multiple locators with 40s timeout (server page load varies)
        WebElement signIn = null;
        WebDriverWait longWait40 = new WebDriverWait(driver, Duration.ofSeconds(40));
        for (String loc : new String[]{
                "//span[normalize-space()='Sign In']",
                "//a[normalize-space()='Sign In']",
                "//*[normalize-space()='Sign In']"}) {
            try { signIn = longWait40.until(ExpectedConditions.elementToBeClickable(By.xpath(loc))); break; }
            catch (Exception ex) { /* try next */ }
        }
        if (signIn == null) throw new RuntimeException("[Step 19] Sign In not found after 40s");
        try { signIn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", signIn); }
        safeSleep(1500);

        // Enter email + consent
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='otp_login_header']")));
        emailInput.clear();
        emailInput.sendKeys(member6Email);
        safeSleep(500);
        try {
            WebElement consent = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#consentCheck1")));
            if (!consent.isSelected()) { try { consent.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", consent); } }
        } catch (Exception e) { /* skip */ }

        // Click Login
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.firebase-user-sentOtp-btn")));
        try { loginBtn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", loginBtn); }
        log.info("  Login button clicked");

        // Fetch OTP from Maildrop
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        safeSleep(10000);
        String otp = fetchOTPFromMaildrop(mailbox, mapper, 10);
        if (otp.isEmpty()) {
            try {
                WebElement resendBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(),'Resend OTP')]")));
                js.executeScript("arguments[0].click();", resendBtn);
                safeSleep(10000);
                otp = fetchOTPFromMaildrop(mailbox, mapper, 10);
            } catch (Exception e) { /* resend not found */ }
        }
        Assert.assertFalse(otp.isEmpty(), "[Step 19] Could not fetch OTP for Member 6");
        log.info("  OTP: {}", otp);

        // Enter OTP + Verify
        WebElement otpField = null;
        try { otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#otp-field-3"))); }
        catch (Exception e) { otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[contains(@id,'otp-field')]"))); }
        otpField.clear();
        otpField.sendKeys(otp);
        safeSleep(500);
        WebElement verifyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='btn-otp-verify-header']")));
        try { verifyBtn.click(); } catch (Exception e) { js.executeScript("arguments[0].click();", verifyBtn); }
        safeSleep(3000);
        Assert.assertTrue(loginPage.isLoginSuccessful(), "[Step 19] Member 6 login failed");

        // Handle Update fields popup
        safeSleep(3000);
        try {
            WebElement submitPopup = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Submit']")));
            js.executeScript("arguments[0].click();", submitPopup);
            safeSleep(3000);
        } catch (Exception e) { /* no popup */ }

        // Navigate to profile for Accept popup
        driver.get(cfg.getUrl() + "/reports/public_profile");
        safeSleep(4000);
        loginPage.closePopupIfPresent();
        safeSleep(2000);

        // Click Accept
        try {
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Accept')]")));
            js.executeScript("arguments[0].click();", acceptBtn);
            log.info("✅ Accept clicked");
            safeSleep(1500);
        } catch (Exception e) {
            driver.navigate().refresh();
            safeSleep(5000);
            WebElement acceptAlt = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Accept')] | //button[normalize-space()='Accept']")));
            js.executeScript("arguments[0].click();", acceptAlt);
            safeSleep(1500);
        }

        // Logout Member 6
        safeSleep(1000);
        driver.get(cfg.getUrl() + "/mybharat_organizations");
        safeSleep(3000);
        loginPage.closePopupIfPresent();
        safeSleep(500);
        WebElement userIcon = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='user-options']")));
        js.executeScript("arguments[0].click();", userIcon);
        safeSleep(1000);
        WebElement logoutBtn2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'firebase-profile-logout-btn')]")));
        js.executeScript("arguments[0].click();", logoutBtn2);
        safeSleep(2000);
        log.info("✅ Member 6 logged out");
    }

    private String fetchOTPFromMaildrop(String mailbox, com.fasterxml.jackson.databind.ObjectMapper mapper, int maxTries) {
        String otp = "";
        for (int attempt = 1; attempt <= maxTries; attempt++) {
            try (org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient =
                    org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {
                org.apache.hc.client5.http.classic.methods.HttpPost listReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                listReq.setHeader("Content-Type", "application/json");
                listReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id subject date } }\"}"));
                String listResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(httpClient.execute(listReq).getEntity());
                com.fasterxml.jackson.databind.JsonNode inbox = mapper.readTree(listResp).path("data").path("inbox");
                if (inbox.size() == 0) { safeSleep(3000); continue; }
                String msgId = inbox.get(0).get("id").asText();
                org.apache.hc.client5.http.classic.methods.HttpPost msgReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                msgReq.setHeader("Content-Type", "application/json");
                msgReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ message(mailbox:\\\"" + mailbox + "\\\", id:\\\"" + msgId + "\\\") { id html data } }\"}"));
                String msgResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(httpClient.execute(msgReq).getEntity());
                com.fasterxml.jackson.databind.JsonNode msg = mapper.readTree(msgResp).path("data").path("message");
                String body = msg.has("html") && !msg.get("html").isNull() ? msg.get("html").asText() : msg.has("data") ? msg.get("data").asText() : "";
                if (!body.isEmpty()) {
                    java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("<strong>(\\d{6})</strong>").matcher(body);
                    if (m1.find()) { otp = m1.group(1); break; }
                    java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("Your OTP:\\s*(\\d{6})").matcher(body);
                    if (m2.find()) { otp = m2.group(1); break; }
                    java.util.regex.Matcher m3 = java.util.regex.Pattern.compile("(\\d{6})").matcher(body);
                    if (m3.find()) { otp = m3.group(1); break; }
                }
                safeSleep(3000);
            } catch (Exception e) { safeSleep(3000); }
        }
        return otp;
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
