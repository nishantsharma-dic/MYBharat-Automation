package pageobjects.Youth; // Package declaration for register page objects

// Import sections organized by functionality
// File handling imports
import java.io.File;
import java.util.ArrayList; // For handling browser tabs/windows

import org.openqa.selenium.By;
// Selenium core imports
import org.openqa.selenium.JavascriptExecutor; // For JavaScript execution
import org.openqa.selenium.Keys; // For keyboard actions
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver; // Core WebDriver
import org.openqa.selenium.WebElement; // For web elements
import org.openqa.selenium.WindowType; // For new window/tab handling
import org.openqa.selenium.support.CacheLookup; // For element caching
import org.openqa.selenium.support.FindBy; // For Page Factory annotations
import org.openqa.selenium.support.PageFactory; // For Page Factory initialization
import org.openqa.selenium.support.ui.Select; // For dropdown handling

// Test utilities imports
import com.github.javafaker.Faker; // For generating random test data

import MYBharat_ResourcesAndAbstractComponents.AbstractComponents.AbstractComponent;
import MYBharat_ResourcesAndAbstractComponents.resources.EmailExcelWriter;

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
public class Registration extends AbstractComponent {

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
	public Registration(WebDriver driver) {
		super(driver); // Initialize parent AbstractComponent
		this.driver = driver; // Store WebDriver instance
		PageFactory.initElements(driver, this); // Initialize Page Factory elements
		faker = new Faker(); // Create Faker for random data
		email = faker.name().fullName().replace(" ", "").replace("'", "").replace(".", "") + "@yopmail.com"; // Generate
																												// email
		mobilenum = get10DigitNumber(); // Generate mobile number
		System.out.println(email); // Log generated email
		EmailExcelWriter.appendEmail(email); // Save email for tracking
	
		
	}

	// Web Element Section - Each element annotated with purpose and usage

	@FindBy(xpath = "//i[@class='fa fa-times']")
	@CacheLookup
	private WebElement closePopupQuiz;

	/** Initial registration button to start process */
	@FindBy(xpath = "//span[@class='fontchange']")
	@CacheLookup // Cache for performance
	private WebElement registerNow;

	@FindBy(xpath = "//input[@id='internationalUserCheckbox']")
	@CacheLookup
	private WebElement internationalUserCheckbox;

	/** Main registration button after initial click */
	@FindBy(xpath = "//button[@class='btn btn_login lang_yuva_register_as_youth_btn fontchange']")
	@CacheLookup
	private WebElement register;

	/** Input field for mobile/email entry */
	@FindBy(xpath = "(//input[@id='user_mobile'])[1]")
	@CacheLookup
	private WebElement enterMobileEmail;

	/** Button to request OTP */
	@FindBy(css = "button.generate_otp")
	@CacheLookup
	private WebElement getOTP;

	/** Input field for entering email in dummy email provider */
	@FindBy(xpath = "//input[@id='login']")
	@CacheLookup
	private WebElement enterYourInboxHere;

	/** Button to confirm email entry and proceed */
	@FindBy(css = ".material-icons-outlined.f36")
	@CacheLookup
	private WebElement go;

	/** Button to refresh inbox for new emails */
	@FindBy(xpath = "//button[@id='refresh']")
	@CacheLookup
	private WebElement refresh;

	/** Email element containing the OTP message */
	@FindBy(xpath = "//p[contains(text(),'Your one-time password (OTP) for registering on My')]")
	@CacheLookup
	private WebElement Password;

	/** OTP input field */
	@FindBy(xpath = "(//input[@id='otp-field-1'])[1]")
	@CacheLookup
	private WebElement otpfield;

	/** Button to verify the entered OTP */
	@FindBy(xpath = "//button[@id='btn-verify-otp']")
	@CacheLookup
	private WebElement verifyOTP;

	/** Input field for first name */
	@FindBy(xpath = "//input[@id='firstname']")
	@CacheLookup
	private WebElement firstName;

	/** Input field for last name */
	@FindBy(xpath = "//input[@id='lastname']")
	@CacheLookup
	private WebElement lastName;

	/** Input field for password */
	@FindBy(xpath = "//input[@id='registerPassword']")
	@CacheLookup
	private WebElement enterpassword;

	/** Input field for confirming password */
	@FindBy(xpath = "//input[@id='confirmPassword']")
	@CacheLookup
	private WebElement confirmPassword;

	/** Input field for day of birth */
	@FindBy(xpath = "//input[@id='dobDD']")
	@CacheLookup
	private WebElement youthDateOfBirth;

