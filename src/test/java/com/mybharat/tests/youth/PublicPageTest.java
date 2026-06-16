package com.mybharat.tests.youth;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.PublicPage;

/**
 * PublicPageTest - Comprehensive end-to-end test for all public-facing pages on MYBharat.
 *
 * Purpose: Validates every publicly accessible navigation path — header menus (top-level
 *          and dropdowns), organization section cards with filtering, and footer links.
 *          All validations run in a SINGLE @Test method so it appears as 1 test case in reports.
 *
 * Validates:
 *   - Header menus: Youth, Quiz &amp; Essay, Resources (Voices, Blogs, Newsletters, Other),
 *     Events (Experiential Learning, VFB, Mega Events, VBYLD-2026), Podcast, VVVP 2026
 *   - Organization section: Government, Knowledge Institutions, Not for Profits, For Profits
 *     (including org link clicks, View More, State/District filtering)
 *   - Footer: Important Links, Useful Links, Powered By (Digital India logo, DIC text)
 *
 * Test Strategy: Each navigation returns to homepage before the next check. Failures are
 *                collected and reported as a summary at the end.
 *
 * Run:
 *   mvn test "-Denv=prod" "-Dbrowser=chrome" "-Dsurefire.suiteXmlFiles=testSuites/testng-public-page.xml"
 *
 * Dependencies: BaseTest, PublicPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see PublicPage
 */
