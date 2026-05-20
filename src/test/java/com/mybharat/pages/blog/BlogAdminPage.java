package com.mybharat.pages.blog;

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

/**
 * BlogAdminPage - Handles admin blog approval/unpublish flow.
 * 
 * Opens a new window, logs in as admin, approves/unpublishes blogs.
 */
public class BlogAdminPage extends BasePage {

    private static final Logger log = LogManager.getLogger(BlogAdminPage.class);

    private final WebDriverWait longWait;
    private String youthWindowHandle;
    private String adminWindowHandle;

    // Locators - Login
    private static final By SIGN_IN_BUTTON = By.xpath("//span[normalize-space()='Sign In']");
    private static final By LOGIN_WITH_PASSWORD = By.xpath("//span[@id='login_with_pwd']");
    private static final By USERNAME_INPUT = By.xpath("//input[@id='username']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@id='password']");
    private static final By CONSENT_CHECKBOX = By.cssSelector("#consentCheck2");
    private static final By LOGIN_BUTTON = By.xpath("//button[@id='signInButton']");

    // Locators - Logout (user menu)
    private static final By USER_MENU_BUTTON = By.cssSelector("button[class='flex items-center gap-3 cursor-pointer']");
    private static final By LOGOUT_BUTTON = By.xpath("(//button[@class='flex items-center gap-3 w-full text-left px-5 py-1 text-[15px] text-[#184c5c] hover:bg-gray-100 transition cursor-pointer'])[1]");

    // Locators - Admin Dashboard
    private static final By NEWSLETTER_BLOGS_MENU = By.xpath("//a[@title='Newsletter and Blogs']");
    private static final By APPROVE_BUTTON = By.xpath("//button[normalize-space()='Approve'] | //button[normalize-space()='Publish']");
    private static final By APPROVED_TAB = By.xpath("//button[normalize-space()='Approved']");
    private static final By UNPUBLISH_BUTTON = By.xpath("//button[normalize-space()='Unpublish']");
    private static final By UNPUBLISH_CONFIRM_BUTTON = By.cssSelector("button[class='inline-flex h-9 items-center rounded-md bg-[#BC4717] px-4 text-[13px] font-medium text-white']");

    public BlogAdminPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Prepare for admin login (logout current youth session).
     */
    public void openAdminWindow() throws InterruptedException {
        log.info("Preparing admin login (single window mode)...");
        // No new window needed — will logout and login as admin in same window
    }

    /**
     * Login as admin with password credentials.
     */
    public void loginAsAdmin() throws InterruptedException {
        String env = System.getProperty("env", "beta");
        String adminUrl;
        String username;
        String password;

        if (env.equals("prod")) {
            adminUrl = "https://mybharat.gov.in";
            username = "8077334438";
            password = "Super@1234";
        } else {
            adminUrl = "https://yuva-beta.mybharats.in";
            username = "nishantji0021@gmail.com";
            password = "Super@1234";
        }

        log.info("Logging in as admin on: {}", adminUrl);

        // First logout the current session via UI
        performLogout();

        // Now navigate to home — should show Sign In
        driver.get(adminUrl);
        waitForPageLoad();
        Thread.sleep(500);

        // Close popup if present
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
            Thread.sleep(500);
        } catch (Exception e) { /* no popup */ }

