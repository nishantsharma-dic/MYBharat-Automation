package com.mybharat.pages.elp;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
 * ELPYouthCompletePage - Handles the Youth completing the ELP after admin approves.
 * 
 * Flow:
 *   1. Logout from admin account
 *   2. Re-login as youth
 *   3. Handle popup if available
 *   4. Click Experiential Learning
 *   5. Click "MY ELP" tab
 *   6. Click "Approved" tab
 *   7. Select joining date (today)
 *   8. Click Submit
 *   9. Select last day (today)
 *  10. Enter completed hours (72)
 *  11. Upload work done file (sample.pdf)
 *  12. Click Submit
 *  13. Rate 5 stars in popup
 *  14. Enter feedback text
 *  15. Click Submit in popup
 */
public class ELPYouthCompletePage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPYouthCompletePage.class);
    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait wait;

    public ELPYouthCompletePage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Logout from the admin/partner account using session clear.
     */
    public void logoutAdmin() throws InterruptedException {
        log.info("Logging out admin via session clear...");
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.clear(); window.sessionStorage.clear();");
        driver.manage().deleteAllCookies();
        driver.get(config.getUrl());
        waitForPageLoad();
        Thread.sleep(5000); // Extra wait for home page to fully load
        log.info("✅ Admin logged out");
    }

    /**
     * Re-login as youth using OTP flow.
     * Uses the same youth email that applied to the ELP (stored in ELPTestContext).
     */
    public void loginAsYouth() throws InterruptedException {
        log.info("Re-logging in as youth...");

        String youthEmail = com.mybharat.utils.ELPTestContext.getYouthEmail();
        log.info("Using stored youth email: {}", youthEmail);

        com.mybharat.pages.youth.LoginPage loginPage = new com.mybharat.pages.youth.LoginPage(driver);

        // Navigate fresh to home page to ensure clean state
        loginPage.navigateToHomePage();
        Thread.sleep(2000);

        // Close any popup that might block Sign In
        loginPage.closePopupIfPresent();
        Thread.sleep(1000);

        loginPage.clickSignIn();
        Thread.sleep(1000);

        loginPage.enterEmailForOTPLogin(youthEmail);
        loginPage.clickConsentCheckbox();
        loginPage.clickLoginToSendOTP();
        loginPage.fetchOTPFromYopmail();
        loginPage.clickVerifyOTP();

        waitForPageLoad();
        Thread.sleep(2000);
        log.info("✅ Youth re-logged in as: {}", youthEmail);
    }

    /**
     * Handle post-login popup if available.
     */
    public void handlePopupIfPresent() throws InterruptedException {
        log.info("Handling post-login popup...");
        Thread.sleep(2000);

        String[] popupButtons = {
            "//button[normalize-space()='Submit']",
            "//button[normalize-space()='OK']",
            "//button[normalize-space()='Close']",
            "//i[@class='fa fa-times']"
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
     * Click "Experiential Learning" from the side menu.
     */
    public void clickExperientialLearning() throws InterruptedException {
        log.info("Clicking 'Experiential Learning' from side menu...");
        Thread.sleep(2000);

        By elpLocator = By.xpath(
                "//a[normalize-space()='Experiential Learning'] | " +
                "//*[normalize-space()='Experiential Learning'][self::a or self::span or self::div or self::li]");

        WebElement elpLink = wait.until(ExpectedConditions.elementToBeClickable(elpLocator));
        scrollToElement(elpLink);
        Thread.sleep(500);
        try { elpLink.click(); } catch (Exception e) { jsClick(elpLink); }

        waitForPageLoad();
        Thread.sleep(2000);
        log.info("✅ On ELP listing page");
    }

    /**
     * Click "MY ELP" tab.
     */
    public void clickMyELPTab() throws InterruptedException {
        log.info("Clicking 'MY ELP' tab...");
        Thread.sleep(2000);

        By myElpXpath = By.xpath(
                "(//button[@class='text-sm font-medium pb-2 whitespace-nowrap text-muted hover:text-text'][normalize-space()='MY ELP'])[1]");
        By myElpCss = By.cssSelector(
                "body section div div div div div:nth-child(2) button:nth-child(5)");

        try {
            WebElement myElpTab = wait.until(ExpectedConditions.elementToBeClickable(myElpXpath));
            scrollToElement(myElpTab);
            Thread.sleep(300);
            try { myElpTab.click(); } catch (Exception e) { jsClick(myElpTab); }
            log.info("✅ Clicked 'MY ELP' tab (XPath)");
        } catch (Exception e) {
            WebElement myElpTab = wait.until(ExpectedConditions.elementToBeClickable(myElpCss));
            scrollToElement(myElpTab);
            Thread.sleep(300);
            jsClick(myElpTab);
            log.info("✅ Clicked 'MY ELP' tab (CSS fallback)");
        }

        waitForPageLoad();
        Thread.sleep(3000);
    }

    /**
     * Click "Approved" tab inside MY ELP.
     */
    public void clickApprovedTab() throws InterruptedException {
        log.info("Clicking 'Approved' tab...");
        Thread.sleep(2000);

        By approvedXpath = By.xpath("//button[normalize-space()='Approved(1)']");
        By approvedCss = By.cssSelector(
                "body > section:nth-child(3) > div:nth-child(1) > div:nth-child(2) > div:nth-child(1) > button:nth-child(2)");

        try {
            WebElement approvedTab = wait.until(ExpectedConditions.elementToBeClickable(approvedXpath));
            scrollToElement(approvedTab);
            Thread.sleep(300);
            try { approvedTab.click(); } catch (Exception e) { jsClick(approvedTab); }
            log.info("✅ Clicked 'Approved' tab (XPath)");
        } catch (Exception e) {
            // Try with contains text for dynamic count
            try {
                WebElement approvedTab = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(normalize-space(),'Approved')]")));
                scrollToElement(approvedTab);
                Thread.sleep(300);
                try { approvedTab.click(); } catch (Exception ex) { jsClick(approvedTab); }
                log.info("✅ Clicked 'Approved' tab (contains text)");
            } catch (Exception e2) {
                WebElement approvedTab = wait.until(ExpectedConditions.elementToBeClickable(approvedCss));
                scrollToElement(approvedTab);
                Thread.sleep(300);
                jsClick(approvedTab);
                log.info("✅ Clicked 'Approved' tab (CSS fallback)");
            }
        }

        waitForPageLoad();
        Thread.sleep(3000);
    }

    /**
     * Select ELP joining date (today's date).
     * The custom date picker requires actual interaction to trigger Submit button.
     * We click the input, type the date, and press Tab to confirm selection.
     */
    public void selectJoiningDate() throws InterruptedException {
        log.info("Selecting joining date (today)...");
        Thread.sleep(3000);

        // Scroll down to find the date input (it might be below the fold)
        scrollPage(300);
        Thread.sleep(2000);

        String todayISO = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("dd"));
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));

        WebElement dateInput = new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//input[@type='date']")));
        scrollToElement(dateInput);
        Thread.sleep(500);

        // Click to focus and open the native date picker
        dateInput.click();
        Thread.sleep(1000);

        // For Chrome on macOS, date input accepts keys in MM/DD/YYYY order
        // Clear any existing value first with keyboard
        dateInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.COMMAND, "a"));
        Thread.sleep(200);
        dateInput.sendKeys(org.openqa.selenium.Keys.DELETE);
        Thread.sleep(200);

        // Type the date parts: month, day, year (Chrome's native format on macOS)
        dateInput.sendKeys(month);
        Thread.sleep(100);
        dateInput.sendKeys(day);
        Thread.sleep(100);
        dateInput.sendKeys(year);
        Thread.sleep(500);

        // Press Tab or Enter to confirm the date selection
        dateInput.sendKeys(org.openqa.selenium.Keys.TAB);
        Thread.sleep(1000);

        log.info("Joining date entered via keyboard: {}/{}/{}", month, day, year);

        // Verify the value was set
        String setValue = dateInput.getAttribute("value");
        log.info("Date input value after keyboard entry: {}", setValue);

        // If keyboard didn't work, fall back to React native setter
        if (setValue == null || setValue.isEmpty()) {
            log.warn("Keyboard entry didn't work, using React native setter...");
            ((JavascriptExecutor) driver).executeScript(
                    "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeInputValueSetter.call(arguments[0], arguments[1]);" +
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    dateInput, todayISO);
            Thread.sleep(500);
            // Click somewhere else to trigger blur
            ((JavascriptExecutor) driver).executeScript("arguments[0].blur();", dateInput);
            Thread.sleep(500);
        }

        Thread.sleep(1000);
        log.info("Joining date selection complete");
    }

    /**
     * Click Submit button (for joining date submission).
     * Tries multiple locators since the button may have different text or location.
     */
    public void clickSubmit() throws InterruptedException {
        log.info("Clicking 'Submit' button...");
        Thread.sleep(2000);

        By[] submitLocators = {
            By.xpath("//button[normalize-space()='Submit']"),
            By.xpath("//button[contains(@class,'bg-secondary') or contains(@class,'bg-primary')]"),
            By.xpath("//button[@type='submit']"),
            By.xpath("//button[contains(text(),'Submit')]"),
            By.xpath("//button[contains(text(),'Save')]"),
            By.xpath("//button[contains(text(),'Confirm')]")
        };

        for (By locator : submitLocators) {
            try {
                WebElement submitBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(locator));
                scrollToElement(submitBtn);
                Thread.sleep(300);
                try { submitBtn.click(); } catch (Exception e) { jsClick(submitBtn); }
                log.info("✅ Clicked Submit with: {}", locator);
                waitForPageLoad();
                Thread.sleep(3000);
                return;
            } catch (Exception e) {
                log.debug("Submit not found with: {}", locator);
            }
        }

        throw new RuntimeException("Submit button not found with any known locator");
    }

    /**
     * Click "Select Last Day*" and enter today's date.
     */
    public void selectLastDay() throws InterruptedException {
        log.info("Selecting last day (today)...");
        Thread.sleep(2000);

        // Click the "Select Last Day*" button to reveal the date input
        WebElement selectLastDayBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Select Last Day*']")));
        scrollToElement(selectLastDayBtn);
        Thread.sleep(300);
        try { selectLastDayBtn.click(); } catch (Exception e) { jsClick(selectLastDayBtn); }
        Thread.sleep(1000);

        // Find the date input that appeared and enter today's date
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try {
            WebElement dateInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@type='date']")));
            scrollToElement(dateInput);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];" +
                    "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                    "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    dateInput, today);
            log.info("Last day set to: {}", today);
        } catch (Exception e) {
            log.warn("Date input not found after clicking Select Last Day, it may already be set");
        }

        Thread.sleep(1000);
    }

    /**
     * Enter completed hours.
     */
    public void enterCompletedHours(String hours) throws InterruptedException {
        log.info("Entering completed hours: {}", hours);
        Thread.sleep(1000);

        WebElement hoursInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Completed hours*']")));
        scrollToElement(hoursInput);
        hoursInput.clear();
        hoursInput.sendKeys(hours);
        Thread.sleep(500);
        log.info("✅ Completed hours entered: {}", hours);
    }

    /**
     * Upload work done file (sample.pdf from UploadImages folder).
     */
    public void uploadWorkDoneFile() throws InterruptedException {
        log.info("Uploading work done file...");
        Thread.sleep(1000);

        String filePath = System.getProperty("user.dir") + File.separator
                + "UploadImages" + File.separator + "sample.pdf";

        // Find the file input - it may be hidden, so we need to make it visible
        By fileInputXpath = By.xpath("(//span[@class='truncate pr-3'])[1]/ancestor::label//input[@type='file'] | //input[@type='file'][last()]");
        By fileInputCss = By.cssSelector("input[type='file']");

        WebElement fileInput = null;
        try {
            fileInput = driver.findElement(fileInputXpath);
        } catch (Exception e) {
            fileInput = driver.findElement(fileInputCss);
        }

        // Make file input visible for sendKeys
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block';" +
                "arguments[0].style.opacity='1';" +
                "arguments[0].style.position='absolute';",
                fileInput);
        Thread.sleep(300);

        fileInput.sendKeys(filePath);
        Thread.sleep(3000); // Wait for upload
        log.info("✅ File uploaded: {}", filePath);
    }

    /**
     * Click Submit after entering completion details (hours + file).
     * Wait longer since file upload may take time before Submit is enabled.
     */
    public void clickCompletionSubmit() throws InterruptedException {
        log.info("Clicking Submit for completion...");
        Thread.sleep(5000); // Wait for file upload to complete on server

        // Scroll to make Submit button visible
        scrollPage(300);
        Thread.sleep(1000);

        By[] submitLocators = {
            By.xpath("//button[normalize-space()='Submit']"),
            By.xpath("//button[contains(@class,'bg-secondary')]"),
            By.xpath("//button[@type='submit']"),
            By.xpath("//button[contains(text(),'Submit')]")
        };

        for (By locator : submitLocators) {
            try {
                WebElement submitBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(locator));
                scrollToElement(submitBtn);
                Thread.sleep(500);
                try { submitBtn.click(); } catch (Exception e) { jsClick(submitBtn); }
                log.info("✅ Completion Submit clicked with: {}", locator);
                Thread.sleep(5000); // Wait for rating popup
                return;
            } catch (Exception e) {
                log.debug("Completion Submit not found with: {}", locator);
            }
        }

        throw new RuntimeException("Completion Submit button not found");
    }

    /**
     * Click 5 stars in the rating popup.
     * The popup may use various star implementations (SVG, button, span with ★ character).
     */
    public void rateFiveStars() throws InterruptedException {
        log.info("Rating 5 stars...");
        Thread.sleep(3000);

        // Try multiple locator strategies for the 5th star
        By[] starLocators = {
            // Stars as buttons with ★ text
            By.xpath("(//button[@type='button'][contains(text(),'★')])[5]"),
            // Stars as buttons inside a modal/overlay
            By.cssSelector("div[class='fixed inset-0 z-50 overflow-y-auto'] button:nth-child(5)"),
            // Stars as SVG or clickable elements (click the last/5th one)
            By.xpath("(//button[contains(@class,'star') or contains(@class,'rating')])[5]"),
            // Stars inside a modal — any 5th clickable element that looks like a star
            By.xpath("(//*[contains(@class,'fixed')]//button)[5]"),
            // Stars as span/button with aria-label
            By.xpath("(//*[@role='button' or self::button][ancestor::*[contains(@class,'fixed') or contains(@class,'modal') or contains(@class,'dialog')]])[5]"),
            // Simply the 5th button in a popup/dialog
            By.xpath("(//div[contains(@class,'z-50')]//button)[5]"),
            // Stars rendered as individual clickable items
            By.xpath("(//div[contains(@class,'overflow-y-auto')]//button)[5]")
        };

        for (By locator : starLocators) {
            try {
                WebElement star = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(locator));
                scrollToElement(star);
                Thread.sleep(300);
                try { star.click(); } catch (Exception e) { jsClick(star); }
                log.info("✅ Rated 5 stars with locator: {}", locator);
                Thread.sleep(1000);
                return;
            } catch (Exception e) {
                log.debug("Star locator not found: {}", locator);
            }
        }

        // Last resort: click all star buttons from 1 to 5 (some rating widgets need sequential clicks)
        log.warn("Individual star locator failed, trying to click stars 1-5 sequentially...");
        try {
            java.util.List<org.openqa.selenium.WebElement> stars = driver.findElements(
                    By.xpath("//div[contains(@class,'fixed') or contains(@class,'modal')]//button[@type='button']"));
            if (stars.size() >= 5) {
                jsClick(stars.get(4)); // Click the 5th star (0-indexed)
                log.info("✅ Rated 5 stars (sequential approach, found {} buttons)", stars.size());
                Thread.sleep(1000);
                return;
            }
        } catch (Exception e) {
            log.debug("Sequential star click failed");
        }

        throw new RuntimeException("Could not find and click 5-star rating element");
    }

    /**
     * Enter feedback text in the textarea.
     */
    public void enterFeedback(String feedback) throws InterruptedException {
        log.info("Entering feedback: {}", feedback);
        Thread.sleep(1000);

        WebElement textarea = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//textarea[@placeholder='Type Here']")));
        scrollToElement(textarea);
        textarea.clear();
        textarea.sendKeys(feedback);
        Thread.sleep(500);
        log.info("✅ Feedback entered");
    }

    /**
     * Click the final Submit button in the rating/feedback popup.
     */
    public void clickFeedbackSubmit() throws InterruptedException {
        log.info("Clicking Submit in feedback popup...");
        Thread.sleep(1000);

        By submitXpath = By.xpath("(//button[@class='inline-flex items-center justify-center gap-2 rounded-md font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 cursor-pointer text-nowrap bg-secondary text-white hover:bg-secondary/90 px-4 py-2.5 text-sm sm:text-base min-h-[40px]'])[1]");
        By submitCss = By.cssSelector("body > div:nth-child(4) > div:nth-child(2) > div:nth-child(1) > div:nth-child(3) > button:nth-child(2)");

        try {
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitXpath));
            scrollToElement(submitBtn);
            Thread.sleep(300);
            try { submitBtn.click(); } catch (Exception e) { jsClick(submitBtn); }
            log.info("✅ Clicked feedback Submit (XPath)");
        } catch (Exception e) {
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitCss));
            scrollToElement(submitBtn);
            Thread.sleep(300);
            jsClick(submitBtn);
            log.info("✅ Clicked feedback Submit (CSS fallback)");
        }

        waitForPageLoad();
        Thread.sleep(3000);
        log.info("✅ ELP completion feedback submitted successfully");
    }
}
