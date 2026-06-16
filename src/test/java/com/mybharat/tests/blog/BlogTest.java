package com.mybharat.tests.blog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.blog.BlogAdminPage;
import com.mybharat.pages.blog.BlogPage;

/**
 * BlogTest - End-to-end test for blog creation on MY Bharat.
 *
 * Purpose: Validates that a logged-in user can write, preview, and publish a blog post.
 *          Also verifies the blog appears in the "My Blogs" section with Pending status.
 *
 * Prerequisites: User must already be logged in (runs AFTER QuizCertificateVerificationTest
 *                in the same browser session within the testng-youth.xml suite).
 *
 * Flow:
 *   1. writeAndPublishBlog() — Navigate to Blogs → Write a Blog → Fill form → Preview → Post
 *   2. verifyBlogInMyBlogs() — Check that the blog appears with "Pending" status
 *
 * Admin Flow (Beta ONLY — skipped on Prod via SkipException):
 *   3. adminApprovesBlog()         — Admin logs in and approves the blog
 *   4. verifyBlogApproved()        — Youth verifies status changed to Approved
 *   5. verifyBlogVisibleOnPublicPage() — Published blog visible publicly
 *   6. adminUnpublishesBlog()      — Admin unpublishes the blog
 *   7. verifyBlogUnpublished()     — Youth verifies Unpublished status
 *   8. verifyBlogRemovedFromPublicPage() — Blog no longer visible publicly
 *
 * Environment Behavior:
 *   - Prod (-Denv=prod): Only tests 1-2 run (create + verify Pending). Tests 3-8 are SKIPPED.
 *   - Beta (-Denv=beta): All tests 1-8 run (full admin approve/unpublish flow).
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-youth.xml
 *
 * Dependencies: BaseTest, BlogPage, BlogAdminPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see BlogPage
 * @see BlogAdminPage
 */
