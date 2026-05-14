package com.mybharat.pages.youth;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * YouthLoginPage - Handles Youth login flow.
 * 
 * Flow: Navigate to home → Click Sign In → Login with Password → Enter credentials → Submit
 */
public class YouthLoginPage extends BasePage {

    private final ConfigReader config = new ConfigReader();

    // -------------------------------------------------------------------------
    // Elements
    // -------------------------------------------------------------------------

    @FindBy(xpath = "//span[normalize-space()='Sign In']")
    private WebElement signinLink;

    @FindBy(id = "login_with_pwd")
    private WebElement loginWithPassword;

    @FindBy(id = "username")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "consentCheck2")
    private WebElement termsCheckbox;

    @FindBy(id = "signInButton")
    private WebElement signInButton;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public YouthLoginPage(WebDriver driver) {
        super(driver);
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    /**
     * Login with the registered youth email and password.
     * Reads email from Excel (saved during profile completion).
     */
    public void loginYouth() throws InterruptedException {
        // Navigate to home page
        driver.get(config.getUrl());
        waitForPageLoad();
        Thread.sleep(3000);

        // Close popup if present
        try {
            WebElement popup = driver.findElement(
                    org.openqa.selenium.By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
            Thread.sleep(500);
        } catch (Exception e) {
            // No popup
        }

        // Click Sign In
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        longWait.until(ExpectedConditions.elementToBeClickable(signinLink)).click();

        // Click "Login with Password"
        longWait.until(ExpectedConditions.elementToBeClickable(loginWithPassword)).click();

        // Get credentials
        String email = getLastSavedEmailFromExcel();
        String password = config.getPassword();

        System.out.println("Login Email: " + email);
        System.out.println("Login Password: " + password);

        // Fill credentials
        longWait.until(ExpectedConditions.visibilityOf(emailInput)).sendKeys(email);
        longWait.until(ExpectedConditions.visibilityOf(passwordInput)).sendKeys(password);

        // Accept terms if not checked
        if (!termsCheckbox.isSelected()) {
            safeClick(termsCheckbox);
        }

        // Click Sign In
        longWait.until(ExpectedConditions.elementToBeClickable(signInButton)).click();
        waitForPageLoad();
        Thread.sleep(3000);

        System.out.println("✅ Youth login successful");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Read the last saved email from Excel file.
     */
    private String getLastSavedEmailFromExcel() {
        String path = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "UserDetails.xlsx";

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("UserData");
            int lastRow = sheet.getLastRowNum();
            Row row = sheet.getRow(lastRow);
            Cell cell = row.getCell(0);
            String email = cell.toString().trim();

            System.out.println("Email read from Excel: " + email);
            return email;

        } catch (Exception e) {
            throw new RuntimeException("Unable to read email from Excel: " + e.getMessage(), e);
        }
    }
}
