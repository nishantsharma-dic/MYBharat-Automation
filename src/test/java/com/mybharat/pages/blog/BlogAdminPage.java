package com.mybharat.pages.blog;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

/**
 * BlogAdminPage - Page Object for the Admin blog management flow.
 *
 * Flow (Approve):
 *   1. loginAsAdmin()  — login with password → loginAsUser (impersonate blog verifier)
 *   2. navigateToNewsletterAndBlogs() — opens Newsletter &amp; Blogs admin section
 *   3. approveBlog(title) — finds the blog entry and clicks Approve/Publish
 *
 * Flow (Unpublish):
 *   1. navigateToNewsletterAndBlogs() → click Approved tab
 *   2. Find blog entry → click Unpublish → confirm popup
 *
 * Environment:
 *   Beta: admin=nishantji0021@gmail.com, verifier=jykmqows@gmail.com
 *   Prod: admin=8077334438, verifier=pankaj.dhamija@gov.in1
 *
 * Developer: Nishant Sharma (QA Team)
 */
public class BlogAdminPage extends BasePage {

    private static final Logger log = LogManager.getLogger(BlogAdminPage.class);

    private final WebDriverWait longWait;

    // Locators - React frontend login
    private static final By SIGN_IN_BUTTON = By.xpath("//span[normalize-space()='Sign In']");
    private static final By LOGIN_WITH_PASSWORD = By.xpath("//span[@id='login_with_pwd']");
    private static final By USERNAME_INPUT = By.xpath("//input[@id='username']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@id='password']");
    private static final By CONSENT_CHECKBOX = By.cssSelector("#consentCheck2");
    private static final By LOGIN_BUTTON = By.xpath("//button[@id='signInButton']");

    // Locators - Logout
    private static final By USER_MENU_BUTTON = By.cssSelector("button[class='flex items-center gap-3 cursor-pointer']");
    private static final By LOGOUT_BUTTON = By.xpath("(//button[@class='flex items-center gap-3 w-full text-left px-5 py-1 text-[15px] text-[#184c5c] hover:bg-gray-100 transition cursor-pointer'])[1]");

    // Locators - Login As User (/settings/loginas_user)
    private static final By LOGIN_AS_USER_USERNAME = By.xpath("//input[@id='username']");
    private static final By LOGIN_AS_USER_SUBMIT = By.xpath("//input[@value='Submit']");

    // Locators - Admin Side Menu & Blog Actions
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
     * No-op — single window mode, no separate admin window needed.
     */
    public void openAdminWindow() {
        log.info("Preparing admin login (single window mode)...");
    }

