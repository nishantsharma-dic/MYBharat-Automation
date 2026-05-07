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

import com.mybharat.utils.ConfigReader;import com.mybharat.pages.BasePage;

/**
 * PlayQuizPage - Handles quiz registration and answering all questions.
 */
public class QuizAttemptPage extends BasePage {

    private final String language;
    private final Random random = new Random();

    @FindBy(css = ".logo.mybharatlogo")
    private WebElement logoIcon;

    @FindBy(xpath = "//span[normalize-space()='Quiz & Essay']")
    private WebElement quizAndEssayTab;

    @FindBy(xpath = "//select[@id='quizLanguage']")
    private WebElement languageDropdown;

    public QuizAttemptPage(WebDriver driver, String language) {
        super(driver);
        this.language = (language != null) ? language : "English";
    }

    public QuizAttemptPage(WebDriver driver) {
        this(driver, "English");
    }

    /**
     * Navigate to quiz section and start the quiz.
     */
    public void startQuiz() throws Exception {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Navigate to home page using config URL
        ConfigReader config = new ConfigReader();
        driver.get(config.getUrl());
        waitForPageLoad();
        Thread.sleep(2000);

        // Close popup if present
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
        } catch (Exception e) { /* no popup */ }

        // Click Quiz & Essay tab
        waitForClickable(quizAndEssayTab);
        safeClick(quizAndEssayTab);
        Thread.sleep(2000);
        scrollPage(1000);
        Thread.sleep(1000);

        // Click Start Quiz — try multiple locators
        WebElement startQuiz = null;
        try {
            startQuiz = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("(//button[@type='button'][normalize-space()='Start Quiz'])[1]")));
        } catch (Exception e) {
            startQuiz = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='start_quiz']")));
        }
        scrollToElement(startQuiz);
        Thread.sleep(500);
        jsClick(startQuiz);

        // Click second "START QUIZ" button (if present)
        try {
            WebElement startQuiz2 = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='START QUIZ']")));
            scrollToElement(startQuiz2);
            Thread.sleep(500);
            jsClick(startQuiz2);
        } catch (Exception e) {
            // Second start quiz button not present — continue
        }

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
        WebDriverWait qWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int totalQuestions = 20;

        Thread.sleep(3000);
        waitForPageLoad();

        for (int q = 1; q <= totalQuestions; q++) {
            System.out.println("Answering question " + q);
            Thread.sleep(1000); // Wait for question to fully load
            waitForPageLoad();

            // Find answer options (fresh lookup every time to avoid stale elements)
            List<WebElement> options = null;
            for (int attempt = 0; attempt < 3; attempt++) {
                try {
                    options = findAnswerOptions(qWait);
                    if (!options.isEmpty()) break;
                } catch (Exception e) {
                    Thread.sleep(1000);
                }
            }
            if (options == null || options.isEmpty()) {
                throw new Exception("No answer options found for question " + q);
            }

            // Select random answer — re-find to avoid stale reference
            try {
                WebElement selected = options.get(random.nextInt(options.size()));
                scrollToElement(selected);
                Thread.sleep(500);
                jsClick(selected);
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                // Element went stale — re-find and click
                Thread.sleep(1000);
                options = findAnswerOptions(qWait);
                WebElement selected = options.get(random.nextInt(options.size()));
                jsClick(selected);
            }

            // Click Next (except for last question)
            if (q < totalQuestions) {
                clickNextButton(qWait, js);
                Thread.sleep(1500); // Wait for next question to load
            }
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

        // Rate the quiz (click 4th star)
        Thread.sleep(2000);
        WebElement ratingStar = qWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//i[4]")));
        js.executeScript("arguments[0].click();", ratingStar);
        Thread.sleep(500);

        // Click feedback submit button
        WebElement feedbackSubmit = qWait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='submit_button']")));
        js.executeScript("arguments[0].click();", feedbackSubmit);
        System.out.println("Feedback submitted successfully");
    }
}
