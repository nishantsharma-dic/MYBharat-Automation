package pageobjects.Youth; // Package declaration for register page objects

import java.util.Random;
import java.util.List;
import java.io.File;

import org.openqa.selenium.By;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import com.github.javafaker.Faker;

import MYBharat_ResourcesAndAbstractComponents.AbstractComponents.AbstractComponent;

import org.apache.commons.io.FileUtils;

// Import sections organized by functionality

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
public class PlayQuiz extends AbstractComponent {

	/** Core WebDriver instance for browser control */
	WebDriver driver;
	Random rand = new Random();
	/** Faker instance for generating random user data */
	Faker faker;

	WebDriverWait wait;

	/** SoftAssert instance for non-blocking assertions */
	SoftAssert softAssert = new SoftAssert();

	private String quizLanguage = "English"; // Default language

	/**
	 * Constructor initializes core components and test data - Initializes WebDriver
	 * - Sets up Page Factory - Creates Faker instance - Generates and stores test
	 * email - Generates mobile number - Records email to Excel for tracking
	 *
	 * @param driver WebDriver instance for browser automation
	 */
	public PlayQuiz(WebDriver driver, String language) {
		super(driver); // Initialize parent AbstractComponent
		this.driver = driver; // Store WebDriver instance
		PageFactory.initElements(driver, this); // Initialize Page Factory elements
		faker = new Faker(); // Create Faker for random data
		wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Initialize wait with 30 second timeout
		this.quizLanguage = language != null ? language : "English";
	}

	// Alternative constructor to maintain backward compatibility
	public PlayQuiz(WebDriver driver) {
		this(driver, "English");
	}
	
	
	@FindBy(xpath="//i[@class='fa fa-times']") 
	@CacheLookup private WebElement closePopupQuiz;

	@FindBy(css = ".logo.mybharatlogo")
	@CacheLookup
	private WebElement logoIcon;

	@FindBy(xpath="//span[normalize-space()='Quiz & Essay']") 
	@CacheLookup private WebElement quizAndEssay;

//	@FindBy(xpath="(//img[@alt='Quiz Banner'])[2]") 
//	@CacheLookup private WebElement quizBanner;

//	@FindBy(xpath = "//button[@id='start_quiz']")
//	@CacheLookup
//	private WebElement startQuiz;
	

	@FindBy(xpath="(//button[@type='button'][normalize-space()='Start Quiz'])[1]") 
	@CacheLookup private WebElement startQuiz;


	@FindBy(xpath = "(//input[@id='check_detail_whether_disability'])[2]")
	@CacheLookup
	private WebElement whetherDivyangOrNot;

	@FindBy(xpath = "//button[@id='checkDetailsFormButton']")
	@CacheLookup
	private WebElement proceed;
	
	
	@FindBy(xpath="//select[@id='quizLanguage']") 
	@CacheLookup private WebElement selectLanguage;
	
	

	@FindBy(xpath = "//button[@id='startQuizButton']")
	@CacheLookup
	private WebElement startQuizButton;

	// All answer options for a question
	@FindBy(xpath = "//input[contains(@class,'form-check-input')]")
	private List<WebElement> answerOptions;

	// Next button
	@FindBy(xpath = "//button[@id='save_button']")
	@CacheLookup
	private WebElement next;

	@FindBy(xpath = "//button[@id='submit_button']")
	@CacheLookup
	private WebElement submitQuiz;

	@FindBy(xpath = "//button[@id='submit_quiz']")
	@CacheLookup
	private WebElement finalsubmitQuiz;

	@FindBy(xpath = "//a[normalize-space()='Overview Questions']")
	@CacheLookup
	private WebElement overviewQuestions;

	@FindBy(xpath = "//button[normalize-space()='Next']")
	@CacheLookup
	private WebElement reviewNext;

	@FindBy(xpath = "//button[normalize-space()='Review Done']")
	@CacheLookup
	private WebElement reviewDone;

	@FindBy(xpath = "//i[5]")
	@CacheLookup
	private WebElement pleaseShareYourValuableFee;

	@FindBy(xpath = "//textarea[@id='feedback']")
	@CacheLookup
	private WebElement provideYourValuableFeedback;

	@FindBy(xpath = "//button[@id='submit_button']")
	@CacheLookup
	private WebElement submit;

