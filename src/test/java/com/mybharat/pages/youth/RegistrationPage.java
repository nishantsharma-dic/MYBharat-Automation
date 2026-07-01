package com.mybharat.pages.youth;

import java.time.Duration;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.javafaker.Faker;
import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * RegistrationPage - Page Object for the Youth registration form on MYBharat.
 *
 * Purpose: Handles the complete new user registration flow — from entering email and
 *          verifying OTP to filling personal/education/location details and submitting.
 *          Uses JavaFaker for randomized test data and Yopmail for OTP retrieval.
 *
 * Flow:
 *   1. enterEmailAndRequestOTP() — enters generated @yopmail.com email, clicks Get OTP
 *   2. fetchAndVerifyOTP()       — opens Yopmail in new tab, extracts OTP, enters and verifies
 *   3. fillRegistrationForm()    — fills all form fields (name, DOB, gender, state, district,
 *                                  address, education, institution, sport, consent checkboxes)
 *   4. submitForm()              — clicks the Register/Submit button
 *   5. clickSubmitPopup()        — handles the post-registration confirmation popup
 *   6. saveEmailToExcel()        — persists the email to Youth_{env}.xlsx for login tests
 *
 * Key Methods:
 *   - getEmail()          — returns the auto-generated email used for this registration
 *   - saveEmailToExcel()  — saves email to environment-specific Excel (Youth_beta/prod.xlsx)
 *   - clickSubmitPopup()  — handles the confirmation popup with multiple locator fallbacks
 *
 * Data Generation:
 *   - Email: {randomName}@yopmail.com (via JavaFaker)
 *   - Mobile: random 10-digit number starting with 9
 *   - Name, DOB, address: randomized via JavaFaker
 *
 * Dependencies: BasePage, ConfigReader, JavaFaker, Apache POI (Excel), Yopmail
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationTest
 * @see LoginPage
 */
public class RegistrationPage extends BasePage {

    private final Faker faker = new Faker();
    private final ConfigReader config = new ConfigReader();
    private String email;
    private String mobileNum;

    // -------------------------------------------------------------------------
    // Elements - OTP Section
    // -------------------------------------------------------------------------

    @FindBy(xpath = "(//input[@id='user_mobile'])[1]")
    private WebElement emailInput;

    @FindBy(css = "button.generate_otp")
    private WebElement getOtpBtn;

    @FindBy(xpath = "//input[@id='login']")
    private WebElement yopmailInbox;

    @FindBy(css = ".material-icons-outlined.f36")
    private WebElement yopmailGoBtn;

    @FindBy(xpath = "//button[@id='refresh']")
    private WebElement yopmailRefresh;

    @FindBy(xpath = "//p[contains(text(),'Your one-time password (OTP) for registering on My')]")
    private WebElement otpEmail;

    @FindBy(xpath = "(//input[@id='otp-field-1'])[1]")
    private WebElement otpField;

    @FindBy(xpath = "//button[@id='btn-verify-otp']")
    private WebElement verifyOtpBtn;

    // -------------------------------------------------------------------------
    // Elements - Registration Form
    // -------------------------------------------------------------------------

    @FindBy(xpath = "//input[@id='firstname']")
    private WebElement firstNameInput;

    @FindBy(xpath = "//input[@id='lastname']")
    private WebElement lastNameInput;

    @FindBy(xpath = "//input[@id='dobDD']")
    private WebElement dobDay;

    @FindBy(xpath = "//input[@id='dobMM']")
    private WebElement dobMonth;

    @FindBy(xpath = "//input[@id='dobYYYY']")
    private WebElement dobYear;

    @FindBy(xpath = "//select[@id='gender']")
    private WebElement genderDropdown;

    @FindBy(xpath = "//select[contains(.,'Select Category')]")
    private WebElement categoryDropdown;

    @FindBy(xpath = "//select[@id='state']")
    private WebElement stateDropdown;

    @FindBy(xpath = "//select[@id='district']")
    private WebElement districtDropdown;

    @FindBy(xpath = "(//input[@id='flexRadioDefault1'])[1]")
    private WebElement urbanRadio;

