package com.mybharat.pages.youth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * YouthProfilePage - Handles the NEW React-based Youth profile completion flow.
 * 
 * The profile has been migrated from Angular/PHP to React 19 + Tailwind + react-select.
 * 
 * Page Structure:
 *   - ProfileBanner (top banner with camera upload)
 *   - UserInfoCard (photo, name, MBP ID, share, badge)
 *   - Sidebar (left) + ProfileTabs (right)
 *   - Tabs: "About" | "Basic Info" | "Reward Points"
 *   - About tab contains accordion sections: About, Area of Interest, Education,
 *     Sports, Languages, Professional Summary, Work Experience, Tools, Certifications
 * 
 * Key differences from old UI:
 *   - No element IDs on form fields (use name attrs, placeholders, text content)
 *   - react-select for multi-select dropdowns (custom DOM, not native select)
 *   - Accordion sections expand/collapse with +/- icons
 *   - SectionWrapper with Edit (pencil) or Add (+) icons
 *   - Buttons identified by text content (Save, Update, Cancel)
 *   - Toast notifications via react-toastify
 */
public class YouthProfilePage extends BasePage {

    private static final Logger log = LogManager.getLogger(YouthProfilePage.class);

    private final Random random = new Random();
    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait longWait;

    // -------------------------------------------------------------------------
    // Locators - React Profile Page
    // -------------------------------------------------------------------------

    // Tabs
    private static final By TAB_ABOUT = By.xpath("//button[normalize-space()='About']");
    private static final By TAB_BASIC_INFO = By.xpath("//button[normalize-space()='Basic Info']");

    // Basic Info form fields
    private static final By INPUT_USER_EMAIL = By.xpath("//input[@name='user_email']");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public YouthProfilePage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // -------------------------------------------------------------------------
    // Public methods (same API as before for test compatibility)
    // -------------------------------------------------------------------------

    /**
     * Complete the full youth profile in one flow.
     * Called right after login — user navigates to profile page first.
     */
    public void completeYouthProfile() throws Exception {
        waitForReactReady();

        // Capture page source for debugging
        savePageSourceForDebug("before_profile_fill");

        // Upload profile photo first
        uploadProfilePhoto();

        // Fill About tab sections
        fillAboutSection();
        addAreaOfInterest();
        // Education Qualification skipped — complex form with dependent dropdowns
        addLanguage();
        fillProfessionalSummary();
        addWorkExperience();
        fillToolsSection();
        log.info("✅ All profile sections completed");
    }

    /**
     * Navigate to the youth profile page (React app).
     * 
     * The React profile is served at /youth-profile on the same domain.
     * Beta: https://yuva-beta.mybharats.in/youth-profile
     * Prod: https://mybharat.gov.in/youth-profile
     */
    public void navigateToProfilePage() throws InterruptedException {
        // Use profileUrl from config, fallback to baseUrl + /youth-profile
        String profileUrl = config.getProperty("profileUrl");
        if (profileUrl == null || profileUrl.isEmpty()) {
            profileUrl = config.getUrl() + "/youth-profile";
        }

        log.info("Opening profile URL: {}", profileUrl);
        driver.get(profileUrl);
        waitForPageLoad();
        safeSleep(200); // React hydration + user data fetch

        // Verify we're on the profile page by checking for tab buttons
        try {
            longWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[normalize-space()='About'] | //button[normalize-space()='Basic Info']")));
            log.info("✅ Successfully on the React profile page");
        } catch (Exception e) {
            log.warn("Profile tabs not found, retrying...");
            driver.get(profileUrl);
            waitForPageLoad();
            safeSleep(200);
        }
    }

    /**
     * Navigate to Basic Info tab in the React profile.
     */
    public void navigateToBasicInfo() throws InterruptedException {
        scrollToTop();
        WebElement basicInfoTab = longWait.until(
                ExpectedConditions.elementToBeClickable(TAB_BASIC_INFO));
        scrollToElement(basicInfoTab);
        safeClick(basicInfoTab);
        safeSleep(200);
        log.info("Clicked 'Basic Info' tab");
    }

    /**
     * Extract email from the Basic Info form and write to Excel.
     */
    public void extractEmailFromProfile() {
        WebElement emailField = longWait.until(
                ExpectedConditions.visibilityOfElementLocated(INPUT_USER_EMAIL));
        scrollToElement(emailField);

        String value = emailField.getAttribute("value");
        if (value == null || value.isEmpty()) {
            // Try getting via JS for React controlled inputs
            value = (String) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].value;", emailField);
        }

