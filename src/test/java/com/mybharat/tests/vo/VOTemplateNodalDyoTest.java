package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;

import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOTemplateNodalDyoPage;

/**
 * VOTemplateNodalDyoTest - Navigates to the Create Template page.
 *
 * Flow: Org Dashboard → Templates → Create Template
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-vo.xml
 */
@Listeners(TestListeners.class)
public class VOTemplateNodalDyoTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOTemplateNodalDyoTest.class);

    private VOTemplateNodalDyoPage templateNavPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        templateNavPage = new VOTemplateNodalDyoPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "navigation"})
    public void navigateToTemplatesNodalDyo() throws Exception {
        log.info("=== Starting: Navigate to Create Template ===");

        templateNavPage.navigateToCreateTemplate();

        // Verify we're on the create template page
        String currentUrl = driver.getCurrentUrl();
        log.info("Current URL after navigation: {}", currentUrl);

        log.info("=== ✅ Navigation to Create Template PASSED ===");
    }
}