    @FindBy(xpath = "//select[@id='ulb']")
    private WebElement ulbDropdown;

    @FindBy(xpath = "(//input[@id='flexRadioDefault2'])[1]")
    private WebElement ruralRadio;

    @FindBy(xpath = "//div[contains(text(),'Search and select a block')]")
    private WebElement blockPlaceholder;

    @FindBy(xpath = "(//div[contains(@class,'choices')]/input)[1]")
    private WebElement blockInput;

    @FindBy(xpath = "//div[contains(text(),'Search and select a panchayat')]")
    private WebElement panchayatPlaceholder;

    @FindBy(xpath = "(//div[contains(@class,'choices')]/input)[2]")
    private WebElement panchayatInput;

    @FindBy(xpath = "//div[contains(text(),'Search and select a village')]")
    private WebElement villagePlaceholder;

    @FindBy(xpath = "(//div[contains(@class,'choices')]/input)[3]")
    private WebElement villageInput;

    @FindBy(xpath = "(//input[@id='pincode_urban'])[1]")
    private WebElement urbanPincode;

    @FindBy(xpath = "(//input[@id='pincode_rural'])[1]")
    private WebElement ruralPincode;

    @FindBy(xpath = "(//input[@id='NSS'])[1]")
    private WebElement yuvaTypeCheckbox;

    @FindBy(xpath = "//select[@id='qualification']")
    private WebElement qualificationDropdown;

    @FindBy(xpath = "//select[@id='institution_type']")
    private WebElement institutionTypeDropdown;

    @FindBy(xpath = "//select[@id='institution_state']")
    private WebElement institutionStateDropdown;

    @FindBy(xpath = "//select[@id='institution_district']")
    private WebElement institutionDistrictDropdown;

    @FindBy(xpath = "//div[contains(text(),'Search and select an institution')]")
    private WebElement institutionPlaceholder;

    @FindBy(xpath = "(//div[contains(@class,'choices')]/input)[4]")
    private WebElement institutionInput;

    @FindBy(xpath = "//div[contains(text(),'Search and select a sport')]")
    private WebElement sportPlaceholder;

    @FindBy(xpath = "(//div[contains(@class,'choices')]/input)[5]")
    private WebElement sportInput;

    @FindBy(xpath = "//input[@id='khel_participate']")
    private WebElement participateCheckbox;

    @FindBy(css = "#defaultCheck1")
    private WebElement consentTermsCheckbox;

    @FindBy(xpath = "//input[@id='ncs_consent']")
    private WebElement consentNCSCheckbox;

    @FindBy(xpath = "//button[@id='registrationButton']")
    private WebElement submitBtn;

    @FindBy(xpath = "//button[@id='btnAdditionalDetails']")
    private WebElement additionalDetailsPopupBtn;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public RegistrationPage(WebDriver driver) {
        super(driver);
        this.email = generateEmail();
        this.mobileNum = randomMobileNumber();
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    /**
     * Get the generated email (for DB verification later).
     */
    public String getEmail() {
        return email;
    }

    /**
     * Enter email and request OTP.
     */
    public void enterEmailAndRequestOTP() throws InterruptedException {
        waitForVisible(emailInput);
        scrollToElement(emailInput);
        waitForClickable(emailInput);
        safeClick(emailInput);
        clearAndType(emailInput, email);
        safeClick(getOtpBtn);
        // Wait for OTP request to complete - wait for either success message or next field
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                d -> d.findElements(By.xpath("//*[contains(text(),'OTP') or contains(@placeholder,'OTP') or contains(@name,'otp')]")).size() > 0
                        || d.findElements(By.cssSelector(".Toastify__toast")).size() > 0);
    }

