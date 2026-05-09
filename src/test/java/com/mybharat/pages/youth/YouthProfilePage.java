package com.mybharat.pages.youth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * YouthProfilePage - Handles the Youth profile completion flow.
 * 
 * Flow: Dismiss modal → Upload images → About → Interests → Language
 *       → Professional Summary → Work Experience → Tools → Certificate
 *       → Extract Email → Change Password
 */
public class YouthProfilePage extends BasePage {

    private final Random random = new Random();
    private final ConfigReader config = new ConfigReader();

    // -------------------------------------------------------------------------
    // Elements - Banner & Logo
    // -------------------------------------------------------------------------

    @FindBy(css = "div.tooltip_image1")
    private WebElement bannerIcon;

    @FindBy(id = "fileInput")
    private WebElement bannerInput;

    @FindBy(css = ".tooltip_image")
    private WebElement logoIcon;

    @FindBy(id = "fileInput1")
    private WebElement logoInput;

    // -------------------------------------------------------------------------
    // Elements - About
    // -------------------------------------------------------------------------

    @FindBy(id = "edit1")
    private WebElement aboutEditBtn;

    @FindBy(id = "aboutid")
    private WebElement aboutInput;

    @FindBy(id = "saveAboutInfo")
    private WebElement aboutSaveBtn;

    // -------------------------------------------------------------------------
    // Elements - Area of Interest
    // -------------------------------------------------------------------------

    @FindBy(id = "add_intrst")
    private WebElement areaOfInterestPlus;

    @FindBy(name = "area_of_interest[]")
    private WebElement areaOfInterestDropdown;

    @FindBy(id = "mySelect4")
    private WebElement subAreaDropdown;

    @FindBy(css = ".firebase-profile-areaofinterest-save-btn")
    private WebElement saveAreaBtn;

    // -------------------------------------------------------------------------
    // Elements - Language
    // -------------------------------------------------------------------------

    @FindBy(id = "add_languages")
    private WebElement languagePlus;

    @FindBy(id = "mySelect1")
    private WebElement languageDropdown;

    @FindBy(id = "lang_submit")
    private WebElement saveLanguageBtn;

    // -------------------------------------------------------------------------
    // Elements - Professional Summary
    // -------------------------------------------------------------------------

    @FindBy(id = "add_prof_sumry")
    private WebElement profSummaryPlus;

    @FindBy(id = "work_exposure")
    private WebElement profSummaryInput;

    @FindBy(id = "s2id_mySelect2")
    private WebElement profSkillLabel;

    @FindBy(css = "ul.select2-results li.select2-result-selectable")
    private List<WebElement> profSkillOptions;

    @FindBy(id = "skills_submit")
    private WebElement profSaveBtn;

    // -------------------------------------------------------------------------
    // Elements - Work Experience
    // -------------------------------------------------------------------------

    @FindBy(id = "add_pluse")
    private WebElement workExpPlus;

    @FindBy(id = "title")
    private WebElement jobTitle;

    @FindBy(id = "company")
    private WebElement company;

    @FindBy(id = "start_date")
    private WebElement startDate;

    @FindBy(id = "end_date")
    private WebElement endDate;

    @FindBy(id = "form_cl7")
    private WebElement workSaveBtn;

    // -------------------------------------------------------------------------
    // Elements - Tools & Social
    // -------------------------------------------------------------------------

    @FindBy(id = "edit16")
    private WebElement toolsSection;

    @FindBy(id = "devops_tools")
    private WebElement toolsInput;

    @FindBy(id = "introduction_video")
    private WebElement introVideo;

    @FindBy(xpath = "//select[@name='social_links[key][]']")
    private WebElement socialTypeDropdown;

    @FindBy(id = "social_url")
    private WebElement socialUrl;

    @FindBy(id = "tools_submit")
    private WebElement saveToolsBtn;

    // -------------------------------------------------------------------------
    // Elements - Certification
    // -------------------------------------------------------------------------

    @FindBy(css = "#registrationCertificateDiv .certificate_layout")
    private WebElement myCertifications;

    @FindBy(css = ".Downloadbtnpng")
    private WebElement certificationDownloadInPng;

    @FindBy(css = ".fa.fa-times")
    private WebElement closeModalBtn;

    // -------------------------------------------------------------------------
    // Elements - Basic Info & Password
    // -------------------------------------------------------------------------

    @FindBy(css = "a[href='#Basic_info']")
    private WebElement basicInfoTab;

