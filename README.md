# MYBharat Selenium Automation Framework

End-to-end test automation for the **MYBharat / YUVA** platform, built with
Java 17 · Selenium 4 · TestNG · ExtentReports · WebDriverManager.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| Firefox / Chrome | latest |

---

## Project Structure

```
src/
├── main/java/
│   ├── MYBharat_ResourcesAndAbstractComponents/
│   │   ├── AbstractComponents/AbstractComponent.java   ← base utilities & waits
│   │   ├── pageobjects/LandingPage.java
│   │   └── resources/
│   │       ├── EmailExcelWriter.java                   ← records test emails
│   │       └── ExtentReporterNG.java                   ← report factory
│   └── pageobjects/Youth/
│       ├── Registration.java
│       ├── PublicProfile.java
│       └── PlayQuiz.java
├── main/resources/
│   ├── log4j2.xml
│   └── (no config files here – use src/test/resources)
└── test/
    ├── java/MYBharat/Test/
    │   ├── TestComponents/
    │   │   ├── BaseTest.java      ← WebDriver lifecycle (@BeforeClass / @AfterClass)
    │   │   ├── Listeners.java     ← ExtentReports + screenshot on failure
    │   │   ├── Retry.java         ← retries flaky tests up to 2 times
    │   │   └── RedashClient.java  ← Redash API helper
    │   └── tests/youth/
    │       ├── YouthTest.java
    │       └── RedashDataDrivenTest.java
    └── resources/
        ├── config.qa.properties
        ├── config.uat.properties
        └── config.prod.properties
testSuites/
├── testng-suite_Youth.xml
└── testng-suite_OrgCreation.xml
testng.xml                         ← default smoke suite
```

---

## Running Tests

### Default (QA, Firefox, smoke tests)
```bash
mvn test
```

### Select browser
```bash
mvn test -Dbrowser=chrome
mvn test -Dbrowser=headless        # headless Chrome
mvn test -Dbrowser=firefox-headless
```

### Select environment
```bash
mvn test -Pqa                      # QA  (default)
mvn test -Puat                     # UAT
mvn test -Pprod                    # Production
```

### Run a specific suite
```bash
mvn test -Dsurefire.suiteXmlFiles=testSuites/testng-suite_Youth.xml
```

### Combine options
```bash
mvn test -Pbrowser=chrome -Puat -Dsurefire.suiteXmlFiles=testSuites/testng-suite_Youth.xml
```

---

## Reports

After a run, open `reports/index.html` in a browser to view the ExtentReports
HTML report. Screenshots for failed tests are saved in `reports/`.

Logs are written to `test-output/logs/automation.log`.

---

## Adding a New Test Class

1. Create your page object under `src/main/java/pageobjects/<Module>/`.
2. Extend `AbstractComponent` and use `@FindBy` + `PageFactory`.
3. Create your test class under `src/test/java/MYBharat/Test/tests/<module>/`.
4. Extend `BaseTest` and annotate with `@Listeners(Listeners.class)`.
5. Add the class to the relevant suite XML in `testSuites/`.

---

## Redash Data-Driven Tests

Pass credentials via system properties (never commit them):
```bash
mvn test -DredashBaseUrl=https://dash-beta.mybharats.in \
         -DredashQueryId=63 \
         -DredashApiKey=<your-key>
```

---

## Environment Variables / System Properties Reference

| Property | Default | Description |
|----------|---------|-------------|
| `env` | `qa` | Config profile: qa / uat / prod |
| `browser` | `firefox` | Browser: chrome / firefox / headless / edge |
| `downloadDir` | `~/Downloads` | Certificate download directory |
| `emailsFile` | `test-output/emails.xlsx` | Email tracking file |
| `tester` | `QA Team` | Name shown in ExtentReports |
| `redashBaseUrl` | – | Redash server URL |
| `redashQueryId` | – | Redash query ID |
| `redashApiKey` | – | Redash API key |
