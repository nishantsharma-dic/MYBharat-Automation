package com.mybharat.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * LandingPage - The main entry page of MYBharat.
 * Handles: popup close, navigate to registration, login.
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
