package com.mybharat.pages.org;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * QuizCreatorPage — Page Object for the Quiz Creator Portal at
 * https://mybharat.gov.in/quizzes/partner
 *
 * Handles: Login, Navigation, Quiz Form Fill (Title, Description, Banner,
 * Category, Eligibility, Schedule, Config), Save Draft, Preview, Submit/Publish.
 *
 * Developer: Nishant Sharma (QA Team)
 */
public class QuizCreatorPage extends BasePage {

    private static final Logger log = LogManager.getLogger(QuizCreatorPage.class);
    private final ConfigReader config = new ConfigReader();
    private final JavascriptExecutor js;

    private static final String QUIZ_PORTAL_URL = "https://mybharat.gov.in/quizzes/partner";

    // =========================================================================
    // LOGIN LOCATORS
    // =========================================================================

    @FindBy(xpath = "//input[@type='email' or @placeholder='Email' or @name='email' or @formcontrolname='email']")
    private WebElement emailInput;

    @FindBy(xpath = "//input[@type='password' or @placeholder='Password' or @name='password' or @formcontrolname='password']")
    private WebElement passwordInput;

    @FindBy(xpath = "//button[contains(text(),'Login') or contains(text(),'Sign In') or contains(text(),'LOG IN')]")
    private WebElement loginButton;

    // =========================================================================
    // NAVIGATION LOCATORS
    // =========================================================================

    @FindBy(xpath = "//a[contains(text(),'Create Quiz') or contains(@href,'create')] | //button[contains(text(),'Create Quiz') or contains(text(),'Create New')]")
    private WebElement createQuizLink;

    // =========================================================================
    // QUIZ FORM — BASIC INFO LOCATORS
    // =========================================================================

    @FindBy(xpath = "//label[contains(text(),'Quiz Title') or contains(text(),'Title')]/following-sibling::input | //input[@placeholder='Quiz Title' or @placeholder='Enter quiz title' or @formcontrolname='title']")
    private WebElement quizTitleInput;

    @FindBy(xpath = "//div[contains(@class,'ck-editor__editable') or contains(@class,'ck-content')] | //textarea[@placeholder='Description' or @formcontrolname='description'] | //label[contains(text(),'Description')]/following-sibling::textarea")
    private WebElement descriptionEditor;

    @FindBy(xpath = "//input[@type='file' and (@accept='image/*' or contains(@accept,'image'))] | //input[@id='bannerImage' or @name='bannerImage']")
    private WebElement bannerImageUpload;

    // =========================================================================
    // QUIZ FORM — CATEGORY LOCATOR
    // =========================================================================

    @FindBy(xpath = "//label[contains(text(),'Category')]/following-sibling::select | //mat-select[@formcontrolname='category'] | //select[@formcontrolname='category'] | //label[contains(text(),'Category')]/following::select[1]")
    private WebElement categoryDropdown;

    // =========================================================================
    // QUIZ FORM — ELIGIBILITY LOCATORS
    // =========================================================================

    @FindBy(xpath = "//label[contains(text(),'Education')]/following-sibling::select | //mat-select[@formcontrolname='education'] | //select[@formcontrolname='education'] | //label[contains(text(),'Education')]/following::select[1]")
    private WebElement educationDropdown;

    @FindBy(xpath = "//input[@formcontrolname='ageFrom' or @placeholder='Age From' or @name='ageFrom'] | //label[contains(text(),'Age')]/following::input[1]")
    private WebElement ageFromInput;

    @FindBy(xpath = "//input[@formcontrolname='ageTo' or @placeholder='Age To' or @name='ageTo'] | //label[contains(text(),'Age')]/following::input[2]")
    private WebElement ageToInput;

    @FindBy(xpath = "//label[contains(text(),'Gender')]/following-sibling::select | //mat-select[@formcontrolname='gender'] | //select[@formcontrolname='gender'] | //label[contains(text(),'Gender')]/following::select[1]")
    private WebElement genderDropdown;

    @FindBy(xpath = "//label[contains(text(),'Language')]/following-sibling::select | //mat-select[@formcontrolname='languages'] | //select[@formcontrolname='languages'] | //label[contains(text(),'Language')]/following::select[1]")
    private WebElement languagesSelect;

