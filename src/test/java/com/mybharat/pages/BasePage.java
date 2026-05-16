package com.mybharat.pages;

import java.time.Duration;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * BasePage - Parent class for all Page Objects.
 * Contains common utility methods: waits, scrolls, clicks.
 * 
 * Every page object extends this class.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    private static final int DEFAULT_WAIT = 25;

    /** CI mode uses longer waits for remote runners with higher latency */
    private static final boolean CI_MODE = Boolean.parseBoolean(System.getProperty("ciMode", "false"));

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int timeout = CI_MODE ? 45 : DEFAULT_WAIT;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        PageFactory.initElements(driver, this);
    }

    // -------------------------------------------------------------------------
    // Wait helpers
    // -------------------------------------------------------------------------

    public void waitForVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // -------------------------------------------------------------------------
    // Click helpers
    // -------------------------------------------------------------------------

    /**
     * Click with fallback to JavaScript click if normal click fails.
     */
    public void safeClick(WebElement element) {
        try {
            waitForClickable(element);
            element.click();
        } catch (Exception e) {
            jsClick(element);
        }
    }

    public void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // -------------------------------------------------------------------------
    // Scroll helpers
    // -------------------------------------------------------------------------

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center',behavior:'smooth'});", element);
    }

    public void scrollPage(int pixels) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + pixels + ")");
    }

    // -------------------------------------------------------------------------
    // Form helpers
    // -------------------------------------------------------------------------

    public Select selectDropdown(WebElement element) {
        return new Select(element);
    }

    public void clearAndType(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Type with JavaScript fallback (for stubborn fields).
     */
    public void safeType(WebElement element, String text) {
        try {
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1]", element, text);
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    public Actions actions() {
        return new Actions(driver);
    }

    /**
     * Generate a random 10-digit Indian mobile number starting with 9.
     */
    public static String randomMobileNumber() {
        Random random = new Random();
        StringBuilder num = new StringBuilder("9");
        for (int i = 1; i < 10; i++) {
            num.append(random.nextInt(10));
        }
        return num.toString();
    }

    /**
     * Wait for page to fully load.
     */
    public void waitForPageLoad() {
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }
}
