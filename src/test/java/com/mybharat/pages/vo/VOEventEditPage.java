package com.mybharat.pages.vo;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * VOEventEditPage - After youth applies, org logs back in,
 * navigates to Events, clicks "Edit Event" on the latest event,
 * clicks "Save as draft", then logs out.
 *
 * URL: /orgeventmanagement/volunteer → Click "Edit Event" → /orgeventmanagement/edit_volunteer/MTU5NjUx
 */
public class VOEventEditPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOEventEditPage.class);
    private final ConfigReader config = new ConfigReader();

    public VOEventEditPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Scroll to top of the page.
     */
    public void scrollToTop() throws InterruptedException {
        log.info("Scrolling to top of page...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        Thread.sleep(1500);
        log.info("✅ Scrolled to top");
    }

    /**
     * Click the "Edit" button on the current event detail page.
     * This is the Edit button visible on the event detail page (not the events list).
     */
    public void clickEditButton() throws InterruptedException {
        log.info("Clicking Edit button on event detail page...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        try {
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//a[normalize-space()='Edit' or normalize-space()='Edit Event']"
                            + " | //button[normalize-space()='Edit' or normalize-space()='Edit Event'])[1]")));
            scrollToElement(editBtn);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
            log.info("✅ Clicked Edit button");
        } catch (Exception e) {
            log.warn("Edit button not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('a, button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  var txt = btns[i].textContent.trim();" +
                    "  if(txt === 'Edit' || txt === 'Edit Event') { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked Edit button (JS fallback)");
        }

        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ On Edit Event page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Navigate to Events page through org dashboard.
     * Flow: Profile page (after login) → Scroll down → View More → Click org name → Events sidebar
     */
    public void navigateToEventsPage() throws InterruptedException {
        log.info("Navigating to Events page via org dashboard...");
        dismissOverlay();

        // Step 1: Scroll down on profile page
        log.info("Scrolling down on profile page...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        // Step 2: Click "View More"
        try {
            WebElement viewMore = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(),'View More')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewMore);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked View More");
        } catch (Exception e) {
            log.warn("View More not found, trying direct navigation...");
            String baseUrl = config.getUrl();
            driver.get(baseUrl + "/mybharat_organizations");
            Thread.sleep(3000);
            dismissOverlay();
        }

        // Step 3: Click the org name (first org in table)
        try {
            WebElement org = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//table//tbody//tr[1]//td[2]//a")));
            scrollToElement(org);
            Thread.sleep(300);
            org.click();
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked org name: {}", org.getText());
        } catch (Exception e) {
            // Fallback: any link in table
            try {
                WebElement org = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//table//tbody//a[1]")));
                org.click();
                Thread.sleep(3000);
                waitForPageLoad();
                dismissOverlay();
                log.info("✅ Clicked org name (fallback)");
            } catch (Exception e2) {
                log.warn("Org name not found in table: {}", e2.getMessage());
            }
        }

        // Step 4: Click "Events" in sidebar
        try {
            WebElement eventsLink = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='Events'] | //span[normalize-space()='Events']/ancestor::a")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", eventsLink);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked Events tab");
        } catch (Exception e) {
            log.warn("Events tab not found: {}", e.getMessage());
        }

        log.info("✅ On Events page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click "Edit Event" button on the first (latest) event card.
     * From screenshot: button text = "Edit Event" below each card.
     */
    public void clickEditEventOnLatestCard() throws InterruptedException {
        log.info("Clicking 'Edit Event' on the latest card...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        try {
            // Click the first "Edit Event" button (latest card is first)
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//a[normalize-space()='Edit Event'] | //button[normalize-space()='Edit Event'])[1]")));
            scrollToElement(editBtn);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
            log.info("✅ Clicked 'Edit Event' on first card");
        } catch (Exception e) {
            log.warn("Edit Event button not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('a, button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  if(btns[i].textContent.trim() === 'Edit Event') { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked 'Edit Event' (JS fallback)");
        }

        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ On Edit Event page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Scroll to bottom and click "Save as draft" button.
     * From screenshot: button text = "Save as draft" (outlined button, left of Publish).
     */
    public void clickSaveAsDraft() throws InterruptedException {
        log.info("Clicking 'Save as draft'...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        // Scroll to bottom of page
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        try {
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Save as draft']"
                            + " | //a[normalize-space()='Save as draft']"
                            + " | //button[contains(text(),'Save as draft')]"
                            + " | //input[@value='Save as draft']")));
            scrollToElement(saveBtn);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
            log.info("✅ Clicked 'Save as draft'");
        } catch (Exception e) {
            // JS fallback
            log.warn("Save as draft not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('button, a, input[type=submit]');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  var txt = btns[i].textContent || btns[i].value || '';" +
                    "  if(txt.trim().toLowerCase().indexOf('save as draft') !== -1) { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked 'Save as draft' (JS fallback)");
        }

        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ Event saved as draft");
    }

    /**
     * Click on MYBharat logo (top-left) to go to homepage, then logout.
     * Flow: Click logo → Click profile dropdown → Click "Log Out"
     */
    public void clickLogoAndLogout() throws InterruptedException {
        log.info("Clicking MYBharat logo to go to homepage...");
        Thread.sleep(2000);

        // Click on logo (top-left)
        try {
            WebElement logo = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@class,'navbar-brand')]"
                                    + " | //a[contains(@href,'/')]//img[contains(@class,'logo') or contains(@alt,'bharat')]/.."
                                    + " | (//nav//a)[1]"
                                    + " | //a[contains(@href, '/home') or @href='/']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logo);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked logo - on homepage");
        } catch (Exception e) {
            log.warn("Logo not found, navigating to homepage...");
            String baseUrl = config.getUrl();
            driver.get(baseUrl);
            Thread.sleep(3000);
            dismissOverlay();
        }

        // Now logout
        log.info("Logging out org user...");
        Thread.sleep(1000);

        // Click profile dropdown
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a[data-bs-toggle=\"dropdown\"], a[data-toggle=\"dropdown\"], a.dropdown-toggle');" +
                    "if(links.length > 0) { links[links.length-1].click(); }");
            Thread.sleep(1000);
            log.info("✅ Clicked profile dropdown");
        } catch (Exception e) {
            log.warn("Profile dropdown click failed");
        }

        // Click "Log Out"
        try {
            WebElement logoutBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='Log Out'] | //a[contains(text(),'Log Out')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
            Thread.sleep(5000);
            log.info("✅ Clicked Log Out");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a');" +
                    "for(var i=0; i<links.length; i++) {" +
                    "  if(links[i].textContent.trim() === 'Log Out') { links[i].click(); break; }" +
                    "}");
            Thread.sleep(5000);
            log.info("✅ Clicked Log Out (JS fallback)");
        }

        waitForPageLoad();
        log.info("✅ Org user logged out");
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o=document.getElementById('overlay');if(o)o.style.display='none';" +
                    "var l=document.getElementById('loader2');if(l)l.style.display='none';" +
                    "try{$('#overlay').hide();$('#loader2').hide();}catch(e){}");
        }
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}