    @FindBy(xpath = "//label[contains(text(),'Sport')]/following-sibling::select | //mat-select[@formcontrolname='sports'] | //select[@formcontrolname='sports'] | //label[contains(text(),'Sport')]/following::select[1]")
    private WebElement sportsSelect;

    // =========================================================================
    // QUIZ FORM — SCHEDULE LOCATORS
    // =========================================================================

    @FindBy(xpath = "//input[@formcontrolname='startDate' or @placeholder='Start Date' or @name='startDate' or @type='date' and contains(@id,'start')] | //label[contains(text(),'Start Date')]/following::input[1]")
    private WebElement startDateInput;

    @FindBy(xpath = "//input[@formcontrolname='endDate' or @placeholder='End Date' or @name='endDate' or @type='date' and contains(@id,'end')] | //label[contains(text(),'End Date')]/following::input[1]")
    private WebElement endDateInput;

    @FindBy(xpath = "//input[@formcontrolname='durationHours' or @placeholder='HH' or @name='durationHours'] | //label[contains(text(),'Duration')]/following::input[1]")
    private WebElement durationHoursInput;

    @FindBy(xpath = "//input[@formcontrolname='durationMinutes' or @placeholder='MM' or @name='durationMinutes'] | //label[contains(text(),'Duration')]/following::input[2]")
    private WebElement durationMinutesInput;

    // =========================================================================
    // QUIZ FORM — CONFIG LOCATORS
    // =========================================================================

    @FindBy(xpath = "//input[@formcontrolname='maxAttempts' or @placeholder='Max Attempts' or @name='maxAttempts'] | //label[contains(text(),'Max Attempts')]/following::input[1]")
    private WebElement maxAttemptsInput;

    @FindBy(xpath = "//input[@formcontrolname='totalQuestions' or @placeholder='Total Questions' or @name='totalQuestions'] | //label[contains(text(),'Total Questions')]/following::input[1]")
    private WebElement totalQuestionsInput;

    @FindBy(xpath = "//input[@formcontrolname='totalMarks' or @name='totalMarks' or @readonly] | //label[contains(text(),'Total Marks')]/following::input[1]")
    private WebElement totalMarksDisplay;

    @FindBy(xpath = "//input[@formcontrolname='passingScore' or @placeholder='Passing Score' or @name='passingScore'] | //label[contains(text(),'Passing Score')]/following::input[1]")
    private WebElement passingScoreInput;

    @FindBy(xpath = "//select[@formcontrolname='passingScoreType' or @name='passingScoreType'] | //mat-select[@formcontrolname='passingScoreType'] | //label[contains(text(),'Passing Score Type') or contains(text(),'Score Type')]/following::select[1]")
    private WebElement passingScoreTypeDropdown;

    // =========================================================================
    // ACTION BUTTON LOCATORS
    // =========================================================================

    @FindBy(xpath = "//button[contains(text(),'Save Draft') or contains(text(),'Save as Draft') or contains(text(),'SAVE DRAFT')]")
    private WebElement saveDraftButton;

    @FindBy(xpath = "//button[contains(text(),'Preview') or contains(text(),'PREVIEW')]")
    private WebElement previewButton;

    @FindBy(xpath = "//button[contains(text(),'Submit') or contains(text(),'Publish') or contains(text(),'SUBMIT') or contains(text(),'PUBLISH')]")
    private WebElement submitButton;

    // =========================================================================
    // VERIFICATION LOCATORS
    // =========================================================================

    @FindBy(xpath = "//*[contains(@class,'success') or contains(@class,'toast-success') or contains(@class,'alert-success')] | //*[contains(text(),'successfully') or contains(text(),'Quiz Created')]")
    private WebElement successMessage;

    @FindBy(xpath = "//*[contains(@class,'error') or contains(@class,'invalid-feedback') or contains(@class,'mat-error') or contains(@class,'validation-error')]")
    private List<WebElement> validationErrors;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public QuizCreatorPage(WebDriver driver) {
        super(driver);
        this.js = (JavascriptExecutor) driver;
    }

    // =========================================================================
    // LOGIN FLOW
    // =========================================================================

