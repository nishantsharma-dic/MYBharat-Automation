# MYBharat Automation Framework

Selenium + TestNG automation framework for the MY Bharat (mybharat.gov.in) platform.

## Quick Start

```bash
# Clone
git clone https://github.com/nishantsharma-dic/MYBharat-Automation.git
cd MYBharat-Automation

# Run all modules (parallel — Public Pages + Youth Flow)
mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-all-modules.xml

# Run on Beta
mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-all-modules.xml
```

## Environments

| Environment | URL | Command |
|-------------|-----|---------|
| Beta | https://yuva-beta.mybharats.in | `-Denv=beta` |
| Production | https://mybharat.gov.in | `-Denv=prod` |

## Test Modules

| # | Module | Test Method | Status | Suite File | Developer |
|---|--------|-------------|--------|------------|-----------|
| 1 | Public Pages | publicPages | ✅ Done | `testng-public-page.xml` | Prashant/Hariom |
| 2 | Registration | registerIndianYouth | ✅ Done | `testng-registration.xml` | Tejas |
| 3 | Logout | logoutUser | ✅ Done | — | Tejas |
| 4 | Login (OTP) | loginWithOTP | ✅ Done | `testng-login.xml` | Tejas |
| 5 | Profile | completeYouthProfile | ✅ Done | — | Alamgeer |
| 6 | Certificate | verifyRegistrationCertificateDownload | ✅ Done | — | Alamgeer |
| 7 | Basic Info | clickBasicInfoAndExtractEmail | ✅ Done | — | Alamgeer |
| 8 | Quiz | attemptCompetitiveQuiz | ✅ Done | — | Uvais |
| 9 | Quiz Certificate | verifyQuizCertificateDownload | ✅ Done | — | Uvais |
| 10 | Blog | writeAndPublishBlog | ✅ Done | `testng-blog.xml` | Sonali |
| 11 | ELP | (in progress) | 🔧 WIP | `testng-elp.xml` | Nishant |
| 12 | Mega Event | (in progress) | 🔧 WIP | `testng-megaevent.xml` | Pal |

## E2E Flow (testng-all-modules.xml)

Runs in **parallel** (2 threads):

```
Thread 1: Public Pages (Header + Org + Footer validation)
Thread 2: Registration → Logout → Login → Profile → Certificate → Basic Info → Quiz → Quiz Certificate → Blog
```

## Available Test Suites

| Suite | File | Description | Command |
|-------|------|-------------|---------|
| All Modules | `testng-all-modules.xml` | Full E2E (parallel) | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-all-modules.xml` |
| Youth Flow | `testng-youth.xml` | Reg → Logout → Login → Profile → Quiz → Blog | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-youth.xml` |
| Registration Only | `testng-registration.xml` | Register new user | `mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-registration.xml` |
| Registration + Quiz | `testng-registration-quiz.xml` | Register + Play Quiz | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-registration-quiz.xml` |
| Public Pages | `testng-public-page.xml` | Header + Org + Footer | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-public-page.xml` |
| Login Only | `testng-login.xml` | Login with last registered email | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-login.xml` |
| Blog | `testng-blog.xml` | Login + Create Blog + Verify | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-blog.xml` |
| Bulk Registration | `testng-registration-bulk.xml` | 10 users in parallel | `mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-registration-bulk.xml` |

## CI/CD

- **Scheduled Run:** Daily automated run on `main` branch (sends email report to seniors)
- **Manual Run:** Trigger from GitHub Actions on `integration` branch (sends email to QA team)
- **Email Report:** Contains pass/fail status for all 11 test cases with developer assignments

## Tech Stack

- Java 17
- Selenium 4.12
- TestNG 7.8
- Maven
- ExtentReports (HTML reports in `reports/index.html`)
- Log4j2 (logging)
- JavaFaker (test data generation)
- Apache POI (Excel read/write for test data)
- WebDriverManager (auto browser driver setup)

## Project Structure

```
src/test/java/com/mybharat/
├── base/BaseTest.java            ← ThreadLocal WebDriver, @BeforeTest/@AfterTest
├── listeners/
│   ├── TestListeners.java        ← ExtentReport + screenshot on failure
│   └── Retry.java                ← Auto-retry failed tests (1 retry)
├── pages/
│   ├── BasePage.java             ← Common: waits, clicks, scrolls
│   ├── youth/                    ← LoginPage, LogoutPage, RegistrationPage, etc.
│   └── blog/                     ← BlogPage, BlogAdminPage
├── tests/
│   ├── youth/                    ← RegistrationTest, LoginTest, QuizAttemptTest, etc.
│   └── blog/                     ← BlogTest
└── utils/
    ├── ConfigReader.java         ← Environment config (beta/prod)
    └── ExtentReportManager.java  ← Report setup

resources/
├── Youth_beta.xlsx               ← Registered user emails (beta)
├── Youth_prod.xlsx               ← Registered user emails (prod)
├── Partner_beta.xlsx             ← ELP/Partner admin emails (beta)
└── Partner_prod.xlsx             ← ELP/Partner admin emails (prod)
```

## Team

| Role | Name | Modules |
|------|------|---------|
| Lead | Nishant Sharma | Framework, CI/CD, Code Review |
| Tester | Tejas | Registration, Login, Logout |
| Tester | Alamgeer | Profile, Certificate, Basic Info |
| Tester | Uvais | Quiz, Quiz Certificate |
| Tester | Sonali | Blog |
| Tester | Prashant/Hariom | Public Pages |
| Tester | Pal | Mega Event |

## Reports

After running tests: `reports/index.html`

## Contributing

See **[CONTRIBUTING.md](CONTRIBUTING.md)** for Git workflow, coding standards, and how to write new tests.
