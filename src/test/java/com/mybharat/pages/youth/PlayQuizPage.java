package com.mybharat.pages.youth;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

/**
 * PlayQuizPage - Handles quiz registration and answering all questions.
 */
public class PlayQuizPage extends BasePage {

    private final String language;
    private final Random random = new Random();

    @FindBy(css = ".logo.mybharatlogo")
    private WebElement logoIcon;

    @FindBy(xpath = "//span[normalize-space()='Quiz & Essay']")
    private WebElement quizAndEssayTab;

    @FindBy(xpath = "//select[@id='quizLanguage']")
    private WebElement languageDropdown;

    public PlayQuizPage(WebDriver driver, String language) {
        super(driver);
        this.language = (language != null) ? language : "English";
    }

    public PlayQuizPage(WebDriver driver) {
        this(driver, "English");
    }

    /**
     * Navigate to quiz section and start the quiz.
     */
    public void startQuiz() throws Exception {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Go to home and open Quiz tab
        safeClick(logoIcon);
        waitForPageLoad();

        // Close popup if present
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
        } catch (Exception e) { /* no popup */ }

        safeClick(quizAndEssayTab);
        Thread.sleep(1000);
        scrollPage(1000);

        // Click Start Quiz
        WebElement startQuiz = longWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='start_quiz']")));
        scrollToElement(startQuiz);
        jsClick(startQuiz);

        // Select "No" for disability
        WebElement disability = longWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("(//input[@id='check_detail_whether_disability'])[2]")));
        jsClick(disability);

        // Click Proceed
        WebElement proceed = longWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='checkDetailsFormButton']")));
        jsClick(proceed);
        Thread.sleep(1000);

        // Select language
        selectQuizLanguage();

        // Click Start Quiz button
        WebElement startBtn = longWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='startQuizButton']")));
        jsClick(startBtn);
    }

    /**
     * Attempt all 20 questions with random answers and submit.
     */
    public void attemptAllQuestionsAndSubmit() throws Exception {
        WebDriverWait qWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int totalQuestions = 20;

        Thread.sleep(2000);
        waitForPageLoad();

        for (int q = 1; q <= totalQuestions; q++) {
            System.out.println("Answering question " + q);

            // Find answer options
            List<WebElement> options = findAnswerOptions(qWait);
            if (options.isEmpty()) {
                throw new Exception("No answer options found for question " + q);
            }

            // Select random answer
            WebElement selected = options.get(random.nextInt(options.size()));
            scrollToElement(selected);
            Thread.sleep(300);
            jsClick(selected);

            // Click Next (except for last question)
            if (q < totalQuestions) {
                clickNextButton(qWait, js);
            }
            Thread.sleep(800);
        }

        // Submit quiz
        submitQuiz(qWait, js);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void selectQuizLanguage() {
        try {
            waitForClickable(languageDropdown);
            Select langSelect = new Select(languageDropdown);
            langSelect.selectByVisibleText(language);
            System.out.println("Selected language: " + language);
        } catch (Exception e) {
            System.out.println("Language selection failed, continuing with default: " + e.getMessage());
        }
    }

    private List<WebElement> findAnswerOptions(WebDriverWait qWait) {
        String[] selectors = {
            "//div[contains(@class,'form-check')]//label",
            "//div[contains(@class,'option')]//label",
            "//div[contains(@class,'radio')]//label"
        };
        for (String selector : selectors) {
            try {
                qWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                List<WebElement> options = driver.findElements(By.xpath(selector));
                if (!options.isEmpty()) return options;
            } catch (Exception e) { /* try next */ }
        }
        return List.of();
    }

    private void clickNextButton(WebDriverWait qWait, JavascriptExecutor js) {
        String[] selectors = {"//button[@id='save_button']", "//button[contains(text(),'Next')]"};
        for (String selector : selectors) {
            try {
                WebElement btn = qWait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                js.executeScript("arguments[0].click();", btn);
                return;
            } catch (Exception e) { /* try next */ }
        }
    }

    private void submitQuiz(WebDriverWait qWait, JavascriptExecutor js) throws Exception {
        // First submit
        WebElement submit = qWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='submit_button']")));
        js.executeScript("arguments[0].click();", submit);
        Thread.sleep(1000);

        // Final confirm
        WebElement confirm = qWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='submit_quiz']")));
        js.executeScript("arguments[0].click();", confirm);
        System.out.println("Quiz submitted successfully");
    }
}
