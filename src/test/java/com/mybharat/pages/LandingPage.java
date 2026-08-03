package com.mybharat.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * LandingPage - Page Object for the MYBharat home/landing page.
 *
 * Purpose: Handles initial interactions on the MYBharat home page including
 *          closing promotional popups and initiating registration flows.
 *
 * Flow:
 *   1. User arrives at home page (beta or prod URL)
 *   2. A quiz/announcement popup may appear → closePopupIfPresent() dismisses it
 *   3. User clicks "Register Now" → then chooses Indian or International registration
 *
 * Key Methods:
 *   - closePopupIfPresent()           — dismisses the quiz popup overlay (if shown)
 *   - clickRegisterForIndian()        — starts registration for Indian users
 *   - clickRegisterForInternational() — checks international checkbox, then registers
 *
 * Environment:
 *   Beta: https://yuva-beta.mybharats.in
 *   Prod: https://mybharat.gov.in
 *
 * Dependencies: BasePage (parent)
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationPage
 * @see BasePage
 */
public class LandingPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LandingPage.class);

    @FindBy(xpath = "//i[@class='fa fa-times']")
    private WebElement closePopup;

    @FindBy(xpath = "//span[@class='fontchange']")
    private WebElement registerNowBtn;

    @FindBy(xpath = "//button[@class='btn btn_login lang_yuva_register_as_youth_btn fontchange']")
    private WebElement registerBtn;

    @FindBy(xpath = "//input[@id='internationalUserCheckbox']")
    private WebElement internationalCheckbox;

    public LandingPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Close the quiz popup if it appears.
     */
    public void closePopupIfPresent() {
        try {
            if (closePopup.isDisplayed()) {
                closePopup.click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Popup not present — continue
        }
    }

    /**
     * Click "Register Now" → then "Register" button for Indian users.
     */
    public void clickRegisterForIndian() {
        try {
            safeClick(registerNowBtn);
            safeClick(registerBtn);
        } catch (Exception e) {
            // Fallback: refresh page, close popup, and retry with JS click
            log.warn("Register Now button click failed, refreshing and retrying...");
            driver.navigate().refresh();
            waitForPageLoad();
            try { Thread.sleep(2000); } catch (InterruptedException ie) { /* skip */ }
            closePopupIfPresent();
            try {
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                org.openqa.selenium.WebElement regNow = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(registerNowBtn));
                js.executeScript("arguments[0].click();", regNow);
                try { Thread.sleep(1000); } catch (InterruptedException ie) { /* skip */ }
                org.openqa.selenium.WebElement regBtn = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(registerBtn));
                js.executeScript("arguments[0].click();", regBtn);
            } catch (Exception e2) {
                log.error("Could not click Register buttons after retry: {}", e2.getMessage());
                throw new RuntimeException("Registration navigation failed: " + e2.getMessage());
            }
        }
    }

    /**
     * Click "Register Now" → check international → then "Register" button.
     */
    public void clickRegisterForInternational() {
        safeClick(registerNowBtn);
        safeClick(internationalCheckbox);
        safeClick(registerBtn);
    }
}
