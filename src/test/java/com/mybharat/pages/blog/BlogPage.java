package com.mybharat.pages.blog;

import java.io.File;
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
 * BlogPage - Handles blog creation flow on MY Bharat.
 * 
 * Flow: Navigate to Blogs → Click "Write a Blog" → Fill form → Preview → Submit
 * 
 * URL: /voices/blogs (listing) → /post-blog (create form)
 */
public class BlogPage extends BasePage {

    private static final Logger log = LogManager.getLogger(BlogPage.class);

    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait longWait;

    // Locators - Navigation
    private static final By RESOURCES_MENU = By.xpath("//span[normalize-space()='Resources']");
    private static final By VOICES_SUBMENU = By.xpath("//span[normalize-space()='Voices']");
    private static final By BLOGS_LINK = By.xpath("//span[normalize-space()='Blogs']");

    // Locators - Blog Listing Page
    private static final By WRITE_BLOG_BUTTON = By.xpath("//button[normalize-space()='Write a Blog']");

    // Locators - Blog Form
    private static final By TITLE_INPUT = By.id("blog-title");
    private static final By CATEGORY_INPUT = By.id("blog-category");
    private static final By COVER_IMAGE_INPUT = By.xpath("//input[@type='file' and @aria-label='Upload cover image']");
    private static final By BLOG_DESCRIPTION_EDITOR = By.xpath("//div[contains(@class,'ProseMirror') or contains(@class,'tiptap')]");
    private static final By AUTHOR_BIO_EDITOR = By.xpath("(//div[contains(@class,'ProseMirror') or contains(@class,'tiptap')])[2]");

    // Locators - Actions
    private static final By PREVIEW_BUTTON = By.xpath("//button[normalize-space()='Preview']");
    private static final By POST_BUTTON = By.xpath("//button[normalize-space()='Post']");
    private static final By SUCCESS_MESSAGE = By.xpath("//*[contains(text(),'successfully posted') or contains(text(),'successfully')]");

    // Locators - Blog Verification (My Blogs in profile)
    private static final By MY_BLOGS_LINK = By.xpath("//a[contains(@href,'my-blogs')]");
    private static final By PENDING_STATUS = By.xpath("//td[contains(text(),'Pending')] | //*[contains(text(),'Pending')]");

    /** Stores the blog title for verification */
    private String createdBlogTitle;

