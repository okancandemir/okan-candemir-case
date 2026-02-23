package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CareersQAPage extends BasePage {

    private static final String URL = "https://insiderone.com/careers/quality-assurance/";

    public CareersQAPage(WebDriver driver) {
        super(driver);
    }

    public CareersQAPage open() {
        logger.info("Open Careers QA page: {}", URL);
        driver.get(URL);
        waitForDocumentReady(Duration.ofSeconds(20));
        return this;
    }

    public boolean careersQA_isAt() {
        String url = driver.getCurrentUrl();
        boolean ok = url != null && url.contains("/careers/quality-assurance/");
        if (!ok) {
            logger.warn("careersQA_isAt failed (currentUrl='{}')", url);
        }
        return ok;
    }

    public boolean careersQA_isSeeAllQAJobsButtonVisible() {
        try {
            WebElement el = waitForVisible(careersQA_seeAllQAJobsButton, Duration.ofSeconds(15));
            boolean visible = isDisplayedSafe(el);
            if (!visible) {
                logger.warn("See All QA Jobs button present but not visible (locator={})", careersQA_seeAllQAJobsButton);
            }
            return visible;
        } catch (RuntimeException e) {
            logger.warn("See All QA Jobs button not visible in time (locator={})", careersQA_seeAllQAJobsButton, e);
            return false;
        }
    }

    public boolean careersQA_isSeeAllQAJobsButtonHrefCorrect() {
        String href = findSeeAllQAJobsHref();
        boolean ok = href != null && href.contains("department=qualityassurance");
        if (!ok) {
            logger.warn("See All QA Jobs href invalid (href='{}') (locator={})", href, careersQA_seeAllQAJobsButton);
        }
        return ok;
    }

    public QAJobsPage careersQA_clickSeeAllQAJobsButton() {
        String expectedHref = findSeeAllQAJobsHref();
        WebDriverWait cookieWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            cookieWait.until(ExpectedConditions.elementToBeClickable(cookieAcceptBtn)).click();
            logger.info("Cookie popup detected and accepted before clicking See All QA Jobs.");
        } catch (TimeoutException e) {
            logger.info("No cookie popup detected within 5 seconds. Continuing.");
        } catch (RuntimeException e) {
            logger.warn("Cookie popup handling failed (non-fatal). Continuing.", e);
        }

        safeClick(careersQA_seeAllQAJobsButton);
        waitForDocumentReady(Duration.ofSeconds(20));

        if (expectedHref != null) {
            String url = driver.getCurrentUrl();
            if (url == null || !url.contains("department=qualityassurance")) {
                logger.info("Re-navigate using See All QA Jobs href to ensure department filter (href='{}')", expectedHref);
                driver.get(expectedHref);
                waitForDocumentReady(Duration.ofSeconds(20));
            }
        }
        return new QAJobsPage(driver);
    }

    private String findSeeAllQAJobsHref() {
        List<WebElement> elements = driver.findElements(careersQA_seeAllQAJobsButton);
        for (WebElement el : elements) {
            String href = getAttributeSafe(el, "href");
            if (href != null && href.contains("department=qualityassurance")) {
                return href;
            }
        }
        return elements.isEmpty() ? null : getAttributeSafe(elements.getFirst(), "href");
    }

    private static boolean isDisplayedSafe(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String getAttributeSafe(WebElement el, String name) {
        try {
            return el.getAttribute(name);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private final By careersQA_seeAllQAJobsButton =
            By.cssSelector("a.btn.btn-outline-secondary.rounded");
}

