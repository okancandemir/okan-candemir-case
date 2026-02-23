package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.CareersQAPage;
import pages.HomePage;
import pages.LeverJobPage;
import pages.QAJobsPage;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InsiderQATest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(InsiderQATest.class);

    @Test
    void insiderQaCaseStudyScenario1() {
        HomePage homePage = new HomePage(driver).open();

        logger.info("Scenario1: Checking for cookie popup...");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            By cookieAccept = By.id("wt-cli-accept-all-btn");

            shortWait.until(ExpectedConditions.elementToBeClickable(cookieAccept)).click();

            logger.info("Scenario1: Cookie popup detected and accepted.");
        } catch (TimeoutException e) {
            logger.info("Scenario1: No cookie popup detected within 5 seconds. Continuing.");
        }

        logger.info("Scenario1: Waiting for homepage elements to load...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("navigation")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//header[@id='navigation']//a[contains(normalize-space(.),'Get a demo')]")
        ));

        logger.info("Scenario1: Homepage main elements loaded.");

        assertTrue(
                homePage.isNavbarVisible(),
                "Navbar should be visible (By.id(\"navigation\"))"
        );
        assertTrue(
                homePage.isLogoValid(),
                "Logo anchor should be visible and href should contain \"insiderone.com\" (By.cssSelector(\"#navigation .header-logo a\"))"
        );
        assertTrue(
                homePage.isNavbarGetDemoClickable(),
                "Navbar \"Get a demo\" link should be clickable (By.xpath(\"//header[@id='navigation']//a[contains(normalize-space(.),'Get a demo')]\"))"
        );
        assertTrue(
                homePage.isEmailInputVisible(),
                "Email input should be visible and enabled (By.id(\"email\"))"
        );
        assertTrue(
                homePage.isHeroGetDemoClickable(),
                "Hero \"Get a demo\" button should be clickable (By.cssSelector(\"section.homepage-hero form .redirect-button\"))"
        );
    }

    @Test
    void insiderQaCaseStudyScenario2To4() {
        CareersQAPage careers = new CareersQAPage(driver).open();
        assertTrue(careers.careersQA_isAt());
        assertTrue(careers.careersQA_isSeeAllQAJobsButtonVisible());
        assertTrue(careers.careersQA_isSeeAllQAJobsButtonHrefCorrect());

        QAJobsPage qaJobs = careers.careersQA_clickSeeAllQAJobsButton();
        assertTrue(qaJobs.qaJobs_isAt());

        assertTrue(qaJobs.qaJobs_waitForJobCardsLoaded());

        logger.info("Job cards loaded, proceeding with department verification.");
        assertTrue(qaJobs.qaJobs_isDepartmentAutoSelectedAsQA());

        qaJobs.qaJobs_selectLocationIstanbulTurkiye();

        assertTrue(qaJobs.qaJobs_waitForJobCardsLoaded());
        assertTrue(qaJobs.qaJobs_isJobsListVisible());
        assertTrue(qaJobs.qaJobs_hasJobCards());

        String originalHandle = driver.getWindowHandle();
        List<QAJobsPage.JobPreview> valid = qaJobs.qaJobs_collectValidQAJobsInIstanbul();
        logger.info("Valid QA Istanbul cards count={}", valid.size());
        assertFalse(valid.isEmpty(), "No valid QA jobs found for Istanbul, Turkey/Turkiye.");

        Set<String> handlesBefore = new HashSet<>(driver.getWindowHandles());
        QAJobsPage.JobPreview selected = qaJobs.qaJobs_clickRandomValidViewRoleWithFallback(valid);
        assertNotNull(selected);
        assertNotNull(selected.href());
        assertTrue(selected.href().contains("jobs.lever.co"));

        wait.until(d -> d.getWindowHandles().size() > handlesBefore.size());
        Set<String> handlesAfter = new HashSet<>(driver.getWindowHandles());
        handlesAfter.removeAll(handlesBefore);
        driver.switchTo().window(handlesAfter.iterator().next());
        logger.info("Step: Switched to Lever tab. url={}, title={}", driver.getCurrentUrl(), driver.getTitle());

        LeverJobPage lever = new LeverJobPage(driver);
        assertTrue(lever.lever_isAt());

        String leverTitle = lever.lever_getTitle();
        String leverDept = lever.lever_getDepartment();
        String leverLoc = lever.lever_getLocation();

        logger.info(
                "Step: Lever page actual values: title='{}', dept='{}', loc='{}'",
                leverTitle,
                leverDept,
                leverLoc
        );
        logger.info(
                "Step: Expected job preview: title='{}', dept='{}', loc='{}', href='{}'",
                selected.title(),
                selected.department(),
                selected.location(),
                selected.href()
        );

        assertTrue(normalized(lever.lever_getTitle()).contains(normalized(selected.title())));
        assertTrue(normalized(lever.lever_getDepartment()).toLowerCase().contains("quality assurance"));

        String leverLocationLower = normalized(lever.lever_getLocation()).toLowerCase();
        assertTrue(leverLocationLower.contains("istanbul"));
        assertTrue(leverLocationLower.contains("turkey") || leverLocationLower.contains("turkiye"));
    }

    private String normalized(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }
}
