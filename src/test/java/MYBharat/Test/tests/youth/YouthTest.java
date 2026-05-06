package MYBharat.Test.tests.youth;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import MYBharat.Test.TestComponents.BaseTest;
import MYBharat.Test.TestComponents.Retry;
import MYBharat_ResourcesAndAbstractComponents.pageobjects.LandingPage;
import pageobjects.Youth.PlayQuiz;
import pageobjects.Youth.PublicProfile;
import pageobjects.Youth.Registration;

/**
 * End-to-end test suite for the Youth module.
 *
 * <p>Test execution order:
 * <ol>
 *   <li>signUpNationalUser     – register an Indian youth account</li>
 *   <li>signUpInternationalUser – register an international youth account</li>
 *   <li>fillYouthPublicProfile  – complete the public profile and download certificate</li>
 *   <li>playQuiz               – attempt and submit a competitive quiz</li>
 * </ol>
 *
 * <p>Run a specific test via the testSuites/testng-suite_Youth.xml suite file.
 */
@Listeners(MYBharat.Test.TestComponents.Listeners.class)
public class YouthTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(YouthTest.class);

    // -------------------------------------------------------------------------
    // Registration tests
    // -------------------------------------------------------------------------

    /**
     * Registers a new Indian (national) youth user end-to-end:
     * landing page → OTP verification → registration form.
     */
    @Test(enabled = true, priority = 1, groups = {"smoke", "youthRegistration"},
          retryAnalyzer = Retry.class)
    public void signUpNationalUser() throws IOException, InterruptedException {
        log.info("Starting test: signUpNationalUser");
        LandingPage landingPage = new LandingPage(driver);
        landingPage.goTo();
        Registration registerYouth = new Registration(driver);
        registerYouth.register_verifyOTPandGoToRegistrationForIndians();
        registerYouth.registerForIndians();
        log.info("Completed test: signUpNationalUser");
    }

    /**
     * Registers a new international youth user end-to-end:
     * landing page → OTP verification → registration form.
     */
    @Test(enabled = true, priority = 2, groups = {"regression", "youthRegistration"},
          retryAnalyzer = Retry.class)
    public void signUpInternationalUser() throws IOException, InterruptedException {
        log.info("Starting test: signUpInternationalUser");
        LandingPage landingPage = new LandingPage(driver);
        landingPage.goTo();
        Registration registerYouth = new Registration(driver);
        registerYouth.register_verifyOTPandGoToRegistrationForInternalationalUsers();
        registerYouth.registerForInternationalUsers();
        log.info("Completed test: signUpInternationalUser");
    }

    // -------------------------------------------------------------------------
    // Profile & quiz tests (depend on a successful registration in the same session)
    // -------------------------------------------------------------------------

    /**
     * Fills the youth public profile and downloads the registration certificate.
     * Depends on a prior successful registration in the same browser session.
     */
    @Test(enabled = false, priority = 3, groups = {"regression"},
          dependsOnMethods = {"signUpNationalUser"},
          retryAnalyzer = Retry.class)
    public void fillYouthPublicProfile() throws IOException, InterruptedException {
        log.info("Starting test: fillYouthPublicProfile");
        PublicProfile publicProfile = new PublicProfile(driver);
        publicProfile.registrationCertificateDownload();
        log.info("Completed test: fillYouthPublicProfile");
    }

    /**
     * Navigates to the quiz section, selects a language, and attempts all questions.
     */
    @Test(enabled = false, priority = 4, groups = {"regression"},
          dependsOnMethods = {"fillYouthPublicProfile"},
          retryAnalyzer = Retry.class)
    public void playQuiz() throws Exception {
        log.info("Starting test: playQuiz");
        PlayQuiz playQuiz = new PlayQuiz(driver, "English");
        playQuiz.registerForQuiz();
        playQuiz.competativeQuiz_AllQuesAttemptAndSubmit();
        log.info("Completed test: playQuiz");
    }
}