    /**
     * Login as admin via React frontend, then impersonate the blog verifier user.
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

        // Logout current youth session
        performLogout();

        // Navigate to home
        driver.get(adminUrl);
        waitForPageLoad();
        Thread.sleep(1000);

        // Close popup if present (may appear multiple times)
        for (int i = 0; i < 3; i++) {
            try {
                WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(
                                "//i[@class='fa fa-times'] | //button[contains(@class,'btn-close')] | //button[normalize-space()='OK'] | //button[normalize-space()='Close']")));
                jsClick(popup);
                Thread.sleep(500);
            } catch (Exception e) { break; }
        }

        // Click Sign In
        WebElement signIn = longWait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BUTTON));
        jsClick(signIn);
        Thread.sleep(1000);

        // Click Login with Password
        WebElement loginWithPwd = longWait.until(ExpectedConditions.elementToBeClickable(LOGIN_WITH_PASSWORD));
        jsClick(loginWithPwd);
        Thread.sleep(1000);

        // Enter credentials
        WebElement usernameInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        usernameInput.clear();
        usernameInput.sendKeys(username);

        WebElement passwordInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Consent checkbox
        try {
            WebElement consent = driver.findElement(CONSENT_CHECKBOX);
            if (!consent.isSelected()) jsClick(consent);
        } catch (Exception e) { /* already checked or not present */ }

        // Click Login
        WebElement loginBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        jsClick(loginBtn);
        waitForPageLoad();
        Thread.sleep(2000);
        log.info("✅ Admin logged in successfully");

        // Impersonate the blog verifier user
        loginAsUser();
    }

    /**
     * Navigate to /settings/loginas_user and impersonate the blog verifier user.
     * Beta: jykmqows@gmail.com | Prod: pankaj.dhamija@gov.in1
     */
    private void loginAsUser() throws InterruptedException {
        String env = System.getProperty("env", "beta");
        String loginAsUserUrl;
        String impersonateEmail;

        if (env.equals("prod")) {
            loginAsUserUrl = "https://mybharat.gov.in/settings/loginas_user";
            impersonateEmail = "pankaj.dhamija@gov.in1";
        } else {
            loginAsUserUrl = "https://yuva-beta.mybharats.in/settings/loginas_user";
            impersonateEmail = "jykmqows@gmail.com";
        }

        log.info("Navigating to Login As User: {}", loginAsUserUrl);
        driver.get(loginAsUserUrl);
        waitForPageLoad();
        Thread.sleep(2000);

        // Check if redirected to CakePHP login page (not loginas_user itself)
        String currentUrl = driver.getCurrentUrl();
        log.info("Current URL: {}", currentUrl);

        if (!currentUrl.contains("loginas_user") &&
                (currentUrl.endsWith("/login") || currentUrl.contains("/pages/login"))) {
            log.info("Redirected to admin login — logging in via CakePHP panel");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement txtUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtusername")));
            txtUsername.clear();
            txtUsername.sendKeys(env.equals("prod") ? "8077334438" : "nishantji0021@gmail.com");

            WebElement txtPassword = driver.findElement(By.id("txtpassword"));
            txtPassword.clear();
            txtPassword.sendKeys("Super@1234");

            jsClick(driver.findElement(By.id("btnlogin")));
            waitForPageLoad();
            Thread.sleep(2000);

            // Re-navigate to loginas_user
            driver.get(loginAsUserUrl);
            waitForPageLoad();
            Thread.sleep(2000);
            log.info("Re-navigated to Login As User after CakePHP login");
        }

        // Find username field (multiple strategies for robustness)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement usernameField = findUsernameField(wait);

        usernameField.clear();
        usernameField.sendKeys(impersonateEmail);
        log.info("Entered impersonation email: {}", impersonateEmail);

        // Click Submit
        WebElement submitBtn;
        try {
            submitBtn = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_AS_USER_SUBMIT));
        } catch (Exception e) {
            submitBtn = driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']"));
        }
        jsClick(submitBtn);
        waitForPageLoad();
        Thread.sleep(2000); // AJAX redirect
        log.info("✅ Logged in as user: {}", impersonateEmail);
    }

    /**
     * Find the username field on /settings/loginas_user with fallback strategies.
     */
    private WebElement findUsernameField(WebDriverWait wait) {
        // Strategy 1: By ID
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        } catch (Exception e1) {
            log.info("Trying fallback locators for username field...");
        }
        // Strategy 2: By name
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        } catch (Exception e2) { /* continue */ }
        // Strategy 3: By placeholder
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//input[@placeholder='Email or Username']")));
        } catch (Exception e3) { /* continue */ }
        // Strategy 4: Any text input
        return new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[type='text'], input.form-control")));
    }

    /**
     * Navigate to Newsletter and Blogs section.
     */
    public void navigateToNewsletterAndBlogs() throws InterruptedException {
        log.info("Navigating to Newsletter and Blogs...");
        WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(NEWSLETTER_BLOGS_MENU));
        scrollToElement(menu);
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

        // Find the blog entry
        try {
            WebElement blogRow = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + blogTitle + "')]")));
            scrollToElement(blogRow);
            blogRow.click();
            Thread.sleep(500);
        } catch (Exception e) {
            log.warn("Blog not found by title, clicking first pending entry");
            try {
                WebElement pendingEntry = longWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//td[contains(text(),'Pending')]/ancestor::tr")));
                pendingEntry.click();
                Thread.sleep(500);
            } catch (Exception e2) {
                log.error("No pending blog entries found");
            }
        }

        // Click Approve/Publish
        WebElement approveBtn = longWait.until(ExpectedConditions.elementToBeClickable(APPROVE_BUTTON));
        scrollToElement(approveBtn);
        jsClick(approveBtn);
        Thread.sleep(500);
        log.info("✅ Blog approved");
    }

    /**
     * Unpublish the blog entry.
     */
    public void unpublishBlog(String blogTitle) throws InterruptedException {
        log.info("Unpublishing blog: {}", blogTitle);

        // Click Approved tab
        WebElement approvedTab = longWait.until(ExpectedConditions.elementToBeClickable(APPROVED_TAB));
        jsClick(approvedTab);
        Thread.sleep(500);

        // Find and click the blog entry
        try {
            WebElement blogRow = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + blogTitle + "')]")));
            scrollToElement(blogRow);
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

        // Click Unpublish
        WebElement unpublishBtn = longWait.until(ExpectedConditions.elementToBeClickable(UNPUBLISH_BUTTON));
        scrollToElement(unpublishBtn);
        jsClick(unpublishBtn);
        Thread.sleep(500);

        // Confirm
        WebElement confirmBtn = longWait.until(ExpectedConditions.elementToBeClickable(UNPUBLISH_CONFIRM_BUTTON));
        jsClick(confirmBtn);
        Thread.sleep(500);
        log.info("✅ Blog unpublished and confirmed");
    }

    /**
     * Switch to youth account: clear session completely for fresh youth login.
     */
    public void switchToYouthWindow() throws InterruptedException {
        log.info("Switching to youth account (clearing session)...");
        // Clear all cookies to remove any admin/partner session from "login as user"
        driver.manage().deleteAllCookies();
        Thread.sleep(500);
        log.info("Session cleared");
    }

    /**
     * Switch to admin account: login as admin again.
     */
    public void switchToAdminWindow() throws InterruptedException {
        log.info("Switching to admin account...");
        loginAsAdmin();
    }

    /**
     * No-op — single window mode.
     */
    public void closeAdminWindow() {
        log.info("Single window mode — no admin window to close");
    }

    /**
     * Perform logout via UI with fallback.
     */
    private void performLogout() throws InterruptedException {
        try {
            WebElement userMenu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON));
            scrollToElement(userMenu);
            jsClick(userMenu);
            Thread.sleep(500);

            WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
            jsClick(logoutBtn);
            waitForPageLoad();
            Thread.sleep(500);
            log.info("Logged out via UI");
        } catch (Exception e) {
            log.warn("UI logout failed, trying fallback: {}", e.getMessage());
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