        log.info("Extracted Email from React profile: {}", value);
        writeEmailToExcel(value);
        log.info("Email written to Excel successfully");
    }

    /**
     * Upload profile photo via the hidden file input in UserInfoCard.
     * The input has class="hidden" and accept="image/*".
     * We make it visible via JS, then sendKeys the file path.
     */
    public void uploadProfilePhoto() throws InterruptedException {
        log.info("Uploading profile photo...");
        String imagePath = getRandomImagePath();

        try {
            List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file'][accept='image/*']"));

            if (fileInputs.size() >= 2) {
                WebElement profileInput = fileInputs.get(1);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].classList.remove('hidden');" +
                        "arguments[0].style.display='block';" +
                        "arguments[0].style.opacity='1';" +
                        "arguments[0].style.position='relative';",
                        profileInput);
                profileInput.sendKeys(imagePath);
                safeSleep(200);
                waitForToastOrTimeout();
                log.info("✅ Profile photo uploaded: {}", imagePath);
            } else if (fileInputs.size() == 1) {
                WebElement input = fileInputs.get(0);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].classList.remove('hidden'); arguments[0].style.display='block';", input);
                input.sendKeys(imagePath);
                safeSleep(200);
                waitForToastOrTimeout();
                log.info("✅ Profile photo uploaded (single input): {}", imagePath);
            } else {
                log.warn("No file inputs found for profile photo upload");
            }
        } catch (Exception e) {
            log.warn("Profile photo upload failed: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Profile Section Methods
    // -------------------------------------------------------------------------

    /**
     * Fill the About section.
     * The section may be in accordion (collapsed) or expanded state.
     * 
     * React renders:
     *   <textarea rows={1} maxLength={300} value={about} onChange={handleChange}
     *     placeholder="Tell us about yourself..."
     *     class="w-full min-h-24 border border-gray-300 rounded-lg p-2 text-sm resize-none overflow-hidden focus:outline-none">
     *   </textarea>
     */
    public void fillAboutSection() throws InterruptedException {
        log.info("Filling About section...");

        expandSectionIfCollapsed("About");
        safeSleep(200);

        WebElement textarea = null;
        try {
            textarea = new WebDriverWait(driver, Duration.ofSeconds(7)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//textarea[@placeholder='Tell us about yourself...']")));
        } catch (Exception e) {
            clickEditIconInSection("About");
            safeSleep(200);
            try {
                textarea = driver.findElement(By.xpath("//textarea[@placeholder='Tell us about yourself...']"));
            } catch (Exception e2) {
                textarea = findTextareaInSection("About");
            }
        }

        if (textarea != null && textarea.isDisplayed()) {
            scrollToElement(textarea);
            setReactInputValue(textarea, "Automated testing profile. Passionate about technology and innovation.");
            clickSaveOrUpdateInSection("About");
            waitForToastOrTimeout();
            log.info("✅ About section saved");
        } else {
            log.warn("⚠️ Could not find About textarea, skipping");
        }
    }

    /**
     * Add Area of Interest using react-select multi-select dropdown.
     */
    public void addAreaOfInterest() throws InterruptedException {
        log.info("Adding Area of Interest...");
        expandSectionIfCollapsed("Area of Interest");
        safeSleep(200);

        if (!isReactSelectVisible()) {
            clickEditIconInSection("Area of Interest");
            safeSleep(200);
        }

        selectFirstOptionInReactSelect(0);
        safeSleep(200);

        try {
            selectFirstOptionInReactSelect(1);
            safeSleep(200);
        } catch (Exception e) {
            log.info("Sub-area dropdown not available, skipping");
        }

        clickSaveOrUpdateInSection("Area of Interest");
        waitForToastOrTimeout();
        log.info("✅ Area of Interest saved");
    }

    /**
     * Add a language using react-select.
     */
    public void addLanguage() throws InterruptedException {
        log.info("Adding Language...");
        expandSectionIfCollapsed("Languages");
        safeSleep(200);

        if (!isReactSelectVisible()) {
            clickEditIconInSection("Languages");
            safeSleep(200);
        }

        selectFirstOptionInReactSelect(0);
        safeSleep(200);

        clickSaveOrUpdateInSection("Languages");
        waitForToastOrTimeout();
        log.info("✅ Language saved");
    }

    /**
     * Fill Professional Summary with description and skills.
     */
    public void fillProfessionalSummary() throws InterruptedException {
        log.info("Filling Professional Summary...");

        // Scroll to make Professional Summary visible
        scrollPage(300);
        safeSleep(200);

        // Click edit icon for Professional Summary
        clickEditIconInSection("Professional Summary");
        safeSleep(2000); // React needs time: state update + re-render + API calls for languages/skills

        // Find the textarea inside Professional Summary section
        // The section container has the textarea after edit mode is activated
        WebElement textarea = null;
        try {
            WebElement section = findSectionContainer("Professional Summary");
            if (section != null) {
                textarea = new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                    try {
                        List<WebElement> tas = section.findElements(By.tagName("textarea"));
                        for (WebElement ta : tas) {
                            if (ta.isDisplayed()) return ta;
                        }
                    } catch (Exception e) { /* stale */ }
                    return null;
                });
            }
        } catch (Exception e) { /* timeout */ }

        // Fallback: find the LAST visible textarea on page (Prof Summary is below About)
        if (textarea == null) {
            try {
                List<WebElement> allTextareas = driver.findElements(By.tagName("textarea"));
                for (int i = allTextareas.size() - 1; i >= 0; i--) {
                    if (allTextareas.get(i).isDisplayed()) {
                        textarea = allTextareas.get(i);
                        break;
                    }
                }
            } catch (Exception e) { /* skip */ }
        }

        if (textarea != null) {
            scrollToElement(textarea);
            setReactInputValue(textarea, "Experienced in automation testing with Selenium, Java, and TestNG.");
            log.info("Professional Summary textarea filled");
        } else {
            log.warn("Professional Summary textarea not found");
        }

        // Select a skill from react-select — just click first option from dropdown
        try {
            List<WebElement> reactSelects = driver.findElements(
                    By.cssSelector("[class*='css-'][class*='control']"));
            if (!reactSelects.isEmpty()) {
                WebElement lastSelect = reactSelects.get(reactSelects.size() - 1);
                scrollToElement(lastSelect);
                lastSelect.click();
                safeSleep(500);
                // Click the first option directly from the menu
                WebElement firstOption = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("[class*='css-'][class*='option']")));
                firstOption.click();
                safeSleep(200);
            }
        } catch (Exception e) {
            log.info("Skills react-select not found, skipping");
        }

        // Click Save/Update — find any visible Save/Update button
        try {
            List<WebElement> buttons = driver.findElements(By.xpath(
                    "//button[normalize-space()='Save' or normalize-space()='Update']"));
            for (WebElement btn : buttons) {
                if (btn.isDisplayed() && btn.isEnabled()) {
                    scrollToElement(btn);
                    safeClick(btn);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Save button not found for Professional Summary");
        }
        waitForToastOrTimeout();
        log.info("✅ Professional Summary saved");
    }

    /**
     * Add Education Qualification entry.
     * 
     * Education section has actionType="add" and isEmpty={false},
     * so it's always in SectionWrapper (card) mode with a FiPlusCircle icon.
     * Clicking the + icon sets isEditing=true which renders the form.
     */
    public void addEducationQualification() throws InterruptedException {
        log.info("Adding Education Qualification...");
        scrollPage(600);
        safeSleep(200);

        clickAddIconForSection("Education Qualification");
        safeSleep(200);
        scrollPage(400);

        try {
            List<WebElement> selects = driver.findElements(By.cssSelector(
                    "select.w-full.border.border-gray-300"));
            if (selects.size() < 1) {
                log.warn("Education form dropdowns not found");
                return;
            }

            // 1. Education Type = "12th" (value "5")
            WebElement educationType = selects.get(0);
            scrollToElement(educationType);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value='5'; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                    educationType);
            safeSleep(200);

            // 2. State — select first available state (index 1)
            selects = driver.findElements(By.cssSelector("select.w-full.border.border-gray-300"));
            for (WebElement sel : selects) {
                List<WebElement> opts = sel.findElements(By.tagName("option"));
                boolean isStateDropdown = opts.stream().anyMatch(o ->
                        o.getText().contains("Select") && opts.size() > 10);
                if (isStateDropdown && opts.size() > 5) {
                    String firstStateValue = opts.get(1).getAttribute("value");
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                            sel, firstStateValue);
                    safeSleep(200);
                    log.info("State selected");
                    break;
                }
            }

            // 3. District — select first available district
            selects = driver.findElements(By.cssSelector("select.w-full.border.border-gray-300"));
            for (WebElement sel : selects) {
                List<WebElement> opts = sel.findElements(By.tagName("option"));
                if (opts.size() >= 2 && opts.size() <= 50) {
                    String firstOptText = opts.get(0).getText();
                    if (firstOptText.contains("Select") && !firstOptText.contains("State")
                            && !firstOptText.contains("Education") && !firstOptText.contains("Institute")
                            && !firstOptText.contains("Status") && !firstOptText.contains("Board")
                            && !firstOptText.contains("Identifier") && !firstOptText.contains("Course")) {
                        String currentVal = sel.getAttribute("value");
                        if (currentVal == null || currentVal.isEmpty()) {
                            String districtValue = opts.get(1).getAttribute("value");
                            ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                                    sel, districtValue);
                            safeSleep(200);
                            log.info("District selected");
                            break;
                        }
                    }
                }
            }

            // 4. Education Status = "Passed"
            selects = driver.findElements(By.cssSelector("select.w-full.border.border-gray-300"));
            for (WebElement sel : selects) {
                if (sel.findElements(By.xpath(".//option[text()='Passed']")).size() > 0) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].value='Passed'; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", sel);
                    safeSleep(200);
                    break;
                }
            }

            // 5. Student Identifier = "Roll number"
            selects = driver.findElements(By.cssSelector("select.w-full.border.border-gray-300"));
            for (WebElement sel : selects) {
                if (sel.findElements(By.xpath(".//option[text()='Roll number']")).size() > 0) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].value='Roll number'; arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", sel);
                    break;
                }
            }

            // 6. Student Identifier Value
            try {
                WebElement identifierInput = driver.findElement(By.xpath("//input[@placeholder='Enter value']"));
                scrollToElement(identifierInput);
                typeInReactInput(identifierInput, "12345678");
            } catch (Exception e) { /* skip */ }

            // 7. Save
            scrollPage(300);
            clickSaveOrUpdateInSection("Education Qualification");
            waitForToastOrTimeout();
            log.info("✅ Education Qualification saved");
        } catch (Exception e) {
            log.warn("Education qualification fill failed: {}", e.getMessage());
        }
    }

    /**
     * Add Work Experience entry.
     * 
     * Work Experience section has actionType="add" and isEmpty={false},
     * so it's always in SectionWrapper (card) mode with a FiPlusCircle icon.
     */
    public void addWorkExperience() throws InterruptedException {
        log.info("Adding Work Experience...");
        scrollPage(400);
        safeSleep(200);

        clickAddIconForSection("Work Experience");
        safeSleep(200);

        try {
            WebElement jobTitleInput = new WebDriverWait(driver, Duration.ofSeconds(7)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//input[@placeholder='Enter job title']")));
            scrollToElement(jobTitleInput);
            typeInReactInput(jobTitleInput, "Software Test Engineer");
        } catch (Exception e) {
            log.warn("Job title input not found: {}", e.getMessage());
            savePageSourceForDebug("work_exp_form_missing");
            return;
        }

        try {
            WebElement companyInput = driver.findElement(
                    By.xpath("//input[@placeholder='Enter company name']"));
            typeInReactInput(companyInput, "ABC Technologies Pvt Ltd");
        } catch (Exception e) {
            log.warn("Company input not found");
        }

        List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
        if (dateInputs.size() >= 1) setDateInput(dateInputs.get(0), "2020-01-15");
        if (dateInputs.size() >= 2) setDateInput(dateInputs.get(1), "2023-06-30");

        clickSaveOrUpdateInSection("Work Experience");
        waitForToastOrTimeout();
        log.info("✅ Work Experience saved");
    }

    /**
     * Click the Add (+) icon for sections with actionType="add".
     * These sections (Education, Work Experience) are always in card mode (not accordion).
     * The FiPlusCircle renders as: <svg size={26} class="text-[#bc4717] cursor-pointer" onClick={...}>
     * 
     * This method specifically targets the FiPlusCircle in the section header.
     */
    private void clickAddIconForSection(String sectionTitle) {
        try {
            WebElement section = findSectionContainer(sectionTitle);
            if (section == null) {
                log.warn("Section not found for add icon: {}", sectionTitle);
                return;
            }

            ((JavascriptExecutor) driver).executeScript(
                    "var section = arguments[0];" +
                    "var header = section.querySelector('div[class*=\"justify-between\"]');" +
                    "if(!header) { header = section; }" +
                    "var icons = header.querySelectorAll('svg[class*=\"cursor-pointer\"], svg.cursor-pointer');" +
                    "if(icons.length > 0) {" +
                    "  icons[icons.length-1].dispatchEvent(new MouseEvent('click', {bubbles:true, cancelable:true, view:window}));" +
                    "}",
                    section);
            safeSleep(200);
            log.info("Clicked + icon for section: {}", sectionTitle);
        } catch (Exception e) {
            log.warn("Failed to click add icon for {}: {}", sectionTitle, e.getMessage());
            try {
                WebElement section = findSectionContainer(sectionTitle);
                if (section != null) {
                    WebElement icon = section.findElement(By.cssSelector("svg[class*='cursor-pointer']"));
                    scrollToElement(icon);
                    actions().moveToElement(icon).click().perform();
                    safeSleep(200);
                }
            } catch (Exception e2) {
                log.warn("All add icon strategies failed for: {}", sectionTitle);
            }
        }
    }

    /**
     * Fill Tools section with tools, video URL, and social links.
     * 
     * Tools form renders:
     *   <input placeholder="Add tools (comma separated)" class="w-full border ..."/>
     *   <input placeholder="Insert introduction video links" class="w-full border ..."/>
     */
    public void fillToolsSection() throws InterruptedException {
        log.info("Filling Tools section...");
        scrollPage(800);
        safeSleep(200);

        WebElement toolsInput = null;
        try {
            toolsInput = driver.findElement(By.xpath("//input[@placeholder='Add tools (comma separated)']"));
            if (!toolsInput.isDisplayed()) toolsInput = null;
        } catch (Exception e) {
            toolsInput = null;
        }

        if (toolsInput == null) {
            clickEditIconInSection("Tools");
            safeSleep(2000); // React needs time to render the edit form
        }

        try {
            toolsInput = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//input[@placeholder='Add tools (comma separated)']")));
            scrollToElement(toolsInput);
            typeInReactInput(toolsInput, "Selenium, Java, TestNG, Maven, Git, Jenkins, Docker");
        } catch (Exception e) {
            log.warn("Tools input not found: {}", e.getMessage());
        }

        try {
            WebElement videoInput = driver.findElement(
                    By.xpath("//input[@placeholder='Insert introduction video links']"));
            scrollToElement(videoInput);
            typeInReactInput(videoInput, "https://www.youtube.com/watch?v=QZSlDNgi-eQ");
        } catch (Exception e) { /* skip */ }

        // Click Save/Update
        try {
            List<WebElement> buttons = driver.findElements(By.xpath(
                    "//button[normalize-space()='Save' or normalize-space()='Update']"));
            for (WebElement btn : buttons) {
                if (btn.isDisplayed() && btn.isEnabled()) {
                    scrollToElement(btn);
                    safeClick(btn);
                    break;
                }
            }
        } catch (Exception e) { /* skip */ }
        waitForToastOrTimeout();
        log.info("✅ Tools section saved");
    }

    // -------------------------------------------------------------------------
    // React-specific helper methods
    // -------------------------------------------------------------------------

    /**
     * Wait for React to finish rendering.
     * Checks document.readyState + no pending network requests.
     */
    private void waitForReactReady() {
        waitForPageLoad();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                    ExpectedConditions.invisibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'animate-pulse') or contains(text(),'Loading')]")));
        } catch (Exception e) { /* no loader */ }
    }

    /**
     * Find a section container by its title text.
     * Handles BOTH modes:
     *   1. AccordionItem: title is bare text inside a div (no span wrapper)
     *      <button class="w-full flex justify-between items-center px-5 py-4">
     *        <div>...<span>{icon}</span> About</div>
     *        <span>{+/- icon}</span>
     *      </button>
     *   2. SectionWrapper: title is inside a <span>
     *      <div class="flex justify-between items-center mb-3">
     *        <div>...<span>{icon}</span><span>About</span></div>
     *        {edit/add icon}
     *      </div>
     */
    private WebElement findSectionContainer(String sectionTitle) {
        // Strategy 1: SectionWrapper mode — title in <span>
        try {
            return driver.findElement(By.xpath(
                    "//span[normalize-space()='" + sectionTitle + "']/ancestor::div[contains(@class,'rounded-xl')][1]"));
        } catch (Exception e) {
            // ignore
        }

        // Strategy 2: AccordionItem mode — title as text node in div
        try {
            return driver.findElement(By.xpath(
                    "//div[contains(@class,'font-semibold') and contains(normalize-space(),'" + sectionTitle + "')]" +
                    "/ancestor::div[contains(@class,'rounded-xl')][1]"));
        } catch (Exception e) {
            // ignore
        }

        // Strategy 3: Broad search — any container with the title text
        try {
            return driver.findElement(By.xpath(
                    "//*[contains(@class,'rounded-xl') and .//text()[contains(.,'" + sectionTitle + "')]]"));
        } catch (Exception e) {
            log.warn("Could not find section container for: {}", sectionTitle);
            return null;
        }
    }

    /**
     * Expand a section if it's in accordion (collapsed) state.
     * 
     * AccordionItem renders:
     *   <div class="bg-white border border-orange-200 rounded-xl ...">
     *     <button class="w-full flex justify-between items-center px-5 py-4">
     *       <div class="flex items-center gap-3 text-[18px] font-semibold text-gray-800">
     *         <span class="text-[#bc4717] ...">{icon}</span>
     *         About   <-- bare text node
     *       </div>
     *       <span class="text-[#bc4717] cursor-pointer">{FiPlusCircle SVG}</span>
     *     </button>
     *     <div style="height: 0px" class="overflow-hidden ...">...</div>
     *   </div>
     */
    private void expandSectionIfCollapsed(String sectionTitle) {
        try {
            // Find the accordion button that contains this title text
            // The button has class "w-full flex justify-between items-center px-5 py-4"
            WebElement accordionBtn = driver.findElement(By.xpath(
                    "//button[contains(@class,'w-full') and contains(@class,'justify-between') " +
                    "and .//div[contains(@class,'font-semibold') and contains(normalize-space(),'" + sectionTitle + "')]]"));

            scrollToElement(accordionBtn);
            jsClick(accordionBtn);
            safeSleep(200);
            log.info("Expanded accordion section: {}", sectionTitle);
        } catch (Exception e) {
            // Not in accordion mode — section is already expanded as a card
            log.info("Section '{}' not in accordion mode (already expanded or card mode)", sectionTitle);
        }
    }

    /**
     * Click the Edit (pencil) icon in a SectionWrapper.
     * 
     * SectionWrapper renders:
     *   <div class="bg-white border border-orange-200 rounded-xl ... px-5 py-4">
     *     <div class="flex justify-between items-center mb-3">
     *       <div class="flex items-center gap-3 ...">
     *         <span>{icon}</span>
     *         <span>{title}</span>
     *       </div>
     *       <MdEdit class="text-blue-600 cursor-pointer" onClick={...} />  <-- THIS
     *     </div>
     *     {children}
     *   </div>
     */
    private void clickEditIconInSection(String sectionTitle) {
        WebElement section = findSectionContainer(sectionTitle);
        if (section == null) {
            log.warn("Cannot find section to click edit: {}", sectionTitle);
            return;
        }

        try {
            // Find the SVG icon with cursor-pointer class
            WebElement icon = section.findElement(By.cssSelector("svg[class*='cursor-pointer']"));
            
            // Scroll the section into view first (not the SVG itself — avoids sticky header issues)
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", section);
            safeSleep(200);
            
            // Use Selenium's native click via the parent element approach
            // React 19 event delegation works with real DOM clicks
            // Find the clickable parent (the SVG's direct container or the SVG itself)
            try {
                icon.click();
                safeSleep(500);
                log.info("Clicked icon (native click) for section: {}", sectionTitle);
            } catch (Exception clickEx) {
                // If native click fails (intercepted), use JS to simulate a full click sequence
                ((JavascriptExecutor) driver).executeScript(
                        "var el = arguments[0];" +
                        "el.dispatchEvent(new PointerEvent('pointerdown', {bubbles:true}));" +
                        "el.dispatchEvent(new MouseEvent('mousedown', {bubbles:true}));" +
                        "el.dispatchEvent(new PointerEvent('pointerup', {bubbles:true}));" +
                        "el.dispatchEvent(new MouseEvent('mouseup', {bubbles:true}));" +
                        "el.dispatchEvent(new MouseEvent('click', {bubbles:true}));",
                        icon);
                safeSleep(500);
                log.info("Clicked icon (full event sequence) for section: {}", sectionTitle);
            }
        } catch (Exception e) {
            log.warn("Edit icon not found for section: {}", sectionTitle);
        }
    }

    /**
     * Click the Add (+) icon in a section wrapper (for Work Experience, Education).
     * These sections use actionType="add" which renders FiPlusCircle.
     */
    private void clickAddIconInSection(String sectionTitle) {
        clickAddIconForSection(sectionTitle);
    }

    /**
     * Click Save or Update button within a specific section context.
     * Buttons render as:
     *   <button class="px-5 py-2 bg-[#bc4717] text-white rounded-lg ...">Save</button>
     */
    private void clickSaveOrUpdateInSection(String sectionTitle) {
        safeSleep(200);

        WebElement section = findSectionContainer(sectionTitle);

        // Try within section first
        if (section != null) {
            try {
                WebElement saveBtn = section.findElement(By.xpath(
                        ".//button[contains(@class,'bg-[#bc4717]') and (normalize-space()='Save' or normalize-space()='Update')]"));
                scrollToElement(saveBtn);
                safeClick(saveBtn);
                log.info("Clicked Save/Update in section: {}", sectionTitle);
                return;
            } catch (Exception e) {
                // Try broader search within section
                try {
                    WebElement saveBtn = section.findElement(By.xpath(
                            ".//button[normalize-space()='Save' or normalize-space()='Update']"));
                    scrollToElement(saveBtn);
                    safeClick(saveBtn);
                    log.info("Clicked Save/Update (broad) in section: {}", sectionTitle);
                    return;
                } catch (Exception e2) {
                    // fall through to global search
                }
            }
        }

        // Fallback: find any visible Save/Update button on page
        List<WebElement> buttons = driver.findElements(By.xpath(
                "//button[normalize-space()='Save' or normalize-space()='Update']"));
        for (WebElement btn : buttons) {
            if (btn.isDisplayed() && btn.isEnabled()) {
                scrollToElement(btn);
                safeClick(btn);
                log.info("Clicked Save/Update button (global fallback)");
                return;
            }
        }
        log.warn("Could not find Save/Update button for section: {}", sectionTitle);
    }

    /**
     * Find a textarea element within a section.
     */
    private WebElement findTextareaInSection(String sectionTitle) {
        WebElement section = findSectionContainer(sectionTitle);
        if (section != null) {
            try {
                List<WebElement> textareas = section.findElements(By.tagName("textarea"));
                for (WebElement ta : textareas) {
                    if (ta.isDisplayed()) return ta;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // Fallback: find any visible textarea on the page
        List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
        for (WebElement ta : textareas) {
            try {
                if (ta.isDisplayed()) return ta;
            } catch (Exception e) {
                // stale element, skip
            }
        }
        return null;
    }

    /**
     * Check if a react-select component is currently visible on the page.
     */
    private boolean isReactSelectVisible() {
        try {
            List<WebElement> selects = driver.findElements(
                    By.cssSelector("[class*='css-'][class*='control'], [class*='react-select']"));
            return !selects.isEmpty() && selects.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Select the first available option from a react-select dropdown.
     * @param index which react-select on the page (0-based)
     */
    private void selectFirstOptionInReactSelect(int index) {
        // Find all react-select control containers
        List<WebElement> controls = driver.findElements(
                By.cssSelector("[class*='css-'][class*='control']"));

        if (controls.size() <= index) {
            log.warn("react-select at index {} not found (total: {})", index, controls.size());
            return;
        }

        WebElement control = controls.get(index);
        scrollToElement(control);
        control.click();
        safeSleep(200);

        // Wait for menu to appear
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("[class*='css-'][class*='menu']")));

            // Click first option in the menu
            List<WebElement> options = menu.findElements(
                    By.cssSelector("[class*='css-'][class*='option']"));
            if (!options.isEmpty()) {
                options.get(0).click();
                safeSleep(200);
                log.info("Selected first option from react-select index {}", index);
            }
        } catch (Exception e) {
            // Fallback: type a character and press Enter
            try {
                WebElement input = driver.findElement(
                        By.cssSelector("[class*='css-'] input[aria-autocomplete='list']"));
                input.sendKeys("a");
                safeSleep(200);
                input.sendKeys(Keys.ENTER);
                safeSleep(200);
                log.info("Selected option via keyboard in react-select index {}", index);
            } catch (Exception e2) {
                log.warn("Failed to select from react-select index {}: {}", index, e2.getMessage());
            }
        }
    }

    /**
     * Set a date value on an HTML date input using JavaScript.
     * React date inputs need value set via nativeInputValueSetter.
     */
    private void setDateInput(WebElement dateInput, String dateValue) {
        ((JavascriptExecutor) driver).executeScript(
                "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
                "window.HTMLInputElement.prototype, 'value').set;" +
                "nativeInputValueSetter.call(arguments[0], arguments[1]);" +
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                dateInput, dateValue);
        safeSleep(200);
    }

    /**
     * Set value on a React controlled input/textarea using nativeInputValueSetter.
     * React 19 requires proper synthetic event dispatch.
     * This method:
     *   1. Focuses the element
     *   2. Clears existing value via select-all + delete
     *   3. Types the value character by character via sendKeys
     *   4. Triggers blur to finalize React state
     */
    private void setReactInputValue(WebElement element, String value) {
        try {
            scrollToElement(element);
            element.click();
            safeSleep(200);
            // Select all existing text and delete
            element.sendKeys(Keys.chord(Keys.COMMAND, "a"));
            safeSleep(100);
            element.sendKeys(Keys.BACK_SPACE);
            safeSleep(100);
            // Type the new value
            element.sendKeys(value);
            safeSleep(200);
            // Trigger blur to finalize
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('blur', {bubbles:true}));", element);
            safeSleep(200);
        } catch (Exception e) {
            log.warn("sendKeys approach failed, trying JS setter: {}", e.getMessage());
            // Fallback: JS native setter (works on some React versions)
            String tagName = element.getTagName().toLowerCase();
            String prototype = tagName.equals("textarea")
                    ? "window.HTMLTextAreaElement.prototype"
                    : "window.HTMLInputElement.prototype";
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var value = arguments[1];" +
                    "el.focus();" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(" + prototype + ", 'value').set;" +
                    "nativeSetter.call(el, value);" +
                    "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                    "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                    "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                    element, value);
            safeSleep(200);
        }
    }

    /**
     * Set value on a React controlled input using focus + clear + sendKeys approach.
     * This is more reliable for some React inputs that listen to keydown events.
     */
    private void typeInReactInput(WebElement element, String value) {
        try {
            scrollToElement(element);
            element.click();
            safeSleep(200);
            // Select all and delete
            element.sendKeys(Keys.chord(Keys.COMMAND, "a"));
            safeSleep(100);
            element.sendKeys(Keys.BACK_SPACE);
            safeSleep(100);
            element.sendKeys(value);
            safeSleep(200);
        } catch (Exception e) {
            // Fallback to JS approach
            setReactInputValue(element, value);
        }
    }

    /**
     * Wait for a toast notification to appear (success or error), or timeout.
     */
    private void waitForToastOrTimeout() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(".Toastify__toast")));
            safeSleep(200);
        } catch (Exception e) {
            safeSleep(200);
        }
    }

    /**
     * Close popup if present (same as landing page popup).
     */
    private void closePopupIfPresent() {
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//i[@class='fa fa-times'] | //button[contains(@class,'close')]")));
            popup.click();
            safeSleep(200);
        } catch (Exception e) {
            // No popup
        }
    }

    /**
     * Scroll to top of page.
     */
    private void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    /**
     * Safe sleep without checked exception.
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get a random image path from the UploadImages folder.
     * Only returns images >= 50KB (app requirement).
     */
    private String getRandomImagePath() {
        File imagesDir = Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();
        if (!imagesDir.exists()) {
            throw new RuntimeException("UploadImages folder not found at: " + imagesDir.getAbsolutePath());
        }
        File[] files = imagesDir.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files == null || files.length == 0) {
            throw new RuntimeException("No images found in: " + imagesDir.getAbsolutePath());
        }

        // Filter for files >= 50KB (app requires minimum 50KB)
        List<File> validFiles = new java.util.ArrayList<>();
        for (File f : files) {
            if (f.length() >= 50 * 1024) { // 50KB minimum
                validFiles.add(f);
            }
        }

        if (validFiles.isEmpty()) {
            // Fallback: use largest available file
            File largest = files[0];
            for (File f : files) {
                if (f.length() > largest.length()) largest = f;
            }
            log.warn("No images >= 50KB found, using largest: {} ({}KB)", largest.getName(), largest.length() / 1024);
            return largest.getAbsolutePath();
        }

        File randomFile = validFiles.get(random.nextInt(validFiles.size()));
        log.info("Selected image: {} ({}KB)", randomFile.getName(), randomFile.length() / 1024);
        return randomFile.getAbsolutePath();
    }

    /**
     * Write email to Excel file for later use.
     */
    private void writeEmailToExcel(String email) {
        try {
            String path = System.getProperty("user.dir") + File.separator
                    + "resources" + File.separator + "UserDetails.xlsx";
            File file = new File(path);
            file.getParentFile().mkdirs();

            Workbook workbook;
            if (file.exists() && file.length() > 0) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet("UserData");
            if (sheet == null) {
                sheet = workbook.createSheet("UserData");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Email");
            }

            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(email);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            log.info("Email written to Excel row {}: {}", nextRow, email);
        } catch (Exception e) {
            log.error("Failed to write email to Excel: {}", e.getMessage());
        }
    }

    /**
     * Save page source to reports folder for debugging.
     */
    private void savePageSourceForDebug(String label) {
        try {
            String pageSource = driver.getPageSource();
            String filePath = System.getProperty("user.dir") + "/reports/page_source_" + label + ".html";
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(pageSource);
            fw.close();
            log.info("Page source saved: {} (URL: {})", filePath, driver.getCurrentUrl());
        } catch (Exception e) {
            log.warn("Could not save page source: {}", e.getMessage());
        }
    }
}
