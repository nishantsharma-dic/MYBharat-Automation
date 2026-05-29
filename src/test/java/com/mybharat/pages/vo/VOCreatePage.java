package com.mybharat.pages.vo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

/**
 * VOCreatePage - Fills the "Add Template" form for VO (Volunteer for Bharat).
 *
 * URL: /orgeventmanagement/add_event_template
 *
 * Form fields (from screenshot):
 *   1. Event Template Name* (input + toggle)
 *   2. Event Type (radio: "Volunteer for Bharat" / "Health and Fitness")
 *   3. Specialization* (select dropdown)
 *   4. Sub Specialization (select dropdown)
 *   5. Add Interest Tags (tag input)
 *   6. Select Participation Type (checkboxes: Volunteer, Attend, Participate)
 *   7. Event Medium* (select: "Select Event Medium")
 *   8. Event Description (textarea + toggle)
 *   9. Event Start Date* / Event Start Time* / Event End Date* / Event End Time*
 *  10. Theme* (input: "Enter Theme")
 *  11. Activity 1* (input: "Enter Name") + Activity 2, 3, 4
 *  12. "Create" button (bottom-right blue)
 */
public class VOCreatePage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOCreatePage.class);
    private final Random random = new Random();

    // Fixed event name prefix — only city changes randomly
    private static final String EVENT_PREFIX = "Swachhta Hi Seva";

    // Indian city names for uniqueness
    private static final String[] CITIES = {
            "Ghaziabad", "Lucknow", "Varanasi", "Jaipur", "Bhopal", "Indore",
            "Patna", "Ranchi", "Dehradun", "Chandigarh", "Agra", "Kanpur",
            "Prayagraj", "Meerut", "Noida", "Gurugram", "Faridabad", "Jodhpur",
            "Udaipur", "Kota", "Nagpur", "Pune", "Nashik", "Surat", "Ahmedabad"
    };

    public VOCreatePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Fill the complete Add Template form and click Create.
     */
    public void fillTemplateFormAndSubmit() throws InterruptedException {
        log.info("=== Filling Add Template form ===");
        dismissOverlay();

        fillTemplateName();
        selectEventType();
        selectSpecialization();
        selectSubSpecialization();
        selectParticipationType();
        selectEventMedium();
        fillDescription();
        fillEventDates();
        fillThemeAndActivities();
        clickAllToggles();

        log.info("✅ Template form filled completely");
    }

    /**
     * Click the "Create" button (bottom-right blue button).
     */
    public void clickCreate() throws InterruptedException {
        log.info("Clicking Create button...");
        dismissOverlay();
        scrollPage(500);
        Thread.sleep(500);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        String[] locators = {
                "//button[normalize-space()='Create']",
                "//button[contains(text(),'Create')]",
                "//input[@type='submit' and @value='Create']",
                "//button[@type='submit']",
                "//a[normalize-space()='Create']"
        };

        for (String xpath : locators) {
            try {
                WebElement createBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                scrollToElement(createBtn);
                Thread.sleep(300);
                safeClick(createBtn);
                log.info("✅ Clicked Create button ({})", xpath);
                Thread.sleep(3000);
                dismissOverlay();
                return;
            } catch (Exception e) {
                // try next
            }
        }

        log.error("Create button not found with any locator");
        throw new RuntimeException("Create button not found on Add Template form");
    }

    // =========================================================================
    // Form Fields
    // =========================================================================

    /**
     * Fill Event Template Name field.
     * Input: placeholder "Enter the Event Template Name"
     */
    private void fillTemplateName() throws InterruptedException {
        log.info("Filling Template Name...");

        // Read last number from Excel and increment by 1
        int nextNumber = getNextTemplateNumber();
        String uniqueName = "Swachhta Hi Seva " + nextNumber;

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Enter the Event Template Name' or @name='category_name']")));
        scrollToElement(nameInput);
        nameInput.clear();
        nameInput.sendKeys(uniqueName);
        Thread.sleep(300);

        // Click the toggle switch next to Template Name
        clickNearestToggle(nameInput);
        log.info("✅ Template Name: {}", uniqueName);
    }

    /**
     * Get the next template number by reading the last number used and incrementing by 1.
     * Stores the counter in resources/Template_Counter.txt
     */
    private int getNextTemplateNumber() {
        String counterPath = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "Template_Counter.txt";
        java.io.File counterFile = new java.io.File(counterPath);
        int currentNumber = 0;

        // Read current number
        if (counterFile.exists()) {
            try (java.util.Scanner scanner = new java.util.Scanner(counterFile)) {
                if (scanner.hasNextInt()) {
                    currentNumber = scanner.nextInt();
                }
            } catch (Exception e) {
                log.warn("Could not read Template_Counter.txt, starting from 1");
            }
        }

        // Increment
        int nextNumber = currentNumber + 1;

        // Save new number
        try (java.io.FileWriter writer = new java.io.FileWriter(counterFile)) {
            writer.write(String.valueOf(nextNumber));
        } catch (Exception e) {
            log.warn("Could not write Template_Counter.txt: {}", e.getMessage());
        }

        return nextNumber;
    }

    /**
     * Select Event Type = "Volunteer for Bharat" radio button.
     * Radio: "Volunteer for Bharat" is pre-selected (blue dot in screenshot)
     */
    private void selectEventType() throws InterruptedException {
        log.info("Selecting Event Type: Volunteer for Bharat...");
        try {
            // Try by label text
            WebElement voRadio = driver.findElement(
                    By.xpath("//input[@type='radio' and following-sibling::*[contains(text(),'Volunteer')] or @id='voRadioBtn']"
                            + " | //label[contains(text(),'Volunteer for Bharat')]/preceding-sibling::input[@type='radio']"
                            + " | //label[contains(text(),'Volunteer for Bharat')]//input[@type='radio']"));
            if (!voRadio.isSelected()) {
                jsClick(voRadio);
                log.info("✅ Selected 'Volunteer for Bharat'");
            } else {
                log.info("✅ 'Volunteer for Bharat' already selected");
            }
        } catch (Exception e) {
            // It's pre-selected in the screenshot, so just continue
            log.info("Event type radio not found — likely pre-selected, continuing");
        }
        Thread.sleep(300);
    }

    /**
     * Select Specialization from dropdown.
     * Label: "Specialization*", placeholder: "Select Specialization"
     */
    private void selectSpecialization() throws InterruptedException {
        log.info("Selecting Specialization...");
        try {
            WebElement specSelect = driver.findElement(
                    By.xpath("//select[contains(@name,'specialization') or @id='mySelect' or @id='specialization']"
                            + " | //select[./option[contains(text(),'Select Specialization')]]"));
            scrollToElement(specSelect);
            Select sel = new Select(specSelect);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Specialization selected: {}", sel.getFirstSelectedOption().getText());
            }
        } catch (Exception e) {
            // Try Select2 widget
            try {
                WebElement select2 = driver.findElement(
                        By.xpath("//span[contains(@class,'select2')]"));
                select2.click();
                Thread.sleep(500);
                WebElement firstOption = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//li[contains(@class,'select2-results__option')]")));
                firstOption.click();
                log.info("✅ Specialization selected via Select2");
            } catch (Exception e2) {
                log.warn("Specialization selection failed: {}", e2.getMessage());
            }
        }
        Thread.sleep(300);
    }

    /**
     * Select Sub Specialization (optional).
     */
    private void selectSubSpecialization() throws InterruptedException {
        log.info("Selecting Sub Specialization...");
        try {
            WebElement subSpecSelect = driver.findElement(
                    By.xpath("//select[contains(@name,'sub_specialization') or @id='sub_specialization']"
                            + " | //select[./option[contains(text(),'Sub Specialization') or contains(text(),'sub specialization')]]"));
            scrollToElement(subSpecSelect);
            Select sel = new Select(subSpecSelect);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                log.info("✅ Sub Specialization selected");
            }
        } catch (Exception e) {
            log.info("Sub Specialization not available or not required — skipping");
        }
        Thread.sleep(300);
    }

    /**
     * Select Participation Type checkboxes.
     * Options: Volunteer, Attend, Participate
     */
    private void selectParticipationType() throws InterruptedException {
        log.info("Selecting Participation Type...");
        String[] types = {"Volunteer", "Attend", "Participate"};

        for (String type : types) {
            try {
                WebElement checkbox = driver.findElement(
                        By.xpath("//label[contains(text(),'" + type + "')]//input[@type='checkbox']"
                                + " | //input[@type='checkbox' and following-sibling::*[contains(text(),'" + type + "')]]"
                                + " | //label[contains(text(),'" + type + "')]/preceding-sibling::input[@type='checkbox']"));
                if (!checkbox.isSelected()) {
                    jsClick(checkbox);
                    log.info("✅ Checked '{}'", type);
                }
            } catch (Exception e) {
                log.info("Checkbox '{}' not found — skipping", type);
            }
        }
        Thread.sleep(300);
    }

    /**
     * Select Event Medium from dropdown.
     * Options: Phygital, Online, In Person
     */
    private void selectEventMedium() throws InterruptedException {
        log.info("Selecting Event Medium...");
        try {
            WebElement mediumSelect = driver.findElement(
                    By.xpath("//select[contains(@name,'event_medium') or contains(@name,'eventMode')]"
                            + " | //select[./option[contains(text(),'Select Event Medium')]]"));
            scrollToElement(mediumSelect);
            Select sel = new Select(mediumSelect);
            // Try "In Person" first, then any available option
            try {
                sel.selectByVisibleText("In Person");
            } catch (Exception e) {
                if (sel.getOptions().size() > 1) {
                    sel.selectByIndex(1);
                }
            }
            log.info("✅ Event Medium selected: {}", sel.getFirstSelectedOption().getText());
        } catch (Exception e) {
            log.warn("Event Medium dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Description textarea.
     */
    private void fillDescription() throws InterruptedException {
        log.info("Filling Event Description...");
        String[] descriptions = {
                "This volunteer opportunity aims to engage youth in community service activities including cleanliness drives, tree plantation, and awareness campaigns for sustainable development.",
                "Join us for a meaningful initiative to empower local communities through skill-building workshops, health awareness sessions, and environmental conservation activities.",
                "A collaborative effort to bring together young volunteers for social impact through education outreach, digital literacy programs, and neighbourhood beautification drives.",
                "This event focuses on building civic responsibility among youth through hands-on participation in public welfare activities, mentorship programs, and cultural exchange.",
                "An initiative under the Volunteer for Bharat program to promote community engagement, environmental sustainability, and holistic development of young citizens."
        };
        try {
            WebElement descTextarea = driver.findElement(
                    By.xpath("//textarea[contains(@name,'description') or @id='exampleTextarea' or @name='category_description']"
                            + " | //label[contains(text(),'Event Description')]/following::textarea[1]"));
            scrollToElement(descTextarea);
            descTextarea.clear();
            descTextarea.sendKeys(descriptions[random.nextInt(descriptions.length)]);
            log.info("✅ Description filled");

            // Click toggle next to description
            clickNearestToggle(descTextarea);
        } catch (Exception e) {
            log.warn("Description textarea not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Fill Event Start Date, Start Time, End Date, End Time.
     * Uses xdsoft datetimepicker calendar with JS fallback.
     */
    private void fillEventDates() throws InterruptedException {
        log.info("Filling Event Dates and Times...");
        scrollPage(300);
        Thread.sleep(500);

        // Event Start Date (1 month ahead, day 15)
        setDateField("start_date", 1);
        Thread.sleep(500);

        // Event Start Time
        setTimeField("start_time", "09:00");
        Thread.sleep(300);

        // Event End Date (2 months ahead, day 15)
        setDateField("end_date", 2);
        Thread.sleep(500);

        // Event End Time
        setTimeField("end_time", "17:00");
        Thread.sleep(300);
    }

    /**
     * Fill Theme and Activity fields.
     * Theme*: input placeholder "Enter Theme"
     * Activity 1*: input placeholder "Enter Name"
     */
    private void fillThemeAndActivities() throws InterruptedException {
        log.info("Filling Theme and Activities...");
        scrollPage(400);
        Thread.sleep(500);

        String[] themes = {
                "Swachh Bharat Seva", "Youth Empowerment", "Green India Campaign",
                "Digital India Outreach", "Health and Wellness", "Community Development"
        };
        String[] activities1 = {
                "Cleanliness Drive", "Tree Plantation", "Health Check-up Camp",
                "Digital Literacy Workshop", "Blood Donation Camp", "Awareness Rally"
        };
        String[] activities2 = {
                "Wall Painting", "Street Play", "Yoga Session",
                "Essay Competition", "Poster Making", "Cultural Program"
        };

        // Theme
        try {
            WebElement themeInput = driver.findElement(
                    By.xpath("//input[@placeholder='Enter Theme' or @name='theme' or contains(@placeholder,'Theme')]"));
            scrollToElement(themeInput);
            themeInput.clear();
            themeInput.sendKeys(themes[random.nextInt(themes.length)]);
            log.info("✅ Theme filled");
        } catch (Exception e) {
            log.warn("Theme input not found: {}", e.getMessage());
        }
        Thread.sleep(300);

        // Activity 1 (required)
        try {
            WebElement activity1 = driver.findElement(
                    By.xpath("(//input[@placeholder='Enter Name'])[1]"
                            + " | (//input[contains(@name,'activity')])[1]"));
            scrollToElement(activity1);
            activity1.clear();
            activity1.sendKeys(activities1[random.nextInt(activities1.length)]);
            log.info("✅ Activity 1 filled");
        } catch (Exception e) {
            log.warn("Activity 1 not found: {}", e.getMessage());
        }
        Thread.sleep(300);

        // Activity 2 (optional)
        try {
            WebElement activity2 = driver.findElement(
                    By.xpath("(//input[@placeholder='Enter Name'])[2]"));
            activity2.clear();
            activity2.sendKeys(activities2[random.nextInt(activities2.length)]);
            log.info("✅ Activity 2 filled");
        } catch (Exception e) {
            // optional — skip
        }
    }

    /**
     * Click ALL toggle switches on the page to enable them.
     * Toggles are: <label class="switch"><input type="checkbox"><span class="slider round"></span></label>
     */
    public void clickAllToggles() throws InterruptedException {
        log.info("Clicking all toggle switches...");
        List<WebElement> toggles = driver.findElements(
                By.xpath("//label[contains(@class,'switch')]//span[contains(@class,'slider')]"
                        + " | //label[contains(@class,'switch')]//input[@type='checkbox']"));
        int clicked = 0;
        for (WebElement toggle : toggles) {
            try {
                if (toggle.isDisplayed()) {
                    scrollToElement(toggle);
                    jsClick(toggle);
                    clicked++;
                    Thread.sleep(200);
                }
            } catch (Exception ignored) {}
        }
        log.info("✅ Clicked {} toggle switches", clicked);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void setDateField(String inputId, int monthsAhead) {
        try {
            WebElement dateInput = driver.findElement(By.id(inputId));
            scrollToElement(dateInput);
            dateInput.click();
            safeSleep(1000);

            // Navigate months forward in xdsoft calendar
            for (int i = 0; i < monthsAhead; i++) {
                try {
                    WebElement nextBtn = driver.findElement(
                            By.cssSelector(".xdsoft_datetimepicker[style*='display: block'] .xdsoft_next"));
                    nextBtn.click();
                    safeSleep(400);
                } catch (Exception e) {
                    break;
                }
            }

            // Pick day 15
            try {
                WebElement dayCell = driver.findElement(
                        By.xpath("//div[contains(@class,'xdsoft_datetimepicker') and contains(@style,'display: block')]"
                                + "//td[contains(@class,'xdsoft_date') and not(contains(@class,'xdsoft_other_month'))"
                                + " and not(contains(@class,'xdsoft_disabled'))]/div[text()='15']"));
                dayCell.click();
                safeSleep(500);
                log.info("✅ Date selected for #{}: 15th, {} month(s) ahead", inputId, monthsAhead);
                return;
            } catch (Exception e) {
                // Calendar day not found
            }
        } catch (Exception e) {
            // Input not found by ID
        }

        // JS fallback — set value directly
        log.warn("Calendar failed for #{}, using JS fallback", inputId);
        try {
            WebElement dateInput = driver.findElement(By.id(inputId));
            LocalDate futureDate = LocalDate.now().plusMonths(monthsAhead).withDayOfMonth(15);
            String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "el.readOnly = false;" +
                    "el.value = arguments[1];" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "try { $(el).trigger('change'); } catch(e) {}",
                    dateInput, dateStr);
            log.info("✅ Date set via JS for #{}: {}", inputId, dateStr);
        } catch (Exception e2) {
            log.error("Date completely failed for #{}: {}", inputId, e2.getMessage());
        }
    }

    private void setTimeField(String inputId, String timeValue) {
        try {
            WebElement timeInput = driver.findElement(By.id(inputId));
            scrollToElement(timeInput);
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, arguments[1]);" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));",
                    timeInput, timeValue);
            log.info("✅ Time set for #{}: {}", inputId, timeValue);
        } catch (Exception e) {
            log.warn("Time field #{} not found: {}", inputId, e.getMessage());
        }
    }

    private void clickNearestToggle(WebElement nearElement) {
        try {
            WebElement toggle = nearElement.findElement(
                    By.xpath("./ancestor::div[contains(@class,'row') or contains(@class,'col') or contains(@class,'form')]"
                            + "//label[contains(@class,'switch')]//span[contains(@class,'slider')]"));
            jsClick(toggle);
            safeSleep(200);
            log.info("✅ Toggle clicked");
        } catch (Exception e) {
            try {
                WebElement toggle = nearElement.findElement(
                        By.xpath("./ancestor::div[1]//span[contains(@class,'slider')]"));
                jsClick(toggle);
                safeSleep(200);
            } catch (Exception e2) {
                // No toggle found — not all fields have toggles
            }
        }
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o = document.getElementById('overlay'); if(o) o.style.display='none';" +
                    "var l = document.getElementById('loader2'); if(l) l.style.display='none';" +
                    "try { $('#overlay').hide(); $('#loader2').hide(); $('.loader').hide(); } catch(e) {}");
            safeSleep(500);
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
