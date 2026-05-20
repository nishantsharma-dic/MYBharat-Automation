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
 * BlogTest - Creates a blog post on MY Bharat.
 * 
 * Runs on the SAME browser session AFTER QuizCertificateVerificationTest.
 * User is already logged in.
 * 
 * Flow: Navigate to Blogs → Write a Blog → Fill form → Preview → Post → Verify success
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

        // Step 1: Navigate to Blogs page
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

    @Test(priority = 3, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogInMyBlogs",
          description = "Admin approves the blog: Open admin window → Login → Newsletter & Blogs → Approve")
    public void adminApprovesBlog() throws Exception {
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
          description = "Verify blog status changed to Approved/Published in youth My Blogs")
    public void verifyBlogApproved() throws Exception {
        log.info("Starting: Verify Blog Approved");

        // Logout admin, login as youth via OTP
        adminPage.switchToYouthWindow();

        // Re-login as youth and navigate to My Blogs
        // Use the LoginPage to login again
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
          description = "Verify published blog is visible on public blogs listing page")
    public void verifyBlogVisibleOnPublicPage() throws Exception {
        log.info("Starting: Verify Blog Visible on Public Page");

        boolean visible = blogPage.isBlogVisibleOnPublicPage();
        Assert.assertTrue(visible, "Published blog should be visible on public blogs page");
        log.info("✅ Blog visible on public page");
    }

    @Test(priority = 6, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogVisibleOnPublicPage",
          description = "Admin unpublishes the blog")
    public void adminUnpublishesBlog() throws Exception {
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
          description = "Verify blog status changed to Unpublished in youth My Blogs")
    public void verifyBlogUnpublished() throws Exception {
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
        Assert.assertTrue(status.contains("Unpublished") || status.contains("Unpublish"),
                "Blog status should be Unpublished but was: " + status);
        log.info("✅ Blog status verified: {}", status);
    }

    @Test(priority = 8, groups = {"regression", "blog"}, dependsOnMethods = "verifyBlogUnpublished",
          description = "Verify unpublished blog is removed from public blogs listing page")
    public void verifyBlogRemovedFromPublicPage() throws Exception {
        log.info("Starting: Verify Blog Removed from Public Page");

        boolean visible = blogPage.isBlogVisibleOnPublicPage();
        Assert.assertFalse(visible, "Unpublished blog should NOT be visible on public blogs page");
        log.info("✅ Blog removed from public page after unpublish");
    }
}