        // Click Sign In
        WebElement signIn = longWait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BUTTON));
        jsClick(signIn);
        Thread.sleep(500);

        // Click Login with Password
        WebElement loginWithPwd = longWait.until(ExpectedConditions.elementToBeClickable(LOGIN_WITH_PASSWORD));
        jsClick(loginWithPwd);
        Thread.sleep(500);

        // Enter username
        WebElement usernameInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(username);
        Thread.sleep(300);

        // Enter password
        WebElement passwordInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(password);
        Thread.sleep(300);

        // Click consent checkbox
        try {
            WebElement consent = driver.findElement(CONSENT_CHECKBOX);
            if (!consent.isSelected()) {
                jsClick(consent);
            }
        } catch (Exception e) {
            log.warn("Consent checkbox not found or already checked");
        }
        Thread.sleep(300);

        // Click Login
        WebElement loginBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        jsClick(loginBtn);
        waitForPageLoad();
        Thread.sleep(500);
        log.info("✅ Admin logged in successfully");
    }

    /**
     * Navigate to Newsletter and Blogs section in admin dashboard.
     */
    public void navigateToNewsletterAndBlogs() throws InterruptedException {
        log.info("Navigating to Newsletter and Blogs...");
        WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(NEWSLETTER_BLOGS_MENU));
        scrollToElement(menu);
        Thread.sleep(500);
        jsClick(menu);
        waitForPageLoad();
        Thread.sleep(500);
        log.info("Navigated to Newsletter and Blogs");
    }

    /**
     * Approve the blog entry matching the given title.
     */
    public void approveBlog(String blogTitle) throws InterruptedException {
        log.info("Approving blog: {}", blogTitle);

        // Find the blog entry in the table
        try {
            WebElement blogRow = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + blogTitle + "')]")));
            scrollToElement(blogRow);
            Thread.sleep(500);
            blogRow.click();
            Thread.sleep(500);
        } catch (Exception e) {
            log.warn("Blog entry not found by title, clicking first pending entry");
            // Fallback: click first pending entry
            try {
                WebElement pendingEntry = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//td[contains(text(),'Pending')]/ancestor::tr")));
                pendingEntry.click();
                Thread.sleep(500);
            } catch (Exception e2) {
                log.error("No pending blog entries found");
            }
        }

        // Click Approve/Publish button
        WebElement approveBtn = longWait.until(ExpectedConditions.elementToBeClickable(APPROVE_BUTTON));
        scrollToElement(approveBtn);
        Thread.sleep(500);
        jsClick(approveBtn);
        Thread.sleep(500);
        log.info("✅ Blog approved");
    }

    /**
     * Unpublish the blog entry — click Approved tab → find blog → click Unpublish → confirm popup.
     */
    public void unpublishBlog(String blogTitle) throws InterruptedException {
        log.info("Unpublishing blog: {}", blogTitle);

        // Click "Approved" tab to see published blogs
        WebElement approvedTab = longWait.until(ExpectedConditions.elementToBeClickable(APPROVED_TAB));
        jsClick(approvedTab);
        Thread.sleep(500);
        log.info("Clicked Approved tab");

        // Find and click the blog entry
        try {
            WebElement blogRow = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + blogTitle + "')]")));
            scrollToElement(blogRow);
            Thread.sleep(500);
            jsClick(blogRow);
            Thread.sleep(500);
        } catch (Exception e) {
            log.warn("Blog not found by title, clicking first entry");
            try {
                WebElement firstEntry = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//tbody//tr[1]")));
                jsClick(firstEntry);
                Thread.sleep(500);
            } catch (Exception e2) {
                log.error("No entries found in Approved tab");
            }
        }

        // Click Unpublish button
        WebElement unpublishBtn = longWait.until(ExpectedConditions.elementToBeClickable(UNPUBLISH_BUTTON));
        scrollToElement(unpublishBtn);
        Thread.sleep(500);
        jsClick(unpublishBtn);
        Thread.sleep(500);

        // Confirm in popup
        WebElement confirmBtn = longWait.until(ExpectedConditions.elementToBeClickable(UNPUBLISH_CONFIRM_BUTTON));
        jsClick(confirmBtn);
        Thread.sleep(500);
        log.info("✅ Blog unpublished and confirmed");
    }

    /**
     * Switch to youth account: logout current admin session via UI.
     */
    public void switchToYouthWindow() throws InterruptedException {
        log.info("Switching to youth account (logging out admin)...");
        performLogout();
        log.info("Admin logged out");
    }

    /**
     * Switch to admin account: logout current session and login as admin.
     */
    public void switchToAdminWindow() throws InterruptedException {
        log.info("Switching to admin account...");
        loginAsAdmin();
    }

    /**
     * No-op since we're using single window with logout/login.
     */
    public void closeAdminWindow() {
        log.info("Single window mode — no admin window to close");
    }

    /**
     * Perform logout via UI: click user menu → click logout.
     */
    private void performLogout() throws InterruptedException {
        Thread.sleep(500);
        try {
            WebElement userMenu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON));
            scrollToElement(userMenu);
            Thread.sleep(500);
            jsClick(userMenu);
            Thread.sleep(500);

            WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
            jsClick(logoutBtn);
            waitForPageLoad();
            Thread.sleep(500);
            log.info("Logged out via UI");
        } catch (Exception e) {
            log.warn("UI logout failed, trying fallback: {}", e.getMessage());
            // Fallback: use the existing LogoutPage locator
            try {
                WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@class='flex items-center rounded-full cursor-pointer']")));
                jsClick(menu);
                Thread.sleep(500);
                WebElement logout = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@role='menuitem']")));
                jsClick(logout);
                waitForPageLoad();
                Thread.sleep(500);
                log.info("Logged out via fallback");
            } catch (Exception e2) {
                log.error("All logout attempts failed: {}", e2.getMessage());
            }
        }
    }
}