    @FindBy(css = ".change-pwd-link")
    private WebElement changePasswordLink;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public YouthProfilePage(WebDriver driver) {
        super(driver);
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    /**
     * Complete the full youth profile in one flow.
     * Called right after registration — user is already on the profile page.
     * Flow: Profile sections → Certificate Download → Basic Info → Extract Email
     */
    public void completeYouthProfile() throws Exception {
        dismissAdditionalDetailsModal();
        uploadBannerAndLogo();
        fillAboutSection();
        addAreaOfInterest();
        addLanguage();
        fillProfessionalSummary();
        addWorkExperience();
        fillToolsSection();
    }

    /**
     * Navigate to the youth profile page by clicking user icon then "MY Bharat Profile".
     * Flow: Click user avatar (id=mlogo) → Click "MY Bharat Profile" (id=publicprofile)
     */
    public void navigateToProfilePage() throws InterruptedException {
        String baseUrl = config.getUrl();

        // First ensure we're on the home page
        try {
            WebElement logo = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".logo.mybharatlogo, a.navbar-brand")));
            jsClick(logo);
            waitForPageLoad();
            Thread.sleep(2000);
        } catch (Exception e) {
            driver.get(baseUrl);
            waitForPageLoad();
            Thread.sleep(2000);
        }

        // Close any popup that may appear
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
            Thread.sleep(500);
        } catch (Exception e) {
            // No popup — continue
        }

        // Step 1: Hover over user profile icon (id=mlogo) to open dropdown
        WebElement userIcon = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("mlogo")));
        actions().moveToElement(userIcon).perform();
        Thread.sleep(1000);

        // Step 2: Click "MY Bharat Profile" link (id=publicprofile)
        WebElement profileLink = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(By.xpath("(//a[@id='publicprofile'])[1]")));
        profileLink.click();
        waitForPageLoad();
        Thread.sleep(3000);

        // Verify we're on the profile page
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.tooltip_image1, .tooltip_image, #edit1, #add_intrst, .profile-banner")));
        System.out.println("Successfully navigated to MY Bharat Profile page");
    }

    /**
     * Dismiss the additional details modal if it appears after registration.
     */
    public void dismissAdditionalDetailsModal() {
        try {
            WebElement modal = driver.findElement(By.id("additionalDetailsModal"));
            if (modal.isDisplayed()) {
                try {
                    WebElement closeBtn = modal.findElement(
                            By.cssSelector("button[data-dismiss='modal'], .close, button.btn-close"));
                    jsClick(closeBtn);
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript(
                            "document.getElementById('additionalDetailsModal').style.display='none';" +
                            "document.querySelector('.modal-backdrop')?.remove();" +
                            "document.body.classList.remove('modal-open');");
                }
                Thread.sleep(1000);
            }
        } catch (Exception ignored) {
            // Modal not present — continue
        }
    }

    /**
     * Upload banner and logo images.
     */
    public void uploadBannerAndLogo() throws InterruptedException {
        // Wait for banner icon to be present on the page
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.tooltip_image1")));

        scrollToElement(bannerIcon);
        bannerInput.sendKeys(getRandomImagePath());

        scrollToElement(logoIcon);
        logoInput.sendKeys(getRandomImagePath());

        Thread.sleep(2000); // Wait for uploads to process
    }

    /**
     * Fill the About section.
     */
    public void fillAboutSection() {
        scrollToElement(aboutEditBtn);
        safeClick(aboutEditBtn);
        waitForVisible(aboutInput);
        aboutInput.sendKeys("This is automated testing profile. Please ignore.");
        safeClick(aboutSaveBtn);
    }

    /**
     * Add area of interest with random selections.
     */
    public void addAreaOfInterest() {
        safeClick(areaOfInterestPlus);
        selectRandomOption(areaOfInterestDropdown);
        selectRandomOption(subAreaDropdown);
        safeClick(saveAreaBtn);
    }

    /**
     * Add a random language.
     */
    public void addLanguage() {
        safeClick(languagePlus);
        selectRandomOption(languageDropdown);
        safeClick(saveLanguageBtn);
    }

    /**
     * Fill professional summary and select a skill.
     */
    public void fillProfessionalSummary() {
        safeClick(profSummaryPlus);
        profSummaryInput.sendKeys("Automated professional summary.");

        safeClick(profSkillLabel);
        int index = random.nextInt(profSkillOptions.size());
        profSkillOptions.get(index).click();

        safeClick(profSaveBtn);
    }

    /**
     * Add work experience details.
     */
    public void addWorkExperience() {
        safeClick(workExpPlus);
        jobTitle.sendKeys("Software Tester");
        company.sendKeys("ABC Technologies");
        startDate.sendKeys("2020-01-01");
        endDate.sendKeys("2022-12-31");
        safeClick(workSaveBtn);
    }

    /**
     * Fill tools, intro video, and social links.
     */
    public void fillToolsSection() {
        jsClick(toolsSection);
        waitForVisible(By.id("devops_tools"));

        clearAndType(toolsInput, "Selenium, Playwright, Maven, TestNG, Java, Git, Jenkins");
        clearAndType(introVideo, "https://www.youtube.com/watch?v=QZSlDNgi-eQ");

        new Select(socialTypeDropdown).selectByIndex(3);
        clearAndType(socialUrl, "https://x.com/MkumarManoj1");

        waitForClickable(saveToolsBtn);
        safeClick(saveToolsBtn);
        scrollPage(700);
    }

    /**
     * Download the registration certificate as PNG.
     * Validates download in user's Downloads folder, then closes the modal.
     */
    public void downloadCertification() throws InterruptedException {
        scrollToElement(myCertifications);
        safeClick(myCertifications);

        waitForClickable(certificationDownloadInPng);
        certificationDownloadInPng.click();

        // Validate certificate downloaded to user's Downloads folder
        String downloadDir = System.getProperty("user.home") + File.separator + "Downloads";
        boolean downloaded = false;
        for (int i = 0; i < 15; i++) {
            File dir = new File(downloadDir);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".png") && !file.getName().contains(".crdownload")) {
                        System.out.println("✅ Certificate downloaded: " + file.getName());
                        downloaded = true;
                        break;
                    }
                }
            }
            if (downloaded) break;
            Thread.sleep(1000);
        }
        Assert.assertTrue(downloaded, "Certificate PNG not downloaded!");

        // Close the certificate modal
        Thread.sleep(1000);
        safeClick(closeModalBtn);
        Thread.sleep(1000);
    }

    /**
     * Navigate to Basic Info tab.
     */
    public void navigateToBasicInfo() throws InterruptedException {
        scrollToElement(basicInfoTab);
        scrollPage(-4000);
        Thread.sleep(500);

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.elementToBeClickable(basicInfoTab));
        scrollToElement(basicInfoTab);
        safeClick(basicInfoTab);
    }

    /**
     * Extract email from profile and write to Excel.
     */
    public void extractEmailFromProfile() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement emailField = longWait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("user_email_id")));

        scrollToElement(emailField);
        String value = emailField.getAttribute("value");
        System.out.println("Extracted Email: " + value);

        writeEmailToExcel(value);
        System.out.println("Email extracted from profile and written to Excel successfully.");
    }

    /**
     * Navigate back to the Profile tab so the certificate section is visible
     * for the next test (RegistrationCertificateVerificationTest).
     */
    public void navigateBackToProfileTab() throws InterruptedException {
        // Click on the Profile tab (first tab) to go back to profile view
        try {
            WebElement profileTab = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("a[href='#Profile'], a[href='#profile'], .nav-link.active:first-child")));
            safeClick(profileTab);
            Thread.sleep(2000);
        } catch (Exception e) {
            // If tab click fails, scroll to top where certificate section is
            scrollPage(-5000);
            Thread.sleep(1000);
        }
        // Scroll down to make certificate section visible
        scrollPage(1000);
        Thread.sleep(1000);
    }

    /**
     * Change password via the modal dialog.
     */
    public void changePassword() throws InterruptedException {
        scrollPage(4000);

        waitForClickable(changePasswordLink);
        changePasswordLink.click();

        // Wait for Change Password modal
        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class,'modal-dialog') and .//text()[contains(.,'Change Password')]]")));

        WebElement newPass = modal.findElement(By.id("newPassword"));
        WebElement confirmPass = modal.findElement(By.id("confirmPassword"));
        WebElement updateBtn = modal.findElement(By.id("updatePasswordButton"));

        waitForClickable(newPass);
        waitForClickable(confirmPass);

        newPass.sendKeys("Pass@123456");
        confirmPass.sendKeys("Pass@123456");

        waitForClickable(updateBtn);
        updateBtn.click();

        // Wait for modal to disappear
        wait.until(ExpectedConditions.invisibilityOf(modal));

        // Wait for success modal
        WebElement successModal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class,'modal-dialog') and contains(.,'successfully changed your password')]")));

        // Click Login button inside success modal
        successModal.findElement(By.xpath(".//button[contains(text(),'Login')]")).click();

        Thread.sleep(3000);

        // Navigate back to the application
        driver.get(config.getProperty("url"));
        Thread.sleep(3000);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Select a random option from a dropdown (skipping the first placeholder).
     */
    private void selectRandomOption(WebElement element) {
        Select select = new Select(element);
        List<WebElement> options = select.getOptions();
        if (options.size() > 1) {
            select.selectByIndex(random.nextInt(options.size() - 1) + 1);
        }
    }

    /**
     * Get a random image path from the UploadImages folder.
     */
    private String getRandomImagePath() {
        File imagesDir = Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();

        if (!imagesDir.exists()) {
            throw new RuntimeException("UploadImages folder not found at: " + imagesDir.getAbsolutePath());
        }

        File[] files = imagesDir.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No images found in: " + imagesDir.getAbsolutePath());
        }

        File randomFile = files[random.nextInt(files.length)];
        System.out.println("Selected Image: " + randomFile.getAbsolutePath());
        return randomFile.getAbsolutePath();
    }

    /**
     * Wait for a file download to complete.
     */
    private boolean waitForDownload(String extension, int timeoutSeconds) {
        File dir = new File(System.getProperty("user.dir") + File.separator + "DownLoad");

        for (int i = 0; i < timeoutSeconds; i++) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(extension) && !file.getName().contains(".crdownload")) {
                        return true;
                    }
                }
            }
            try { Thread.sleep(1000); } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Write email to Excel file for later use.
     */
    private void writeEmailToExcel(String email) {
        try {
            String path = System.getProperty("user.dir") + File.separator
                    + "resources" + File.separator + "UserDetails.xlsx";
            File file = new File(path);

            Workbook workbook;
            if (file.exists() && file.length() > 0) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet("UserData");
            if (sheet == null) {
                sheet = workbook.createSheet("UserData");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Email");
            }

            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(email);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("Email written successfully at: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
