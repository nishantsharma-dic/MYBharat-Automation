package com.mybharat.pages;

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
        safeClick(registerNowBtn);
        safeClick(registerBtn);
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