	/** Input field for month of birth */
	@FindBy(xpath = "//input[@id='dobMM']")
	@CacheLookup
	private WebElement youthMonthOfBirth;

	/** Input field for year of birth */
	@FindBy(xpath = "//input[@id='dobYYYY']")
	@CacheLookup
	private WebElement youthYearOfBirth;

	/** Dropdown for selecting gender */
	@FindBy(xpath = "//select[@id='gender']")
	@CacheLookup
	private WebElement youthgender;

	@FindBy(xpath = "//select[@id='country']")
	@CacheLookup
	private WebElement country;

	@FindBy(css = "#address_line1")
	@CacheLookup
	private WebElement addressLine1;

	@FindBy(css = "#address_line2")
	@CacheLookup
	private WebElement addressLine2;

	/** Dropdown for selecting state */
	@FindBy(xpath = "//select[@id='state']")
	@CacheLookup
	private WebElement YouthState;

	/** Dropdown for selecting district */
	@FindBy(xpath = "//select[@id='district']")
	@CacheLookup
	private WebElement Youthdistrict;

	/** Radio button for urban residence */
	@FindBy(xpath = "(//input[@id='flexRadioDefault1'])[1]")
	@CacheLookup
	private WebElement urban;


	/** Dropdown for selecting urban local body */
	@FindBy(xpath = "//select[@id='ulb']")
	@CacheLookup
	private WebElement uLB;

	/** Radio button for rural residence */
	@FindBy(xpath = "(//input[@id='flexRadioDefault2'])[1]")
	@CacheLookup
	private WebElement rural;

	/** Placeholder for block selection dropdown */
	@FindBy(xpath = "//div[@class='choices__item choices__placeholder choices__item--selectable'][normalize-space()='Search and select a block']")
	@CacheLookup
	private WebElement searchAndSelectABlock;

	/** Input field for searching and selecting a block */
	@FindBy(xpath = "(//div[contains(@class,'choices')]/input)[1]")
	private WebElement searchAndSelectABlockInput;

	/** Placeholder for panchayat selection dropdown */
	@FindBy(xpath = "//div[@class='choices__item choices__placeholder choices__item--selectable'][normalize-space()='Search and select a panchayat']")
	@CacheLookup
	private WebElement searchAndSelectAPanchayat;

	/** Input field for searching and selecting a panchayat */
	@FindBy(xpath = "(//div[contains(@class,'choices')]/input)[2]")
	private WebElement searchAndSelectAPanchayatInput;

	/** Placeholder for village selection dropdown */
	@FindBy(xpath = "//div[@class='choices__item choices__placeholder choices__item--selectable'][normalize-space()='Search and select a village']")
	@CacheLookup
	private WebElement searchAndSelectAVillage;

	/** Input field for searching and selecting a village */
	@FindBy(xpath = "(//div[contains(@class,'choices')]/input)[3]")
	private WebElement searchAndSelectAVillageInput;

	/** Input field for urban pincode */
	@FindBy(xpath = "(//input[@id='pincode_urban'])[1]")
	@CacheLookup
	private WebElement urbanPincode;

	/** Input field for rural pincode */
	@FindBy(xpath = "(//input[@id='pincode_rural'])[1]")
	@CacheLookup
	private WebElement ruralPincode;

	/** Checkbox for selecting Yuva type */
	@FindBy(xpath = "(//input[@id='NSS'])[1]")
	@CacheLookup
	private WebElement yuvaType;

	/** Dropdown for selecting highest qualification */
	@FindBy(xpath = "//select[@id='qualification']")
	@CacheLookup
	private WebElement highestQualification;

	/** Dropdown for selecting institution type */
	@FindBy(xpath = "//select[@id='institution_type']")
	@CacheLookup
	private WebElement institutionType;

	/** Dropdown for selecting school state */
	@FindBy(xpath = "//select[@id='institution_state']")
	@CacheLookup
	private WebElement schoolState;

	/** Dropdown for selecting school district */
	@FindBy(xpath = "//select[@id='institution_district']")
	@CacheLookup
	private WebElement schoolDistrict;

	/** Placeholder for institution selection dropdown */
	@FindBy(xpath = "//div[@class='choices__item choices__placeholder choices__item--selectable'][normalize-space()='Search and select an institution']")
	@CacheLookup
	private WebElement searchAndSelectAnInstituti;

	/** Input field for searching and selecting an institution */
	@FindBy(xpath = "(//div[contains(@class,'choices')]/input)[4]")
	private WebElement searchAndSelectAnInstitutiInput;

