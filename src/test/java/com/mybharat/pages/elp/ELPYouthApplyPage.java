package com.mybharat.pages.elp;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * ELPYouthApplyPage - Handles the Youth applying to an ELP after login.
 * 
 * Flow:
 *   1. After login → lands on youth-profile page
 *   2. Close/submit any popup that appears
 *   3. Click "Experiential Learning" from the left side menu
 *   4. (TODO) Search and apply to the created ELP
 */
public class ELPYouthApplyPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPYouthApplyPage.class);
    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait wait;

    public ELPYouthApplyPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Close or submit any popup that appears after login on the profile page.
     */
    public void handlePostLoginPopup() throws InterruptedException {
        log.info("Handling post-login popup...");
        Thread.sleep(2000);

        // Try clicking Submit/OK/Close button on popup
        String[] popupButtons = {
            "//button[normalize-space()='Submit']",
            "//button[normalize-space()='OK']",
            "//button[normalize-space()='Ok']",
            "//button[normalize-space()='Close']",
            "//button[contains(@class,'btn-primary')]",
            "//button[contains(@class,'submit')]",
            "//i[@class='fa fa-times']",
            "//button[@class='close']",
            "//button[contains(@class,'btn-close')]",
            "//span[@aria-hidden='true'][text()='×']/parent::button"
        };

        for (String xpath : popupButtons) {
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                if (btn.isDisplayed()) {
                    try { btn.click(); } catch (Exception e) { jsClick(btn); }
                    log.info("✅ Popup handled with: {}", xpath);
                    Thread.sleep(1000);
                    return;
                }
            } catch (Exception e) {
                // Try next
            }
        }

        log.info("No popup found — continuing");
    }

    /**
     * Click "Basic Info" tab on the youth profile page.
     */
    public void clickBasicInfoTab() throws InterruptedException {
        log.info("Clicking 'Basic Info' tab...");
        WebElement basicInfoTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Basic Info']")));
        scrollToElement(basicInfoTab);
        Thread.sleep(300);
        try { basicInfoTab.click(); } catch (Exception e) { jsClick(basicInfoTab); }
        Thread.sleep(1000);
        log.info("✅ Clicked 'Basic Info' tab");
    }

    /**
     * Fill Basic Info fields: First Name, Last Name, Gender, Date of Birth and click Save.
     */
    public void fillBasicInfoAndSave(String firstName, String lastName, String gender, String dob) throws InterruptedException {
        log.info("Filling Basic Info: {} {} | Gender: {} | DOB: {}", firstName, lastName, gender, dob);

        // First Name (input name="first_name")
        WebElement firstNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='first_name']")));
        scrollToElement(firstNameInput);
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
        log.info("First Name entered: {}", firstName);
        Thread.sleep(300);

        // Last Name (input name="last_name")
        WebElement lastNameInput = driver.findElement(By.xpath("//input[@name='last_name']"));
        scrollToElement(lastNameInput);
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
        log.info("Last Name entered: {}", lastName);
        Thread.sleep(300);

        // Gender (select or radio — check what's rendered)
        // From the source: it's a <select name="gender"> or radio buttons
        try {
            // Try as select dropdown
            WebElement genderSelect = driver.findElement(By.xpath("//select[@name='gender']"));
            scrollToElement(genderSelect);
            new org.openqa.selenium.support.ui.Select(genderSelect).selectByVisibleText(gender);
            log.info("Gender selected (dropdown): {}", gender);
        } catch (Exception e) {
            // Try as radio button or custom input
            try {
                WebElement genderOption = driver.findElement(By.xpath(
                        "//input[@name='gender' and @value='" + gender.toLowerCase() + "'] | " +
                        "//label[contains(text(),'" + gender + "')]/preceding-sibling::input | " +
                        "//label[contains(text(),'" + gender + "')]//input"));
                jsClick(genderOption);
                log.info("Gender selected (radio): {}", gender);
            } catch (Exception e2) {
                log.warn("Gender field not found as select or radio, skipping");
            }
        }
        Thread.sleep(300);

        // Date of Birth (input name="dob", format: dd-MM-yyyy)
        WebElement dobInput = driver.findElement(By.xpath("//input[@name='dob']"));
        scrollToElement(dobInput);
        // Clear existing value and set new one via JS (date inputs are often readonly)
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = ''; arguments[0].setAttribute('value', arguments[1]);" +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                dobInput, dob);
        log.info("DOB entered: {}", dob);
        Thread.sleep(500);

        // Scroll down and click Save button
        scrollPage(500);
        Thread.sleep(500);
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[normalize-space()='Save'] | " +
                "//button[normalize-space()='Update'] | " +
                "//button[@type='submit'][contains(text(),'Save')]")));
        scrollToElement(saveBtn);
        Thread.sleep(300);
        try { saveBtn.click(); } catch (Exception e) { jsClick(saveBtn); }
        Thread.sleep(2000);
        log.info("✅ Basic Info saved");
    }

    /**
     * Click "Experiential Learning" from the left side menu on the youth profile page.
     */
    public void clickExperientialLearning() throws InterruptedException {
        log.info("Clicking 'Experiential Learning' from side menu...");

        // Ensure we're on the profile page
        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("youth-profile") && !currentUrl.contains("profile")) {
            log.info("Not on profile page, navigating...");
            driver.get(config.getUrl().replace("mybharat.gov.in", "web.mybharat.gov.in") + "/youth-profile");
            waitForPageLoad();
            Thread.sleep(2000);
            handlePostLoginPopup();
        }

        // Click "Experiential Learning" in the left sidebar
        WebElement elpLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//a[normalize-space()='Experiential Learning'] | " +
                "//*[normalize-space()='Experiential Learning'][self::a or self::span or self::div or self::li]")));
        scrollToElement(elpLink);
        Thread.sleep(500);
        try {
            elpLink.click();
        } catch (Exception e) {
            jsClick(elpLink);
        }

        waitForPageLoad();
        Thread.sleep(2000);
        log.info("✅ Clicked 'Experiential Learning' — URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click "Opportunity near me" button on the ELP listing page.
     * This opens a "Search by Location" sidebar with a map and Apply button.
     */
    public void clickOpportunityNearMe() throws InterruptedException {
        log.info("Clicking 'Opportunity near me' button...");

        // Scroll down to make the button visible (it's below the filter section)
        scrollPage(300);
        Thread.sleep(1000);

        // The button has text inside a nested <p> element: <button><div><p>Opportunity near me</p></div></button>
        WebElement opportunityBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//p[normalize-space()='Opportunity near me']/ancestor::button | " +
                "//button[.//p[contains(text(),'Opportunity near me')]] | " +
                "//button[contains(.,'Opportunity near me')] | " +
                "//*[contains(text(),'Opportunity near me')]/ancestor::button")));
        scrollToElement(opportunityBtn);
        Thread.sleep(500);
        try { opportunityBtn.click(); } catch (Exception e) { jsClick(opportunityBtn); }
        log.info("Clicked 'Opportunity near me' button");

        // Wait for the sidebar/modal to appear (it fetches geolocation first)
        Thread.sleep(5000);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(),'Search by Location')] | " +
                    "//*[contains(text(),'Your Current Location')] | " +
                    "//*[contains(text(),'Latitude')]")));
            log.info("✅ 'Search by Location' sidebar opened");
        } catch (Exception e) {
            // Sidebar might not open if geolocation is blocked — try clicking again
            log.warn("Sidebar not visible, retrying click...");
            jsClick(opportunityBtn);
            Thread.sleep(5000);
        }
    }

    /**
     * Click "Apply" button inside the "Search by Location" modal.
     * This applies the location filter and shows nearby ELPs.
     */
    public void clickApplyInLocationModal() throws InterruptedException {
        log.info("Clicking 'Apply' button in location modal...");

        // Wait for location to be fetched (Latitude/Longitude should appear)
        Thread.sleep(3000);

        // Click Apply button in the modal
        WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[normalize-space()='Apply'][ancestor::*[contains(@class,'modal') or contains(@class,'dialog') or contains(@class,'popup')]] | " +
                "//div[contains(@class,'modal')]//button[normalize-space()='Apply'] | " +
                "//button[normalize-space()='Apply'][last()]")));
        scrollToElement(applyBtn);
        Thread.sleep(500);
        try { applyBtn.click(); } catch (Exception e) { jsClick(applyBtn); }

        // Wait for results to load
        waitForPageLoad();
        Thread.sleep(3000);
        log.info("✅ Location filter applied — ELP results should be visible");
    }
}
