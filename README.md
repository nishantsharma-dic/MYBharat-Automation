# MYBharat Automation Framework

Selenium + TestNG automation framework for the MYBharat / YUVA platform.

## Quick Start

```bash
# Clone
git clone https://github.com/nishantsharma-dic/MYBharat-Automation.git
cd MYBharat-Automation

# Run smoke tests on Beta
mvn test -Denv=beta -Dbrowser=chrome

# Run all modules sequentially
mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-all-modules.xml

# Run on Production
mvn test -Denv=prod -Dbrowser=firefox
```

## Environments

| Environment | URL | Command |
|-------------|-----|---------|
| Beta | https://yuva-beta.mybharats.in | `-Denv=beta` |
| Production | https://mybharat.gov.in | `-Denv=prod` |

## Modules

| # | Module | Status | Suite File |
|---|--------|--------|------------|
| 1 | Youth Registration | ✅ Done | `testng-youth.xml` |
| 2 | Login | 🔲 TODO | — |
| 3 | Youth Profile | 🔲 TODO | — |
| 4 | CV Builder | 🔲 TODO | — |
| 5 | Organisation Creation | 🔲 TODO | — |
| 6 | Join Organisation | 🔲 TODO | — |

## Tech Stack

- Java 17
- Selenium 4.12
- TestNG 7.8
- Maven
- ExtentReports (HTML reports)
- Log4j2 (logging)
- JavaFaker (test data)
- Redash (DB verification)

## Reports

After running tests, open: `reports/index.html`

## Team Guide

See **[CONTRIBUTING.md](CONTRIBUTING.md)** for:
- Git branching model & daily workflow
- How to write new tests
- Code review checklist
- Naming conventions
- Troubleshooting