	/** Placeholder for sport selection dropdown */
	@FindBy(xpath = "//div[contains(text(),'Search and select a sport')]")
	@CacheLookup
	private WebElement searchAndSelectASport;

	/** Input field for searching and selecting a sport */
	@FindBy(xpath = "(//div[contains(@class,'choices')]/input)[5]")
	private WebElement searchAndSelectASportInput;

	/** Checkbox for participation in sports */
	@FindBy(xpath = "//input[@id='khel_participate']")
	@CacheLookup
	private WebElement doYouWishToParticipateIn;

	/** Checkbox for consenting to terms and conditions */
	@FindBy(xpath = "(//input[@id='defaultCheck1'])[1]")
	@CacheLookup
	private WebElement iConsentToTerms;

	/** Button to submit the registration form */
	@FindBy(xpath = "//button[@id='registrationButton']")
	@CacheLookup
	private WebElement submit;

	/** Image element for the registration certificate */
	@FindBy(xpath = "//img[@src='https://cdn-prod.mybharat.in/mybharat/assets/img/yuva_landing/Recreated-Certificate.jpg']")
	@CacheLookup
	private WebElement certificateImage;

	/** Button to download the certificate as PNG */
	@FindBy(xpath = "//div[@class='Downloadbtnpng']")
	@CacheLookup
	private WebElement downloadPNG;

	/**
	 * Completes OTP verification process - Navigates registration flow - Handles
	 * email verification - Processes OTP submission - Validates each step with soft
	 * assertions
	 *
	 * @throws InterruptedException for wait operations
	 */

