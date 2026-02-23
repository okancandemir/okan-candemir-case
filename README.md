# Insider QA Case Study – Selenium (Java, JUnit5) – No BDD Tool

This project automates the requested flows on InsiderOne website using:
- **Selenium 4**
- **Java 21**
- **JUnit 5**
- **SLF4J + Log4j2** (test logs)
- **Page Object Model (POM)** (no BDD framework)

> Architecture rule:
> - **No assertions inside Page classes**
> - Assertions only in `tests/*` classes

---


---

## How to Run

### IntelliJ
- Open `InsiderQATest`
- Run test methods individually, or run the whole class

### Maven
```bash
mvn test

What We Validate
Scenario 1 – Home Page Main Blocks

Flow:

Page URL is correct

Navbar is visible and non-empty

Logo exists and href is correct

Navbar “Get a demo” is clickable

Hero email input is visible

Hero “Get a demo” button is clickable

Test: insiderQaCaseStudyScenario1()



Scenario 2–4 – QA Careers & Jobs Listing + Lever Redirect

Flow:

Open QA Careers page

Validate page + “See all QA jobs” button + href correctness

Click “See all QA jobs” → open positions page

Wait for job cards to load (stable sync point)

Verify department auto-selected as QA (then continue)

Select location = Istanbul, Turkiye

Validate jobs list presence

Validate every listed job card contains required QA/location values

Pick eligible card(s), click View Role (handles multi-tab)

Switch to Lever tab and validate title/department/location on Lever page

Test: insiderQaCaseStudyScenario2To4()