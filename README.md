# MYBharat Automation Framework

Selenium + TestNG automation framework for the MY Bharat (mybharat.gov.in) platform.

## Quick Start

```bash
# Clone
git clone https://github.com/nishantsharma-dic/MYBharat-Automation.git
cd MYBharat-Automation

# Run all modules (parallel — Youth Flow + Youth Club)
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

| # | Module | Test Method | Suite File | Developer |
|---|--------|-------------|------------|-----------|
| 1 | Registration | registerIndianYouth | `testng-registration.xml` | Tejas |
| 2 | DB Verification | verifyUserInDatabase | — | Tejas |
| 3 | Logout | logoutUser | — | Tejas |
| 4 | Login (OTP) | loginWithOTP | `testng-login.xml` | Tejas |
| 5 | Profile | completeYouthProfile | — | Alamgeer |
| 6 | Certificate | verifyRegistrationCertificateDownload | — | Alamgeer |
| 7 | Basic Info | clickBasicInfoAndExtractEmail | — | Alamgeer |
| 8 | Quiz | attemptCompetitiveQuiz | — | Uvais |
| 9 | Quiz Certificate | verifyQuizCertificateDownload | — | Uvais |
| 10 | Blog | writeAndPublishBlog | `testng-blog.xml` | Sonali |
| 11 | Youth Club | step15_submit | `testng-youthclub.xml` | Tejas |
| 12 | Youth Club Approve | step19_superAdminApprove | — | Tejas |

## E2E Flow (testng-all-modules.xml)

Runs in **2 parallel threads**:

```
Thread 1 (Youth Flow):
  Registration → Logout → Login (OTP) → Profile → Certificate → Basic Info → Quiz → Quiz Certificate → Blog

Thread 2 (Youth Club):
  Register 6 Members → Create Youth Club → SuperAdmin Approve
```

## OTP Strategy

All OTP fetching uses the **Maildrop.cc GraphQL API** (no browser tab needed):
- Registration emails: `{name}@maildrop.cc`
- Youth Club members: `yco{timestamp}@maildrop.cc`
- OTP extracted via API polling (no Yopmail dependency)

## CI/CD (GitHub Actions)

| Workflow | Branch | Schedule | Runner |
|----------|--------|----------|--------|
| Scheduled Run | main | 9:40 AM, 1:00 PM, 5:00 PM IST | GitHub Actions (ubuntu + xvfb) |
| Manual Run | integration | On-demand | GitHub Actions (ubuntu + xvfb) |

- Uses `xvfb` for headed Chrome mode (identical to local behavior)
- `-DciMode=true` enables longer waits for overseas network latency
- Email report sent to team after each run

## Available Test Suites

| Suite | File | Description |
|-------|------|-------------|
| All Modules | `testng-all-modules.xml` | Full E2E (2 parallel threads) |
| Youth Flow | `testng-youth.xml` | Reg → Logout → Login → Profile → Quiz → Blog |
| Registration | `testng-registration.xml` | Register new user only |
| Login | `testng-login.xml` | Login with last registered email |
| Blog | `testng-blog.xml` | Login + Create Blog |
| Youth Club | `testng-youthclub.xml` | Register members + Create club + Approve |

## Tech Stack

- Java 17
- Selenium 4.25
- TestNG 7.8
- Maven
- WebDriverManager 5.9.2
- ExtentReports 5.1 (HTML reports)
- Log4j2 (logging)
- JavaFaker (test data)
- Apache POI (Excel read/write)
- Apache HttpClient 5 (Maildrop API)
- Jackson (JSON parsing)

## Project Structure

```
src/test/java/com/mybharat/
├── base/BaseTest.java            ← ThreadLocal WebDriver, @BeforeTest/@AfterTest
├── listeners/
│   ├── TestListeners.java        ← ExtentReport + screenshot on failure
│   └── Retry.java                ← Auto-retry failed tests (1 retry)
├── pages/
│   ├── BasePage.java             ← Common: waits, clicks, scrolls, CI mode timeouts
│   ├── youth/                    ← LoginPage, LogoutPage, RegistrationPage, YouthProfilePage, QuizAttemptPage
│   ├── org/                      ← CreateYouthClubPage
│   ├── superadmin/               ← SuperAdminLoginPage, OrgApprovalPage
│   └── blog/                     ← BlogPage
├── tests/
│   ├── youth/                    ← RegistrationTest, LoginTest, QuizAttemptTest, etc.
│   ├── org/                      ← RegisterMembersForYouthClubTest, CreateYouthClubTest
│   └── blog/                     ← BlogTest
└── utils/
    └── ConfigReader.java         ← Environment config (beta/prod)

resources/
├── Youth_beta.xlsx               ← Registered user emails (beta)
├── Youth_prod.xlsx               ← Registered user emails (prod)
├── Partner_beta.xlsx             ← Partner admin emails (beta)
├── Partner_prod.xlsx             ← Partner admin emails (prod)
├── superadmin_beta.xlsx          ← SuperAdmin credentials (beta)
└── superadmin_prod.xlsx          ← SuperAdmin credentials (prod)
```

## Team

| Role | Name |
|------|------|
| Lead | Nishant Sharma |
| Tester | Nishant Pal |
| Tester | Manoj Kumar |
| Tester | Mohit Kumar |

## Reports

After running tests: `reports/index.html`

## Contributing

See **[CONTRIBUTING.md](CONTRIBUTING.md)** for Git workflow, coding standards, and how to write new tests.
