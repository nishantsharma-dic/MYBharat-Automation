package com.mybharat.pages.vo;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

public class YouthApplyOnVOPage extends BasePage {

    private static final Logger log = LogManager.getLogger(YouthApplyOnVOPage.class);
    private final ConfigReader config = new ConfigReader();

    public YouthApplyOnVOPage(WebDriver driver) {
        super(driver);
    }

    public void openVolunteerForBharat() throws InterruptedException {
        log.info("Opening Volunteer for Bharat page...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        String baseUrl = config.getUrl();
        driver.get(baseUrl + "/youth-profile");
        waitForPageLoad();
        Thread.sleep(3000);
        dismissOverlay();

        WebElement voLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Volunteer for Bharat'] | //a[contains(@href,'events_yuva')]")));
        scrollToElement(voLink);
        Thread.sleep(300);
        voLink.click();
        Thread.sleep(3000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ On Volunteer for Bharat page. URL: {}", driver.getCurrentUrl());
    }

    public void searchEvent(String eventName) throws InterruptedException {
        log.info("Searching for event: {}", eventName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        // Use the event name from Excel (latest created event)
        String searchKeyword = eventName;

        // Select "All" in Country dropdown
        try {
            WebElement countryDropdown = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.name("filter-country")));
            Select select = new Select(countryDropdown);
            try { select.selectByVisibleText("All"); } catch (Exception e) { select.selectByIndex(0); }
            Thread.sleep(500);
            log.info("✅ Country: All");
        } catch (Exception e) { log.warn("Country dropdown not found"); }

        // Type search keyword (name="filter-vo-name")
        try {
            WebElement eventNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.name("filter-vo-name")));
            eventNameInput.clear();
            eventNameInput.sendKeys(searchKeyword);
            Thread.sleep(500);
            log.info("✅ Event name typed: {}", searchKeyword);
        } catch (Exception e) { log.warn("Event name input not found"); }