    /**
     * Navigate to the Quiz Partner Portal and login with email/password.
     */
    public void login(String email, String password) {
        log.info("Logging in to Quiz Creator Portal with email: {}", email);
        driver.get(QUIZ_PORTAL_URL);
        waitForPageLoad();
        closePopupIfPresent();
        safeSleep(2000);

        waitForVisible(emailInput);
        clearAndType(emailInput, email);
        log.info("  Email entered");

        waitForVisible(passwordInput);
        clearAndType(passwordInput, password);
        log.info("  Password entered");

        waitForClickable(loginButton);
        safeClick(loginButton);
        log.info("  Login button clicked");

        // Wait for dashboard/home to load after login
        safeSleep(3000);
        waitForPageLoad();
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d ->
                !d.getCurrentUrl().contains("login") || d.getCurrentUrl().contains("partner"));
        log.info("✅ Login successful — URL: {}", driver.getCurrentUrl());
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    /**
     * Navigate to the Create Quiz page from the partner dashboard.
     */
    public void navigateToCreateQuiz() {
        log.info("Navigating to Create Quiz");
        waitForPageLoad();
        safeSleep(2000);

        try {
            waitForClickable(createQuizLink);
            scrollToElement(createQuizLink);
            safeClick(createQuizLink);
        } catch (Exception e) {
            // Fallback: direct navigation
            log.warn("Create Quiz link not found, navigating directly");
            driver.get(QUIZ_PORTAL_URL + "/create");
        }

        waitForPageLoad();
        safeSleep(2000);
        log.info("✅ Create Quiz page loaded — URL: {}", driver.getCurrentUrl());
    }

    // =========================================================================
    // QUIZ FORM — BASIC INFO
    // =========================================================================

    /**
     * Enter the quiz title.
     */
    public void enterQuizTitle(String title) {
        log.info("Entering Quiz Title: {}", title);
        waitForVisible(quizTitleInput);
        scrollToElement(quizTitleInput);
        clearAndType(quizTitleInput, title);
        safeSleep(500);
        log.info("  ✅ Quiz Title entered");
    }

    /**
     * Enter quiz description. Handles both CKEditor and plain textarea.
     */
    public void enterDescription(String description) {
        log.info("Entering Description");
        scrollToElement(descriptionEditor);
        safeSleep(500);

        try {
            // Try CKEditor first
            WebElement ckEditor = driver.findElement(By.xpath(
                    "//div[contains(@class,'ck-editor__editable') or contains(@class,'ck-content')]"));
            waitForClickable(ckEditor);
            safeClick(ckEditor);
            ckEditor.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            ckEditor.sendKeys(Keys.DELETE);
            ckEditor.sendKeys(description);
            log.info("  ✅ Description entered via CKEditor");
        } catch (Exception e) {
            // Fallback: plain textarea
            try {
                waitForVisible(descriptionEditor);
                clearAndType(descriptionEditor, description);
                log.info("  ✅ Description entered via textarea");
            } catch (Exception e2) {
                // JS fallback for stubborn editors
                js.executeScript(
                        "var editors = document.querySelectorAll('[contenteditable=true], textarea[name*=\"desc\"], textarea[formcontrolname*=\"desc\"]');" +
                        "if(editors.length > 0) { editors[0].innerHTML = arguments[0]; editors[0].dispatchEvent(new Event('input', {bubbles:true})); }",
                        description);
                log.info("  ✅ Description entered via JS fallback");
            }
        }
    }

    /**
     * Upload banner image for the quiz.
     */
    public void uploadBannerImage(String imagePath) {
        log.info("Uploading Banner Image: {}", imagePath);

        try {
            // Try standard file input
            WebElement fileInput = driver.findElement(By.xpath(
                    "//input[@type='file'][contains(@accept,'image') or not(@accept)]"));
            fileInput.sendKeys(imagePath);
            safeSleep(3000);
            log.info("  ✅ Banner image uploaded via file input");
        } catch (Exception e) {
            // Inject hidden file input if not available
            log.info("  Standard file input not found, injecting one");
            injectFileInputAndUpload(imagePath, "bannerImage");
        }
    }

    /**
     * Upload banner using default test image from resources.
     */
    public void uploadBannerImage() {
        uploadBannerImage(getTestImagePath());
    }

    // =========================================================================
    // QUIZ FORM — CATEGORY
    // =========================================================================