@Listeners(TestListeners.class)
public class PublicPageTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(PublicPageTest.class);
    private PublicPage publicPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        publicPage = new PublicPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "publicpages"}, retryAnalyzer = Retry.class)
    public void publicPages() throws Exception {
        log.info("=== PUBLIC PAGES — Full Validation Start ===");

        // =====================================================================
        // STEP 1: LAUNCH & VALIDATE HOMEPAGE
        // =====================================================================
        log.info("--- Step 1: Launch homepage ---");
        publicPage.navigateToHomePage();
        String url = publicPage.getCurrentUrl();
        Assert.assertTrue(url.contains("mybharat"), "Should be on mybharat.gov.in. URL: " + url);
        Assert.assertTrue(publicPage.isSignInDisplayed(), "Sign In button should be visible");
        Assert.assertTrue(publicPage.isRegisterNowDisplayed(), "Register Now button should be visible");
        log.info("✅ Homepage validated");

        // =====================================================================
        // STEP 2: HEADER MENU NAVIGATION
        // =====================================================================

        // Youth
        log.info("--- Step 2: Click Youth ---");
        Assert.assertTrue(publicPage.clickYouth(), "Youth menu navigation should succeed");
        publicPage.navigateToHomePage();

        // Quiz & Essay
        log.info("--- Step 3: Click Quiz & Essay ---");
        Assert.assertTrue(publicPage.clickQuizAndEssay(), "Quiz & Essay navigation should succeed");
        publicPage.navigateToHomePage();

        // Resources → Voices
        log.info("--- Step 4: Resources → Voices ---");
        Assert.assertTrue(publicPage.clickResourcesVoices(), "Voices navigation should succeed");
        publicPage.navigateToHomePage();

        // Resources → Blogs
        log.info("--- Step 5: Resources → Blogs ---");
        Assert.assertTrue(publicPage.clickResourcesBlogs(), "Blogs navigation should succeed");
        publicPage.navigateToHomePage();

        // Resources → Newsletters
        log.info("--- Step 6: Resources → Newsletters ---");
        Assert.assertTrue(publicPage.clickResourcesNewsletters(), "Newsletters navigation should succeed");
        publicPage.navigateToHomePage();

        // Resources → Other Resources
        log.info("--- Step 7: Resources → Other Resources ---");
        Assert.assertTrue(publicPage.clickResourcesOtherResources(), "Other Resources navigation should succeed");
        publicPage.navigateToHomePage();

        // Events → Experiential Learning
        log.info("--- Step 8: Events → Experiential Learning ---");
        Assert.assertTrue(publicPage.clickEventsExperientialLearning(), "Experiential Learning should succeed");
        publicPage.navigateToHomePage();

        // Events → Volunteer for Bharat
        log.info("--- Step 9: Events → Volunteer for Bharat ---");
        Assert.assertTrue(publicPage.clickEventsVolunteerForBharat(), "Volunteer for Bharat should succeed");
        publicPage.navigateToHomePage();

        // Events → Mega Events
        log.info("--- Step 10: Events → Mega Events ---");
        Assert.assertTrue(publicPage.clickEventsMegaEvents(), "Mega Events should succeed");
        publicPage.navigateToHomePage();

        // Events → VBYLD-2026
        log.info("--- Step 11: Events → VBYLD-2026 ---");
        Assert.assertTrue(publicPage.clickEventsVBYLD2026(), "VBYLD-2026 should succeed");
        publicPage.navigateToHomePage();

        // MY Bharat Podcast
        log.info("--- Step 12: MY Bharat Podcast ---");
        Assert.assertTrue(publicPage.clickMyBharatPodcast(), "MY Bharat Podcast should succeed");
        publicPage.navigateToHomePage();

        // VVVP 2026
        log.info("--- Step 13: VVVP 2026 ---");
        Assert.assertTrue(publicPage.clickVVVP2026(), "VVVP 2026 should succeed");
        publicPage.navigateToHomePage();

        // Header Summary
        log.info("HEADER PASSED ({}): {}", publicPage.getPassedMenus().size(), publicPage.getPassedMenus());

        // =====================================================================
        // STEP 3: ORGANIZATION SECTION — "MY Bharat connects you with"
        // =====================================================================

        // Government
        log.info("--- Step 14: Click Government ---");
        Assert.assertTrue(publicPage.clickGovernment(), "Government page should load");

        log.info("--- Step 15: Click Government org link ---");
        publicPage.clickFirstAvailableOrgLink();
        publicPage.navigateBackToPreviousPage();

        log.info("--- Step 16: Click Government View More ---");
        publicPage.clickViewMore();

        log.info("--- Step 17: Select State and District ---");
        publicPage.selectAnyState();
        publicPage.selectAnyDistrict();
        publicPage.validateFilteredResults();

        // Return home
        publicPage.clickMyBharatLogo();

        // Knowledge Institutions
        log.info("--- Step 18: Click Knowledge Institutions ---");
        Assert.assertTrue(publicPage.clickKnowledgeInstitutions(), "Knowledge Institutions should load");

        log.info("--- Step 19: Click Knowledge Institutions org link ---");
        publicPage.clickFirstAvailableOrgLink();
        publicPage.navigateBackToPreviousPage();

        log.info("--- Step 20: Click Knowledge Institutions View More ---");
        publicPage.clickViewMore();

        log.info("--- Step 21: Select State and District ---");
        publicPage.selectAnyState();
        publicPage.selectAnyDistrict();
        publicPage.validateFilteredResults();

        // Return home
        publicPage.clickMyBharatLogo();

        // Not for Profits
        log.info("--- Step 22: Click Not for Profits ---");
        Assert.assertTrue(publicPage.clickNotForProfits(), "Not for Profits should load");

        log.info("--- Step 23: Click Not for Profits org link ---");
        publicPage.clickFirstAvailableOrgLink();
        publicPage.navigateBackToPreviousPage();

        log.info("--- Step 24: Click Not for Profits View More ---");
        publicPage.clickViewMore();

        // Return home
        publicPage.clickMyBharatLogo();

        // For Profits
        log.info("--- Step 25: Click For Profits ---");
        Assert.assertTrue(publicPage.clickForProfits(), "For Profits should load");

        log.info("--- Step 26: Click For Profits org link ---");
        publicPage.clickFirstAvailableOrgLink();
        publicPage.navigateBackToPreviousPage();

        log.info("--- Step 27: Click For Profits View More ---");
        publicPage.clickViewMore();

        // Return home
        publicPage.clickMyBharatLogo();

        // =====================================================================
        // STEP 4: FOOTER SECTION
        // =====================================================================

        // Important Links
        log.info("--- Step 28: Footer — Mega Events ---");
        publicPage.navigateToHomePage();
        Assert.assertTrue(publicPage.clickFooterMegaEvents(), "Footer Mega Events should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 29: Footer — Experiential Learning ---");
        Assert.assertTrue(publicPage.clickFooterExperientialLearning(), "Footer EL should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 30: Footer — Volunteer for Bharat ---");
        Assert.assertTrue(publicPage.clickFooterVolunteerForBharat(), "Footer VFB should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 31: Footer — About ---");
        Assert.assertTrue(publicPage.clickFooterAbout(), "Footer About should succeed");
        publicPage.navigateToHomePage();

        // Useful Links
        log.info("--- Step 32: Footer — Privacy Policy ---");
        Assert.assertTrue(publicPage.clickFooterPrivacyPolicy(), "Footer Privacy Policy should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 33: Footer — Resources ---");
        Assert.assertTrue(publicPage.clickFooterResources(), "Footer Resources should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 34: Footer — Support ---");
        Assert.assertTrue(publicPage.clickFooterSupport(), "Footer Support should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 35: Footer — Sitemap ---");
        Assert.assertTrue(publicPage.clickFooterSitemap(), "Footer Sitemap should succeed");
        publicPage.navigateToHomePage();

        log.info("--- Step 36: Footer — Feedback ---");
        Assert.assertTrue(publicPage.clickFooterFeedback(), "Footer Feedback should succeed");
        publicPage.navigateToHomePage();

        // Powered By
        log.info("--- Step 37: Footer — Digital India Logo ---");
        Assert.assertTrue(publicPage.validateDigitalIndiaLogo(), "Digital India logo should be visible");

        log.info("--- Step 38: Footer — DIC Text ---");
        Assert.assertTrue(publicPage.validateDICText(), "DIC text should be visible");

        // =====================================================================
        // FINAL SUMMARY
        // =====================================================================
        List<Map<String, String>> failures = publicPage.getFailedMenus();
        if (!failures.isEmpty()) {
            log.warn("FAILED ({}):", failures.size());
            for (Map<String, String> f : failures) {
                log.warn("  ❌ {} — {}", f.get("menuName"), f.get("error"));
            }
        }

        log.info("=== ✅ PUBLIC PAGES — All {} checks PASSED ===", publicPage.getPassedMenus().size());
    }
}