        // Click search
        try {
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@class='filter-content-search']"
                            + " | //button[contains(@class,'search') or contains(text(),'Search')]"
                            + " | //input[@type='submit' and contains(@value,'Search')]")));
            searchBtn.click();
            Thread.sleep(5000); // Wait for search results to load
            dismissOverlay();
            log.info("✅ Search clicked");
        } catch (Exception e) {
            log.warn("Search button not found, trying Enter key...");
            try {
                WebElement eventNameInput = driver.findElement(By.name("filter-vo-name"));
                eventNameInput.sendKeys(Keys.ENTER);
                Thread.sleep(5000);
                dismissOverlay();
            } catch (Exception e2) {
                log.warn("Enter key fallback also failed");
            }
        }
    }

    public void clickEventByName(String eventName) throws InterruptedException {
        boolean success = tryClickEventCard(eventName);
        if (!success) {
            throw new RuntimeException("Event card not found or page not found for: " + eventName);
        }
    }

    /**
     * Try to click an event card. Returns true if successfully landed on event detail page.
     * Returns false if page not found or no cards available.
     */
    public boolean tryClickEventCard(String eventName) throws InterruptedException {
        log.info("Trying to click event card: {}", eventName);
        dismissOverlay();
        Thread.sleep(2000);

        // Strategy 1: Find clickable cards/links that contain the event name text
        String shortName = eventName.toLowerCase().trim();
        List<WebElement> eventCards = driver.findElements(By.xpath(
                "//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + shortName + "') "
                + "and not(contains(@href,'facebook')) and not(contains(@href,'twitter')) and not(contains(@href,'sharer'))]"));

        // Strategy 2: Find any card-like links (exclude social/nav links)
        if (eventCards.isEmpty()) {
            eventCards = driver.findElements(By.xpath(
                    "//div[contains(@class,'card') or contains(@class,'event')]//a[not(contains(@href,'facebook')) and not(contains(@href,'twitter')) and not(contains(@href,'sharer')) and not(contains(@href,'#'))]"));
        }

        // Strategy 3: JS — find links with event-related hrefs
        if (eventCards.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<WebElement> jsCards = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                    "var results = []; var all = document.querySelectorAll('a');" +
                    "for(var i=0; i<all.length; i++) {" +
                    "  var h = (all[i].href || '').toLowerCase();" +
                    "  var t = (all[i].textContent || '').toLowerCase();" +
                    "  if(h.indexOf('facebook') !== -1 || h.indexOf('twitter') !== -1 || h.indexOf('sharer') !== -1) continue;" +
                    "  if(h.indexOf('#') === h.length-1) continue;" +
                    "  if(t.indexOf('" + shortName + "') !== -1) results.push(all[i]);" +
                    "}" +
                    "return results;");
            if (jsCards != null && !jsCards.isEmpty()) {
                eventCards = jsCards;
            }
        }

        // Strategy 4: Just find any clickable event card on the page (by common patterns)
        if (eventCards.isEmpty()) {
            eventCards = driver.findElements(By.xpath(
                    "//a[contains(@href,'events_detail') and not(contains(@href,'facebook'))]"
                    + " | //a[contains(@href,'event_detail') and not(contains(@href,'facebook'))]"
                    + " | //a[contains(@href,'volunteer') and contains(@class,'card')]"));
        }

        if (eventCards.isEmpty()) {
            log.warn("No event cards found after searching for: {}", eventName);
            return false;
        }

        log.info("Found {} event card(s)", eventCards.size());

        // Click first valid card; if page not found, try next
        for (int i = 0; i < eventCards.size() && i < 5; i++) {
            WebElement card = eventCards.get(i);
            String href = card.getAttribute("href");
            String text = card.getText().trim();
            log.info("Trying card {} — text: '{}' | href: {}", i + 1, 
                    text.length() > 50 ? text.substring(0, 50) + "..." : text, href);

            // Skip social/share links
            if (href != null && (href.contains("facebook") || href.contains("twitter") || href.contains("sharer"))) {
                log.info("Skipping social share link");
                continue;
            }

            if (href != null && !href.isEmpty() && !href.endsWith("#")) {
                driver.get(href);
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
            }

            Thread.sleep(4000);
            dismissOverlay();

            // Check if page loaded (not "page not found")
            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("page not found") || pageSource.contains("404")) {
                log.warn("Page not found for card {}. Going back...", i + 1);
                driver.navigate().back();
                Thread.sleep(3000);
                dismissOverlay();
                continue;
            }

            // Check if we're on event detail page
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("event") || currentUrl.contains("detail")) {
                log.info("✅ On event detail page. URL: {}", currentUrl);
                return true;
            }

            // Check for event content on page
            boolean hasContent = driver.findElements(By.xpath(
                    "//button[contains(text(),'Apply')]"
                    + " | //a[contains(text(),'Images by Youth')]"
                    + " | //h1 | //h2 | //h3")).size() > 0;
            if (hasContent && !pageSource.contains("page not found")) {
                log.info("✅ Event page loaded. URL: {}", currentUrl);
                return true;
            }

            log.warn("Card {} didn't load event page. Going back...", i + 1);
            driver.navigate().back();
            Thread.sleep(3000);
            dismissOverlay();
        }

        log.warn("Could not open any event card for: {}", eventName);
        return false;
    }

    private boolean waitForLoaderToStop(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void forceHideLoader() {
        ((JavascriptExecutor) driver).executeScript(
                "var o=document.getElementById('overlay');if(o)o.style.display='none';" +
                "var l=document.getElementById('loader2');if(l)l.style.display='none';" +
                "var all=document.querySelectorAll('[class*=loader],[class*=overlay],[class*=spinner]');" +
                "for(var i=0;i<all.length;i++){all[i].style.display='none';}" +
                "try{$('#overlay').hide();$('#loader2').hide();}catch(e){}");
    }

    private boolean isEventDetailPageLoaded() {
        try {
            String url = driver.getCurrentUrl();
            String pageSource = driver.getPageSource().toLowerCase();
            
            // Check for error pages
            if (pageSource.contains("page not found") || pageSource.contains("404")) {
                return false;
            }
            
            // Check if the page has event detail content
            if (url.contains("events_detail") || url.contains("event_detail")) {
                return driver.findElements(By.xpath(
                        "//a[contains(text(),'Images by Youth') or contains(text(),'Images By Youth')]"
                        + " | //button[contains(text(),'Apply')]"
                        + " | //div[contains(@class,'event-detail')]"
                        + " | //h1 | //h2 | //h3"
                        + " | //div[contains(@class,'card')]")).size() > 0;
            }
            
            // If URL doesn't contain event_detail but page has content, still consider loaded
            return driver.findElements(By.xpath("//h1 | //h2 | //h3")).size() > 0
                    && !pageSource.contains("page not found");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click the first available VO event card on the page (no search, just pick first visible).
     */
    public boolean clickFirstAvailableVOCard() throws InterruptedException {
        log.info("Trying to click first available VO card on the page...");
        dismissOverlay();
        Thread.sleep(2000);

        @SuppressWarnings("unchecked")
        List<WebElement> cards = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                "var results = [];" +
                "var all = document.querySelectorAll('a');" +
                "for(var i=0; i<all.length; i++) {" +
                "  var h = (all[i].href || '').toLowerCase();" +
                "  var rect = all[i].getBoundingClientRect();" +
                "  if(rect.width < 50 || rect.height < 50) continue;" +
                "  if(h.indexOf('facebook') !== -1 || h.indexOf('twitter') !== -1 || h.indexOf('sharer') !== -1) continue;" +
                "  if(h.indexOf('event') !== -1 || h.indexOf('volunteer') !== -1 || h.indexOf('detail') !== -1) {" +
                "    if(h.indexOf('#') !== h.length-1) results.push(all[i]);" +
                "  }" +
                "}" +
                "return results;");

        if (cards == null || cards.isEmpty()) {
            cards = driver.findElements(By.xpath(
                    "//div[contains(@class,'card') or contains(@class,'event') or contains(@class,'col')]//a[string-length(@href) > 20 and not(contains(@href,'facebook')) and not(contains(@href,'#'))]"));
        }

        if (cards == null || cards.isEmpty()) {
            log.warn("No VO event cards found on the page");
            return false;
        }

        log.info("Found {} VO cards on page", cards.size());

        for (int i = 0; i < cards.size() && i < 5; i++) {
            WebElement card = cards.get(i);
            String href = card.getAttribute("href");
            log.info("Trying VO card {} — href: {}", i + 1, href);

            if (href != null && !href.isEmpty() && !href.endsWith("#")) {
                driver.get(href);
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
            }

            Thread.sleep(4000);
            dismissOverlay();

            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("page not found") || pageSource.contains("404")) {
                log.warn("Page not found for card {}. Going back...", i + 1);
                driver.navigate().back();
                Thread.sleep(3000);
                dismissOverlay();
                continue;
            }

            String currentUrl = driver.getCurrentUrl();
            boolean hasContent = driver.findElements(By.xpath(
                    "//button[contains(text(),'Apply')]"
                    + " | //a[contains(text(),'Images by Youth')]"
                    + " | //div[contains(@class,'event')]"
                    + " | //h1 | //h2")).size() > 0;
            if (hasContent && !pageSource.contains("page not found")) {
                log.info("✅ Opened VO event page. URL: {}", currentUrl);
                return true;
            }

            log.warn("Card {} didn't load event page. Going back...", i + 1);
            driver.navigate().back();
            Thread.sleep(3000);
            dismissOverlay();
        }

        return false;
    }

    /**
     * Click any available event on the VO page (no search, just pick first visible event).
     */
    public boolean clickAnyAvailableEvent() throws InterruptedException {
        log.info("Trying to click any available event on the page...");
        dismissOverlay();
        Thread.sleep(2000);

        // Find all clickable event cards on the page
        @SuppressWarnings("unchecked")
        List<WebElement> cards = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                "var results = []; var all = document.querySelectorAll('a');" +
                "for(var i=0; i<all.length; i++) {" +
                "  var h = (all[i].href || '').toLowerCase();" +
                "  var t = (all[i].textContent || '').trim();" +
                "  if(h.indexOf('facebook') !== -1 || h.indexOf('twitter') !== -1 || h.indexOf('sharer') !== -1) continue;" +
                "  if(h.indexOf('#') === h.length-1 || t.length < 5) continue;" +
                "  if(h.indexOf('event') !== -1 && h.indexOf('detail') !== -1) results.push(all[i]);" +
                "}" +
                "return results;");

        if (cards == null || cards.isEmpty()) {
            // Try broader search — any card-like element
            cards = driver.findElements(By.xpath(
                    "//div[contains(@class,'card')]//a[string-length(@href) > 10 and not(contains(@href,'facebook'))]"));
        }

        if (cards == null || cards.isEmpty()) {
            log.error("No event cards found on the page at all");
            return false;
        }

        log.info("Found {} available event cards", cards.size());

        for (int i = 0; i < cards.size() && i < 5; i++) {
            WebElement card = cards.get(i);
            String href = card.getAttribute("href");
            log.info("Trying any-event card {} — href: {}", i + 1, href);

            if (href != null && !href.isEmpty() && !href.endsWith("#")) {
                driver.get(href);
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
            }

            Thread.sleep(4000);
            dismissOverlay();

            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("page not found") || pageSource.contains("404")) {
                driver.navigate().back();
                Thread.sleep(3000);
                dismissOverlay();
                continue;
            }

            log.info("✅ Opened an event page. URL: {}", driver.getCurrentUrl());
            return true;
        }

        return false;
    }

    public void clickApplyButton() throws InterruptedException {
        log.info("Clicking Apply button...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@id,'elp_edit') or contains(text(),'Apply')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", applyBtn);
        Thread.sleep(300);
        applyBtn.click();
        Thread.sleep(3000);
        dismissOverlay();
        log.info("✅ Clicked Apply button");
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o=document.getElementById('overlay');if(o)o.style.display='none';" +
                    "var l=document.getElementById('loader2');if(l)l.style.display='none';" +
                    "try{$('#overlay').hide();$('#loader2').hide();}catch(e){}");
        }
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}
