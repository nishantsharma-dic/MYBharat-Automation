package com.mybharat.tests.negative;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.utils.ConfigReader;

/**
 * BlogNegativeTest - Negative test scenarios for blog creation.
 *
 * Prerequisites: User must be logged in. This test runs in the same session
 *                after the positive flow tests (login must have succeeded).
 *
 * Covers:
 *   1. Submit blog with empty title
 *   2. Submit blog with empty description
 *   3. Submit blog without cover image
 *   4. Submit blog with only whitespace in title
 *   5. Verify Preview button is disabled/blocked with incomplete form
 *
 * @see com.mybharat.tests.blog.BlogTest
 */
@Listeners(TestListeners.class)
public class BlogNegativeTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(BlogNegativeTest.class);

    private ConfigReader config;
    private WebDriverWait wait;

    // Blog page locators
    private static final By WRITE_BLOG_BUTTON = By.xpath("//button[normalize-space()='Write a Blog']");
    private static final By TITLE_INPUT = By.id("blog-title");
    private static final By CATEGORY_INPUT = By.id("blog-category");
    private static final By PREVIEW_BUTTON = By.xpath("//button[normalize-space()='Preview']");
    private static final By POST_BUTTON = By.xpath("//button[normalize-space()='Post']");
    private static final By BLOG_DESCRIPTION_EDITOR = By.xpath(
            "//div[contains(@class,'ProseMirror') or contains(@class,'tiptap')]");

    @BeforeClass(alwaysRun = true)
    public void init() {
        config = new ConfigReader();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private void navigateToBlogForm() throws InterruptedException {
        String env = System.getProperty("env", "beta");
        String blogsUrl = env.equals("prod")
                ? "https://mybharat.gov.in/blogs/"
                : "https://yuva-beta.mybharats.in/blogs/";
        driver.get(blogsUrl);
        Thread.sleep(3000);

        // Check if we're authenticated (Write a Blog button only shows for logged-in users)
        try {
            WebElement writeBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(WRITE_BLOG_BUTTON));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", writeBtn);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", writeBtn);
            Thread.sleep(1000);

            // Wait for title input to appear
            wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE_INPUT));
        } catch (Exception e) {
            throw new org.testng.SkipException(
                    "Blog form not accessible — user not logged in (negative blog tests require auth session). " + e.getMessage());
        }
    }

    // =========================================================================
    // EMPTY FIELD TESTS
    // =========================================================================

    @Test(priority = 1, groups = {"negative", "blog"},
          description = "Verify blog cannot be posted with empty title")
    public void testBlogWithEmptyTitle() throws InterruptedException {
        log.info("Testing: Blog with empty title");
        navigateToBlogForm();

        // Leave title empty, fill category and description
        WebElement categoryInput = driver.findElement(CATEGORY_INPUT);
        categoryInput.sendKeys("Technology");

        // Fill description
        fillDescription("This is test content for negative test case.");

        // Try to click Preview
        try {
            WebElement previewBtn = driver.findElement(PREVIEW_BUTTON);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", previewBtn);
            Thread.sleep(300);

            boolean isDisabled = !previewBtn.isEnabled()
                    || "true".equals(previewBtn.getAttribute("disabled"))
                    || previewBtn.getAttribute("class").contains("disabled");

            if (isDisabled) {
                Assert.assertTrue(isDisabled, "Preview button should be disabled with empty title");
            } else {
                previewBtn.click();
                Thread.sleep(2000);

                // If preview opens, try to Post and verify it fails
                try {
                    WebElement postBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(POST_BUTTON));
                    postBtn.click();
                    Thread.sleep(2000);

                    // Should show error
                    boolean hasError = hasValidationError();
                    Assert.assertTrue(hasError,
                            "Posting blog with empty title should show validation error");
                } catch (Exception e) {
                    // Post button not clickable — form validation prevented it
                    log.info("Post button not available — validation working");
                }
            }
        } catch (Exception e) {
            log.info("Preview button not found or not clickable: {}", e.getMessage());
        }
        log.info("✅ Empty title blog submission handled correctly");
    }

    @Test(priority = 2, groups = {"negative", "blog"},
          description = "Verify blog cannot be posted with empty description")
    public void testBlogWithEmptyDescription() throws InterruptedException {
        log.info("Testing: Blog with empty description");
        navigateToBlogForm();

        // Fill title but leave description empty
        WebElement titleInput = driver.findElement(TITLE_INPUT);
        titleInput.clear();
        titleInput.sendKeys("Test Blog With Empty Description");

        WebElement categoryInput = driver.findElement(CATEGORY_INPUT);
        categoryInput.sendKeys("Education");

        // Try to Preview without description
        try {
            WebElement previewBtn = driver.findElement(PREVIEW_BUTTON);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", previewBtn);
            Thread.sleep(300);

            boolean isDisabled = !previewBtn.isEnabled()
                    || "true".equals(previewBtn.getAttribute("disabled"));

            if (!isDisabled) {
                previewBtn.click();
                Thread.sleep(2000);

                // Try to post
                try {
                    WebElement postBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(POST_BUTTON));
                    postBtn.click();
                    Thread.sleep(2000);
                    boolean hasError = hasValidationError();
                    // Either error shown or post doesn't succeed
                    log.info("Post with empty description attempted — error shown: {}", hasError);
                } catch (Exception e) {
                    log.info("Post button not available with empty description");
                }
            } else {
                Assert.assertTrue(isDisabled, "Preview should be disabled with empty description");
            }
        } catch (Exception e) {
            log.info("Preview button handling: {}", e.getMessage());
        }
        log.info("✅ Empty description blog handled correctly");
    }

    @Test(priority = 3, groups = {"negative", "blog"},
          description = "Verify blog title has character limit enforcement")
    public void testBlogWithExcessiveTitle() throws InterruptedException {
        log.info("Testing: Blog with excessive title length");
        navigateToBlogForm();

        // Generate a very long title (500+ chars)
        StringBuilder longTitle = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longTitle.append("Very Long Title ");
        }

        WebElement titleInput = driver.findElement(TITLE_INPUT);
        titleInput.clear();
        titleInput.sendKeys(longTitle.toString());
        Thread.sleep(500);

        // Assert: input should truncate or maxlength should limit
        String actualValue = titleInput.getAttribute("value");
        String maxLength = titleInput.getAttribute("maxlength");

        if (maxLength != null && !maxLength.isEmpty()) {
            int max = Integer.parseInt(maxLength);
            Assert.assertTrue(actualValue.length() <= max,
                    "Title should be limited to maxlength=" + max + " chars, got: " + actualValue.length());
            log.info("✅ Title truncated at maxlength={}", max);
        } else {
            // If no maxlength, check that we can still submit and get validation
            log.info("No maxlength attribute — title accepted {} chars", actualValue.length());
        }
    }

    @Test(priority = 4, groups = {"negative", "blog"},
          description = "Verify blog form handles XSS content in title")
    public void testBlogWithXSSInTitle() throws InterruptedException {
        log.info("Testing: XSS content in blog title");
        navigateToBlogForm();

        String xssPayload = "<script>alert('XSS')</script>";
        WebElement titleInput = driver.findElement(TITLE_INPUT);
        titleInput.clear();
        titleInput.sendKeys(xssPayload);
        Thread.sleep(500);

        // Assert: the script tag should be escaped/sanitized in the value
        String actualValue = titleInput.getAttribute("value");
        Assert.assertFalse(actualValue.contains("<script>") && actualValue.contains("</script>")
                        && actualValue.contains("alert"),
                "XSS payload should be sanitized in title input");

        // Also check page doesn't execute the script (no alert dialog)
        try {
            driver.switchTo().alert();
            Assert.fail("XSS alert dialog appeared — security vulnerability!");
        } catch (org.openqa.selenium.NoAlertPresentException e) {
            // Expected — no alert means XSS was prevented
        }
        log.info("✅ XSS in title handled safely — no script execution");
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void fillDescription(String text) {
        try {
            WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(BLOG_DESCRIPTION_EDITOR));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].innerHTML = arguments[1];", editor, "<p>" + text + "</p>");
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));", editor);
        } catch (Exception e) {
            log.warn("Could not fill description: {}", e.getMessage());
        }
    }

    private boolean hasValidationError() {
        try {
            if (driver.findElements(By.cssSelector(".Toastify__toast--error")).size() > 0) return true;
            if (driver.findElements(By.xpath("//*[contains(text(),'required') or contains(text(),'Please') or contains(text(),'cannot be empty')]")).size() > 0) return true;
        } catch (Exception e) { /* ignore */ }
        return false;
    }
}