    /**
     * Select quiz category from dropdown.
     */
    public void selectCategory(String category) {
        log.info("Selecting Category: {}", category);
        scrollToElement(categoryDropdown);
        safeSleep(500);

        try {
            // Try standard HTML select
            new org.openqa.selenium.support.ui.Select(categoryDropdown).selectByVisibleText(category);
            log.info("  ✅ Category selected via standard select");
        } catch (Exception e) {
            // Try mat-select or custom dropdown
            selectCustomDropdown(categoryDropdown, category, "Category");
        }
        safeSleep(1000);
    }

    // =========================================================================
    // QUIZ FORM — ELIGIBILITY
    // =========================================================================

    /**
     * Select education eligibility.
     */
    public void selectEducation(String education) {
        log.info("Selecting Education: {}", education);
        scrollToElement(educationDropdown);
        safeSleep(500);

        try {
            new org.openqa.selenium.support.ui.Select(educationDropdown).selectByVisibleText(education);
            log.info("  ✅ Education selected via standard select");
        } catch (Exception e) {
            selectCustomDropdown(educationDropdown, education, "Education");
        }
        safeSleep(500);
    }

    /**
     * Select multiple education levels (for multi-select).
     */
    public void selectEducation(String... educationLevels) {
        log.info("Selecting Education levels: {}", (Object) educationLevels);
        scrollToElement(educationDropdown);
        safeSleep(500);
        selectMultipleOptions(educationDropdown, "Education", educationLevels);
    }

    /**
     * Enter age range for eligibility.
     */
    public void enterAgeRange(String ageFrom, String ageTo) {
        log.info("Entering Age Range: {} - {}", ageFrom, ageTo);

        waitForVisible(ageFromInput);
        scrollToElement(ageFromInput);
        clearAndType(ageFromInput, ageFrom);

        waitForVisible(ageToInput);
        clearAndType(ageToInput, ageTo);

        safeSleep(300);
        log.info("  ✅ Age range entered");
    }

    /**
     * Select gender eligibility.
     */
    public void selectGender(String gender) {
        log.info("Selecting Gender: {}", gender);
        scrollToElement(genderDropdown);
        safeSleep(500);

        try {
            new org.openqa.selenium.support.ui.Select(genderDropdown).selectByVisibleText(gender);
            log.info("  ✅ Gender selected via standard select");
        } catch (Exception e) {
            selectCustomDropdown(genderDropdown, gender, "Gender");
        }
        safeSleep(500);
    }

    /**
     * Select multiple genders (for multi-select).
     */
    public void selectGender(String... genders) {
        log.info("Selecting Genders: {}", (Object) genders);
        scrollToElement(genderDropdown);
        safeSleep(500);
        selectMultipleOptions(genderDropdown, "Gender", genders);
    }

    /**
     * Select languages for the quiz.
     */
    public void selectLanguages(String... languages) {
        log.info("Selecting Languages: {}", (Object) languages);
        scrollToElement(languagesSelect);
        safeSleep(500);
        selectMultipleOptions(languagesSelect, "Language", languages);
    }

    /**
     * Select sports categories for the quiz.
     */
    public void selectSports(String... sports) {
        log.info("Selecting Sports: {}", (Object) sports);
        scrollToElement(sportsSelect);
        safeSleep(500);
        selectMultipleOptions(sportsSelect, "Sport", sports);
    }

    // =========================================================================
    // QUIZ FORM — SCHEDULE
    // =========================================================================

    /**
     * Set quiz start date. Accepts format yyyy-MM-dd.
     */
    public void setStartDate(String startDate) {
        log.info("Setting Start Date: {}", startDate);
        scrollToElement(startDateInput);
        safeSleep(500);
        setDateField(startDateInput, startDate);
        log.info("  ✅ Start date set");
    }

    /**
     * Set quiz end date. Accepts format yyyy-MM-dd.
     */
    public void setEndDate(String endDate) {
        log.info("Setting End Date: {}", endDate);
        scrollToElement(endDateInput);
        safeSleep(500);
        setDateField(endDateInput, endDate);
        log.info("  ✅ End date set");
    }

