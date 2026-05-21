package com.mybharat.pages.vo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
 * VOCreatePage - Fills the "Add Event Template" form for VO (Volunteer for Bharat).
 *
 * URL: /orgeventmanagement/add_event_template
 *
 * Form fields:
 *   1. Event Template Name (required, max 150 chars)
 *   2. Event Type (radio: "Volunteer For Bharat" or "Health and Fitness")
 *   3. Specialization (multi-select, required)
 *   4. Sub Specialization (multi-select)
 *   5. Interest Tags (tag input)
 *   6. Participation Type (checkboxes: Volunteer, Attend, Participate)
 *   7. Event Medium (select: Phygital, Online, In Person - required)
 *   8. Event Description (textarea, 200 chars)
 *   9. Event Dates (Start/End date and time)
 *  10. Theme and Activities
 *  11. Submit ("Create" button)
 */
public class VOCreatePage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOCreatePage.class);

    public VOCreatePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Fill the complete VO Template creation form and submit.
     */
    public void fillTemplateFormAndSubmit() throws InterruptedException {
        log.info("Filling VO Template creation form...");

        fillTemplateName();
        selectEventType();
        selectSpecialization();
        selectParticipationType();
        selectEventMedium();
        fillDescription();
        fillEventDates();
        fillThemeAndActivities();

        log.info("✅ VO Template form filled completely");
    }

    /**
     * Click the Submit/Create button.
     */
    public void clickSubmit() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='submit-button' or @type='submit']"
                            + " | //input[@type='submit']")));
            scrollToElement(submitBtn);
            Thread.sleep(300);
            safeClick(submitBtn);
            log.info("✅ Clicked Submit/Create button");
        } catch (Exception e) {
            try {
                WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Create') or contains(text(),'Submit')]")));
                scrollToElement(submitBtn);
                safeClick(submitBtn);
                log.info("✅ Clicked Create button (text fallback)");
            } catch (Exception e2) {
                log.error("Submit button not found: {}", e2.getMessage());
                throw new RuntimeException("Submit button not found on Create Template form");
            }
        }

        waitForPageLoad();
        Thread.sleep(3000);
    }

    // =========================================================================
    // Form Section Methods
    // =========================================================================

    private void fillTemplateName() throws InterruptedException {
        log.info("Filling Template Name...");
        String uniqueName = "VO Automation Template " + System.currentTimeMillis() % 100000;

        WebElement nameInput = driver.findElement(
                By.xpath("//input[@name='category_name']"));
        scrollToElement(nameInput);
        nameInput.clear();
        nameInput.sendKeys(uniqueName);
        Thread.sleep(300);
        clickNearestToggle(nameInput);
        log.info("Template Name: {}", uniqueName);
    }

    private void selectEventType() throws InterruptedException {
        log.info("Selecting Event Type: Volunteer For Bharat...");
        try {
            WebElement voRadio = driver.findElement(By.id("voRadioBtn"));
            scrollToElement(voRadio);
            if (!voRadio.isSelected()) {
                jsClick(voRadio);
            }
            log.info("✅ Selected 'Volunteer For Bharat' radio");
        } catch (Exception e) {
            try {
                WebElement voRadio = driver.findElement(
                        By.xpath("//input[@name='event_type' and @value='vo']"));
                jsClick(voRadio);
                log.info("✅ Selected 'vo' radio (fallback)");
            } catch (Exception e2) {
                log.warn("Event type radio not found: {}", e2.getMessage());
            }
        }
        Thread.sleep(500);
    }

    private void selectSpecialization() throws InterruptedException {
        log.info("Selecting Specialization...");
        try {
            WebElement specSelect = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("mySelect")));
            scrollToElement(specSelect);
            Select sel = new Select(specSelect);
            if (sel.getOptions().size() > 0) {
                sel.selectByIndex(0);
                log.info("✅ Selected first specialization option");
            }
        } catch (Exception e) {
            try {
                WebElement select2Container = driver.findElement(
                        By.xpath("//div[contains(@class,'select2-container')]"));
                select2Container.click();
                Thread.sleep(500);
                WebElement firstResult = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//li[contains(@class,'select2-result')]")));
                firstResult.click();
                log.info("✅ Selected specialization via Select2");
            } catch (Exception e2) {
                log.warn("Specialization selection failed: {}", e2.getMessage());
            }
        }
        Thread.sleep(300);
    }

    private void selectParticipationType() throws InterruptedException {
        log.info("Selecting Participation Type...");
        try {
            WebElement volunteerCheckbox = driver.findElement(
                    By.xpath("//input[@name='youth_action[]' and @value='1']"
                            + " | //input[@name='youth_actions[]' and @value='1']"));
            scrollToElement(volunteerCheckbox);
            if (!volunteerCheckbox.isSelected()) {
                jsClick(volunteerCheckbox);
            }
            log.info("✅ Checked 'Volunteer' participation type");
        } catch (Exception e) {
            log.info("Participation type checkbox not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void selectEventMedium() throws InterruptedException {
        log.info("Selecting Event Medium...");
        try {
            WebElement mediumSelect = driver.findElement(
                    By.xpath("//select[@name='event_medium' or @name='eventMode']"));
            scrollToElement(mediumSelect);
            Select sel = new Select(mediumSelect);
            sel.selectByVisibleText("In Person");
            log.info("✅ Selected Event Medium: In Person");
        } catch (Exception e) {
            log.warn("Event Medium dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void fillDescription() throws InterruptedException {
        log.info("Filling Description...");
        try {
            WebElement descTextarea = driver.findElement(
                    By.xpath("//textarea[@name='category_description' or @id='exampleTextarea' or @id='description']"));
            scrollToElement(descTextarea);
            descTextarea.clear();
            descTextarea.sendKeys("Automation test - Volunteer opportunity for community outreach and youth engagement activities.");
            log.info("✅ Description filled");
            clickNearestToggle(descTextarea);
        } catch (Exception e) {
            log.warn("Description textarea not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void fillEventDates() throws InterruptedException {
        log.info("Filling Event Dates and Times...");
        selectDateFromCalendar("start_date", 1);
        Thread.sleep(500);
        fillTimeField("start_time", "09", "00");
        Thread.sleep(300);
        selectDateFromCalendar("end_date", 2);
        Thread.sleep(500);
        fillTimeField("end_time", "17", "00");
        Thread.sleep(300);
    }

    private void selectDateFromCalendar(String inputId, int monthsAhead) {
        try {
            WebElement dateInput = driver.findElement(By.id(inputId));
            scrollToElement(dateInput);
            dateInput.click();
            safeSleep(1000);

            for (int i = 0; i < monthsAhead; i++) {
                WebElement nextBtn = driver.findElement(
                        By.cssSelector(".xdsoft_datetimepicker[style*='display: block'] .xdsoft_next"));
                nextBtn.click();
                safeSleep(400);
            }

            WebElement dayCell = driver.findElement(
                    By.xpath("//div[contains(@class,'xdsoft_datetimepicker') and contains(@style,'display: block')]"
                            + "//td[contains(@class,'xdsoft_date') and not(contains(@class,'xdsoft_other_month')) and not(contains(@class,'xdsoft_disabled'))]"
                            + "/div[text()='15']"));
            dayCell.click();
            safeSleep(500);
            log.info("✅ Date selected for #{}: 15th, {} month(s) ahead", inputId, monthsAhead);
        } catch (Exception e) {
            log.warn("Calendar failed for #{}. Using JS fallback", inputId);
            try {
                WebElement dateInput = driver.findElement(By.id(inputId));
                LocalDate futureDate = LocalDate.now().plusMonths(monthsAhead).withDayOfMonth(15);
                String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                ((JavascriptExecutor) driver).executeScript(
                        "var el = arguments[0]; el.readOnly = false; el.value = arguments[1];" +
                        "$(el).trigger('change');", dateInput, dateStr);
                log.info("✅ Date set via JS for #{}: {}", inputId, dateStr);
            } catch (Exception e2) {
                log.error("Date completely failed for #{}: {}", inputId, e2.getMessage());
            }
        }
    }

    private void fillTimeField(String inputId, String hours, String minutes) {
        try {
            WebElement timeInput = driver.findElement(By.id(inputId));
            scrollToElement(timeInput);
            String timeValue = hours + ":" + minutes;
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, arguments[1]);" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));",
                    timeInput, timeValue);
            log.info("✅ Time set for #{}: {}", inputId, timeValue);
        } catch (Exception e) {
            log.warn("Time field #{} failed: {}", inputId, e.getMessage());
        }
    }

    private void fillThemeAndActivities() throws InterruptedException {
        log.info("Filling Theme and Activities...");
        scrollPage(400);
        Thread.sleep(500);

        try {
            WebElement themeInput = driver.findElement(
                    By.xpath("//input[@name='theme' or @placeholder='Enter Theme' or contains(@placeholder,'Theme')]"));
            scrollToElement(themeInput);
            themeInput.clear();
            themeInput.sendKeys("Community Service Drive");
            log.info("✅ Theme filled");
        } catch (Exception e) {
            log.warn("Theme input not found: {}", e.getMessage());
        }
        Thread.sleep(300);

        try {
            WebElement activity1 = driver.findElement(
                    By.xpath("(//input[contains(@name,'activity') or contains(@placeholder,'Enter Name')])[1]"));
            scrollToElement(activity1);
            activity1.clear();
            activity1.sendKeys("Awareness Campaign");
            log.info("✅ Activity 1 filled");
        } catch (Exception e) {
            log.warn("Activity 1 input not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    /**
     * Click ALL toggle switches on the page to enable them.
     */
    public void clickAllToggles() throws InterruptedException {
        log.info("Clicking all toggle switches...");
        java.util.List<WebElement> toggles = driver.findElements(
                By.xpath("//label[contains(@class,'switch')]//span[contains(@class,'slider')]"));
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

    private void clickNearestToggle(WebElement nearElement) {
        try {
            WebElement toggle = nearElement.findElement(
                    By.xpath("./ancestor::div[contains(@class,'row') or contains(@class,'col')]"
                            + "//label[contains(@class,'switch')]//span[contains(@class,'slider')]"));
            jsClick(toggle);
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        } catch (Exception e) {
            try {
                WebElement toggle = nearElement.findElement(
                        By.xpath("./ancestor::div[1]//span[contains(@class,'slider')]"));
                jsClick(toggle);
            } catch (Exception e2) {
                // No toggle found
            }
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
