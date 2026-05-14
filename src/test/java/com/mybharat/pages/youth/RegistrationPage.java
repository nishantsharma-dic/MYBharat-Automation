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
 * RegistrationPage - Handles the Youth registration form.
 * 
 * Flow: Enter email → Get OTP → Verify OTP → Fill form → Submit
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
        Thread.sleep(500);
        safeClick(emailInput);
        clearAndType(emailInput, email);
        safeClick(getOtpBtn);
        Thread.sleep(2000); // Wait for OTP to be sent
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
    }

    /**
     * Fill the complete registration form for Indian users.
     */
    public void fillRegistrationForm() throws InterruptedException {
        Thread.sleep(1000);

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

        // State & District
        Select state = selectDropdown(stateDropdown);
        state.selectByIndex(faker.number().numberBetween(1, 15));

        waitForClickable(districtDropdown);
        Select district = selectDropdown(districtDropdown);
        district.selectByIndex(faker.number().numberBetween(1, 2));

        // Address - try Urban first
        safeClick(urbanRadio);
        Select localBody = selectDropdown(ulbDropdown);
        if (localBody.getOptions().size() > 1) {
            localBody.selectByIndex(1);
            urbanPincode.sendKeys(String.valueOf(faker.number().numberBetween(100000, 999999)));
        } else {
            // Fall back to Rural
            safeClick(ruralRadio);
            safeClick(blockPlaceholder);
            blockInput.sendKeys("a");
            blockInput.sendKeys(Keys.ENTER);
            safeClick(panchayatPlaceholder);
            panchayatInput.sendKeys("a");
            panchayatInput.sendKeys(Keys.ENTER);
            safeClick(villagePlaceholder);
            villageInput.sendKeys("a");
            villageInput.sendKeys(Keys.ENTER);
            ruralPincode.sendKeys(String.valueOf(faker.number().numberBetween(100000, 999999)));
        }

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
     * Click the "Additional Details" button on the popup that appears after registration.
     * Handles any browser alert that may appear after clicking.
     */
    public void clickAdditionalDetailsPopup() throws InterruptedException {
        Thread.sleep(5000); // Wait for popup to fully load after registration
        waitForVisible(additionalDetailsPopupBtn);
        waitForClickable(additionalDetailsPopupBtn);
        scrollToElement(additionalDetailsPopupBtn);
        Thread.sleep(500);
        jsClick(additionalDetailsPopupBtn);

        // Handle any browser alert that appears
        Thread.sleep(2000);
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert present — continue
        }
        Thread.sleep(1000);
    }

    /**
     * Click the submit popup button that appears after registration form submission.
     * Tries CSS selector first, falls back to XPath alternative, then additionalDetails button.
     */
    public void clickSubmitPopup() throws InterruptedException {
        Thread.sleep(5000); // Wait for popup to fully load after registration
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Try primary: the submit popup button with specific CSS path
            WebElement popup = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("body > div:nth-child(1) > div:nth-child(1) > main:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > main:nth-child(2) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(3) > div:nth-child(3) > button:nth-child(1)")));
            scrollToElement(popup);
            Thread.sleep(500);
            jsClick(popup);
            System.out.println("✅ Clicked submit popup button (CSS locator)");
        } catch (Exception e1) {
            try {
                // Try alternative XPath locator
                WebElement popupAlt = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//button[contains(@class,'bg-[#bc4717] hover:bg-orange-700 text-white px-5 py-2 rounded cursor-pointer flex items-center justify-center gap-2')])[1]")));
                scrollToElement(popupAlt);
                Thread.sleep(500);
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
        Thread.sleep(2000);
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert present — continue
        }
        Thread.sleep(1000);
    }

    /**
     * Open user menu and click logout.
     */
    public void logout() throws InterruptedException {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Thread.sleep(2000);

        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@class='flex items-center rounded-full cursor-pointer']")));
            scrollToElement(menu);
            Thread.sleep(500);
            jsClick(menu);
            System.out.println("✅ Opened user menu");
        } catch (Exception e) {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@class='flex items-center rounded-full cursor-pointer']")));
            jsClick(menu);
            System.out.println("✅ Opened user menu (fallback)");
        }

        Thread.sleep(1000);

        try {
            WebElement logout = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@role='menuitem']")));
            jsClick(logout);
            System.out.println("✅ Clicked logout");
        } catch (Exception e) {
            WebElement logout = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@role='menuitem']")));
            jsClick(logout);
            System.out.println("✅ Clicked logout (fallback)");
        }

        waitForPageLoad();
        Thread.sleep(3000);
        System.out.println("✅ User logged out successfully");
    }

    /**
     * Save the registration email to Excel file (resources/UserDetails.xlsx).
     */
    public void saveEmailToExcel() {
        String path = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "UserDetails.xlsx";
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
