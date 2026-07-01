package com.mybharat.pages.elp;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * ELPAdminAcceptPage - Handles the admin accepting a youth's ELP application.
 * 
 * Flow:
 *   1. Youth is on /elp/listing?tab=my-elp after applying
 *   2. Logout youth (click username → click logout)
 *   3. Re-login as nodal admin
 *   4. Navigate to organisation → Experiential Learning
 *   5. Find the created ELP and click it
 *   6. Click "Accept" on the youth's application
 */
public class ELPAdminAcceptPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPAdminAcceptPage.class);
    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait wait;

    public ELPAdminAcceptPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Logout the youth user by clicking the username dropdown and then logout button.
     * Uses the specific locators from the ELP listing page.
     */
    public void logoutYouth() throws InterruptedException {
        log.info("Logging out youth user...");

        Thread.sleep(5000); // Wait 5 sec as specified

        // Click on the username text to open dropdown
        By usernameLocator = By.xpath("(//p[@class='text-[14px] font-medium text-[#0b4a6f]'])[1]");
        By usernameCss = By.cssSelector("p[class='text-[14px] font-medium text-[#0b4a6f]']");

        try {
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(usernameLocator));
            scrollToElement(username);
            Thread.sleep(300);
            try { username.click(); } catch (Exception e) { jsClick(username); }
            log.info("Clicked username dropdown");
        } catch (Exception e) {
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(usernameCss));
            scrollToElement(username);
            Thread.sleep(300);
            jsClick(username);
            log.info("Clicked username dropdown (CSS fallback)");
        }

        Thread.sleep(1000);

        // Click logout button
        By logoutXpath = By.xpath("(//button[@class='flex items-center gap-3 w-full text-left px-5 py-1 text-[15px] text-[#184c5c] hover:bg-gray-100 transition cursor-pointer'])[1]");
        By logoutCss = By.cssSelector("button[class='flex items-center gap-3 w-full text-left px-5 py-1 text-[15px] text-[#184c5c] hover:bg-gray-100 transition cursor-pointer']");

        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutXpath));
            scrollToElement(logoutBtn);
            Thread.sleep(300);
            try { logoutBtn.click(); } catch (Exception e) { jsClick(logoutBtn); }
            log.info("✅ Clicked logout button");
        } catch (Exception e) {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutCss));
            scrollToElement(logoutBtn);
            Thread.sleep(300);
            jsClick(logoutBtn);
            log.info("✅ Clicked logout button (CSS fallback)");
        }

        waitForPageLoad();
        Thread.sleep(3000);
        log.info("✅ Youth logged out successfully");
    }

    /**
     * Login as the nodal admin using the stored email.
     * Reuses the ELPLoginPage logic.
     */
    public void loginAsAdmin(String adminEmail) throws InterruptedException {
        log.info("Re-logging in as admin: {}", adminEmail);

        ELPLoginPage loginPage = new ELPLoginPage(driver);
        loginPage.loginWithSpecificEmail(adminEmail);

        log.info("✅ Re-logged in as admin: {}", adminEmail);
    }

    /**
     * Navigate to the organisation's Experiential Learning section.
     * Reuses the same navigation as ELPNavigationPage.
     */
    public void navigateToELPSection() throws InterruptedException {
        log.info("Navigating to ELP admin section...");

        ELPNavigationPage navPage = new ELPNavigationPage(driver);
        navPage.navigateToProfileAndClickViewMore();

        log.info("✅ On ELP admin listing page");
    }

    /**
     * Find and click the created ELP by title on the admin listing page.
     */
    public void clickCreatedELP(String elpTitle) throws InterruptedException {
        log.info("Looking for ELP to accept: {}", elpTitle);

        Thread.sleep(3000);

        // Try exact match first
        By exactLocator = By.xpath("//h3[normalize-space()='" + elpTitle + "']");

        try {
            WebElement elpCard = wait.until(ExpectedConditions.elementToBeClickable(exactLocator));
            scrollToElement(elpCard);
            Thread.sleep(500);
            try { elpCard.click(); } catch (Exception e) { jsClick(elpCard); }
            log.info("✅ Clicked ELP: {}", elpTitle);
        } catch (Exception e) {
            // Try partial match with village name
            log.warn("Exact title not found, trying partial match...");
            String villageName = elpTitle.contains(" - ") ?
                    elpTitle.substring(elpTitle.lastIndexOf(" - ") + 3) : elpTitle;
            By partialLocator = By.xpath("//*[contains(text(),'" + villageName + "')][self::h3 or self::h4 or self::p or self::a or self::span]");
            WebElement elpCard = wait.until(ExpectedConditions.elementToBeClickable(partialLocator));
            scrollToElement(elpCard);
            Thread.sleep(500);
            try { elpCard.click(); } catch (Exception ex) { jsClick(elpCard); }
            log.info("✅ Clicked ELP (partial match): {}", villageName);
        }

        waitForPageLoad();
        Thread.sleep(3000);
        log.info("ELP detail page loaded — URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click the "Accept" button on the youth's application in the ELP detail page.
     * The page is at /elp/admin-listing/{id}?tab=youthlist showing applicants.
     * The Accept button might be in a table row, card, or as a standalone button.
     */
    public void clickAccept() throws InterruptedException {
        log.info("Clicking 'Accept' button...");

        Thread.sleep(3000);

        // Scroll down to find the applicants section
        scrollPage(300);
        Thread.sleep(2000);

        // Try multiple locators for the Accept button
        By[] acceptLocators = {
            By.xpath("//button[normalize-space()='Accept']"),
            By.xpath("//button[contains(text(),'Accept')]"),
            By.xpath("//a[normalize-space()='Accept']"),
            By.xpath("//*[normalize-space()='Accept'][self::button or self::a]"),
            // In table rows
            By.xpath("//td//button[normalize-space()='Accept']"),
            // In card layouts
            By.xpath("//div[contains(@class,'card')]//button[normalize-space()='Accept']"),
            // Accept with approve text
            By.xpath("//button[normalize-space()='Approve']"),
            By.xpath("//button[contains(text(),'Approve')]")
        };

        for (By locator : acceptLocators) {
            try {
                WebElement acceptBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(locator));
                scrollToElement(acceptBtn);
                Thread.sleep(500);
                try { acceptBtn.click(); } catch (Exception e) { jsClick(acceptBtn); }
                log.info("✅ Clicked Accept/Approve with locator: {}", locator);
                waitForPageLoad();
                Thread.sleep(3000);
                log.info("✅ Application accepted");
                return;
            } catch (Exception e) {
                log.debug("Accept not found with: {}", locator);
            }
        }

        // Last resort: scroll more and try again
        log.warn("Accept button not found on first attempt, scrolling further...");
        scrollPage(500);
        Thread.sleep(2000);
        
        // Try a broader search — any button in the page that says Accept
        try {
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(translate(normalize-space(), 'ACCEPT', 'accept'), 'accept')][self::button or self::a or ancestor::button]")));
            scrollToElement(acceptBtn);
            Thread.sleep(500);
            jsClick(acceptBtn);
            log.info("✅ Clicked Accept (broad search)");
            waitForPageLoad();
            Thread.sleep(3000);
            return;
        } catch (Exception e) {
            // Take note of current page state
            log.error("Accept button not found. Current URL: {}", driver.getCurrentUrl());
            log.error("Page title: {}", driver.getTitle());
            throw new RuntimeException("Accept button not found on ELP admin youthlist page");
        }
    }
}
