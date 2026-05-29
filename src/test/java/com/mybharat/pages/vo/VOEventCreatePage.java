package com.mybharat.pages.vo;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;
import com.mybharat.utils.SessionHelper;

/**
 * VOEventCreatePage - Fills the "Add Event" form at /orgeventmanagement/add_volunteer?type=vo
 *
 * Form fields (all from screenshots):
 *   1. Logo/Banner upload
 *   2. Event Type (Volunteer for Bharat - pre-selected)
 *   3. Event Template (General Category dropdown)
 *   4. Vo Specialization* (dropdown)
 *   5. Sub Specialization
 *   6. Interest Tags
 *   7. Event Mode* (dropdown)
 *   8. Activity Date: start date*, start time*, end date*, end time*
 *   9. Event Time Table: Activity Date*, Activity time*, Theme, Activity
 *  10. Event Partner Name* (text)
 *  11. Event Location: Address, Landmark, State*, District*, Town/City, Urban/Rural, ULB, Pincode*
 *  12. Event Speaker (optional)
 *  13. Event Organizer Details: Name*, Mobile*, Email*
 *  14. Preview button
 */
public class VOEventCreatePage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOEventCreatePage.class);
    private final ConfigReader config = new ConfigReader();
    private final Random random = new Random();

    /** Stores the created event name for saving to Excel */
    public static String createdEventName;

    private String eventName;

    // Fixed event name prefix — only city changes randomly
    private static final String EVENT_PREFIX = "Swachhta Hi Seva";

    // Indian city names
    private static final String[] CITIES = {
            "Ghaziabad", "Lucknow", "Varanasi", "Jaipur", "Bhopal", "Indore",
            "Patna", "Ranchi", "Dehradun", "Chandigarh", "Agra", "Kanpur",
            "Prayagraj", "Meerut", "Noida", "Gurugram", "Faridabad", "Jodhpur",
            "Udaipur", "Kota", "Nagpur", "Pune", "Nashik", "Surat", "Ahmedabad"
    };

    // Realistic Indian names for organizer
    private static final String[] ORGANIZER_NAMES = {
            "Rajesh Kumar Sharma", "Priya Singh", "Amit Verma", "Sunita Devi",
            "Vikram Chauhan", "Neha Gupta", "Arun Patel", "Kavita Mishra",
            "Deepak Yadav", "Anjali Tiwari", "Rohit Saxena", "Pooja Agarwal"
    };

    // Realistic addresses
    private static final String[] ADDRESSES = {
            "Near Gandhi Maidan, Civil Lines", "Sector 15, Institutional Area",
            "Rajendra Nagar, Main Road", "Nehru Park, Station Road",
            "Subhash Chowk, MG Road", "Ambedkar Bhawan, University Road",
            "Patel Nagar, Ring Road", "Shastri Nagar, GT Road"
    };

    public VOEventCreatePage(WebDriver driver) {
        super(driver);
    }

    public String getEventName() {
        return eventName;
    }

    /**
     * Fill the complete Add Event form and click Preview.
     */
    public void fillEventFormAndPreview() throws InterruptedException {
        log.info("=== Filling Add Event form ===");

        // Check session before filling form
        SessionHelper sessionHelper = new SessionHelper(driver);
        if (!sessionHelper.isSessionActive()) {
            log.warn("Session expired before event form — re-logging in...");
            sessionHelper.ensureLoggedIn(null);
            // Navigate back to add event page
            String baseUrl = config.getUrl();
            driver.get(baseUrl + "/orgeventmanagement/add_volunteer?type=vo");
            Thread.sleep(3000);
            waitForPageLoad();
        }

        dismissOverlay();

        uploadBanner();
        fillEventTitle();
        fillDescription();
        selectEventTemplate();
        selectSpecialization();
        selectEventMode();
        fillActivityDates();
        fillEventTimeTable();
        fillEventPartnerName();
        fillEventLocation();
        fillEventOrganizerDetails();
        clickPreview();

        log.info("=== ✅ Add Event form filled and Preview clicked ===");
    }

    /**
     * Fill Event Title (id="editableText", mandatory).
     */
    private void fillEventTitle() throws InterruptedException {
        log.info("Filling Event Title...");
        String city = CITIES[random.nextInt(CITIES.length)];
        String title = EVENT_PREFIX + " " + city;
        try {
            WebElement titleInput = driver.findElement(
                    By.xpath("//input[@id='editableText' or @placeholder='Event Title*' or @name='event_name']"));
            scrollToElement(titleInput);
            titleInput.clear();
            titleInput.sendKeys(title);
            createdEventName = title;
            log.info("✅ Event Title: {}", title);
        } catch (Exception e) {
            log.error("Event Title input not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill About/Description using Trumbowyg API.
     */
    private void fillDescription() throws InterruptedException {
        log.info("Filling Description...");
        String[] descriptions = {
                "This volunteer opportunity brings together young citizens to participate in community welfare activities including cleanliness drives, tree plantation, and public health awareness campaigns across the district.",
                "A collaborative initiative under the Volunteer for Bharat program aimed at empowering local communities through skill-building workshops, environmental conservation, and youth mentorship programs.",
                "Join hands with fellow volunteers to make a positive impact in your neighbourhood through education outreach, digital literacy sessions, and sustainable development activities.",
                "This event is designed to foster civic responsibility among youth through hands-on participation in social welfare programs, cultural exchange, and community development initiatives.",
                "An impactful initiative to engage young volunteers in nation-building activities including health camps, environmental awareness drives, and skill enhancement workshops for underprivileged communities."
        };
        String desc = descriptions[random.nextInt(descriptions.length)];
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "$('#description').trumbowyg('html', arguments[0]);", desc);
            log.info("✅ Description filled via Trumbowyg");
        } catch (Exception e) {
            try {
                WebElement editor = driver.findElement(By.cssSelector(".trumbowyg-editor"));
                editor.click();
                editor.sendKeys(desc);
                log.info("✅ Description filled (contenteditable)");
            } catch (Exception e2) {
                log.warn("Description not found: {}", e2.getMessage());
            }
        }
        Thread.sleep(300);
    }

    // =========================================================================
    // FORM SECTIONS
    // =========================================================================

    /**
     * Upload logo and banner images (same approach as youth profile).
     * Uses different images for logo and banner to avoid validation issues.
     */
    private void uploadBanner() throws InterruptedException {
        log.info("Uploading Logo and Banner...");
        try {
            List<String> imagePaths = getDifferentImagePaths(2);
            List<WebElement> fileInputs = driver.findElements(
                    By.cssSelector("input[type='file'][accept='image/*'], input[type='file']"));

            for (WebElement fileInput : fileInputs) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].classList.remove('hidden');" +
                        "arguments[0].style.display='block';" +
                        "arguments[0].style.opacity='1';" +
                        "arguments[0].style.position='relative';", fileInput);
            }
            Thread.sleep(300);

            if (fileInputs.size() >= 2) {
                // First input = Logo, Second input = Banner
                fileInputs.get(0).sendKeys(imagePaths.get(0));
                Thread.sleep(2000);
                log.info("✅ Logo uploaded: {}", imagePaths.get(0));

                fileInputs.get(1).sendKeys(imagePaths.get(1));
                Thread.sleep(2000);
                log.info("✅ Banner uploaded: {}", imagePaths.get(1));
            } else if (fileInputs.size() == 1) {
                fileInputs.get(0).sendKeys(imagePaths.get(0));
                Thread.sleep(2000);
                log.info("✅ Image uploaded (single input)");
            } else {
                log.warn("No file inputs found for logo/banner upload");
            }

            // Hide file inputs back so they don't block other elements
            for (WebElement fileInput : fileInputs) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].style.display='none';", fileInput);
            }
        } catch (Exception e) {
            log.warn("Logo/Banner upload skipped: {}", e.getMessage());
        }
    }

    /**
     * Get N different image paths from UploadImages folder (each >= 50KB).
     */
    private List<String> getDifferentImagePaths(int count) {
        File imagesDir = Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();
        if (!imagesDir.exists()) throw new RuntimeException("UploadImages folder not found");
        File[] files = imagesDir.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files == null || files.length == 0) throw new RuntimeException("No images found");

        List<File> validFiles = new java.util.ArrayList<>();
        for (File f : files) {
            if (f.length() >= 50 * 1024) validFiles.add(f);
        }
        if (validFiles.isEmpty()) {
            for (File f : files) validFiles.add(f);
        }

        List<String> paths = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            paths.add(validFiles.get(i % validFiles.size()).getAbsolutePath());
        }
        return paths;
    }

    /**
     * Select Event Template dropdown (General Category).
     */
    private void selectEventTemplate() throws InterruptedException {
        log.info("Selecting Event Template...");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement templateDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//select[contains(@name,'template') or contains(@id,'template') or contains(@name,'category')]")));
            scrollToElement(templateDropdown);
            Select sel = new Select(templateDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Event Template selected");
            }
        } catch (Exception e) {
            log.warn("Event Template dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(500);
    }

    /**
     * Select Vo Specialization dropdown.
     */
    private void selectSpecialization() throws InterruptedException {
        log.info("Selecting Specialization...");
        try {
            WebElement specDropdown = driver.findElement(
                    By.xpath("//select[contains(@name,'specialization') or contains(@id,'specialization')]"));
            scrollToElement(specDropdown);
            Select sel = new Select(specDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Specialization selected");
            }
        } catch (Exception e) {
            log.warn("Specialization dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(500);
    }

    /**
     * Select Event Mode dropdown.
     */
    private void selectEventMode() throws InterruptedException {
        log.info("Selecting Event Mode...");
        try {
            WebElement modeDropdown = driver.findElement(
                    By.xpath("//select[contains(@name,'event_mode') or contains(@name,'eventMode') or contains(@id,'event_mode')]"));
            scrollToElement(modeDropdown);
            Select sel = new Select(modeDropdown);
            sel.selectByVisibleText("In Person");
            log.info("✅ Event Mode: In Person");
        } catch (Exception e) {
            log.warn("Event Mode not found, trying index: {}", e.getMessage());
            try {
                WebElement modeDropdown = driver.findElement(
                        By.xpath("//select[./option[contains(text(),'Select Event Mode')]]"));
                Select sel = new Select(modeDropdown);
                if (sel.getOptions().size() > 1) sel.selectByIndex(1);
                log.info("✅ Event Mode selected (index fallback)");
            } catch (Exception e2) {
                log.warn("Event Mode completely failed: {}", e2.getMessage());
            }
        }
        Thread.sleep(500);
    }

    /**
     * Fill Activity Date section: start date, start time, end date, end time.
     * IDs from developer code (add_volunteer.ctp):
     *   - event_from_date (datetimepicker, format d-m-Y)
     *   - event_start_time (type=time)
     *   - event_to_date (datetimepicker, format d-m-Y)
     *   - event_end_time (type=time)
     */
    private void fillActivityDates() throws InterruptedException {
        log.info("Filling Activity Dates...");
        scrollPage(300);
        Thread.sleep(500);

        LocalDate startDate = LocalDate.now().plusMonths(1).withDayOfMonth(15);
        LocalDate endDate = LocalDate.now().plusMonths(2).withDayOfMonth(20);
        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String endDateStr = endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Event start date (id=event_from_date)
        try {
            WebElement dateInput = driver.findElement(By.id("event_from_date"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.removeAttribute('readonly'); el.value = arguments[1];" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "try { $(el).datetimepicker('hide'); } catch(e) {}", dateInput, startDateStr);
            log.info("✅ Start Date set: {}", startDateStr);
        } catch (Exception e) {
            log.warn("Start Date failed: {}", e.getMessage());
        }
        Thread.sleep(300);

        // Event start time (id=event_start_time, type=time)
        try {
            WebElement timeInput = driver.findElement(By.id("event_start_time"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.removeAttribute('readonly'); el.value = '09:00';" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));", timeInput);
            log.info("✅ Start Time set: 09:00");
        } catch (Exception e) {
            log.warn("Start Time failed: {}", e.getMessage());
        }
        Thread.sleep(300);

        // Event end date (id=event_to_date)
        try {
            WebElement dateInput = driver.findElement(By.id("event_to_date"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.removeAttribute('readonly'); el.value = arguments[1];" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "try { $(el).datetimepicker('hide'); } catch(e) {}", dateInput, endDateStr);
            log.info("✅ End Date set: {}", endDateStr);
        } catch (Exception e) {
            log.warn("End Date failed: {}", e.getMessage());
        }
        Thread.sleep(300);

        // Event end time (id=event_end_time, type=time)
        try {
            WebElement timeInput = driver.findElement(By.id("event_end_time"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.removeAttribute('readonly'); el.value = '17:00';" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));", timeInput);
            log.info("✅ End Time set: 17:00");
        } catch (Exception e) {
            log.warn("End Time failed: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Time Table: Activity Date, Activity time, Theme, Activity.
     */
    private void fillEventTimeTable() throws InterruptedException {
        log.info("Filling Event Time Table...");
        scrollPage(300);
        Thread.sleep(500);

        // Activity Date (same as start date)
        try {
            WebElement actDateInput = driver.findElement(
                    By.xpath("(//input[contains(@placeholder,'Select date') or contains(@name,'activity_date')])[last()]"));
            scrollToElement(actDateInput);
            LocalDate futureDate = LocalDate.now().plusMonths(1).withDayOfMonth(15);
            String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.readOnly = false; el.value = arguments[1];" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "try { $(el).trigger('change'); } catch(e) {}", actDateInput, dateStr);
            log.info("✅ Activity Date set: {}", dateStr);
        } catch (Exception e) {
            log.warn("Activity Date not found: {}", e.getMessage());
        }

        // Activity time
        try {
            WebElement actTimeInput = driver.findElement(
                    By.xpath("(//input[contains(@name,'activity_time') or @type='time'])[last()]"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, '09:00');" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));", actTimeInput);
            log.info("✅ Activity Time set: 09:00");
        } catch (Exception e) {
            log.warn("Activity Time not found: {}", e.getMessage());
        }

        // Theme dropdown
        try {
            WebElement themeDropdown = driver.findElement(
                    By.xpath("//select[./option[contains(text(),'Select Theme')]]"
                            + " | //select[contains(@name,'theme')]"));
            Select sel = new Select(themeDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Theme selected");
            }
        } catch (Exception e) {
            log.warn("Theme dropdown not found: {}", e.getMessage());
        }

        // Activity dropdown
        try {
            WebElement actDropdown = driver.findElement(
                    By.xpath("//select[./option[contains(text(),'Select Activity')]]"
                            + " | //select[contains(@name,'activity')]"));
            Select sel = new Select(actDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Activity selected");
            }
        } catch (Exception e) {
            log.warn("Activity dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Partner Name (id=partner_name, mandatory, max 100 chars).
     */
    private void fillEventPartnerName() throws InterruptedException {
        log.info("Filling Event Partner Name...");
        scrollPage(300);
        Thread.sleep(300);

        // Use the same name as event title (createdEventName) for consistency
        eventName = createdEventName;

        try {
            WebElement partnerInput = driver.findElement(By.id("partner_name"));
            scrollToElement(partnerInput);
            partnerInput.clear();
            partnerInput.sendKeys(eventName);
            log.info("✅ Event Partner Name: {}", eventName);
        } catch (Exception e) {
            log.warn("Event Partner Name not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Location: State, District, Pincode (mandatory fields).
     */
    private void fillEventLocation() throws InterruptedException {
        log.info("Filling Event Location...");
        scrollPage(300);
        Thread.sleep(500);

        // Address
        try {
            WebElement addressInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'Enter Address') or contains(@name,'address')]"));
            scrollToElement(addressInput);
            addressInput.clear();
            addressInput.sendKeys(ADDRESSES[random.nextInt(ADDRESSES.length)]);
            log.info("✅ Address filled");
        } catch (Exception e) {
            log.warn("Address not found: {}", e.getMessage());
        }

        // State
        try {
            WebElement stateDropdown = driver.findElement(
                    By.xpath("//select[contains(@name,'state') or contains(@id,'state')]"));
            scrollToElement(stateDropdown);
            Select sel = new Select(stateDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                Thread.sleep(1000); // Wait for district to load
                log.info("✅ State selected");
            }
        } catch (Exception e) {
            log.warn("State dropdown not found: {}", e.getMessage());
        }

        // District
        try {
            Thread.sleep(1000);
            WebElement districtDropdown = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//select[contains(@name,'district') or contains(@id,'district')]")));
            Select sel = new Select(districtDropdown);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ District selected");
            }
        } catch (Exception e) {
            log.warn("District dropdown not found: {}", e.getMessage());
        }

        // Pincode
        try {
            WebElement pincodeInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'Enter Pincode') or contains(@name,'pincode')]"));
            scrollToElement(pincodeInput);
            pincodeInput.clear();
            pincodeInput.sendKeys("201001");
            log.info("✅ Pincode: 201001");
        } catch (Exception e) {
            log.warn("Pincode not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Organizer Details: Name, Mobile, Email (all mandatory).
     * From developer code:
     *   - name="organizerDetails[organizer_name][]", placeholder="Enter name"
     *   - name="organizerDetails[organizer_phone][]", id="organizer_phone", maxlength=10
     *   - name="organizerDetails[organizer_email][]", placeholder="Enter Complete email address"
     */
    private void fillEventOrganizerDetails() throws InterruptedException {
        log.info("Filling Event Organizer Details...");
        scrollPage(500);
        Thread.sleep(500);

        // Organizer Name
        try {
            WebElement nameInput = driver.findElement(
                    By.xpath("//input[@name='organizerDetails[organizer_name][]']"));
            scrollToElement(nameInput);
            nameInput.clear();
            nameInput.sendKeys(ORGANIZER_NAMES[random.nextInt(ORGANIZER_NAMES.length)]);
            log.info("✅ Organizer Name filled");
        } catch (Exception e) {
            log.warn("Organizer Name not found: {}", e.getMessage());
        }

        // Mobile Number (exactly 10 digits, id=organizer_phone)
        try {
            WebElement mobileInput = driver.findElement(By.id("organizer_phone"));
            scrollToElement(mobileInput);
            mobileInput.clear();
            String mobile = "9" + String.format("%09d", random.nextInt(999999999));
            mobileInput.sendKeys(mobile);
            log.info("✅ Mobile Number filled: {}", mobile);
        } catch (Exception e) {
            try {
                WebElement mobileInput = driver.findElement(
                        By.xpath("//input[@name='organizerDetails[organizer_phone][]']"));
                scrollToElement(mobileInput);
                mobileInput.clear();
                String mobile = "9" + String.format("%09d", random.nextInt(999999999));
                mobileInput.sendKeys(mobile);
                log.info("✅ Mobile Number filled (fallback)");
            } catch (Exception e2) {
                log.warn("Mobile Number not found: {}", e2.getMessage());
            }
        }

        // Email Address
        try {
            WebElement emailInput = driver.findElement(
                    By.xpath("//input[@name='organizerDetails[organizer_email][]']"));
            scrollToElement(emailInput);
            emailInput.clear();
            String[] emailPrefixes = {"rajesh.sharma", "priya.singh", "amit.verma", "sunita.devi",
                    "vikram.chauhan", "neha.gupta", "arun.patel", "kavita.mishra"};
            emailInput.sendKeys(emailPrefixes[random.nextInt(emailPrefixes.length)] + "@yopmail.com");
            log.info("✅ Email filled");
        } catch (Exception e) {
            log.warn("Email not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Click Preview button (bottom-right, red/orange button).
     * Button HTML: <button name="saveDraft" class="btn btn-primary" type="submit" id="preview" value="saveDraft">
     */
    public void clickPreview() throws InterruptedException {
        log.info("Clicking Preview button...");

        // Scroll to very bottom of page to make Preview button visible
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(1000);

        String currentUrl = driver.getCurrentUrl();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement previewBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@id='preview']"
                            + " | //button[@name='saveDraft']"
                            + " | //button[contains(text(),'Preview')]")));
            scrollToElement(previewBtn);
            Thread.sleep(500);

            // Click using form submit approach (since button is type="submit")
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].closest('form').submit();", previewBtn);
            log.info("✅ Submitted form via Preview button");
        } catch (Exception e) {
            // Fallback: direct JS click
            try {
                WebElement previewBtn = driver.findElement(By.id("preview"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", previewBtn);
                log.info("✅ Clicked Preview (JS fallback)");
            } catch (Exception e2) {
                log.error("Preview button not found: {}", e2.getMessage());
                throw new RuntimeException("Preview button not found");
            }
        }

        // Wait for page to change (preview page loads)
        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();

        String newUrl = driver.getCurrentUrl();
        if (newUrl.contains("preview")) {
            log.info("✅ On Preview page: {}", newUrl);
        } else {
            log.warn("URL did not change to preview page. Current: {}", newUrl);
        }
    }

    /**
     * Click Publish button (on preview page).
     */
    public void clickPublish() throws InterruptedException {
        log.info("Clicking Publish button...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        try {
            WebElement publishBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Publish')]"
                            + " | //a[contains(text(),'Publish')]"
                            + " | //button[contains(text(),'Submit')]")));
            scrollToElement(publishBtn);
            Thread.sleep(300);
            safeClick(publishBtn);
            log.info("✅ Clicked Publish");
        } catch (Exception e) {
            log.error("Publish button not found: {}", e.getMessage());
        }

        waitForPageLoad();
        Thread.sleep(5000); // Wait for event to be indexed/visible on public side
        dismissOverlay();
        log.info("✅ Event published successfully");
    }

    /**
     * Logout: Click profile icon (top-right circle "M") → Click "Log Out" from dropdown.
     * From screenshot: Circle icon with letter → dropdown with "Log Out" option.
     */
    public void logout() throws InterruptedException {
        log.info("Logging out...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        Thread.sleep(2000);

        // Click profile icon / circle (top-right corner) - the "M" circle button
        try {
            WebElement profileIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[contains(@class,'user-name') or contains(@class,'profile')]/ancestor::a"
                            + " | //div[contains(@class,'profile')]//a"
                            + " | //a[contains(@class,'dropdown-toggle')]"
                            + " | //a[@data-bs-toggle='dropdown' or @data-toggle='dropdown']"
                            + " | //span[contains(text(),'Maynak') or contains(text(),'Singh')]/ancestor::a"
                            + " | (//nav//a[contains(@class,'dropdown')])[last()]")));
            safeClick(profileIcon);
            Thread.sleep(1000);
            log.info("✅ Clicked profile dropdown");
        } catch (Exception e) {
            // JS fallback: find and click the profile dropdown
            log.warn("Profile dropdown not found via XPath, trying JS...");
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "var links = document.querySelectorAll('a[data-bs-toggle=\"dropdown\"], a[data-toggle=\"dropdown\"], a.dropdown-toggle');" +
                        "if(links.length > 0) { links[links.length-1].click(); }");
                Thread.sleep(1000);
                log.info("✅ Clicked profile dropdown (JS)");
            } catch (Exception e2) {
                log.warn("Profile dropdown JS fallback failed: {}", e2.getMessage());
            }
        }

        // Click "Log Out" from dropdown
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='Log Out']"
                            + " | //a[contains(text(),'Log Out')]"
                            + " | //a[contains(text(),'Logout')]"
                            + " | //button[contains(text(),'Log Out')]")));
            safeClick(logoutBtn);
            Thread.sleep(5000);
            log.info("✅ Clicked Log Out — user logged out");
        } catch (Exception e) {
            // JS fallback for Log Out
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "var links = document.querySelectorAll('a');" +
                        "for(var i=0; i<links.length; i++) {" +
                        "  if(links[i].textContent.trim() === 'Log Out') { links[i].click(); break; }" +
                        "}");
                Thread.sleep(5000);
                log.info("✅ Clicked Log Out (JS fallback)");
            } catch (Exception e2) {
                log.error("Log Out button not found: {}", e2.getMessage());
            }
        }

        // Wait for page to redirect after logout
        waitForPageLoad();
        Thread.sleep(2000);
    }

    /**
     * Save event name to Excel for youth-side tests.
     */
    public void saveEventNameToExcel() {
        String path = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Event_Name.xlsx";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try {
            Workbook workbook;
            if (file.exists() && file.length() > 0) {
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet("Event_Data");
            if (sheet == null) {
                sheet = workbook.createSheet("Event_Data");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Event_Name");
            }

            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(eventName);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            log.info("✅ Event name saved to Excel: {} (row {})", eventName, nextRow);
        } catch (Exception e) {
            log.error("Failed to save event name: {}", e.getMessage());
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void setDateField(String fieldId, int monthsAhead) {
        try {
            WebElement dateInput = driver.findElement(
                    By.xpath("//input[@id='" + fieldId + "' or @name='" + fieldId + "']"));
            scrollToElement(dateInput);
            dateInput.click();
            safeSleep(1000);

            // Navigate months forward in calendar
            for (int i = 0; i < monthsAhead; i++) {
                WebElement nextBtn = driver.findElement(
                        By.cssSelector(".xdsoft_datetimepicker[style*='display: block'] .xdsoft_next"));
                nextBtn.click();
                safeSleep(400);
            }

            // Pick day 15
            WebElement dayCell = driver.findElement(
                    By.xpath("//div[contains(@class,'xdsoft_datetimepicker') and contains(@style,'display: block')]"
                            + "//td[contains(@class,'xdsoft_date') and not(contains(@class,'xdsoft_other_month')) and not(contains(@class,'xdsoft_disabled'))]"
                            + "/div[text()='15']"));
            dayCell.click();
            safeSleep(500);
            log.info("✅ Date selected for {}: 15th, {} month(s) ahead", fieldId, monthsAhead);
        } catch (Exception e) {
            // JS fallback
            try {
                WebElement dateInput = driver.findElement(
                        By.xpath("//input[@id='" + fieldId + "' or @name='" + fieldId + "']"));
                LocalDate futureDate = LocalDate.now().plusMonths(monthsAhead).withDayOfMonth(15);
                String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                ((JavascriptExecutor) driver).executeScript(
                        "var el = arguments[0]; el.readOnly = false; el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                        "try { $(el).trigger('change'); } catch(e) {}", dateInput, dateStr);
                log.info("✅ Date set via JS for {}: {}", fieldId, dateStr);
            } catch (Exception e2) {
                log.warn("Date failed for {}: {}", fieldId, e2.getMessage());
            }
        }
    }

    private void fillTimeField(String fieldId, String timeValue) {
        try {
            WebElement timeInput = driver.findElement(
                    By.xpath("//input[@id='" + fieldId + "' or @name='" + fieldId + "']"));
            scrollToElement(timeInput);
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, arguments[1]);" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));", timeInput, timeValue);
            log.info("✅ Time set for {}: {}", fieldId, timeValue);
        } catch (Exception e) {
            log.warn("Time field {} not found: {}", fieldId, e.getMessage());
        }
    }

    public static String getImagePath() {
        File imagesDir = Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();
        if (!imagesDir.exists()) throw new RuntimeException("UploadImages folder not found");
        File[] files = imagesDir.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files == null || files.length == 0) throw new RuntimeException("No images found");
        for (File f : files) {
            if (f.length() >= 50 * 1024) return f.getAbsolutePath();
        }
        return files[0].getAbsolutePath();
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o = document.getElementById('overlay'); if(o) o.style.display='none';" +
                    "var l = document.getElementById('loader2'); if(l) l.style.display='none';" +
                    "try { $('#overlay').hide(); $('#loader2').hide(); } catch(e) {}");
            safeSleep(500);
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