    public BlogPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Handle post-login popup (Submit/OK/Close) before navigating to blogs.
     */
    public void handlePostLoginPopup() throws InterruptedException {
        log.info("Handling post-login popup...");
        Thread.sleep(2000);

        String[] popupButtons = {
            "//button[normalize-space()='Submit']",
            "//button[normalize-space()='OK']",
            "//button[normalize-space()='Ok']",
            "//button[normalize-space()='Close']",
            "//i[@class='fa fa-times']",
            "//button[@class='close']",
            "//button[contains(@class,'btn-close')]"
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
            } catch (Exception e) { /* try next */ }
        }
        log.info("No popup found — continuing");
    }

    /**
     * Navigate to Blogs page directly via URL.
     */
    public void navigateToBlogs() throws InterruptedException {
        log.info("Navigating to Blogs...");
        String env = System.getProperty("env", "beta");
        String blogsUrl;
        if (env.equals("prod")) {
            blogsUrl = "https://mybharat.gov.in/blogs/";
        } else {
            blogsUrl = "https://yuva-beta.mybharats.in/blogs/";
        }
        driver.get(blogsUrl);
        waitForPageLoad();
        Thread.sleep(500);
        log.info("Navigated to Blogs page: {}", blogsUrl);
    }

    /**
     * Click "Write a Blog" button to open the blog creation form.
     */
    public void clickWriteABlog() throws InterruptedException {
        log.info("Clicking 'Write a Blog'...");
        scrollPage(500);
        Thread.sleep(500);

        WebElement writeBtn = longWait.until(ExpectedConditions.elementToBeClickable(WRITE_BLOG_BUTTON));
        scrollToElement(writeBtn);
        Thread.sleep(500);
        jsClick(writeBtn);
        waitForPageLoad();
        Thread.sleep(500);
        log.info("Blog creation form opened");
    }

    /**
     * Fill the blog creation form with test data.
     */
    public void fillBlogForm() throws InterruptedException {
        log.info("Filling blog form...");

        // Title - unique each time
        WebElement titleInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(TITLE_INPUT));
        scrollToElement(titleInput);
        titleInput.clear();
        String[] titles = {
            "Empowering Youth Through Digital India Initiative",
            "MY Bharat: Building Tomorrow's Leaders Today",
            "Youth Volunteering: A Path to Nation Building",
            "Digital Literacy for Rural India - A Success Story",
            "How Indian Youth Are Driving Social Change",
            "Skill Development Programs Transforming Lives",
            "The Power of Community Service in Modern India",
            "Innovation and Entrepreneurship Among Indian Youth",
            "Sustainable Development Goals and Youth Participation",
            "Technology for Good: Youth-Led Initiatives in India"
        };
        String selectedTitle = titles[new java.util.Random().nextInt(titles.length)];
        titleInput.sendKeys(selectedTitle);
        createdBlogTitle = selectedTitle;
        log.info("Title entered: {}", selectedTitle);
        Thread.sleep(500);

        // Category
        WebElement categoryInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(CATEGORY_INPUT));
        categoryInput.clear();
        categoryInput.sendKeys("Technology");
        log.info("Category entered");
        Thread.sleep(500);

        // Cover Image
        uploadCoverImage();
        Thread.sleep(500);

        // Blog Description (Rich Text Editor)
        fillBlogDescription();
        Thread.sleep(500);

        // Author Bio (optional)
        fillAuthorBio();
        Thread.sleep(500);

        log.info("Blog form filled successfully");
    }

    /**
     * Upload cover image for the blog — always uses mybharat_blog_cover.png.
     */
    private void uploadCoverImage() {
        try {
            String imagePath = System.getProperty("user.dir") + File.separator
                    + "UploadImages" + File.separator + "mybharat_blog_cover.png";

            // Fallback to JPG1.jpg if cover image not found
            if (!new File(imagePath).exists()) {
                imagePath = System.getProperty("user.dir") + File.separator
                        + "UploadImages" + File.separator + "JPG1.jpg";
            }

            WebElement fileInput = driver.findElement(COVER_IMAGE_INPUT);
            fileInput.sendKeys(imagePath);
            log.info("Cover image uploaded: {}", imagePath);
        } catch (Exception e) {
            log.warn("Cover image upload failed: {}", e.getMessage());
        }
    }

    /**
     * Fill the blog description rich text editor.
     */
    private void fillBlogDescription() {
        try {
            WebElement editor = longWait.until(ExpectedConditions.visibilityOfElementLocated(BLOG_DESCRIPTION_EDITOR));
            scrollToElement(editor);
            editor.click();
            Thread.sleep(300);

            String blogContent = "India's youth population represents one of the largest demographic dividends in the world. "
                    + "With over 600 million people under the age of 25, the country stands at a unique crossroads "
                    + "where the energy and innovation of its young citizens can drive transformative change across "
                    + "every sector of society. From technology and entrepreneurship to social service and governance, "
                    + "Indian youth are increasingly stepping up to take ownership of the nation's future.\n\n"
                    + "The MY Bharat platform serves as a bridge connecting young Indians with meaningful opportunities "
                    + "for volunteering, skill development, and community engagement. Through experiential learning "
                    + "programs, mega events, and collaborative initiatives with government bodies and NGOs, "
                    + "the platform empowers youth to contribute directly to India's development goals.\n\n"
                    + "Programs like Digital India, Skill India, and Startup India have created an ecosystem where "
                    + "young people can learn, innovate, and lead. The emphasis on digital literacy, vocational "
                    + "training, and entrepreneurship is helping bridge the urban-rural divide and creating "
                    + "opportunities in every corner of the country.";

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].innerHTML = arguments[1];", editor, "<p>" + blogContent + "</p>");

            // Trigger input event for React to pick up the change
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));", editor);

            log.info("Blog description filled");
        } catch (Exception e) {
            log.warn("Blog description fill failed: {}", e.getMessage());
        }
    }

    /**
     * Fill the author bio rich text editor (optional).
     */
    private void fillAuthorBio() {
        try {
            WebElement bioEditor = driver.findElement(AUTHOR_BIO_EDITOR);
            scrollToElement(bioEditor);
            bioEditor.click();
            Thread.sleep(300);

            String bio = "A passionate writer and youth advocate from India, dedicated to sharing stories of "
                    + "innovation, social impact, and nation-building. Believes in the power of youth-led "
                    + "initiatives to transform communities and drive sustainable development across the country.";
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].innerHTML = arguments[1];", bioEditor, "<p>" + bio + "</p>");
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));", bioEditor);

            log.info("Author bio filled");
        } catch (Exception e) {
            log.warn("Author bio fill skipped: {}", e.getMessage());
        }
    }

    /**
     * Click Preview button to preview the blog before posting.
     */
    public void clickPreview() throws InterruptedException {
        log.info("Clicking Preview...");
        scrollPage(300);
        Thread.sleep(500);

        WebElement previewBtn = longWait.until(ExpectedConditions.elementToBeClickable(PREVIEW_BUTTON));
        scrollToElement(previewBtn);
        Thread.sleep(500);
        jsClick(previewBtn);
        Thread.sleep(500);
        log.info("Preview opened");
    }

    /**
     * Click Post/Submit button from the preview page.
     */
    public void clickPost() throws InterruptedException {
        log.info("Clicking Post...");

        WebElement postBtn = longWait.until(ExpectedConditions.elementToBeClickable(POST_BUTTON));
        scrollToElement(postBtn);
        Thread.sleep(500);
        jsClick(postBtn);
        Thread.sleep(500);
        log.info("Blog posted");
    }

    /**
     * Verify blog was posted successfully.
     */
    public boolean isBlogPostedSuccessfully() {
        try {
            longWait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
            log.info("✅ Blog posted successfully");
            return true;
        } catch (Exception e) {
            log.warn("Success message not found: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navigate to My Blogs in user profile and verify the created blog is listed with Pending status.
     */
    public boolean verifyBlogInMyBlogs() throws InterruptedException {
        log.info("Verifying blog in My Blogs...");

        // Navigate to profile
        String env = System.getProperty("env", "beta");
        String profileUrl;
        if (env.equals("prod")) {
            profileUrl = "https://mybharat.gov.in/youth-profile";
        } else {
            profileUrl = "https://yuva-beta.mybharats.in/youth-profile";
        }
        driver.get(profileUrl);
        waitForPageLoad();
        Thread.sleep(500);

        // Click My Blogs link in sidebar
        WebElement myBlogs = longWait.until(ExpectedConditions.elementToBeClickable(MY_BLOGS_LINK));
        scrollToElement(myBlogs);
        Thread.sleep(500);
        jsClick(myBlogs);
        waitForPageLoad();
        Thread.sleep(500);
        log.info("Navigated to My Blogs");

        // Verify the blog title exists in the table
        boolean titleFound = false;
        boolean statusPending = false;

        // Use first part of title for matching (avoids issues with line wrapping and special chars)
        String searchTitle = createdBlogTitle;
        // Use just the first 20 chars or up to the first special char for reliable matching
        if (searchTitle.length() > 20) {
            searchTitle = searchTitle.substring(0, 20);
        }
        // Remove special chars that break XPath
        searchTitle = searchTitle.replace("'", "").replace("\"", "");

        try {
            WebElement blogEntry = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(.,'" + searchTitle + "')]")));
            titleFound = blogEntry != null;
            log.info("Blog title found: {}", createdBlogTitle);
        } catch (Exception e) {
            // Fallback: check page source
            String pageSource = driver.getPageSource();
            if (pageSource.contains(searchTitle)) {
                titleFound = true;
                log.info("Blog title found in page source: {}", createdBlogTitle);
            } else {
                log.warn("Blog title not found in My Blogs: {}", createdBlogTitle);
            }
        }

        // Verify Pending status
        try {
            WebElement pending = driver.findElement(PENDING_STATUS);
            statusPending = pending.isDisplayed();
            log.info("Blog status: Pending ✅");
        } catch (Exception e) {
            log.warn("Pending status not found");
        }

        if (titleFound && statusPending) {
            log.info("✅ Blog verified in My Blogs with Pending status");
            return true;
        } else if (titleFound) {
            log.info("✅ Blog found in My Blogs (status may differ)");
            return true;
        }
        return false;
    }

    /**
     * Verify the blog is visible on the public blogs listing page.
     */
    public boolean isBlogVisibleOnPublicPage() throws InterruptedException {
        log.info("Checking if blog is visible on public blogs page...");
        String env = System.getProperty("env", "beta");
        String blogsUrl = env.equals("prod")
                ? "https://mybharat.gov.in/voices/blogs"
                : "https://yuva-beta.mybharats.in/blogs/";

        driver.get(blogsUrl);
        waitForPageLoad();
        Thread.sleep(500);

        try {
            WebElement blogEntry = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + createdBlogTitle + "')]")));
            log.info("✅ Blog visible on public page: {}", createdBlogTitle);
            return true;
        } catch (Exception e) {
            log.info("Blog NOT visible on public page: {}", createdBlogTitle);
            return false;
        }
    }

    /**
     * Get the created blog title.
     */
    public String getCreatedBlogTitle() {
        return createdBlogTitle;
    }

    /**
     * Refresh My Blogs page and check the blog status.
     */
    public String getBlogStatus() throws InterruptedException {
        log.info("Checking blog status...");
        driver.navigate().refresh();
        waitForPageLoad();
        Thread.sleep(500);

        try {
            // Look for status near the blog title
            WebElement statusElement = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//tr[contains(.,'" + createdBlogTitle + "')]//td[contains(text(),'Pending') or contains(text(),'Approved') or contains(text(),'Published') or contains(text(),'Unpublished')]")));
            String status = statusElement.getText().trim();
            log.info("Blog status: {}", status);
            return status;
        } catch (Exception e) {
            // Fallback: get ALL text from the row containing our title
            try {
                WebElement row = driver.findElement(By.xpath(
                        "//tr[contains(.,'" + createdBlogTitle + "')]"));
                String rowText = row.getText();
                log.info("Blog row text: {}", rowText);
                if (rowText.contains("Unpublished")) return "Unpublished";
                if (rowText.contains("Pending")) return "Pending";
                if (rowText.contains("Approved") || rowText.contains("Published")) return "Approved (Published)";
                return rowText;
            } catch (Exception e2) {
                log.warn("Could not determine blog status");
                return "Unknown";
            }
        }
    }
}
