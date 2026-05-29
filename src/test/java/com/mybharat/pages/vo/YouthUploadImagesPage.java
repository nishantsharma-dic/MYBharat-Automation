package com.mybharat.pages.vo;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

public class YouthUploadImagesPage extends BasePage {

    private static final Logger log = LogManager.getLogger(YouthUploadImagesPage.class);

    public YouthUploadImagesPage(WebDriver driver) {
        super(driver);
    }

    public void uploadAndSubmitImages() throws InterruptedException {
        log.info("Starting: Upload Images for VO Event");
        clickImagesbyYouthTab();
        clickAddImagesButton();
        uploadImages();
        handleMaxImageValidation();
        clickSendForApproval();
        log.info("✅ Images uploaded and submitted for approval");
    }

    public void clickImagesbyYouthTab() throws InterruptedException {
        log.info("Clicking 'Images by Youth' tab...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight * 0.7);");
        Thread.sleep(2000);

        try {
            WebElement youthTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Images by Youth') or contains(text(),'Images By Youth')]"
                            + " | //a[@id='profile-tab']"
                            + " | //li//a[contains(text(),'Youth')]"
                            + " | //button[contains(text(),'Images by Youth')]")));
            scrollToElement(youthTab);
            Thread.sleep(300);
            youthTab.click();
            Thread.sleep(1000);
            log.info("✅ Clicked 'Images by Youth' tab");
        } catch (Exception e) {
            // JS fallback: find tab by text
            log.warn("Images by Youth tab not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var tabs = document.querySelectorAll('a, button, li');" +
                    "for(var i=0; i<tabs.length; i++) {" +
                    "  if(tabs[i].textContent.toLowerCase().indexOf('images by youth') !== -1) {" +
                    "    tabs[i].click(); break;" +
                    "  }" +
                    "}");
            Thread.sleep(1000);
            log.info("✅ Clicked 'Images by Youth' tab (JS fallback)");
        }
    }

    public void clickAddImagesButton() throws InterruptedException {
        log.info("Clicking 'Add Images for this Event'...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Add Images') or @id='upload-tab'] | //a[contains(text(),'Add Images')]")));
        scrollToElement(addBtn);
        Thread.sleep(300);
        addBtn.click();
        Thread.sleep(1000);
        log.info("✅ Clicked 'Add Images for this Event'");
    }

    public void uploadImages() throws InterruptedException {
        log.info("Uploading 3 images...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        String imagePath = VOEventCreatePage.getImagePath();

        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image-upload")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block';arguments[0].style.opacity='1';arguments[0].classList.remove('hidden');", fileInput);
        Thread.sleep(300);

        for (int i = 0; i < 3; i++) {
            fileInput.sendKeys(imagePath);
            Thread.sleep(500);
        }
        log.info("✅ Uploaded 3 images");
        Thread.sleep(1000);
    }

    public void handleMaxImageValidation() throws InterruptedException {
        log.info("Handling max image validation...");
        try {
            WebElement errorMsg = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(),'maximum of 10')]")));
            log.warn("Validation error: {}", errorMsg.getText());

            List<WebElement> deleteIcons = driver.findElements(By.xpath("//span[@id='0'] | //div[@class='preview']//span[contains(@class,'delete')]"));
            if (!deleteIcons.isEmpty()) {
                WebElement lastDelete = deleteIcons.get(deleteIcons.size() - 1);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", lastDelete);
                Thread.sleep(300);
                lastDelete.click();
                Thread.sleep(1000);
                log.info("✅ Deleted last image");
            }
        } catch (TimeoutException e) {
            log.info("✅ No max image error");
        }
    }

    public void clickSendForApproval() throws InterruptedException {
        log.info("Clicking 'Send For Approval'...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement sendBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='gallery_publish_btn' or contains(text(),'Send For Approval')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", sendBtn);
        Thread.sleep(500);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sendBtn);
        Thread.sleep(3000);
        log.info("✅ Clicked 'Send For Approval'");
    }
}
