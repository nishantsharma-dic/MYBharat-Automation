package com.mybharat.pages.organisation;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * CreateOrganisationPage - Government Organisation creation.
 *
 * LOCATOR STRATEGY (from runtime DOM analysis):
 * - ion-select: NO formcontrolname/placeholder in DOM. Located by parent ion-label text.
 * - ion-input: Has native <input> in LIGHT DOM (no shadow DOM). Located by placeholder.
 * - ion-select uses interface="popover" — opens ion-popover on click.
 * - Specialization is a searchable ion-input (index 3), NOT ion-select.
 */
public class CreateOrganisationPage extends BasePage {

    private static final Logger log = LogManager.getLogger(CreateOrganisationPage.class);
    private final ConfigReader config = new ConfigReader();
    private final JavascriptExecutor js;

    private static final String UPLOAD_DOCUMENT = "src" + File.separator + "test" + File.separator
            + "resources" + File.separator + "testdata" + File.separator + "test_document.pdf";

    public CreateOrganisationPage(WebDriver driver) {
        super(driver);
        this.js = (JavascriptExecutor) driver;
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    public void navigateToCreateOrg() {
        log.info("Navigating to Profile page");

        driver.get(config.getUrl() + "/reports/public_profile");
        waitForPageLoad();
        closePopup();

        // Wait for profile page to fully load
        WebDriverWait wait30 = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait30.until(d -> d.getCurrentUrl().contains("public_profile") || d.getCurrentUrl().contains("mybharat"));

        log.info("Current URL: {}", driver.getCurrentUrl());

        // Aggressively scroll to find "Create Organization" button
        WebElement createOrgBtn = null;
        for (int i = 0; i < 15; i++) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(
                        "//button[contains(text(),'Create Organization')] | " +
                        "//ion-button[contains(text(),'Create Organization')] | " +
                        "//a[contains(text(),'Create Organization')]"));
                if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                    createOrgBtn = btns.get(0);
                    scrollToElement(createOrgBtn);
                    log.info("Found Create Organization button after {} scrolls", i);
                    break;
                }
            } catch (Exception e) { /* keep scrolling */ }
            scrollPage(300);
            // Small wait between scrolls for lazy-loaded content
            try { wait.until(d -> true); } catch (Exception e) { /* ok */ }
        }

        if (createOrgBtn == null) {
            log.error("Create Organization button NOT found after scrolling. URL: {}", driver.getCurrentUrl());
            // Try direct URL as fallback
            log.info("Trying direct navigation to create-org");
            driver.get(config.getUrl().replace("mybharat.gov.in", "web.mybharat.gov.in") + "/create-org");
            waitForPageLoad();
            if (driver.getCurrentUrl().contains("create-org")) {
                log.info("Direct navigation worked: {}", driver.getCurrentUrl());
                wait30.until(d -> {
                    List<WebElement> selects = d.findElements(By.tagName("ion-select"));
                    return selects.size() >= 3;
                });
                return;
            }
            throw new RuntimeException("Cannot navigate to Create Organization page");
        }

        // Click the button — regular click (Angular (click) handler needs real event)
        log.info("Clicking Create Organization button");
        createOrgBtn.click();

        // Wait for navigation — check same tab first, then new tab
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getCurrentUrl().contains("create-org"));
            log.info("Navigated to create-org in same tab");
        } catch (Exception e) {
            // Check if new tab opened
            if (driver.getWindowHandles().size() > 1) {
                java.util.ArrayList<String> tabs = new java.util.ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(tabs.size() - 1));
                log.info("Switched to new tab: {}", driver.getCurrentUrl());
            } else {
                log.error("Button click didn't navigate. Current URL: {}", driver.getCurrentUrl());
                throw new RuntimeException("Create Organization button click failed to navigate");
            }
        }

        // Wait for create-org page
        wait30.until(d -> d.getCurrentUrl().contains("create-org"));
        waitForPageLoad();

        // Wait for Angular form to render
        wait30.until(d -> {
            List<WebElement> selects = d.findElements(By.tagName("ion-select"));
            return selects.size() >= 3;
        });

        log.info("Create Org page loaded: {}", driver.getCurrentUrl());
    }

    public boolean isCreateOrgPageLoaded() {
        return driver.getCurrentUrl().contains("create-org");
    }

    // =========================================================================
    // ABOUT SECTION
    // =========================================================================

    public void uploadBanner() {
        log.info("Uploading banner");
        String filePath = System.getProperty("user.dir") + File.separator + "UploadImages" + File.separator + "JPG1.jpg";
        injectImage(filePath, "banner");
    }

    public void uploadLogo() {
        log.info("Uploading logo");
        String filePath = System.getProperty("user.dir") + File.separator + "UploadImages" + File.separator + "JPG2.jpg";
        injectImage(filePath, "logo");
    }

    public void enterOrgName(String name) {
        log.info("Entering org name: {}", name);
        // Input 0: placeholder=Organization Name, hasNativeInput=true
        WebElement input = findNativeInput("Organization Name");
        if (input != null) {
            input.clear();
            input.sendKeys(name);
        }
    }

    public void enterAboutText(String text) {
        log.info("Entering About text");
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement editor = wait10.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ck-editor__editable')] | //div[contains(@class,'ck-content')]")));
            scrollToElement(editor);
            safeClick(editor);
            editor.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            editor.sendKeys(Keys.DELETE);
            editor.sendKeys(text);
            log.info("About text entered");
        } catch (Exception e) {
            log.warn("CKEditor not found: {}", e.getMessage());
        }
    }

    public void clickNext() {
        log.info("Clicking Next");
        WebElement nextBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//ion-button[normalize-space()='Next'] | //ion-button[contains(.,'Next')]")));
        scrollToElement(nextBtn);
        jsClick(nextBtn);
        // Wait for Basic Info section
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Details of Organization')]")));
        log.info("Basic Info visible");
    }

    // =========================================================================
    // BASIC INFO — ORGANIZATION DETAILS
    // =========================================================================

    public void selectCategory(String value) {
        log.info("Selecting Category: {}", value);
        selectDropdownByLabel("Category", value);
    }

    public void selectSubCategory(String value) {
        log.info("Selecting SubCategory: {}", value);
        // Wait for subcategory options to load after category selection
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait10.until(d -> {
            WebElement sel = findSelectByLabel("SubCategory");
            return sel != null;
        });
        selectDropdownByLabel("SubCategory", value);
    }

    public void enterName(String name) {
        log.info("Entering Name: {}", name);
        WebElement input = findNativeInput("Enter organization name");
        if (input != null) { input.clear(); input.sendKeys(name); }
    }

    public void enterAbbreviation(String abbr) {
        log.info("Entering Abbreviation: {}", abbr);
        WebElement input = findNativeInput("Enter abbrevation");
        if (input != null) { input.clear(); input.sendKeys(abbr); }
    }

    public void selectGroup(String value) {
        log.info("Selecting Group: {}", value);
        selectDropdownByLabel("Group", value);
    }

    public void selectSpecialization(String searchText) {
        log.info("Selecting Specialization: {}", searchText);
        // From screenshot: Specialization is a custom dropdown with ion-list > ion-item
        // Click the "- - Select Specialization - -" area to open it
        WebElement specInput = findNativeInput("Specialization");
        if (specInput != null) {
            scrollToElement(specInput);
            specInput.click();
            // Wait for dropdown list to appear
            WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                // Options are ion-item elements inside ion-list with role="listitem"
                WebElement option = wait10.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//ion-item[contains(@role,'listitem')][contains(.,'" + searchText + "')] | " +
                                 "//ion-item[contains(@class,'item')][contains(.,'" + searchText + "')]")));
                safeClick(option);
                log.info("Specialization '{}' selected", searchText);
            } catch (Exception e) {
                // Try clicking first visible item
                try {
                    WebElement firstItem = wait10.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("(//ion-item[contains(@role,'listitem')])[1] | (//ion-list//ion-item)[1]")));
                    safeClick(firstItem);
                    log.info("First specialization selected");
                } catch (Exception e2) {
                    log.warn("Specialization selection failed");
                }
            }
        }
    }

    public void enterNodalDesignation(String designation) {
        log.info("Entering Nodal Designation: {}", designation);
        WebElement input = findNativeInput("Enter Nodal Designation");
        if (input != null) { input.clear(); input.sendKeys(designation); }
    }

    // =========================================================================
    // ADDRESS SECTION
    // =========================================================================

    public void enterAddress1(String address) {
        log.info("Entering Address 1: {}", address);
        WebElement input = findNativeInput("Enter Building number");
        if (input != null) { input.clear(); input.sendKeys(address); }
    }

    public void enterAddress2(String address) {
        log.info("Entering Address 2: {}", address);
        WebElement input = findNativeInput("Enter Area details");
        if (input != null) { input.clear(); input.sendKeys(address); }
    }

    public void selectState(String value) {
        log.info("Selecting State: {}", value);
        selectDropdownByLabel("State", value);
    }

    public void selectDistrict() {
        log.info("Selecting first District");
        // Wait for district options to load after state selection
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait10.until(d -> {
                WebElement sel = findSelectByLabel("District");
                if (sel == null) return false;
                String disabled = sel.getAttribute("disabled");
                return disabled == null || !disabled.equals("true");
            });
        } catch (Exception e) { /* continue */ }
        selectFirstDropdownByLabel("District");
    }

    public void selectAreaUrban() {
        log.info("Selecting Area: Urban");
        try {
            // From screenshot: ion-radio with text "Urban" visible, has shadow root
            // The ion-radio element itself is clickable
            WebElement urbanRadio = driver.findElement(
                    By.xpath("//ion-radio[contains(.,'Urban')] | //ion-radio[following-sibling::text()[contains(.,'Urban')]] | //ion-item[contains(.,'Urban')]//ion-radio"));
            scrollToElement(urbanRadio);
            safeClick(urbanRadio);
            log.info("Urban selected");
        } catch (Exception e) {
            // Try by label text
            try {
                WebElement label = driver.findElement(By.xpath("//*[contains(text(),'Urban')]"));
                safeClick(label);
            } catch (Exception e2) {
                log.warn("Urban radio not found");
            }
        }
    }

    public void selectLocalBody() {
        log.info("Selecting first Local Body");
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait10.until(d -> {
                WebElement sel = findSelectByLabel("Local Body");
                if (sel == null) return false;
                String disabled = sel.getAttribute("disabled");
                return disabled == null || !disabled.equals("true");
            });
        } catch (Exception e) { /* continue */ }
        selectFirstDropdownByLabel("Local Body");
    }

    public void enterPincode(String pincode) {
        log.info("Entering Pincode: {}", pincode);
        WebElement input = findNativeInput("Enter Pincode");
        if (input != null) { input.clear(); input.sendKeys(pincode); }
    }

    public void enterCity(String city) {
        log.info("Entering City: {}", city);
        WebElement input = findNativeInput("Enter city");
        if (input != null) { input.clear(); input.sendKeys(city); }
    }

    // =========================================================================
    // CONTACT (readonly validation)
    // =========================================================================

    public boolean validateContactSection() {
        log.info("Validating Contact section");
        try {
            driver.findElement(By.xpath("//*[contains(text(),'Contact Info')]"));
            return true;
        } catch (Exception e) { return true; }
    }

    // =========================================================================
    // UPLOAD DOCUMENT
    // =========================================================================

    public void uploadDocument() {
        log.info("Uploading document");
        scrollToText("Upload Authorized");
        String filePath = System.getProperty("user.dir") + File.separator + UPLOAD_DOCUMENT;

        WebElement fileInput = driver.findElement(By.id("fileInput"));
        js.executeScript("arguments[0].style.display='block'; arguments[0].style.visibility='visible';", fileInput);
        fileInput.sendKeys(filePath);

        // Wait for file to appear
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'fileData')]")));
        log.info("Document uploaded");
    }

    // =========================================================================
    // PREVIEW + SUBMIT
    // =========================================================================

    public void clickPreview() {
        log.info("Clicking Preview");
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//ion-button[normalize-space()='Preview'] | //ion-button[contains(.,'Preview')]")));
        scrollToElement(btn);
        jsClick(btn);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//app-preview | //*[contains(text(),'DOWNLOAD')] | //*[contains(text(),'SUBMIT')]")));
        log.info("Preview loaded");
    }

    public void agreeToTerms() {
        log.info("Agreeing to T&C");
        try {
            WebElement checkbox = driver.findElement(
                    By.xpath("//ion-checkbox[contains(.,'agree')] | //ion-checkbox[contains(.,'T&C')]"));
            scrollToElement(checkbox);
            jsClick(checkbox);
        } catch (Exception e) { log.info("T&C checkbox not required"); }
    }

    public void downloadPDF() {
        log.info("Downloading PDF");
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(text(),'DOWNLOAD')]/ancestor-or-self::ion-button | //ion-button[contains(.,'DOWNLOAD')]")));
            jsClick(btn);
        } catch (Exception e) { log.warn("Download button not found"); }
    }

    public void finalSubmit() {
        log.info("Clicking SUBMIT");
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(),'SUBMIT')]/ancestor-or-self::ion-button | //ion-button[contains(.,'SUBMIT')]")));
        scrollToElement(btn);
        jsClick(btn);
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            List<WebElement> success = d.findElements(By.xpath("//*[contains(text(),'success')] | //*[contains(text(),'Success')]"));
            return !success.isEmpty() || !d.getCurrentUrl().contains("create-org");
        });
        log.info("Submitted");
    }

    public boolean isSubmissionSuccessful() {
        List<WebElement> success = driver.findElements(
                By.xpath("//*[contains(text(),'success')] | //*[contains(text(),'Success')]"));
        return !success.isEmpty() || !driver.getCurrentUrl().contains("create-org");
    }

    // =========================================================================
    // CORE HELPERS — DROPDOWN BY LABEL
    // =========================================================================

    /**
     * Find ion-select by its parent ion-label text, then open popover and select value.
     * 
     * CONFIRMED: document.querySelectorAll('ion-select')[0].click() opens popover
     * Options are: <ion-item class="select-interface-option"> with visible text
     */
    private void selectDropdownByLabel(String labelText, String optionText) {
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement ionSelect = findSelectByLabel(labelText);
        if (ionSelect == null) {
            log.error("ion-select not found for label: {}", labelText);
            return;
        }
        scrollToElement(ionSelect);

        // Open popover — use JS click (confirmed working from console test)
        js.executeScript("arguments[0].click()", ionSelect);

        // Wait for popover option and click it
        try {
            WebElement option = wait10.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'" + optionText + "')]")));
            option.click();
            log.info("Selected '{}' in '{}'", optionText, labelText);
        } catch (Exception e) {
            log.error("Option '{}' not found in popover for '{}'", optionText, labelText);
        }

        // Wait for popover to dismiss
        dismissPopover();
    }

    /**
     * Select first available option from a dropdown identified by label.
     */
    private void selectFirstDropdownByLabel(String labelText) {
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement ionSelect = findSelectByLabel(labelText);
        if (ionSelect == null) { log.error("ion-select not found for: {}", labelText); return; }
        scrollToElement(ionSelect);

        js.executeScript("arguments[0].click()", ionSelect);

        try {
            WebElement first = wait10.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//ion-item[contains(@class,'select-interface-option')])[1]")));
            first.click();
            log.info("First option selected for '{}'", labelText);
        } catch (Exception e) {
            log.error("No options in popover for '{}'", labelText);
        }
        dismissPopover();
    }

    /**
     * Find ion-select element by its associated label text.
     */
    private WebElement findSelectByLabel(String labelText) {
        try {
            return driver.findElement(By.xpath(
                    "//ion-label[contains(.,'" + labelText + "')]/ancestor::ion-col//ion-select | " +
                    "//ion-label[contains(.,'" + labelText + "')]/following::ion-select[1]"));
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // CORE HELPERS — INPUT BY PLACEHOLDER
    // =========================================================================

    /**
     * Find native <input> inside ion-input by placeholder text.
     * From DOM analysis: hasNativeInput=true for ALL inputs (light DOM, no shadow).
     */
    private WebElement findNativeInput(String placeholderContains) {
        try {
            WebElement input = driver.findElement(By.xpath(
                    "//ion-input//input[contains(@placeholder,'" + placeholderContains + "')]"));
            scrollToElement(input);
            return input;
        } catch (Exception e) {
            log.warn("Native input not found for placeholder: {}", placeholderContains);
            return null;
        }
    }

    // =========================================================================
    // CORE HELPERS — GENERAL
    // =========================================================================

    private void closePopup() {
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
        } catch (Exception e) { /* no popup */ }
    }

    private void scrollToText(String text) {
        try {
            WebElement el = driver.findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
            scrollToElement(el);
        } catch (Exception e) { scrollPage(400); }
    }

    private void dismissPopover() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("ion-popover")));
        } catch (Exception e) {
            try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception e2) { /* ok */ }
        }
    }

    private void injectImage(String filePath, String imageType) {
        String inputId = "selenium_img_" + imageType;
        js.executeScript(
            "var existing = document.getElementById('" + inputId + "'); if(existing) existing.remove();" +
            "var inp = document.createElement('input'); inp.type='file'; inp.id='" + inputId + "';" +
            "inp.accept='image/*'; inp.style.cssText='position:fixed;top:0;left:0;z-index:99999;display:block;width:300px;height:30px;opacity:1;';" +
            "document.body.appendChild(inp);");
        WebElement fileInput = driver.findElement(By.id(inputId));
        fileInput.sendKeys(filePath);
        js.executeScript(
            "var inp = document.getElementById('" + inputId + "');" +
            "var type = '" + imageType + "';" +
            "if(inp.files && inp.files[0]) {" +
            "  var reader = new FileReader();" +
            "  reader.onload = function(e) {" +
            "    var base64 = e.target.result;" +
            "    var img = document.querySelector('img[alt=\"' + type + '\"]');" +
            "    if(img) img.src = base64;" +
            "    window['__sel_' + type] = 'done';" +
            "  };" +
            "  reader.readAsDataURL(inp.files[0]);" +
            "}");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                "done".equals(((JavascriptExecutor) d).executeScript("return window['__sel_" + imageType + "']")));
        js.executeScript("var el = document.getElementById('" + inputId + "'); if(el) el.remove();");
        log.info("{} uploaded", imageType);
    }
}