    /**
     * Set quiz duration in hours and minutes.
     */
    public void setDuration(String hours, String minutes) {
        log.info("Setting Duration: {}:{}", hours, minutes);

        waitForVisible(durationHoursInput);
        scrollToElement(durationHoursInput);
        clearAndType(durationHoursInput, hours);

        waitForVisible(durationMinutesInput);
        clearAndType(durationMinutesInput, minutes);

        safeSleep(300);
        log.info("  ✅ Duration set");
    }

    // =========================================================================
    // QUIZ FORM — CONFIGURATION
    // =========================================================================

    /**
     * Enter max attempts allowed.
     */
    public void enterMaxAttempts(String maxAttempts) {
        log.info("Entering Max Attempts: {}", maxAttempts);
        waitForVisible(maxAttemptsInput);
        scrollToElement(maxAttemptsInput);
        clearAndType(maxAttemptsInput, maxAttempts);
        safeSleep(300);
        log.info("  ✅ Max Attempts entered");
    }

    /**
     * Enter total number of questions.
     */
    public void enterTotalQuestions(String totalQuestions) {
        log.info("Entering Total Questions: {}", totalQuestions);
        waitForVisible(totalQuestionsInput);
        scrollToElement(totalQuestionsInput);
        clearAndType(totalQuestionsInput, totalQuestions);
        safeSleep(300);
        log.info("  ✅ Total Questions entered");
    }

    /**
     * Get the auto-calculated total marks (readonly field).
     */
    public String getTotalMarks() {
        waitForVisible(totalMarksDisplay);
        String value = totalMarksDisplay.getAttribute("value");
        if (value == null || value.isEmpty()) {
            value = totalMarksDisplay.getText();
        }
        log.info("  Total Marks (auto-calc): {}", value);
        return value;
    }

    /**
     * Enter passing score.
     */
    public void enterPassingScore(String passingScore) {
        log.info("Entering Passing Score: {}", passingScore);
        waitForVisible(passingScoreInput);
        scrollToElement(passingScoreInput);
        clearAndType(passingScoreInput, passingScore);
        safeSleep(300);
        log.info("  ✅ Passing Score entered");
    }

    /**
     * Select passing score type: "Percentage" or "Marks".
     */
    public void selectPassingScoreType(String scoreType) {
        log.info("Selecting Passing Score Type: {}", scoreType);
        scrollToElement(passingScoreTypeDropdown);
        safeSleep(500);

        try {
            new org.openqa.selenium.support.ui.Select(passingScoreTypeDropdown).selectByVisibleText(scoreType);
            log.info("  ✅ Score Type selected via standard select");
        } catch (Exception e) {
            selectCustomDropdown(passingScoreTypeDropdown, scoreType, "Passing Score Type");
        }
        safeSleep(500);
    }

    // =========================================================================
    // ACTION BUTTONS
    // =========================================================================

    /**
     * Click Save Draft button.
     */
    public void clickSaveDraft() {
        log.info("Clicking Save Draft");
        scrollToElement(saveDraftButton);
        waitForClickable(saveDraftButton);
        safeClick(saveDraftButton);
        safeSleep(3000);
        waitForPageLoad();
        log.info("  ✅ Save Draft clicked");
    }

    /**
     * Click Preview button.
     */
    public void clickPreview() {
        log.info("Clicking Preview");
        scrollToElement(previewButton);
        waitForClickable(previewButton);
        safeClick(previewButton);
        safeSleep(3000);
        waitForPageLoad();
        log.info("  ✅ Preview clicked");
    }

    /**
     * Click Submit/Publish button to publish the quiz.
     */
    public void clickSubmitPublish() {
        log.info("Clicking Submit/Publish");
        scrollToElement(submitButton);
        waitForClickable(submitButton);
        safeClick(submitButton);
        safeSleep(3000);
        waitForPageLoad();

        // Handle confirmation dialog if present
        handleConfirmationDialog();
        safeSleep(3000);
        log.info("  ✅ Submit/Publish clicked");
    }

    // =========================================================================
    // VERIFICATION METHODS
    // =========================================================================

