package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;

import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOCreatePage;

/**
 * VOCreateTest - Fills the VO Template creation form and submits.
 *
 * Prerequisite: Must be on the Create Template page (after VOTemplateNodalDyoTest).
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-vo.xml
 */
@Listeners(TestListeners.class)
public class VOCreateTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOCreateTest.class);

    private VOCreatePage voCreatePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        voCreatePage = new VOCreatePage(driver);
    }

    @Test(priority = 1, groups = {"vo", "create"})
    public void createVOTemplate() throws Exception {
        log.info("=== Starting: Create VO Template ===");

        // Fill the template form (includes clicking all toggles)
        voCreatePage.fillTemplateFormAndSubmit();

        // Click Create button
        voCreatePage.clickCreate();

        log.info("=== ✅ VO Template Created PASSED ===");
    }
}
