package com.mybharat.pages.youth;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.mybharat.pages.BasePage;

/**
 * PublicProfilePage - Handles Youth public profile and certificate download.
 */
public class RegistrationCertificateVerificationPage extends BasePage {

    @FindBy(xpath = "//img[contains(@src, 'Recreated-Certificate.jpg')]")
    private WebElement certificateImage;

    @FindBy(xpath = "//div[@class='Downloadbtnpng']")
    private WebElement downloadPngBtn;

    @FindBy(xpath = "//i[@class='fa fa-times']")
    private WebElement closeBtn;

    public RegistrationCertificateVerificationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Download the registration certificate and verify it exists.
     * 
     * @return true if certificate file was downloaded successfully
     */
    public boolean downloadCertificate() throws InterruptedException {
        // Dismiss any leftover alert from previous step
        try {
            driver.switchTo().alert().accept();
            Thread.sleep(1000);
        } catch (Exception e) {
            // No alert — continue
        }

        String downloadDir = System.getProperty("downloadDir",
                System.getProperty("user.home") + File.separator + "Downloads");

        // Clean download folder
        cleanFolder(downloadDir);

        // Wait for loader to disappear
        try {
            waitForInvisible(By.xpath("//div[@class='loader']"));
        } catch (Exception e) {
            // Loader might not be present — continue
        }

        // Click certificate and download
        try {
            WebElement certImg = waitForVisible(By.xpath("//img[contains(@src, 'Recreated-Certificate.jpg')]"));
            scrollToElement(certImg);
            safeClick(certImg);
            waitForClickable(downloadPngBtn);
            downloadPngBtn.click();
            Thread.sleep(4000); // Wait for download
        } catch (Exception e) {
            System.err.println("Certificate image not found: " + e.getMessage());
            return false;
        }

        // Verify file exists
        File dir = new File(downloadDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("My_Bharat_Certificate") && file.getName().endsWith(".png")) {
                        System.out.println("Certificate downloaded: " + file.getName());
                        closeBtn.click();
                        return true;
                    }
                }
            }
        }

        closeBtn.click();
        return false;
    }

    private void cleanFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) file.delete();
                }
            }
        }
    }
}