@Listeners(TestListeners.class)
public class BlogTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(BlogTest.class);

    private BlogPage blogPage;
    private BlogAdminPage adminPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        blogPage = new BlogPage(driver);
        adminPage = new BlogAdminPage(driver);
    }

    @Test(priority = 1, groups = {"regression", "blog"}, retryAnalyzer = Retry.class,
          description = "Write and publish a blog: Navigate to Blogs → Write a Blog → Fill Title, Category, Cover Image, Description → Preview → Post")
    public void writeAndPublishBlog() throws Exception {
        log.info("Starting: Write and Publish Blog");

        // Step 1: Navigate to Blogs page (same window, user already logged in)
        blogPage.navigateToBlogs();
        log.info("Step 1: Navigated to Blogs page");

        // Step 2: Click Write a Blog
        blogPage.clickWriteABlog();
        log.info("Step 2: Blog form opened");

        // Step 3: Fill blog form
        blogPage.fillBlogForm();
        log.info("Step 3: Blog form filled");

        // Step 4: Click Preview
        blogPage.clickPreview();
        log.info("Step 4: Preview opened");

        // Step 5: Click Post
        blogPage.clickPost();
        log.info("Step 5: Blog posted");

        // Step 6: Verify success
        boolean success = blogPage.isBlogPostedSuccessfully();
        Assert.assertTrue(success, "Blog should be posted successfully");
        log.info("✅ Blog published successfully");
    }

    @Test(priority = 2, groups = {"regression", "blog"}, dependsOnMethods = "writeAndPublishBlog",
          description = "Verify created blog appears in My Blogs section of user profile with Pending status")
    public void verifyBlogInMyBlogs() throws Exception {
        log.info("Starting: Verify Blog in My Blogs");

        boolean verified = blogPage.verifyBlogInMyBlogs();
        Assert.assertTrue(verified, "Blog '" + blogPage.getCreatedBlogTitle() + "' should appear in My Blogs with Pending status");
        log.info("✅ Blog verified in My Blogs: {}", blogPage.getCreatedBlogTitle());
    }

    // =========================================================================
    // ADMIN/VERIFIER FLOW — Runs ONLY on Beta environment
    // On Prod: Tests 1-2 run (create blog + verify Pending status)
    // On Beta: Full flow runs (create → verify → admin approve → verify approved
    //          → verify public → admin unpublish → verify unpublished → verify removed)
    // =========================================================================

    private boolean isBeta() {
        return "beta".equalsIgnoreCase(System.getProperty("env", "beta"));
    }

    @Test(priority = 3, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogInMyBlogs",
          description = "Admin approves the blog: Open admin window → Login → Newsletter & Blogs → Approve (Beta only)")
    public void adminApprovesBlog() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping adminApprovesBlog — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Admin Approves Blog");

        // Open new window for admin
        adminPage.openAdminWindow();

        // Login as admin
        adminPage.loginAsAdmin();

        // Navigate to Newsletter and Blogs
        adminPage.navigateToNewsletterAndBlogs();

        // Approve the blog
        adminPage.approveBlog(blogPage.getCreatedBlogTitle());

        log.info("✅ Admin approved the blog");
    }

    @Test(priority = 4, groups = {"regression", "blog"}, dependsOnMethods = "adminApprovesBlog",
          description = "Verify blog status changed to Approved/Published in youth My Blogs (Beta only)")
    public void verifyBlogApproved() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping verifyBlogApproved — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Verify Blog Approved");

        // Logout admin, login as youth via OTP
        adminPage.switchToYouthWindow();

        // Re-login as youth and navigate to My Blogs
        com.mybharat.pages.youth.LoginPage loginPage = new com.mybharat.pages.youth.LoginPage(driver);
        loginPage.navigateToHomePage();
        loginPage.closePopupIfPresent();
        loginPage.clickSignIn();
        loginPage.enterEmailForOTPLogin();
        loginPage.clickConsentCheckbox();
        loginPage.clickLoginToSendOTP();
        loginPage.fetchOTPFromYopmail();
        loginPage.clickVerifyOTP();
        Thread.sleep(2000);

        // Navigate to My Blogs and check status
        blogPage.verifyBlogInMyBlogs();
        String status = blogPage.getBlogStatus();
        Assert.assertTrue(status.contains("Approved") || status.contains("Published"),
                "Blog status should be Approved/Published but was: " + status);
        log.info("✅ Blog status verified: {}", status);
    }

    @Test(priority = 5, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogApproved",
          description = "Verify published blog is visible on public blogs listing page (Beta only)")
    public void verifyBlogVisibleOnPublicPage() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping verifyBlogVisibleOnPublicPage — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Verify Blog Visible on Public Page");

        boolean visible = blogPage.isBlogVisibleOnPublicPage();
        Assert.assertTrue(visible, "Published blog should be visible on public blogs page");
        log.info("✅ Blog visible on public page");
    }

    @Test(priority = 6, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogVisibleOnPublicPage",
          description = "Admin unpublishes the blog (Beta only)")
    public void adminUnpublishesBlog() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping adminUnpublishesBlog — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Admin Unpublishes Blog");

        // Logout youth, login as admin
        adminPage.switchToAdminWindow();

        // Navigate to Newsletter and Blogs
        adminPage.navigateToNewsletterAndBlogs();

        // Unpublish
        adminPage.unpublishBlog(blogPage.getCreatedBlogTitle());

        log.info("✅ Admin unpublished the blog");
    }

    @Test(priority = 7, groups = {"regression", "blog"}, dependsOnMethods = "adminUnpublishesBlog",
          description = "Verify blog status changed to Unpublished in youth My Blogs (Beta only)")
    public void verifyBlogUnpublished() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping verifyBlogUnpublished — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Verify Blog Unpublished");

        // Logout admin, login as youth
        adminPage.switchToYouthWindow();

        // Re-login as youth
        com.mybharat.pages.youth.LoginPage loginPage = new com.mybharat.pages.youth.LoginPage(driver);
        loginPage.navigateToHomePage();
        loginPage.closePopupIfPresent();
        loginPage.clickSignIn();
        loginPage.enterEmailForOTPLogin();
        loginPage.clickConsentCheckbox();
        loginPage.clickLoginToSendOTP();
        loginPage.fetchOTPFromYopmail();
        loginPage.clickVerifyOTP();
        Thread.sleep(2000);

        // Navigate to My Blogs and check status
        blogPage.verifyBlogInMyBlogs();
        String status = blogPage.getBlogStatus();
        // After unpublish, blog may show "Unpublished" status OR be removed from list entirely
        Assert.assertTrue(
                status.contains("Unpublished") || status.contains("Unpublish") || status.equals("Unknown"),
                "Blog status should be Unpublished or removed from list but was: " + status);
        log.info("✅ Blog status verified: {}", status);
    }

    @Test(priority = 8, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogUnpublished",
          description = "Verify unpublished blog is removed from public blogs listing page (Beta only)")
    public void verifyBlogRemovedFromPublicPage() throws Exception {
        if (!isBeta()) {
            log.info("⏭ Skipping verifyBlogRemovedFromPublicPage — runs only on Beta environment");
            throw new org.testng.SkipException("Admin flow skipped on prod environment");
        }
        log.info("Starting: Verify Blog Removed from Public Page");

        boolean visible = blogPage.isBlogVisibleOnPublicPage();
        Assert.assertFalse(visible, "Unpublished blog should NOT be visible on public blogs page");
        log.info("✅ Blog removed from public page after unpublish");
    }
}