	public void register_verifyOTPandGoToRegistrationForIndians() throws InterruptedException {
		// Close quiz popup if present and visible
		try {
			if (closePopupQuiz.isDisplayed() && closePopupQuiz.isEnabled()) {
				closePopupQuiz.click();
				// Wait for overlay to disappear
				Thread.sleep(500); // or use explicit wait for overlay to be gone
			}
			
			

		} catch (Exception e) {
			// Popup not present, continue
		}
		// Wait and validate initial registration button

		waitForWebElementToClickable(registerNow);
		softAssert.assertTrue(registerNow.isDisplayed() && registerNow.isEnabled(),
				"Initial registration button should be accessible");
		registerNow.click();

		// Wait and validate main register button
		waitForWebElementToClickable(register);
		softAssert.assertTrue(register.isDisplayed() && register.isEnabled(),
				"Main registration button should be accessible");
		register.click();

		// Enhanced handling for mobile/email input field
		waitForWebElementToVisible(enterMobileEmail);
		// Scroll element into view using JavaScript
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView(true);", enterMobileEmail);
		// Add a small wait to ensure the element is truly visible
		Thread.sleep(500);

		// Try multiple approaches to interact with the element
		try {
			// First try normal click
			enterMobileEmail.click();
		} catch (Exception e) {
			try {
				// If normal click fails, try JavaScript click
				js.executeScript("arguments[0].click();", enterMobileEmail);
			} catch (Exception e2) {
				// If JavaScript click fails, try moving to element first
				js.executeScript("arguments[0].scrollIntoView({block: 'center'});", enterMobileEmail);
				Thread.sleep(500);
				enterMobileEmail.click();
			}
		}

		// Clear any existing value and enter email
		enterMobileEmail.clear();
		enterMobileEmail.sendKeys(email);
		System.out.println("Entered email: " + email);

		// Validate and click OTP request button with enhanced handling
		waitForWebElementToClickable(getOTP);
		softAssert.assertTrue(getOTP.isDisplayed() && getOTP.isEnabled(),
				"Get OTP button should be visible and enabled");
		try {
			getOTP.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", getOTP);
		}

		// Rest of the OTP verification process
		Thread.sleep(2000); // Wait for 2 seconds for OTP to be sent
		// Switch to new browser tab for email verification
		driver.switchTo().newWindow(WindowType.TAB);

		// Open dummy email provider in new tab
		String dummyemailUrl = getProperty("dummyEmail");
		driver.get(dummyemailUrl);

		// Clear inbox field if displayed and enabled
		if (enterYourInboxHere.isDisplayed() && enterYourInboxHere.isEnabled()) {
			enterYourInboxHere.clear();
		}

		// Enter email prefix in the inbox field to receive OTP
		String[] parts = email.split("@");
		enterYourInboxHere.sendKeys(parts[0]);
		
		// Click on go button to access inbox
		go.click();

		// Refresh inbox to get the latest email
		refresh.click();

		// Switch to iframe containing the email content
		driver.switchTo().frame("ifmail");

		// Extract OTP from the email content
		String OTP = Password.getText().split("\\. This")[0].trim().split(" is ")[1].trim();
		System.out.println("Youth email is " + email);
		System.out.println("OTP is : " + OTP);

		// Close the new tab and switch back to the original tab
		ArrayList<String> tab = new ArrayList<>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1)).close();
		driver.switchTo().window(tab.get(0));

		// Enter the extracted OTP in the OTP field
		softAssert.assertTrue(otpfield.isDisplayed() && otpfield.isEnabled(),
				"OTP field should be visible and enabled");
		otpfield.sendKeys(OTP);

		// Validate and click on "Verify OTP" button
		softAssert.assertTrue(verifyOTP.isDisplayed() && verifyOTP.isEnabled(),
				"Verify OTP button should be visible and enabled");
		verifyOTP.click();
		softAssert.assertAll(); // Assert all soft assertions
	}

	/**
	 * Fills out the complete registration form with random data Form sections
	 * include: - Personal details (name, password) - Date of birth - Location
	 * (state, district) - Address (urban/rural) - Education and institution details
	 * - Sports preferences
	 * 
	 * @throws InterruptedException if thread sleep is interrupted
	 */
	public void registerForIndians() throws InterruptedException {
		// Initialize JavaScript executor for reliable element interaction
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Wait for page to be ready
		Thread.sleep(1000);

		// Personal Information Section
		// Enhanced handling for firstName field
		waitForWebElementToVisible(firstName);
		js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", firstName);
		Thread.sleep(500); // Wait for scroll

		softAssert.assertTrue(firstName.isDisplayed() && firstName.isEnabled(),
				"First name field should be visible and enabled");

		// Try multiple approaches to interact with firstName
		try {
			firstName.click();
		} catch (Exception e) {
			try {
				js.executeScript("arguments[0].click();", firstName);
			} catch (Exception e2) {
				js.executeScript("arguments[0].focus();", firstName);
			}
		}

		// Enter first name with verification
		String firstNameValue = faker.name().firstName();
		firstName.clear();
		firstName.sendKeys(firstNameValue);
		// Verify value was entered
		softAssert.assertEquals(firstName.getAttribute("value"), firstNameValue,
				"First name should be entered correctly");

		// Enhanced handling for lastName field
		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", lastName);
		Thread.sleep(500);
		softAssert.assertTrue(lastName.isDisplayed() && lastName.isEnabled(),
				"Last name field should be visible and enabled");
		try {
			lastName.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", lastName);
		}
		String lastNameValue = faker.name().lastName();
		lastName.clear();
		lastName.sendKeys(lastNameValue);

//		// Enhanced handling for password fields - with separate scrolling for each
//		// field
//		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", enterpassword);
//		Thread.sleep(500);
//		softAssert.assertTrue(enterpassword.isDisplayed() && enterpassword.isEnabled(),
//				"Password field should be visible and enabled");
//
//		// Enter password with verification
//		try {
//			enterpassword.clear();
//			enterpassword.sendKeys(getProperty("password"));
//		} catch (Exception e) {
//			js.executeScript("arguments[0].value=arguments[1]", enterpassword, getProperty("password"));
//		}
//
//		// Explicitly scroll to confirm password field separately
//		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmPassword);
//		Thread.sleep(500); // Give it time to scroll
//
//		// Check if confirm password is visible after scrolling, with debugging
//		boolean isConfirmPasswordVisible = confirmPassword.isDisplayed();
//		boolean isConfirmPasswordEnabled = confirmPassword.isEnabled();
//		System.out.println(
//				"Confirm password visible: " + isConfirmPasswordVisible + ", enabled: " + isConfirmPasswordEnabled);
//
//		// Only proceed with assertions and actions if element exists
//		if (isConfirmPasswordVisible) {
//			softAssert.assertTrue(isConfirmPasswordVisible && isConfirmPasswordEnabled,
//					"Confirm password field should be visible and enabled");
//
//			// Try different approaches to enter confirm password
//			String password = getProperty("password");
//			try {
//				confirmPassword.clear();
//				confirmPassword.sendKeys(password);
//			} catch (Exception e) {
//				try {
//					js.executeScript("arguments[0].value=arguments[1]", confirmPassword, password);
//				} catch (Exception e2) {
//					System.err.println("Failed to enter confirm password: " + e2.getMessage());
//					// Skip assertion for this field if we can't interact with it
//				}
//			}
//		} else {
//			System.err.println("Confirm password field not visible - skipping assertion");
//			// Take screenshot for debugging
//			try {
//				org.openqa.selenium.TakesScreenshot ts = (org.openqa.selenium.TakesScreenshot) driver;
//				java.io.File source = ts.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
//				java.io.File destination = new java.io.File("/Users/nisha/Downloads/password_field_debug.png");
//				org.apache.commons.io.FileUtils.copyFile(source, destination);
//				System.out.println("Screenshot saved to: " + destination.getAbsolutePath());
//			} catch (Exception e) {
//				System.err.println("Failed to take screenshot: " + e.getMessage());
//			}
//		}

		// Enhanced handling for DOB fields with verification
		js.executeScript("window.scrollBy(0, 100);"); // Scroll a bit more
		Thread.sleep(500);

		// Date of Birth handling with verification
		String day = String.valueOf(faker.number().numberBetween(1, 29));
		String month = String.valueOf(faker.number().numberBetween(1, 12));
		String year = String.valueOf(faker.number().numberBetween(1970, 2003));

		// Enter DOB with verification for each field
		enterValueWithJSFallback(youthDateOfBirth, day, js);
		enterValueWithJSFallback(youthMonthOfBirth, month, js);
		enterValueWithJSFallback(youthYearOfBirth, year, js);

		// Gender selection with verification
		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", youthgender);
		Thread.sleep(500);
		Select gender = select(youthgender);
		String selectedGender = faker.options().option("Male", "Female", "Others");
		gender.selectByVisibleText(selectedGender);
		softAssert.assertEquals(gender.getFirstSelectedOption().getText(), selectedGender,
				"Gender should be selected correctly");

		// Select state
		Select state = AbstractComponent.select(YouthState);
		softAssert.assertTrue(state.getOptions().size() > 0, "State dropdown should have options");
		int randomStateIndex = faker.number().numberBetween(1, 15); // Random index for state selection
		state.selectByIndex(randomStateIndex);

		// Select district by index
		waitForWebElementToClickable(Youthdistrict);
		Select district = AbstractComponent.select(Youthdistrict);
		softAssert.assertTrue(district.getOptions().size() > 0, "District dropdown should have options");
		int randomIndex = faker.number().numberBetween(1, 2); // Random index for district selection
		district.selectByIndex(randomIndex);

		// Address Section
		// Validate and select urban or rural residence
		softAssert.assertTrue(urban.isDisplayed() && urban.isEnabled(), "Urban radio should be visible and enabled");
		urban.click();
		Select Localbody = AbstractComponent.select(uLB);

		// If urban local body options are available, select one
		if (Localbody.getOptions().size() > 1) {
			Localbody.selectByIndex(1);
			// Validate and fill urban pincode
			softAssert.assertTrue(urbanPincode.isDisplayed() && urbanPincode.isEnabled(),
					"Urban pincode field should be visible and enabled");
			urbanPincode.sendKeys(String.valueOf(faker.number().numberBetween(100000, 999999)));
		} else {
			// Otherwise, select rural residence and fill details
			softAssert.assertTrue(rural.isDisplayed() && rural.isEnabled(),
					"Rural radio should be visible and enabled");
			rural.click();
			// Validate and select block, panchayat, and village
			softAssert.assertTrue(searchAndSelectABlock.isDisplayed() && searchAndSelectABlock.isEnabled(),
					"Block dropdown should be visible and enabled");
			searchAndSelectABlock.click();
			searchAndSelectABlockInput.sendKeys("a");
			searchAndSelectABlockInput.sendKeys(Keys.ENTER);
			softAssert.assertTrue(searchAndSelectAPanchayat.isDisplayed() && searchAndSelectAPanchayat.isEnabled(),
					"Panchayat dropdown should be visible and enabled");
			searchAndSelectAPanchayat.click();
			searchAndSelectAPanchayatInput.sendKeys("a");
			searchAndSelectAPanchayatInput.sendKeys(Keys.ENTER);
			softAssert.assertTrue(searchAndSelectAVillage.isDisplayed() && searchAndSelectAVillage.isEnabled(),
					"Village dropdown should be visible and enabled");
			searchAndSelectAVillage.click();
			searchAndSelectAVillageInput.sendKeys("a");
			searchAndSelectAVillageInput.sendKeys(Keys.ENTER);
			// Validate and fill rural pincode
			softAssert.assertTrue(ruralPincode.isDisplayed() && ruralPincode.isEnabled(),
					"Rural pincode field should be visible and enabled");
			ruralPincode.sendKeys(String.valueOf(faker.number().numberBetween(100000, 999999)));

		}
		// Validate and select Yuva type
		softAssert.assertTrue(yuvaType.isDisplayed() && yuvaType.isEnabled(),
				"Yuva type checkbox should be visible and enabled");
		yuvaType.click();

		// Education Section
		// Select highest qualification
		Select highest_Qualification = AbstractComponent.select(highestQualification);
		softAssert.assertTrue(highest_Qualification.getOptions().size() > 0,
				"Qualification dropdown should have options");
		highest_Qualification.selectByIndex(4); // Adjust as necessary

		// Select institution type
		Select institution = AbstractComponent.select(institutionType);
		softAssert.assertTrue(institution.getOptions().size() > 0, "Institution type dropdown should have options");
		institution.selectByIndex(1); // Adjust as necessary

		// Select school state and district
		Select institutionState = AbstractComponent.select(schoolState);
		softAssert.assertTrue(institutionState.getOptions().size() > 0, "School state dropdown should have options");
		institutionState.selectByIndex(6);

		Select institutionDistrict = AbstractComponent.select(schoolDistrict);
		softAssert.assertTrue(institutionDistrict.getOptions().size() > 0,
				"School district dropdown should have options");
		institutionDistrict.selectByIndex(1);

		// Institution and Sports Section
		// Validate and select institution and sport
		softAssert.assertTrue(searchAndSelectAnInstituti.isDisplayed() && searchAndSelectAnInstituti.isEnabled(),
				"Institution dropdown should be visible and enabled");
		searchAndSelectAnInstituti.click();
		searchAndSelectAnInstitutiInput.sendKeys("s");
		searchAndSelectAnInstitutiInput.sendKeys(Keys.ENTER);

		softAssert.assertTrue(searchAndSelectASport.isDisplayed() && searchAndSelectASport.isEnabled(),
				"Sport dropdown should be visible and enabled");
		searchAndSelectASport.click();
		searchAndSelectASportInput.sendKeys("B");
		searchAndSelectASportInput.sendKeys(Keys.ENTER);

		// Participation Section
		// Validate and select participation in sports
		softAssert.assertTrue(doYouWishToParticipateIn.isDisplayed() && doYouWishToParticipateIn.isEnabled(),
				"Participate checkbox should be visible and enabled");
		doYouWishToParticipateIn.click();

		// Consent Section
		// Validate and click on "I consent to terms of use" checkbox
		softAssert.assertTrue(iConsentToTerms.isDisplayed() && iConsentToTerms.isEnabled(),
				"Consent checkbox should be visible and enabled");
		iConsentToTerms.click();

		// Submit Section
		// Validate and click on "Submit" button
		softAssert.assertTrue(submit.isDisplayed() && submit.isEnabled(),
				"Submit button should be visible and enabled");
		try {
			submit.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", submit);
		}

		// Verify form submission
		softAssert.assertAll();
	}

	/**
	 * Helper method to enter values with JavaScript fallback
	 */
	private void enterValueWithJSFallback(WebElement element, String value, JavascriptExecutor js) {
		try {
			element.clear();
			element.sendKeys(value);
		} catch (Exception e) {
			js.executeScript("arguments[0].value=arguments[1]", element, value);
		}
		// Verify value was entered
		String actualValue = element.getAttribute("value");
		softAssert.assertEquals(actualValue, value,
				"Value should be entered correctly for element: " + element.getAttribute("id"));
	}

	public void register_verifyOTPandGoToRegistrationForInternalationalUsers() throws InterruptedException {
		// Close quiz popup if present and visible
		try {
			if (closePopupQuiz.isDisplayed() && closePopupQuiz.isEnabled()) {
				closePopupQuiz.click();
				// Wait for overlay to disappear
				Thread.sleep(500); // or use explicit wait for overlay to be gone
			}
		} catch (Exception e) {
			// Popup not present, continue
		}
		// Wait and validate initial registration button

		waitForWebElementToClickable(registerNow);
		softAssert.assertTrue(registerNow.isDisplayed() && registerNow.isEnabled(),
				"Initial registration button should be accessible");
		registerNow.click();

		internationalUserCheckbox.click();

		// Wait and validate main register button
		waitForWebElementToClickable(register);
		softAssert.assertTrue(register.isDisplayed() && register.isEnabled(),
				"Main registration button should be accessible");
		register.click();

		// Enhanced handling for mobile/email input field
		waitForWebElementToVisible(enterMobileEmail);
		// Scroll element into view using JavaScript
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView(true);", enterMobileEmail);
		// Add a small wait to ensure the element is truly visible
		Thread.sleep(500);

		// Try multiple approaches to interact with the element
		try {
			// First try normal click
			enterMobileEmail.click();
		} catch (Exception e) {
			try {
				// If normal click fails, try JavaScript click
			 js.executeScript("arguments[0].click();", enterMobileEmail);
			} catch (Exception e2) {
				// If JavaScript click fails, try moving to element first
				js.executeScript("arguments[0].scrollIntoView({block: 'center'});", enterMobileEmail);
				Thread.sleep(500);
				enterMobileEmail.click();
			}
		}

		// Clear any existing value and enter email
		enterMobileEmail.clear();
		enterMobileEmail.sendKeys(email);
		System.out.println("Entered email: " + email);

		// Validate and click OTP request button with enhanced handling
		waitForWebElementToClickable(getOTP);
		softAssert.assertTrue(getOTP.isDisplayed() && getOTP.isEnabled(),
				"Get OTP button should be visible and enabled");
		try {
			getOTP.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", getOTP);
		}

		// Rest of the OTP verification process
		Thread.sleep(2000); // Wait for 2 seconds for OTP to be sent
		// Switch to new browser tab for email verification
		driver.switchTo().newWindow(WindowType.TAB);

		// Open dummy email provider in new tab
		String dummyemailUrl = getProperty("dummyEmail");
		driver.get(dummyemailUrl);

		// Clear inbox field if displayed and enabled
		if (enterYourInboxHere.isDisplayed() && enterYourInboxHere.isEnabled()) {
			enterYourInboxHere.clear();
		}

		// Enter email prefix in the inbox field to receive OTP
		String[] parts = email.split("@");
		enterYourInboxHere.sendKeys(parts[0]);

		// Click on go button to access inbox
		go.click();

		// Refresh inbox to get the latest email
		refresh.click();

		// Switch to iframe containing the email content
		driver.switchTo().frame("ifmail");

		// Extract OTP from the email content
		String OTP = Password.getText().split("\\. This")[0].trim().split(" is ")[1].trim();
		System.out.println("Youth email is " + email);
		System.out.println("OTP is : " + OTP);

		// Close the new tab and switch back to the original tab
		ArrayList<String> tab = new ArrayList<>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1)).close();
		driver.switchTo().window(tab.get(0));

		// Enter the extracted OTP in the OTP field
		softAssert.assertTrue(otpfield.isDisplayed() && otpfield.isEnabled(),
				"OTP field should be visible and enabled");
		otpfield.sendKeys(OTP);

		// Validate and click on "Verify OTP" button
		softAssert.assertTrue(verifyOTP.isDisplayed() && verifyOTP.isEnabled(),
				"Verify OTP button should be visible and enabled");
		verifyOTP.click();
		softAssert.assertAll(); // Assert all soft assertions
	}

	public void registerForInternationalUsers() throws InterruptedException {
		// Initialize JavaScript executor for reliable element interaction
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Wait for page to be ready
		Thread.sleep(1000);

		// Personal Information Section
		// Enhanced handling for firstName field
		waitForWebElementToVisible(firstName);
		js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", firstName);
		Thread.sleep(500); // Wait for scroll

		softAssert.assertTrue(firstName.isDisplayed() && firstName.isEnabled(),
				"First name field should be visible and enabled");

		// Try multiple approaches to interact with firstName
		try {
			firstName.click();
		} catch (Exception e) {
			try {
				js.executeScript("arguments[0].click();", firstName);
			} catch (Exception e2) {
				js.executeScript("arguments[0].focus();", firstName);
			}
		}

		// Enter first name with verification
		String firstNameValue = faker.name().firstName();
		firstName.clear();
		firstName.sendKeys(firstNameValue);
		// Verify value was entered
		softAssert.assertEquals(firstName.getAttribute("value"), firstNameValue,
				"First name should be entered correctly");

		// Enhanced handling for lastName field
		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", lastName);
		Thread.sleep(500);
		softAssert.assertTrue(lastName.isDisplayed() && lastName.isEnabled(),
				"Last name field should be visible and enabled");
		try {
			lastName.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", lastName);
		}
		String lastNameValue = faker.name().lastName();
		lastName.clear();
		lastName.sendKeys(lastNameValue);

//		// Enhanced handling for password fields - with separate scrolling for each
//		// field
//		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", enterpassword);
//		Thread.sleep(500);
//		softAssert.assertTrue(enterpassword.isDisplayed() && enterpassword.isEnabled(),
//				"Password field should be visible and enabled");
//
//		// Enter password with verification
//		try {
//			enterpassword.clear();
//			enterpassword.sendKeys(getProperty("password"));
//		} catch (Exception e) {
//			js.executeScript("arguments[0].value=arguments[1]", enterpassword, getProperty("password"));
//		}
//
//		// Explicitly scroll to confirm password field separately
//		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmPassword);
//		Thread.sleep(500); // Give it time to scroll
//
//		// Check if confirm password is visible after scrolling, with debugging
//		boolean isConfirmPasswordVisible = confirmPassword.isDisplayed();
//		boolean isConfirmPasswordEnabled = confirmPassword.isEnabled();
//		System.out.println(
//				"Confirm password visible: " + isConfirmPasswordVisible + ", enabled: " + isConfirmPasswordEnabled);
//
//		// Only proceed with assertions and actions if element exists
//		if (isConfirmPasswordVisible) {
//			softAssert.assertTrue(isConfirmPasswordVisible && isConfirmPasswordEnabled,
//					"Confirm password field should be visible and enabled");
//
//			// Try different approaches to enter confirm password
//			String password = getProperty("password");
//			try {
//				confirmPassword.clear();
//				confirmPassword.sendKeys(password);
//			} catch (Exception e) {
//				try {
//					js.executeScript("arguments[0].value=arguments[1]", confirmPassword, password);
//				} catch (Exception e2) {
//					System.err.println("Failed to enter confirm password: " + e2.getMessage());
//					// Skip assertion for this field if we can't interact with it
//				}
//			}
//		} else {
//			System.err.println("Confirm password field not visible - skipping assertion");
//			// Take screenshot for debugging
//			try {
//				org.openqa.selenium.TakesScreenshot ts = (org.openqa.selenium.TakesScreenshot) driver;
//				java.io.File source = ts.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
//				java.io.File destination = new java.io.File("/Users/nisha/Downloads/password_field_debug.png");
//				org.apache.commons.io.FileUtils.copyFile(source, destination);
//				System.out.println("Screenshot saved to: " + destination.getAbsolutePath());
//			} catch (Exception e) {
//				System.err.println("Failed to take screenshot: " + e.getMessage());
//			}
//		}

		// Enhanced handling for DOB fields with verification
		js.executeScript("window.scrollBy(0, 100);"); // Scroll a bit more
		Thread.sleep(500);

		// Date of Birth handling with verification
		String day = String.valueOf(faker.number().numberBetween(1, 29));
		String month = String.valueOf(faker.number().numberBetween(1, 12));
		String year = String.valueOf(faker.number().numberBetween(1970, 2003));

		// Enter DOB with verification for each field
		enterValueWithJSFallback(youthDateOfBirth, day, js);
		enterValueWithJSFallback(youthMonthOfBirth, month, js);
		enterValueWithJSFallback(youthYearOfBirth, year, js);

		// Gender selection with verification
		js.executeScript("arguments[0].scrollIntoView({block: 'center'});", youthgender);
		Thread.sleep(500);
		Select gender = select(youthgender);
		String selectedGender = faker.options().option("Male", "Female", "Others");
		gender.selectByVisibleText(selectedGender);
		softAssert.assertEquals(gender.getFirstSelectedOption().getText(), selectedGender,
				"Gender should be selected correctly");

		// Select Country
		Select countryselect = AbstractComponent.select(country);

		softAssert.assertTrue(countryselect.getOptions().size() > 0, "State dropdown should have options");
		int randomStateIndex = faker.number().numberBetween(1, 50); // Random index for state selection
		countryselect.selectByIndex(randomStateIndex);
		String selectedCountry = countryselect.getFirstSelectedOption().getText();

		// Address Section
		// Validate and select urban or rural residence
		addressLine1.sendKeys(faker.address().buildingNumber() + " " + faker.address().streetName());
		addressLine2.sendKeys(faker.address().cityName() + " " + selectedCountry);

		// Consent Section
		// Validate and click on "I consent to terms of use" checkbox
		softAssert.assertTrue(iConsentToTerms.isDisplayed() && iConsentToTerms.isEnabled(),
				"Consent checkbox should be visible and enabled");
		iConsentToTerms.click();

		// Submit Section
		// Validate and click on "Submit" button
		softAssert.assertTrue(submit.isDisplayed() && submit.isEnabled(),
				"Submit button should be visible and enabled");
		try {
			submit.click();
		} catch (Exception e) {
			js.executeScript("arguments[0].click();", submit);
		}

		// Verify form submission
		softAssert.assertAll();

		System.out.println(js.executeScript("return document.title;"));
		System.out.println(js.executeScript("return document.URL;"));
		System.out.println(js.executeScript("return document.domain;"));
		System.out.println(js.executeScript("return document.documentElement.outerHTML;"));

	}

}