	public void registerForQuiz() throws Exception {
		try {
			
			
			// Wait for any loaders to disappear
			waitForLoaderToDisappear(By.id("certificateModal"));
			
			// Click logo and ensure page is loaded
			logoIcon.click();
			wait.until(webDriver -> ((JavascriptExecutor) webDriver)
				.executeScript("return document.readyState").equals("complete"));
			
			closePopupQuiz.click();
			
			// Click Quiz tab with retry mechanism
			int maxAttempts = 3;
			for (int attempt = 0; attempt < maxAttempts; attempt++) {
				try {
					waitForWebElementToClickable(quizAndEssay);
					quizAndEssay.click();
					Thread.sleep(1000);
					break;
				} catch (Exception e) {
					if (attempt == maxAttempts - 1) throw e;
					Thread.sleep(1000);
				}
			}
			
			scrollPage(1000);
			
			// After clicking quiz banner, wait for page load and get fresh reference to start quiz button
			System.out.println("Waiting for start quiz button...");
			try {
				// Wait for page load
				wait.until(webDriver -> ((JavascriptExecutor) webDriver)
					.executeScript("return document.readyState").equals("complete"));
				
				// Wait for start quiz button with fresh lookup
				By startQuizLocator = By.xpath("//button[@id='start_quiz']");
				WebElement startQuizBtn = wait.until(ExpectedConditions.elementToBeClickable(startQuizLocator));
				
				// Scroll into view
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", startQuizBtn);
				Thread.sleep(1000);
				
				// Click using JavaScript
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", startQuizBtn);
				System.out.println("Start quiz button clicked successfully");
				
			} catch (Exception e) {
				System.out.println("Error clicking start quiz button: " + e.getMessage());
				// Try alternative approach with fresh element
				try {
					WebElement freshStartQuiz = driver.findElement(By.xpath("//button[@id='start_quiz']"));
					Actions actions = new Actions(driver);
					actions.moveToElement(freshStartQuiz).click().perform();
					System.out.println("Start quiz button clicked using Actions");
				} catch (Exception e2) {
					throw new Exception("Failed to click start quiz button after multiple attempts: " + e2.getMessage());
				}
			}

			

				// Get fresh references and click disability option
				WebElement disabilityOption = wait.until(ExpectedConditions.elementToBeClickable(
					By.xpath("(//input[@id='check_detail_whether_disability'])[2]")));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", disabilityOption);
				Thread.sleep(500);

				// Click proceed button
				WebElement proceedBtn = wait.until(ExpectedConditions.elementToBeClickable(
					By.xpath("//button[@id='checkDetailsFormButton']")));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", proceedBtn);
				Thread.sleep(1000);

				// Dynamic language selection with multiple fallback approaches
				try {
					wait.until(ExpectedConditions.elementToBeClickable(selectLanguage));
					
					// Map of language names to their values
					java.util.Map<String, String> languageValues = new java.util.HashMap<>();
					languageValues.put("Hindi", "hi");
					languageValues.put("English", "en");
					languageValues.put("Bengali", "bn");
					languageValues.put("Marathi", "mr");
					languageValues.put("Tamil", "ta");
					languageValues.put("Telugu", "te");
					languageValues.put("Kannada", "kn");
					languageValues.put("Malayalam", "ml");
					languageValues.put("Gujarati", "gu");
					languageValues.put("Odia", "or");
					languageValues.put("Nepali", "ne");
					languageValues.put("Manipuri", "mn");
					languageValues.put("Maithili", "mai");
					languageValues.put("Kashmiri", "ks");
					languageValues.put("Goan Konkani", "kok");
					languageValues.put("Dogri", "doi");
					languageValues.put("Bodo", "brx");
					languageValues.put("Assamese", "as");
					languageValues.put("Punjabi", "pa");
					languageValues.put("Sanskrit", "sa");
					languageValues.put("Santali", "sat");
					languageValues.put("Sindhi", "sd");
					languageValues.put("Urdu", "ur");
					
					String langValue = languageValues.getOrDefault(quizLanguage, "en");
					
					// Try multiple approaches to select the language
					boolean languageSelected = false;
					
					// Approach 1: Using JavaScript to set value
					try {
						JavascriptExecutor js = (JavascriptExecutor) driver;
						js.executeScript("arguments[0].value=arguments[1]", selectLanguage, langValue);
						languageSelected = true;
					} catch (Exception e) {
						System.out.println("JS value set failed, trying next approach");
					}
					
					// Approach 2: Using Select class with value
					if (!languageSelected) {
						try {
							Select languageSelect = new Select(selectLanguage);
							languageSelect.selectByValue(langValue);
							languageSelected = true;
						} catch (Exception e) {
							System.out.println("Select by value failed, trying next approach");
						}
					}
					
					// Approach 3: Using Select class with visible text
					if (!languageSelected) {
						try {
							Select languageSelect = new Select(selectLanguage);
							languageSelect.selectByVisibleText(quizLanguage);
							languageSelected = true;
						} catch (Exception e) {
							System.out.println("Select by visible text failed, trying next approach");
						}
					}
					
					// Approach 4: Direct JavaScript click on option
					if (!languageSelected) {
						try {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							WebElement langOption = driver.findElement(
								By.xpath(String.format("//select[@id='quizLanguage']/option[text()='%s']", quizLanguage)));
							js.executeScript(
								"arguments[0].selected = true; arguments[0].parentElement.dispatchEvent(new Event('change'))",
								langOption);
							languageSelected = true;
						} catch (Exception e) {
							System.out.println("All language selection approaches failed");
						}
					}
					
					if (languageSelected) {
						System.out.println("Successfully selected language: " + quizLanguage);
					} else {
						System.out.println("Could not select language " + quizLanguage + ", continuing with default");
					}
					
				} catch (Exception e) {
					System.out.println("Language selection failed: " + e.getMessage());
				}

				Thread.sleep(1000);
				
				// Wait for and click start quiz button with fresh reference
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='startQuizButton']")));
				WebElement startQuizButton = driver.findElement(By.xpath("//button[@id='startQuizButton']"));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", startQuizButton);

				softAssert.assertAll();
		} catch (Exception e) {
			System.out.println("Error during quiz registration: " + e.getMessage());
			// Take screenshot on failure
			try {
				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(screenshot, new File("quiz_registration_failure.png"));
				System.out.println("Failure screenshot saved as quiz_registration_failure.png");
			} catch (Exception se) {
				System.out.println("Could not take failure screenshot: " + se.getMessage());
			}
			throw e;
		}
	}

	public void competativeQuiz_AllQuesAttemptAndSubmit() throws Exception {
		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Increased timeout
			JavascriptExecutor js = (JavascriptExecutor) driver;
			int questionsAttempted = 0;
			final int TOTAL_QUESTIONS = 20;
			int maxRetries = 3;

			// Wait for quiz to load completely
			Thread.sleep(2000);
			wait.until(webDriver -> ((JavascriptExecutor) webDriver)
				.executeScript("return document.readyState").equals("complete"));
			
			while (questionsAttempted < TOTAL_QUESTIONS) {
				System.out.println("Attempting question " + (questionsAttempted + 1));
				int retryCount = 0;
				boolean answered = false;
				
				while (retryCount < maxRetries && !answered) {
					try {
						// Try multiple selectors to find the answer options
						List<WebElement> options = null;
						String[] selectors = {
							"//div[contains(@class,'form-check')]//label",
							"//div[contains(@class,'option')]//label",
							"//div[contains(@class,'radio')]//label",
							"//div[contains(@class,'answer')]//label"
						};

						for (String selector : selectors) {
							try {
								wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
								options = driver.findElements(By.xpath(selector));
								if (!options.isEmpty()) {
									System.out.println("Found " + options.size() + " options using selector: " + selector);
									break;
								}
							} catch (Exception e) {
								continue;
							}
						}

						if (options == null || options.isEmpty()) {
							throw new Exception("No answer options found with any selector");
						}

						// Select and click a random option
						WebElement selectedOption = options.get(new Random().nextInt(options.size()));
						
						// Scroll the option into view
						js.executeScript("arguments[0].scrollIntoView({block: 'center'});", selectedOption);
						Thread.sleep(500);

						// Try multiple click approaches
						try {
							// Attempt 1: Direct click on label
							selectedOption.click();
						} catch (Exception e1) {
							try {
								// Attempt 2: JavaScript click on label
							 js.executeScript("arguments[0].click();", selectedOption);
							} catch (Exception e2) {
								try {
									// Attempt 3: Find and click associated radio input
									String inputId = selectedOption.getAttribute("for");
									if (inputId != null) {
										WebElement radioInput = driver.findElement(By.id(inputId));
										js.executeScript("arguments[0].click();", radioInput);
									} else {
										// Attempt 4: Find any input within the label's parent
										WebElement radioInput = selectedOption.findElement(By.xpath("./preceding-sibling::input[@type='radio']"));
										js.executeScript("arguments[0].click();", radioInput);
									}
								} catch (Exception e3) {
									throw new Exception("All click attempts failed");
								}
							}
						}

						// Try multiple approaches to click next button
						String[] nextButtonSelectors = {
							"//button[@id='save_button']",
							"//button[contains(text(),'Next')]",
							"//button[contains(@class,'next')]"
						};

						boolean nextClicked = false;
						for (String selector : nextButtonSelectors) {
							try {
								WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
								js.executeScript("arguments[0].click();", nextButton);
								nextClicked = true;
								break;
							} catch (Exception e) {
								continue;
							}
						}

						if (!nextClicked && questionsAttempted < TOTAL_QUESTIONS - 1) {
							throw new Exception("Could not click next button");
						}

						answered = true;
						questionsAttempted++;
						System.out.println("Successfully answered question " + questionsAttempted);
						Thread.sleep(1000); // Wait for next question to load

					} catch (Exception e) {
						System.out.println("Retry " + (retryCount + 1) + " failed: " + e.getMessage());
						retryCount++;
						Thread.sleep(1000);
					}
				}

				if (!answered && retryCount >= maxRetries) {
					throw new Exception("Failed to answer question " + (questionsAttempted + 1) + 
					 " after " + maxRetries + " attempts");
				}
			}

			// Only proceed with submission after all questions are attempted
			if (questionsAttempted == TOTAL_QUESTIONS) {
				System.out.println("All " + TOTAL_QUESTIONS + " questions attempted, proceeding to submit...");
				
				// Submit quiz with multiple attempts
				boolean submitSuccess = false;
				try {
					// First submit button
					String[] submitSelectors = {
						"//button[@id='submit_button']",
						"//button[contains(text(),'Submit')]",
						"//button[contains(@class,'submit')]"
					};
					
					for (String selector : submitSelectors) {
						try {
							WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
							js.executeScript("arguments[0].click();", submitBtn);
							submitSuccess = true;
							break;
						} catch (Exception e) {
							continue;
						}
					}
					
					if (!submitSuccess) {
						throw new Exception("Could not click first submit button");
					}
					
					Thread.sleep(1000);
					
					// Final submit button
					submitSuccess = false;
					String[] finalSubmitSelectors = {
						"//button[@id='submit_quiz']",
						"//button[contains(text(),'Confirm')]",
						"//button[contains(@class,'confirm')]"
					};
					
					for (String selector : finalSubmitSelectors) {
						try {
							WebElement finalSubmitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
							js.executeScript("arguments[0].click();", finalSubmitBtn);
							submitSuccess = true;
							break;
						} catch (Exception e) {
							continue;
						}
					}
					
					if (!submitSuccess) {
						throw new Exception("Could not click final submit button");
					}
					
					System.out.println("Quiz submitted successfully");
					
				} catch (Exception e) {
					System.out.println("Error during submission: " + e.getMessage());
					throw e;
				}
			}
		} catch (Exception e) {
			System.out.println("Quiz attempt failed: " + e.getMessage());
			// Take screenshot on failure
			try {
				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(screenshot, new File("quiz_attempt_failure.png"));
				System.out.println("Failure screenshot saved as quiz_attempt_failure.png");
			} catch (Exception se) {
				System.out.println("Could not take failure screenshot: " + se.getMessage());
			}
			throw e;
		}
	}

	public void reviewQuiz() throws InterruptedException {
		waitForWebElementToClickable(overviewQuestions);
		overviewQuestions.click();
		for (int i = 0; i < 20; i++) {
			// wait until clickable
			waitForWebElementToClickable(reviewNext);

			// click
			reviewNext.click();

			// small pause to allow question to load (can be tuned)
			Thread.sleep(300);
		}

		waitForWebElementToClickable(reviewDone);
		reviewDone.click();
		waitForWebElementToClickable(pleaseShareYourValuableFee);
		pleaseShareYourValuableFee.click();
		provideYourValuableFeedback.sendKeys("Good Experience");
		submit.click();

	}
	
	public void javaScriptfunctions() throws InterruptedException {

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
}
}