# MYBharat Automation Framework — Team Guide

## Table of Contents
1. [Project Structure](#project-structure)
2. [Environment Setup](#environment-setup)
3. [Git Branching Model](#git-branching-model)
4. [How to Run Tests](#how-to-run-tests)
5. [How to Write New Tests](#how-to-write-new-tests)
6. [Code Review Checklist](#code-review-checklist)
7. [Naming Conventions](#naming-conventions)
8. [Troubleshooting](#troubleshooting)

---

## Project Structure

```
src/test/java/com/mybharat/
├── base/
│   └── BaseTest.java              ← ThreadLocal WebDriver, @BeforeTest/@AfterTest (parallel support)
├── listeners/
│   ├── TestListeners.java         ← ExtentReport + screenshot on failure
│   └── Retry.java                 ← Retries failed tests 1 time (handles flaky elements)
├── pages/
│   ├── BasePage.java              ← Common methods: waits, clicks, scrolls, form helpers
│   ├── LandingPage.java           ← App home page actions
│   ├── youth/                     ← Youth module page objects
│   │   ├── LoginPage.java         ← OTP + Password login
│   │   ├── LogoutPage.java        ← Multi-portal logout (React + PHP)
│   │   ├── RegistrationPage.java
│   │   ├── YouthProfilePage.java
│   │   ├── PublicPage.java        ← Public pages validation
│   │   └── QuizAttemptPage.java   ← Dynamic question count support
│   └── blog/                      ← Blog module page objects
│       ├── BlogPage.java
│       └── BlogAdminPage.java
├── tests/
│   ├── youth/                     ← Youth module tests
│   │   ├── RegistrationTest.java
│   │   ├── LogoutTest.java
│   │   ├── LoginTest.java
│   │   ├── YouthProfileTest.java
│   │   ├── BasicInfoTest.java
│   │   ├── QuizAttemptTest.java
│   │   ├── QuizCertificateVerificationTest.java
│   │   ├── RegistrationCertificateVerificationTest.java
│   │   └── PublicPageTest.java
│   └── blog/
│       └── BlogTest.java
└── utils/
    ├── ConfigReader.java          ← Reads environment config (beta/prod)
    ├── ExtentReportManager.java   ← HTML report configuration
    └── RedashClient.java          ← DB verification via Redash API

resources/
├── Youth_beta.xlsx                ← Registered user emails (beta)
├── Youth_prod.xlsx                ← Registered user emails (prod)
├── Partner_beta.xlsx              ← ELP/Partner admin emails (beta)
└── Partner_prod.xlsx              ← ELP/Partner admin emails (prod)

testSuites/
├── testng-all-modules.xml         ← Full E2E (parallel: Public Pages + Youth Flow)
├── testng-youth.xml               ← Youth flow only
├── testng-registration.xml        ← Registration only
├── testng-registration-quiz.xml   ← Registration + Quiz
├── testng-public-page.xml         ← Public pages only
├── testng-login.xml               ← Login only
├── testng-blog.xml                ← Blog (Login + Create + Verify)
└── testng-registration-bulk.xml   ← 10 parallel registrations
```

### Key Concepts

| Folder | Purpose | Who works here |
|--------|---------|----------------|
| `pages/` | Page Object classes — represent UI pages | Everyone |
| `tests/` | Test classes — contain @Test methods | Everyone |
| `base/` | BaseTest — browser lifecycle | Lead only |
| `utils/` | Utilities (config, reports, DB client) | Lead only |
| `listeners/` | TestNG hooks (reports, retry) | Lead only |

---

## Environment Setup

### Prerequisites
- Java 17 (JDK)
- Maven 3.8+
- Git
- IDE: IntelliJ IDEA or Eclipse
- Browser: Chrome or Firefox installed

### First Time Setup

```bash
# 1. Clone the repository
git clone https://github.com/nishantsharma-dic/MYBharat-Automation.git
cd MYBharat-Automation

# 2. Switch to integration branch
git checkout integration

# 3. Verify build works
mvn compile test-compile

# 4. You're ready! Create your feature branch (see Git section below)
```

### IDE Setup (IntelliJ)
1. Open → Import Project → Select `pom.xml`
2. Wait for Maven to download dependencies
3. Right-click `testng.xml` → Run (to verify setup)

---

## Git Branching Model

### Branch Structure
```
main (protected)          ← Stable, production-ready. Only lead merges here.
  └── integration (protected)  ← Team integration branch. All PRs target here.
        ├── feature/nishant/youth-registration
        ├── feature/rahul/cv-builder
        ├── feature/priya/org-creation
        └── feature/MB-101-login-flow
```

### Rules
- **NEVER push directly to `main` or `integration`** — it will be rejected
- All changes go through Pull Requests with code review
- Always create feature branches FROM `integration`
- Always `git fetch` before pushing

### Daily Workflow (Step by Step)

#### Starting new work:
```bash
# 1. Switch to integration and get latest code
git checkout integration
git pull origin integration

# 2. Create your feature branch
git checkout -b feature/<your-name>/<module-name>
# Example: git checkout -b feature/nishant/cv-builder

# 3. Write your code...

# 4. Stage and commit
git add .
git commit -m "feat: add CV builder page object and test"

# 5. Before pushing — ALWAYS fetch latest integration
git fetch origin integration

# 6. Rebase your branch on latest integration (avoids conflicts)
git rebase origin/integration

# 7. Push your branch
git push -u origin feature/<your-name>/<module-name>

# 8. Go to GitHub → Create Pull Request → target: integration
```

#### If you get conflicts during rebase:
```bash
# Fix the conflicting files in your IDE, then:
git add .
git rebase --continue
```

#### After your PR is merged:
```bash
# Switch back to integration and pull the merged code
git checkout integration
git pull origin integration

# Delete your old feature branch locally
git branch -d feature/<your-name>/<module-name>

# Start new work with a fresh branch
git checkout -b feature/<your-name>/<next-task>
```

### Branch Naming Convention

| Format | Example | When to use |
|--------|---------|-------------|
| `feature/<name>/<module>` | `feature/nishant/youth-registration` | General feature work |
| `feature/<ticket-id>-<description>` | `feature/MB-101-login-flow` | When you have a ticket/task ID |
| `bugfix/<name>/<description>` | `bugfix/rahul/fix-otp-timeout` | Bug fixes |

### Commit Message Format
```
feat: add registration page object for youth module
fix: increase OTP wait timeout to 30 seconds
refactor: move common waits to BasePage
test: add DB verification after registration
docs: update CONTRIBUTING.md with new module info
```

---

## How to Run Tests

### Quick Commands

```bash
# Run smoke tests on BETA with Firefox (default)
mvn test

# Run on BETA with Chrome
mvn test -Denv=beta -Dbrowser=chrome

# Run on PROD with Firefox
mvn test -Denv=prod -Dbrowser=firefox

# Run headless (no browser window — for CI)
mvn test -Denv=beta -Dbrowser=headless

# Run specific module only
mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-youth.xml

# Run ALL modules sequentially (one click)
mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-all-modules.xml

# Run with DB verification via Redash
mvn test -Denv=beta -Dbrowser=chrome \
  -DredashBaseUrl=https://dash-beta.mybharats.in \
  -DredashQueryId=63 \
  -DredashApiKey=YOUR_API_KEY
```

### Where to find reports
After test execution:
- **HTML Report:** `reports/index.html` (open in browser)
- **Screenshots (on failure):** `reports/<testName>.png`
- **TestNG default output:** `test-output/`

---

## How to Write New Tests

### Step 1: Create Page Object

Create a new file in `src/test/java/com/mybharat/pages/<module>/`:

```java
package com.mybharat.pages.cvbuilder;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.mybharat.pages.BasePage;

public class CVBuilderPage extends BasePage {

    // Declare elements
    @FindBy(xpath = "//button[@id='create-cv']")
    private WebElement createCvBtn;

    @FindBy(xpath = "//input[@id='cv-title']")
    private WebElement cvTitleInput;

    // Constructor — always call super(driver)
    public CVBuilderPage(WebDriver driver) {
        super(driver);
    }

    // Action methods — one method per user action
    public void clickCreateCV() {
        safeClick(createCvBtn);
    }

    public void enterTitle(String title) {
        clearAndType(cvTitleInput, title);
    }
}
```

### Step 2: Create Test Class

Create a new file in `src/test/java/com/mybharat/tests/<module>/`:

```java
package com.mybharat.tests.cvbuilder;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.cvbuilder.CVBuilderPage;

@Listeners(TestListeners.class)
public class CVBuilderTest extends BaseTest {

    private CVBuilderPage cvPage;

    @BeforeClass
    @Override
    public void setUp() {
        super.setUp();  // MUST call super — starts the browser
        cvPage = new CVBuilderPage(driver);
    }

    @Test(priority = 1, groups = {"smoke"}, retryAnalyzer = Retry.class)
    public void createNewCV() {
        openApp();  // Opens the URL from config
        // ... your test steps
        cvPage.clickCreateCV();
        cvPage.enterTitle("My Resume");
        // Assert something
        Assert.assertTrue(true, "CV should be created");
    }
}
```

### Step 3: Add to TestNG Suite

Edit `testSuites/testng-all-modules.xml` and uncomment/add your module:

```xml
<test name="Module - CV Builder">
    <classes>
        <class name="com.mybharat.tests.cvbuilder.CVBuilderTest"/>
    </classes>
</test>
```

### Step 4: Push via PR
```bash
git add .
git commit -m "feat: add CV builder page object and test"
git push -u origin feature/<your-name>/cv-builder
# Then create PR on GitHub targeting integration
```

---

## Code Review Checklist

When reviewing a teammate's PR, check:

### ✅ OOPs & Design
- [ ] Page Object extends `BasePage`
- [ ] Test class extends `BaseTest`
- [ ] No business logic in test class (only in page objects)
- [ ] No hardcoded values (use config or Faker)

### ✅ Naming Conventions
- [ ] Class names: PascalCase (`RegistrationPage`, `YouthTest`)
- [ ] Method names: camelCase (`fillRegistrationForm`, `verifyUserInDB`)
- [ ] Variables: camelCase (`emailInput`, `submitBtn`)
- [ ] Package names: lowercase (`com.mybharat.pages.youth`)

### ✅ Code Quality
- [ ] Uses `safeClick()` instead of raw `.click()` for unreliable elements
- [ ] Uses `waitForVisible()` / `waitForClickable()` before interacting
- [ ] No `Thread.sleep()` longer than 2 seconds (use explicit waits instead)
- [ ] Proper assertions with meaningful messages
- [ ] No `System.out.println` — use `log.info()` instead

### ✅ Test Structure
- [ ] `@Test` has `priority` set (for sequential execution)
- [ ] `@Test` has `groups` set (smoke/regression)
- [ ] `retryAnalyzer = Retry.class` added for flaky tests
- [ ] `dependsOnMethods` used when tests depend on each other

---

## Naming Conventions

### Files & Classes

| Type | Convention | Example |
|------|-----------|---------|
| Page Object | `<PageName>Page.java` | `RegistrationPage.java` |
| Test Class | `<Module>Test.java` | `YouthRegistrationTest.java` |
| Utility | `<Purpose>.java` | `ConfigReader.java` |
| Config | `config-<env>.properties` | `config-beta.properties` |
| Suite | `testng-<module>.xml` | `testng-youth.xml` |

### Packages

```
com.mybharat.pages.youth          ← Youth module pages
com.mybharat.pages.cvbuilder      ← CV Builder module pages
com.mybharat.pages.organisation   ← Organisation module pages
com.mybharat.tests.youth          ← Youth module tests
com.mybharat.tests.cvbuilder      ← CV Builder module tests
com.mybharat.tests.organisation   ← Organisation module tests
```

### Test Groups

| Group | Purpose | When to run |
|-------|---------|-------------|
| `smoke` | Critical happy-path tests | Every PR, every build |
| `regression` | Full coverage tests | Before release |
| `registration` | Registration-specific | Module testing |

---

## Troubleshooting

### "Config file not found" error
- Check you're passing the correct `-Denv=beta` or `-Denv=prod`
- Verify file exists: `src/test/resources/config-beta.properties`

### Browser not starting
- Ensure Chrome/Firefox is installed
- Try: `mvn test -Dbrowser=chrome` or `mvn test -Dbrowser=firefox`
- For CI/servers: `mvn test -Dbrowser=headless`

### Tests failing with timeout
- Increase wait in `BasePage.java` (DEFAULT_WAIT constant)
- Check if the application is actually accessible on that environment

### Git push rejected
- You're trying to push to `main` or `integration` directly
- Create a feature branch first: `git checkout -b feature/<name>/<task>`

### Merge conflicts
```bash
git checkout integration
git pull origin integration
git checkout your-feature-branch
git rebase origin/integration
# Fix conflicts in IDE
git add .
git rebase --continue
git push --force-with-lease origin your-feature-branch
```

---

## Team

### Developers (Built the features)

| Name | Modules Developed |
|------|-------------------|
| Tejas | Registration, Login, Logout |
| Alamgeer | Profile, Certificate, Basic Info |
| Uvais | Quiz, Quiz Certificate |
| Sonali | Blog |
| Prashant/Hariom | Public Pages |

### Testers (Automation script writers)

| Role | Name | Automation Modules |
|------|------|-------------------|
| Lead | Nishant Sharma | Framework, CI/CD, Registration, Login, Quiz, ELP Cycle, Blog, Certificate |
| Tester | Nishant Pal | Mega Event, Nodal Registration, Join Partner, Youth Club |
| Tester | Manoj Kumar | Public Pages, Profile, Basic Info, VO Cycle, CV Builder, Public Profile, NCS, Health & Fitness |
| Tester | Mohit Kumar | Essay (Create/Play/Evaluate), Mentor/Mentee |

---

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────┐
│  DAILY COMMANDS                                          │
├─────────────────────────────────────────────────────────┤
│  Start work:                                            │
│    git checkout integration                             │
│    git pull origin integration                          │
│    git checkout -b feature/<name>/<module>              │
│                                                         │
│  Save work:                                             │
│    git add .                                            │
│    git commit -m "feat: description"                    │
│    git fetch origin integration                         │
│    git rebase origin/integration                        │
│    git push -u origin feature/<name>/<module>           │
│                                                         │
│  Run tests:                                             │
│    mvn test -Denv=beta -Dbrowser=chrome                 │
│                                                         │
│  After PR merged:                                       │
│    git checkout integration                             │
│    git pull origin integration                          │
│    git branch -d feature/<name>/<old-branch>            │
└─────────────────────────────────────────────────────────┘
```
