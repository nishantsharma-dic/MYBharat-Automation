package pageobjects.Youth; // Package declaration for register page objects

// Import sections organized by functionality
// File handling imports
import java.io.File;


// Selenium core imports
import org.openqa.selenium.By; // For element locators
import org.openqa.selenium.ElementClickInterceptedException; // For handling click intercepts
import org.openqa.selenium.JavascriptExecutor; // For JavaScript execution

import org.openqa.selenium.WebDriver; // Core WebDriver
import org.openqa.selenium.WebElement; // For web elements

import org.openqa.selenium.support.CacheLookup; // For element caching
import org.openqa.selenium.support.FindBy; // For Page Factory annotations
import org.openqa.selenium.support.PageFactory; // For Page Factory initialization


// Test utilities imports
import com.github.javafaker.Faker; // For generating random test data

import MYBharat_ResourcesAndAbstractComponents.AbstractComponents.AbstractComponent;

import org.testng.asserts.SoftAssert; // For soft assertions


/**
 * Page Object class representing the Youth Registration functionality.
 * Implements Page Factory pattern for element initialization and management.
 * Handles complete registration flow including: - User registration with OTP
 * verification - Form filling with randomized data - Certificate download and
 * verification
 * 
 * @see AbstractComponent
 */
public class PublicProfile extends AbstractComponent {

	/** Core WebDriver instance for browser control */
	WebDriver driver;

	/** Faker instance for generating random user data */
	Faker faker;

	/** Stores generated email for registration tracking */
	String email;

	/** Stores generated mobile number for registration */
	String mobilenum;

	/** SoftAssert instance for non-blocking assertions */
	SoftAssert softAssert = new SoftAssert();

	/**
	 * Constructor initializes core components and test data - Initializes WebDriver
	 * - Sets up Page Factory - Creates Faker instance - Generates and stores test
	 * email - Generates mobile number - Records email to Excel for tracking
	 *
	 * @param driver WebDriver instance for browser automation
	 */
	public PublicProfile(WebDriver driver) {
		super(driver); // Initialize parent AbstractComponent
		this.driver = driver; // Store WebDriver instance
		PageFactory.initElements(driver, this); // Initialize Page Factory elements
		faker = new Faker(); // Create Faker for random data
		
		}

	// Web Element Section - Each element annotated with purpose and usage

	
	
	
	
	

	/** Image element for the registration certificate */
	@FindBy(xpath = "//img[@src='https://cdn-prod.mybharat.in/mybharat/assets/img/yuva_landing/Recreated-Certificate.jpg']")
	@CacheLookup
	private WebElement certificateImage;

	/** Button to download the certificate as PNG */
	@FindBy(xpath = "//div[@class='Downloadbtnpng']")
	@CacheLookup
	private WebElement downloadPNG;

	
	@FindBy(xpath="//i[@class='fa fa-times']") 
	@CacheLookup private WebElement closeButton;
	
	
	
	
	/**
	 * Downloads and verifies the registration certificate Process: 1. Scrolls to
	 * certificate 2. Cleans download directory 3. Clicks certificate and download
	 * button 4. Verifies file exists in downloads
	 * 
	 * @throws InterruptedException if thread sleep is interrupted
	 */
	public void registrationCertificateDownload() throws InterruptedException {
		// Scroll and prepare for download
		scrollPage(2000);
		// Resolve download directory: override with -DdownloadDir=<path>, default to user home Downloads
		String downloadDirPath = System.getProperty("downloadDir",
				System.getProperty("user.home") + File.separator + "Downloads");

		// Clean the folder before downloading
		cleanFolder(downloadDirPath);

		waitForLoaderToDisappear(By.xpath("//div[@class='loader']"));
		// Use a more robust XPath for the certificate image
		By certificateImgBy = By.xpath("//img[contains(@src, 'Recreated-Certificate.jpg')]");
		boolean certImageFound = true;
		try {
			WebElement certImg = waitForWebElementToVisible(certificateImgBy, 15); // Wait up to 15s
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView(true);", certImg);
			waitForWebElementToClickable(certImg);
			try {
				certImg.click();
			} catch (ElementClickInterceptedException e) {
				js.executeScript("arguments[0].click();", certImg);
			}
			waitForWebElementToClickable(downloadPNG);
			downloadPNG.click();
			Thread.sleep(4000); // Wait 4 seconds for download to complete
		} catch (org.openqa.selenium.TimeoutException | org.openqa.selenium.NoSuchElementException e) {
			System.err.println("Certificate image not found. Skipping download.");
			certImageFound = false;
		}
		softAssert.assertTrue(certImageFound, "Certificate image should be present after registration.");

		// Create a File object representing the download directory
		File downloadDir = new File(downloadDirPath);

		// Check if the folder exists
		if (downloadDir.exists() && downloadDir.isDirectory()) {
			// List all files in the folder
			File[] files = downloadDir.listFiles();

			// Print the list of files
			System.out.println("Files in folder: " + downloadDirPath);
			if (files != null) {
				for (File file : files) {
					System.out.println(file.getName());
				}
			}

			// Search for any file that starts with "My_Bharat_Certificate" and ends with
			// ".png"
			boolean found = false;
			String foundFileName = null;
			if (files != null) {
				for (File file : files) {
					if (file.isFile() && file.getName().startsWith("My_Bharat_Certificate")
							&& file.getName().endsWith(".png")) {
						System.out.println("File '" + file.getName() + "' found in folder: " + downloadDirPath);
						found = true;
						foundFileName = file.getName();
						break; // Exit the loop once the file is found
					}
				}
			}
			softAssert.assertTrue(found, "Certificate PNG file should be downloaded to: " + downloadDirPath);
			softAssert.assertAll();
			// If the file is not found
			if (!found) {
				System.out.println("File 'My_Bharat_Certificate.png' not found in folder: " + downloadDirPath);
			}
		} else {
			System.out.println("Folder does not exist or is not a directory: " + downloadDirPath);
		}
		closeButton.click();

	}

}