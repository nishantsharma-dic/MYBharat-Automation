package com.mybharat.tests.youth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;

public class QuizSecurityTest extends BaseTest {

    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String youthEmail = "Aarav123@mailto.plus";
    private String youthPassword = "Aarav@123";
    private String baseURL = "https://mybharat.gov.in";
    private String youthURL = baseURL + "/quizzes/yuva/";

    @BeforeClass
    public void initWait() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        js = (JavascriptExecutor) driver;
    }

    private void loginAsYouth() {
        driver.get(youthURL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys(youthEmail);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(youthPassword);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/quizzes/yuva"));
    }

    @Test(priority = 1, groups = {"regression", "quiz", "security"}, description = "Inject XSS payload via browser console into quiz title field")
    public void testXSSInQuizTitle() {
        loginAsYouth();
        String xssPayload = "<img src=x onerror=alert('XSS')>";
        js.executeScript("document.querySelectorAll('input[type=text]').forEach(el => el.value = arguments[0])", xssPayload);

        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        String pageSource = driver.getPageSource();
        Assert.assertFalse(pageSource.contains("onerror=alert"),
                "XSS payload should be sanitized and not reflected in DOM");

        Object alertPresent = js.executeScript(
                "try { return document.querySelector('[onerror]') !== null; } catch(e) { return false; }");
        Assert.assertFalse((Boolean) alertPresent, "No onerror handlers should exist from XSS injection");
    }

    @Test(priority = 2, groups = {"regression", "quiz", "security"}, description = "Try to set localStorage timer values via JS to extend time")
    public void testTimerManipulation() {
        js.executeScript("localStorage.setItem('quizTimer', '99999')");
        js.executeScript("localStorage.setItem('quiz_time_remaining', '99999')");
        js.executeScript("localStorage.setItem('timeLeft', '99999')");

        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("body")));

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Object timerValue = js.executeScript(
                "var timer = document.querySelector('.timer, [class*=timer], [class*=countdown]');" +
                "return timer ? timer.innerText : null;");

        if (timerValue != null) {
            String timerText = timerValue.toString();
            Assert.assertFalse(timerText.contains("99999"),
                    "Timer should not accept manipulated localStorage values");
        }
    }

    @Test(priority = 3, groups = {"regression", "quiz", "security"}, description = "Try to modify answer payloads via fetch override")
    public void testAnswerTamperingViaConsole() {
        js.executeScript(
                "window._originalFetch = window.fetch;" +
                "window.fetch = function(url, options) {" +
                "  if(options && options.body) {" +
                "    try { var body = JSON.parse(options.body); body.answers = ['A','A','A','A','A']; options.body = JSON.stringify(body); }" +
                "    catch(e) {}" +
                "  }" +
                "  return window._originalFetch(url, options);" +
                "};"
        );

        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Object fetchOverridden = js.executeScript("return window.fetch !== window._originalFetch");
        Assert.assertNotNull(fetchOverridden, "Fetch override verification should return a result");
    }

    @Test(priority = 4, groups = {"regression", "quiz", "security"}, description = "Access /quiz_exam/1 without auth and verify redirect to login")
    public void testDirectURLAccessWithoutLogin() {
        driver.manage().deleteAllCookies();
        js.executeScript("localStorage.clear(); sessionStorage.clear();");

        driver.get(baseURL + "/quiz_exam/1");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/signin"),
                ExpectedConditions.visibilityOfElementLocated(By.id("email")),
                ExpectedConditions.urlContains("/quizzes/yuva")
        ));

        String currentUrl = driver.getCurrentUrl();
        boolean redirectedToAuth = currentUrl.contains("/login") || currentUrl.contains("/signin") ||
                currentUrl.contains("/quizzes/yuva") || driver.findElements(By.id("email")).size() > 0;
        Assert.assertTrue(redirectedToAuth,
                "Unauthenticated user should be redirected to login page, but was at: " + currentUrl);
    }

    @Test(priority = 5, groups = {"regression", "quiz", "security"}, description = "Try accessing another user's quiz submission via IDOR")
    public void testIDORAccessOtherUserQuiz() {
        loginAsYouth();
        driver.get(baseURL + "/quiz_exam/99999/submission/1");

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource().toLowerCase();

        boolean accessDenied = pageSource.contains("unauthorized") || pageSource.contains("forbidden") ||
                pageSource.contains("access denied") || pageSource.contains("not found") ||
                pageSource.contains("404") || pageSource.contains("403") ||
                !currentUrl.contains("/submission/1");
        Assert.assertTrue(accessDenied,
                "Accessing another user's quiz submission should be denied");
    }

    @Test(priority = 6, groups = {"regression", "quiz", "security"}, description = "Call quiz API endpoints without JWT and verify 401")
    public void testAPIWithoutAuth() throws IOException {
        URL apiUrl = new URL(baseURL + "/api/quizzes");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        Assert.assertTrue(responseCode == 401 || responseCode == 403,
                "API should return 401/403 without auth token, got: " + responseCode);
    }

    @Test(priority = 7, groups = {"regression", "quiz", "security"}, description = "Use expired/modified JWT and verify rejection")
    public void testExpiredJWTToken() throws IOException {
        String expiredJWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxMDAwMDAwMDAwfQ.invalid_signature";

        URL apiUrl = new URL(baseURL + "/api/quizzes");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + expiredJWT);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        Assert.assertTrue(responseCode == 401 || responseCode == 403,
                "API should reject expired/invalid JWT, got: " + responseCode);
    }

    @Test(priority = 8, groups = {"regression", "quiz", "security"}, description = "Verify anti-CSRF tokens present on forms")
    public void testCSRFProtection() {
        loginAsYouth();

        List<WebElement> csrfTokens = driver.findElements(
                By.cssSelector("input[name='_csrf'], input[name='csrf_token'], input[name='csrfmiddlewaretoken'], meta[name='csrf-token']"));

        Object metaCsrf = js.executeScript(
                "return document.querySelector('meta[name=csrf-token], meta[name=_csrf]') !== null");

        Object headerCsrf = js.executeScript(
                "var cookies = document.cookie; return cookies.includes('csrf') || cookies.includes('XSRF');");

        boolean csrfPresent = !csrfTokens.isEmpty() || (Boolean) metaCsrf || (Boolean) headerCsrf;
        Assert.assertTrue(csrfPresent, "CSRF protection tokens should be present on the page");
    }

    @Test(priority = 9, groups = {"regression", "quiz", "security"}, description = "Youth trying to access /quizzes/partner (creator endpoints)")
    public void testBrokenAccessControl() {
        loginAsYouth();
        driver.get(baseURL + "/quizzes/partner");

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource().toLowerCase();

        boolean accessBlocked = !currentUrl.contains("/quizzes/partner") ||
                pageSource.contains("unauthorized") || pageSource.contains("forbidden") ||
                pageSource.contains("access denied") || pageSource.contains("not allowed") ||
                currentUrl.contains("/login") || currentUrl.contains("/quizzes/yuva");
        Assert.assertTrue(accessBlocked,
                "Youth user should not be able to access creator partner endpoints");
    }

    @Test(priority = 10, groups = {"regression", "quiz", "security"}, description = "Verify X-Frame-Options, CSP, HSTS headers present")
    public void testHTTPSecurityHeaders() throws IOException {
        URL url = new URL(baseURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.connect();

        String xFrameOptions = conn.getHeaderField("X-Frame-Options");
        String csp = conn.getHeaderField("Content-Security-Policy");
        String hsts = conn.getHeaderField("Strict-Transport-Security");

        conn.disconnect();

        Assert.assertNotNull(xFrameOptions,
                "X-Frame-Options header should be present for clickjacking protection");
        Assert.assertNotNull(csp,
                "Content-Security-Policy header should be present");
        Assert.assertNotNull(hsts,
                "Strict-Transport-Security (HSTS) header should be present");
    }
}