    /**
     * Open Yopmail in new tab, fetch OTP, come back and verify.
     */
    public void fetchAndVerifyOTP() throws InterruptedException {
        // Open new tab for Yopmail
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(config.getDummyEmailUrl());

        // Enter email prefix
        if (yopmailInbox.isDisplayed()) {
            yopmailInbox.clear();
        }
        String emailPrefix = email.split("@")[0];
        yopmailInbox.sendKeys(emailPrefix);
        yopmailGoBtn.click();
        yopmailRefresh.click();

        // Get OTP from email
        driver.switchTo().frame("ifmail");
        String otpText = otpEmail.getText();
        String otp = otpText.split("\\. This")[0].trim().split(" is ")[1].trim();
        System.out.println("Email: " + email + " | OTP: " + otp);

        // Close tab and switch back
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1)).close();
        driver.switchTo().window(tabs.get(0));

        // Enter OTP and verify
        otpField.sendKeys(otp);
        safeClick(verifyOtpBtn);

        // Wait for OTP verification to complete — the server processes the OTP
        // and then either redirects to the registration form OR shows an error.
        // On CI (overseas), this server-side processing takes significantly longer.
        Thread.sleep(5000);
        waitForPageLoad();

        // Additional wait: keep checking until the registration form appears or timeout
        int verifyTimeout = Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 30 : 10;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(verifyTimeout)).until(
                    d -> d.findElements(By.id("firstname")).size() > 0
                            || d.findElements(By.xpath("//input[@id='firstname']")).size() > 0
                            || d.getCurrentUrl().contains("register"));
        } catch (Exception e) {
            // If still not on form, the OTP verify might have failed — click verify again
            try {
                WebElement verifyBtn = driver.findElement(By.xpath("//button[@id='btn-verify-otp']"));
                if (verifyBtn.isDisplayed()) {
                    safeClick(verifyBtn);
                    Thread.sleep(5000);
                    waitForPageLoad();
                }
            } catch (Exception e2) { /* already moved past OTP */ }
        }
    }

    /**
     * Fill the complete registration form for Indian users.
     */
    public void fillRegistrationForm() throws InterruptedException {
        // Wait for registration form to load (longer on CI due to network latency)
        int timeout = Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 60 : 15;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(
                    ExpectedConditions.visibilityOf(firstNameInput));
        } catch (Exception e) {
            // Fallback: try finding by ID directly (PageFactory ref may be stale after tab switch)
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("firstname")));
        }

        // Personal details
        waitForVisible(firstNameInput);
        scrollToElement(firstNameInput);
        safeType(firstNameInput, faker.name().firstName());
        safeType(lastNameInput, faker.name().lastName());

        // Date of birth
        String day = String.valueOf(faker.number().numberBetween(1, 28));
        String month = String.valueOf(faker.number().numberBetween(1, 12));
        String year = String.valueOf(faker.number().numberBetween(1970, 2003));
        safeType(dobDay, day);
        safeType(dobMonth, month);
        safeType(dobYear, year);

        // Gender
        scrollToElement(genderDropdown);
        Select gender = selectDropdown(genderDropdown);
        gender.selectByVisibleText(faker.options().option("Male", "Female", "Others"));

        // Category (random index 2-4)
        scrollToElement(categoryDropdown);
        Select category = selectDropdown(categoryDropdown);
        category.selectByIndex(faker.number().numberBetween(2, 5)); // index 2,3,4

        // State & District — Uttar Pradesh (value=35), Ghaziabad (value=726)
        Select state = selectDropdown(stateDropdown);
        state.selectByValue("35"); // UTTAR PRADESH

        waitForClickable(districtDropdown);
        Select district = selectDropdown(districtDropdown);
        district.selectByValue("726"); // GHAZIABAD

        // Address - Urban: Modinagar (value=248981)
        safeClick(urbanRadio);
        Select localBody = selectDropdown(ulbDropdown);
        localBody.selectByValue("248981"); // Modinagar
        urbanPincode.sendKeys("201204");

        // Yuva type
        safeClick(yuvaTypeCheckbox);

        // Education
        selectDropdown(qualificationDropdown).selectByIndex(4);
        selectDropdown(institutionTypeDropdown).selectByIndex(1);
        selectDropdown(institutionStateDropdown).selectByIndex(6);
        selectDropdown(institutionDistrictDropdown).selectByIndex(1);

        // Institution
        safeClick(institutionPlaceholder);
        institutionInput.sendKeys("s");
        institutionInput.sendKeys(Keys.ENTER);

        // Sport
        safeClick(sportPlaceholder);
        sportInput.sendKeys("B");
        sportInput.sendKeys(Keys.ENTER);

        // Participation (skip if element not present on page)
        try {
            if (participateCheckbox.isDisplayed()) {
                safeClick(participateCheckbox);
            }
        } catch (Exception e) {
            // Element not on this version of the form — skip
        }

        // Click "I consent to terms of use" (#defaultCheck1)
        waitForClickable(consentTermsCheckbox);
        scrollToElement(consentTermsCheckbox);
        jsClick(consentTermsCheckbox);

        // Click "I consent to provide my data to NCS" (#ncs_consent)
        jsClick(consentNCSCheckbox);
    }

    /**
     * Click the Submit button.
     */
    public void submitForm() {
        scrollToElement(submitBtn);
        safeClick(submitBtn);
    }

    /**
     * Click the submit popup button that appears after registration form submission.
     * Tries CSS selector first, falls back to XPath alternative, then additionalDetails button.
     */
    public void clickSubmitPopup() throws InterruptedException {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Wait for popup to appear
        longWait.until(d -> d.findElements(By.cssSelector("button[class*='bg-[#bc4717]']")).size() > 0
                || d.findElements(By.xpath("//button[contains(@class,'bg-[#bc4717]')]")).size() > 0);

        try {
            // Try primary: the submit popup button with specific CSS path
            WebElement popup = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("body > div:nth-child(1) > div:nth-child(1) > main:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > main:nth-child(2) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(3) > div:nth-child(3) > button:nth-child(1)")));
            scrollToElement(popup);
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.elementToBeClickable(popup));
            jsClick(popup);
            System.out.println("✅ Clicked submit popup button (CSS locator)");
        } catch (Exception e1) {
            try {
                // Try alternative XPath locator
                WebElement popupAlt = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//button[contains(@class,'bg-[#bc4717] hover:bg-orange-700 text-white px-5 py-2 rounded cursor-pointer flex items-center justify-center gap-2')])[1]")));
                scrollToElement(popupAlt);
                new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.elementToBeClickable(popupAlt));
                jsClick(popupAlt);
                System.out.println("✅ Clicked submit popup button (XPath locator)");
            } catch (Exception e2) {
                // Fallback: try additionalDetailsPopupBtn
                try {
                    WebElement fallback = longWait.until(
                            ExpectedConditions.elementToBeClickable(additionalDetailsPopupBtn));
                    scrollToElement(fallback);
                    jsClick(fallback);
                    System.out.println("✅ Clicked submit popup button (fallback: additionalDetails)");
                } catch (Exception e3) {
                    System.out.println("⚠️ No submit popup button found — continuing");
                }
            }
        }

        // Handle any browser alert that appears
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert present — continue
        }
    }



    /**
     * Save the registration email to Excel file.
     * Uses environment-specific file: Youth_beta.xlsx or Youth_prod.xlsx
     * Skips if email already exists in the file (prevents duplicates on retry).
     */
    public void saveEmailToExcel() {
        String env = System.getProperty("env", "beta");
        String path = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "Youth_" + env + ".xlsx";
        java.io.File file = new java.io.File(path);
        file.getParentFile().mkdirs();

        try {
            org.apache.poi.ss.usermodel.Workbook workbook;
            if (file.exists() && file.length() > 0) {
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            }

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("UserData");
            if (sheet == null) {
                sheet = workbook.createSheet("UserData");
                org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Email");
            }

            // Check if email already exists (prevent duplicates on retry)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row existingRow = sheet.getRow(i);
                if (existingRow != null && existingRow.getCell(0) != null) {
                    if (email.equals(existingRow.getCell(0).getStringCellValue())) {
                        System.out.println("⏭ Email already in Excel, skipping: " + email);
                        workbook.close();
                        return;
                    }
                }
            }

            int nextRow = sheet.getLastRowNum() + 1;
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(email);

            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("✅ Email saved to Excel: " + email + " (row " + nextRow + ")");
        } catch (Exception e) {
            System.err.println("❌ Failed to save email to Excel: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String generateEmail() {
        String name = faker.name().fullName()
                .replace(" ", "")
                .replace("'", "")
                .replace(".", "");
        return name + "@yopmail.com";
    }
}