    /**
     * Verify if the quiz was created/published successfully.
     * Checks for success toast, URL change, or success message on page.
     */
    public boolean isQuizCreatedSuccessfully() {
        log.info("Checking if quiz was created successfully");
        safeSleep(3000);

        // Check 1: Success message/toast on page
        try {
            WebElement success = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(
                            "//*[contains(@class,'success') or contains(@class,'toast-success')] | " +
                            "//*[contains(text(),'successfully') or contains(text(),'Quiz Created') or contains(text(),'published')]")));
            if (success.isDisplayed()) {
                log.info("✅ Quiz created successfully — success message found");
                return true;
            }
        } catch (Exception e) {
            // Not found via element — try other checks
        }

        // Check 2: URL changed to quiz detail/list page
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        if (currentUrl.contains("quiz") && (currentUrl.contains("detail") || currentUrl.contains("list")
                || currentUrl.contains("view") || !currentUrl.contains("create"))) {
            log.info("✅ Quiz created successfully — URL changed: {}", currentUrl);
            return true;
        }

        // Check 3: Page source contains success indicators
        String pageSource = driver.getPageSource().toLowerCase();
        if (pageSource.contains("quiz created successfully") || pageSource.contains("published successfully")
                || pageSource.contains("quiz saved")) {
            log.info("✅ Quiz created successfully — found in page source");
            return true;
        }

        log.warn("Quiz creation success not confirmed");
        return false;
    }

    /**
     * Get the quiz title from the form or the confirmation page.
     */
    public String getQuizTitle() {
        log.info("Getting Quiz Title");

        try {
            waitForVisible(quizTitleInput);
            String value = quizTitleInput.getAttribute("value");
            if (value != null && !value.isEmpty()) {
                log.info("  Quiz Title: {}", value);
                return value;
            }
        } catch (Exception e) {
            // Try from page content (on confirmation/detail page)
        }

        // Fallback: try to find title from page heading or breadcrumb
        try {
            WebElement titleEl = driver.findElement(By.xpath(
                    "//h1[contains(@class,'title') or contains(@class,'quiz')] | " +
                    "//h2[contains(@class,'title') or contains(@class,'quiz')] | " +
                    "//*[@class='quiz-title']"));
            String text = titleEl.getText().trim();
            log.info("  Quiz Title (from page): {}", text);
            return text;
        } catch (Exception e) {
            log.warn("  Could not retrieve quiz title");
            return "";
        }
    }

    /**
     * Check if form validation errors are displayed.
     */
    public boolean isFormValidationErrorShown() {
        log.info("Checking for form validation errors");
        safeSleep(1000);

        if (validationErrors != null && !validationErrors.isEmpty()) {
            for (WebElement error : validationErrors) {
                if (error.isDisplayed()) {
                    log.info("  Validation error found: {}", error.getText());
                    return true;
                }
            }
        }

        // Also check for Angular/Material error states
        List<WebElement> matErrors = driver.findElements(By.xpath(
                "//*[contains(@class,'mat-error') or contains(@class,'ng-invalid') and contains(@class,'ng-touched')] | " +
                "//*[contains(@class,'error-message') or contains(@class,'field-error')]"));
        for (WebElement el : matErrors) {
            if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                log.info("  Validation error found: {}", el.getText());
                return true;
            }
        }

        log.info("  No validation errors found");
        return false;
    }

    /**
     * Get all visible validation error messages.
     */
    public List<String> getValidationErrorMessages() {
        log.info("Getting validation error messages");
        List<String> messages = new java.util.ArrayList<>();
        safeSleep(1000);

        // Collect from @FindBy annotated errors
        if (validationErrors != null) {
            for (WebElement error : validationErrors) {
                try {
                    if (error.isDisplayed() && !error.getText().trim().isEmpty()) {
                        messages.add(error.getText().trim());
                    }
                } catch (Exception e) { /* stale element */ }
            }
        }

        // Collect from Angular/Material error elements
        List<WebElement> matErrors = driver.findElements(By.xpath(
                "//*[contains(@class,'mat-error')] | //*[contains(@class,'error-message')] | " +
                "//*[contains(@class,'invalid-feedback')] | //*[contains(@class,'field-error')] | " +
                "mat-error | .mat-mdc-form-field-error"));
        for (WebElement el : matErrors) {
            try {
                if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                    String msg = el.getText().trim();
                    if (!messages.contains(msg)) {
                        messages.add(msg);
                    }
                }
            } catch (Exception e) { /* stale element */ }
        }

        log.info("  Found {} validation error(s): {}", messages.size(), messages);
        return messages;
    }

    // =========================================================================
    // COMPOSITE METHODS (Convenience)
    // =========================================================================

    /**
     * Fill the entire quiz form with all required fields.
     */
    public void fillQuizForm(String title, String description, String category,
                             String ageFrom, String ageTo, String startDate, String endDate,
                             String durationHrs, String durationMins, String maxAttempts,
                             String totalQuestions, String passingScore, String passingScoreType) {

        log.info("=== Filling Complete Quiz Form ===");
        enterQuizTitle(title);
        enterDescription(description);
        uploadBannerImage();
        selectCategory(category);
        enterAgeRange(ageFrom, ageTo);
        setStartDate(startDate);
        setEndDate(endDate);
        setDuration(durationHrs, durationMins);
        enterMaxAttempts(maxAttempts);
        enterTotalQuestions(totalQuestions);
        enterPassingScore(passingScore);
        selectPassingScoreType(passingScoreType);
        log.info("=== Quiz Form Filled ===");
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Select an option from a custom (Material/Angular) dropdown component.
     */
    private void selectCustomDropdown(WebElement dropdown, String optionText, String fieldName) {
        try {
            // Click to open dropdown
            safeClick(dropdown);
            safeSleep(1000);

            // Try mat-option first (Angular Material)
            WebElement option = null;
            try {
                option = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(
                                "//mat-option[contains(.,'" + optionText + "')] | " +
                                "//li[contains(.,'" + optionText + "')] | " +
                                "//div[contains(@class,'option') or contains(@class,'item')][contains(.,'" + optionText + "')]")));
            } catch (Exception e) {
                // Try generic dropdown overlay
                option = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(
                                "//*[contains(@class,'dropdown') or contains(@class,'overlay') or contains(@class,'panel')]" +
                                "//*[contains(text(),'" + optionText + "')]")));
            }

            if (option != null) {
                scrollToElement(option);
                safeClick(option);
                safeSleep(500);
                log.info("  ✅ {} selected: {}", fieldName, optionText);
            }
        } catch (Exception e) {
            // JS fallback: set value directly
            log.warn("  Custom dropdown selection failed for {}, using JS fallback: {}", fieldName, e.getMessage());
            js.executeScript(
                    "arguments[0].value = arguments[1];" +
                    "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));" +
                    "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));",
                    dropdown, optionText);
        }
    }

    /**
     * Select multiple options from a multi-select dropdown/component.
     */
    private void selectMultipleOptions(WebElement selectElement, String fieldName, String... options) {
        try {
            // Try standard multi-select
            org.openqa.selenium.support.ui.Select select =
                    new org.openqa.selenium.support.ui.Select(selectElement);
            if (select.isMultiple()) {
                for (String option : options) {
                    select.selectByVisibleText(option);
                    safeSleep(300);
                }
                log.info("  ✅ {} selected via multi-select: {}", fieldName, (Object) options);
                return;
            }
        } catch (Exception e) {
            // Not a standard select — handle custom multi-select
        }

        // Custom multi-select (click to open panel, then click each option)
        try {
            safeClick(selectElement);
            safeSleep(1000);

            for (String option : options) {
                try {
                    WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                            ExpectedConditions.elementToBeClickable(By.xpath(
                                    "//mat-option[contains(.,'" + option + "')] | " +
                                    "//li[contains(@class,'option') or contains(@class,'item')][contains(.,'" + option + "')] | " +
                                    "//div[contains(@class,'option')][contains(.,'" + option + "')] | " +
                                    "//span[contains(@class,'option')][contains(.,'" + option + "')] | " +
                                    "//label[contains(.,'" + option + "')]/ancestor::*[contains(@class,'option')]")));
                    safeClick(opt);
                    safeSleep(300);
                } catch (Exception optEx) {
                    log.warn("  Option '{}' not found in {} multi-select", option, fieldName);
                }
            }

            // Close multi-select panel (click outside or press Escape)
            try {
                driver.findElement(By.xpath("//div[contains(@class,'backdrop') or contains(@class,'overlay')]")).click();
            } catch (Exception e) {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            }
            safeSleep(500);
            log.info("  ✅ {} multi-select completed", fieldName);
        } catch (Exception e) {
            log.warn("  Multi-select failed for {}: {}", fieldName, e.getMessage());
        }
    }

    /**
     * Set a date field value, handling both native date inputs and datepickers.
     */
    private void setDateField(WebElement dateInput, String dateValue) {
        try {
            // Try native input type=date
            String inputType = dateInput.getAttribute("type");
            if ("date".equals(inputType)) {
                // Use JS to set value for native date inputs (avoids locale issues)
                js.executeScript(
                        "var el = arguments[0];" +
                        "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeSetter.call(el, arguments[1]);" +
                        "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                        "el.dispatchEvent(new Event('change', {bubbles: true}));",
                        dateInput, dateValue);
                safeSleep(500);
                return;
            }

            // Try clicking to open datepicker, then type the value
            safeClick(dateInput);
            safeSleep(500);
            clearAndType(dateInput, dateValue);
            dateInput.sendKeys(Keys.TAB); // Close datepicker
            safeSleep(500);
        } catch (Exception e) {
            // Final JS fallback
            js.executeScript(
                    "arguments[0].value = arguments[1];" +
                    "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    dateInput, dateValue);
            safeSleep(300);
        }
    }

    /**
     * Handle confirmation dialog (e.g., "Are you sure you want to publish?").
     */
    private void handleConfirmationDialog() {
        try {
            // Check for browser alert
            driver.switchTo().alert().accept();
            log.info("  Confirmation alert accepted");
            return;
        } catch (Exception e) { /* no browser alert */ }

        // Check for modal/dialog confirm button
        try {
            WebElement confirmBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath(
                            "//button[contains(text(),'Confirm') or contains(text(),'Yes') or contains(text(),'OK')] | " +
                            "//mat-dialog-actions//button[contains(@class,'primary')]")));
            safeClick(confirmBtn);
            safeSleep(2000);
            log.info("  Confirmation dialog confirmed");
        } catch (Exception e) {
            // No confirmation dialog found — that's OK
        }
    }

    /**
     * Close any popup/modal that appears on page load.
     */
    private void closePopupIfPresent() {
        try {
            WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath(
                            "//button[contains(@class,'close') or contains(@aria-label,'Close')] | " +
                            "//i[@class='fa fa-times'] | " +
                            "//*[contains(@class,'modal-close') or contains(@class,'dialog-close')]")));
            closeBtn.click();
            safeSleep(500);
            log.info("  Popup closed");
        } catch (Exception e) {
            // No popup present — continue
        }
    }

    /**
     * Inject a hidden file input and upload an image (fallback for custom upload components).
     */
    private void injectFileInputAndUpload(String filePath, String inputId) {
        js.executeScript(
                "var existing = document.getElementById('" + inputId + "'); if(existing) existing.remove();" +
                "var inp = document.createElement('input'); inp.type='file'; inp.id='" + inputId + "';" +
                "inp.accept='image/*'; inp.style.cssText='position:fixed;top:0;left:0;z-index:99999;display:block;width:300px;height:30px;opacity:1;';" +
                "document.body.appendChild(inp);");

        WebElement fileInput = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                ExpectedConditions.presenceOfElementLocated(By.id(inputId)));
        fileInput.sendKeys(filePath);
        safeSleep(3000);

        // Clean up injected input
        js.executeScript("var el=document.getElementById('" + inputId + "');if(el)el.remove();");
        log.info("  ✅ Image uploaded via injected file input");
    }

    /**
     * Get path to a test image from resources.
     */
    private String getTestImagePath() {
        String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator
                + "test" + File.separator + "resources" + File.separator + "testdata";
        File[] imgs = new File(dir).listFiles((d, n) -> n.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (imgs != null && imgs.length > 0) return imgs[0].getAbsolutePath();

        String uploadDir = System.getProperty("user.dir") + File.separator + "UploadImages";
        File[] uploads = new File(uploadDir).listFiles((d, n) -> n.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (uploads != null && uploads.length > 0) return uploads[0].getAbsolutePath();

        throw new RuntimeException("No test images found in testdata or UploadImages directory");
    }

    /**
     * Thread.sleep wrapper with interrupt handling.
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
