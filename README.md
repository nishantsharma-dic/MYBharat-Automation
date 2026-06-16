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

| # | Module | Test Method | Status | Suite File | Developer | Tester |
|---|--------|-------------|--------|------------|-----------|--------|
| 1 | Public Pages | publicPages | ✅ Done | `testng-public-page.xml` | Prashant/Hariom | Manoj Kumar |
| 2 | Registration | registerIndianYouth | ✅ Done | `testng-registration.xml` | Tejas | Nishant Sharma |
| 3 | Logout | logoutUser | ✅ Done | — | Tejas | Nishant Sharma |
| 4 | Login (OTP) | loginWithOTP | ✅ Done | `testng-login.xml` | Tejas | Nishant Sharma |
| 5 | Profile | completeYouthProfile | ✅ Done | — | Alamgeer | Manoj Kumar |
| 6 | Certificate | verifyRegistrationCertificateDownload | ✅ Done | — | Alamgeer | Nishant Sharma |
| 7 | Basic Info | clickBasicInfoAndExtractEmail | ✅ Done | — | Alamgeer | Manoj Kumar |
| 8 | Quiz | attemptCompetitiveQuiz | ✅ Done | — | Uvais | Nishant Sharma |
| 9 | Quiz Certificate | verifyQuizCertificateDownload | ✅ Done | — | Uvais | Nishant Sharma |
| 10 | Blog | writeAndPublishBlog | ✅ Done | `testng-blog.xml` | Sonali | Nishant Sharma |
| 11 | ELP Cycle | (in progress) | 🔧 WIP | `testng-elp.xml` | — | Nishant Sharma |
| 12 | Mega Event | (in progress) | 🔧 WIP | `testng-megaevent.xml` | — | Nishant Pal |
| 13 | VO Cycle | (planned) | 🔲 TODO | — | — | Manoj Kumar |
| 14 | CV Builder | (planned) | 🔲 TODO | — | — | Manoj Kumar |
| 15 | Essay (Create/Play/Evaluate) | (planned) | 🔲 TODO | — | — | Mohit Kumar |
| 16 | NCS Integration | (planned) | 🔲 TODO | — | — | Manoj Kumar |
| 17 | Health and Fitness | (planned) | 🔲 TODO | — | — | Manoj Kumar |
| 18 | Youth Club Registration | (planned) | 🔲 TODO | — | — | Nishant Pal |
| 19 | Mentor/Mentee | (planned) | 🔲 TODO | — | — | Mohit Kumar |
| 20 | New React Public Profile | (planned) | 🔲 TODO | — | — | Manoj Kumar |

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
| Lead | Nishant Sharma | Framework, CI/CD, Code Review, Registration, Login, Quiz, ELP, Blog, Certificate |
| Tester | Nishant Pal | Mega Event, Nodal Registration, Join Partner, Youth Club |
| Tester | Manoj Kumar | Public Pages, Profile, Basic Info, VO Cycle, CV Builder, Public Profile, NCS, Health & Fitness |
| Tester | Mohit Kumar | Essay (Create/Play/Evaluate), Mentor/Mentee |

## Reports

After running tests: `reports/index.html`

## Contributing

See **[CONTRIBUTING.md](CONTRIBUTING.md)** for Git workflow, coding standards, and how to write new tests.
