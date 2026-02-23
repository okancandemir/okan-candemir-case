package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class QAJobsPage extends BasePage {

    public record JobPreview(String title, String department, String location, String href) {
    }

    public QAJobsPage(WebDriver driver) {
        super(driver);
    }

    public boolean qaJobs_isAt() {
        String url = driver.getCurrentUrl();
        boolean ok = url != null && url.contains("/careers/open-positions/");
        if (!ok) {
            logger.warn("qaJobs_isAt failed (currentUrl='{}')", url);
        }
        return ok;
    }

    public boolean qaJobs_isDepartmentAutoSelectedAsQA() {
        try {
            String url = driver.getCurrentUrl();
            logger.info("Check department auto-selected on URL: {}", url);

            wait.until(ExpectedConditions.visibilityOfElementLocated(qaJobs_departmentSelect));
            boolean ok;
            try {
                ok = wait.until(d -> {
                    WebElement selectEl = d.findElement(qaJobs_departmentSelect);

                    String selectedText = "";
                    try {
                        selectedText = new Select(selectEl).getFirstSelectedOption().getText();
                    } catch (RuntimeException ignored) {
                        // best-effort;
                    }

                    String selectedClass = "";
                    try {
                        List<WebElement> checked = selectEl.findElements(By.cssSelector("option:checked"));
                        WebElement selected = checked.isEmpty()
                                ? selectEl.findElement(By.cssSelector("option[selected]"))
                                : checked.getFirst();
                        selectedClass = selected.getAttribute("class");
                    } catch (RuntimeException ignored) {
                        // best-effort
                    }

                    String normalizedText = normalizeWhitespace(selectedText);
                    boolean textOk = normalizedText.toLowerCase(Locale.ROOT).contains("quality assurance");

                    String normalizedClass = normalizeWhitespace(selectedClass);
                    boolean classOk = normalizedClass.toLowerCase(Locale.ROOT).contains("qualityassurance");

                    return textOk || classOk;
                });
            } catch (TimeoutException e) {
                ok = false;
            }

            WebElement selectEl = driver.findElement(qaJobs_departmentSelect);
            String selectValue = selectEl.getAttribute("value");
            String selectedText = "";
            try {
                selectedText = new Select(selectEl).getFirstSelectedOption().getText();
            } catch (RuntimeException ignored) {
                // best-effort
            }

            String selectedClass = "";
            try {
                List<WebElement> checked = selectEl.findElements(By.cssSelector("option:checked"));
                WebElement selected = checked.isEmpty()
                        ? selectEl.findElement(By.cssSelector("option[selected]"))
                        : checked.getFirst();
                selectedClass = selected.getAttribute("class");
            } catch (RuntimeException ignored) {
                // best-effort
            }

            logger.info(
                    "Department dropdown state: selectedText='{}', selectedClass='{}', selectValue='{}'",
                    normalizeWhitespace(selectedText),
                    normalizeWhitespace(selectedClass),
                    normalizeWhitespace(selectValue)
            );

            if (ok) {
                logger.info("Department auto-selected as Quality Assurance.");
            }
            return ok;
        } catch (RuntimeException e) {
            logger.warn("qaJobs_isDepartmentAutoSelectedAsQA failed (non-fatal).", e);
            return false;
        }
    }

    public void qaJobs_selectLocationIstanbulTurkiye() {
        String beforeTextSnapshot = readJobsListContainerTextSafe();
        safeSelectByVisibleText(qaJobs_locationSelect, "Istanbul, Turkiye");
        waitForJobsListToRefresh(beforeTextSnapshot, Duration.ofSeconds(4));
        waitForDuration(Duration.ofSeconds(4));
        waitForJobListToBePopulated(Duration.ofSeconds(20));
    }

    public boolean qaJobs_waitForJobCardsLoaded() {
        return waitForJobListToBePopulated(Duration.ofSeconds(25));
    }

    public boolean qaJobs_isJobsListVisible() {
        try {
            WebElement el = waitForVisible(qaJobs_jobsListContainer, Duration.ofSeconds(15));
            boolean visible = isDisplayedSafe(el);
            if (!visible) {
                logger.warn("Jobs list container not visible (locator={})", qaJobs_jobsListContainer);
            }
            return visible;
        } catch (RuntimeException e) {
            logger.warn("qaJobs_isJobsListVisible failed (non-fatal).", e);
            return false;
        }
    }

    public boolean qaJobs_hasJobCards() {
        try {
            waitUntilCountAtLeast(qaJobs_jobCards, 1, Duration.ofSeconds(20));
        } catch (RuntimeException e) {
            logger.debug("waitUntilCountAtLeast for job cards did not succeed (continuing).", e);
        }

        int count = driver.findElements(qaJobs_jobCards).size();
        boolean ok = count > 0;
        if (!ok) {
            logger.warn("No job cards found (count={}) (locator={})", count, qaJobs_jobCards);
        }
        return ok;
    }

    public List<JobPreview> qaJobs_collectValidQAJobsInIstanbul() {
        qaJobs_waitForJobCardsLoaded();

        List<WebElement> cards = driver.findElements(qaJobs_jobCards);
        if (cards.isEmpty()) {
            logger.warn("qaJobs_collectValidQAJobsInIstanbul: no job cards found (locator={}).", qaJobs_jobCards);
            return List.of();
        }

        List<JobPreview> valid = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            WebElement card = cards.get(i);

            String title = normalizeWhitespace(readTextInCard(card, qaJobs_jobTitleInCard));
            String department = normalizeWhitespace(readTextInCard(card, qaJobs_jobDepartmentInCard));
            String location = normalizeWhitespace(readTextInCard(card, qaJobs_jobLocationInCard));
            String href = normalizeWhitespace(readAttributeInCard(card, qaJobs_viewRoleInCard, "href"));

            logger.info(
                    "Card(index={}) title='{}' dept='{}' loc='{}' href='{}'",
                    i,
                    title,
                    department,
                    location,
                    href
            );

            String titleLower = title.toLowerCase(Locale.ROOT);
            String deptLower = department.toLowerCase(Locale.ROOT);
            String locLower = location.toLowerCase(Locale.ROOT);

            boolean matchedTitle = titleLower.contains("quality assurance");
            boolean matchedDept = deptLower.contains("quality assurance");
            boolean matchedLoc = locLower.contains("istanbul")
                    && (locLower.contains("turkey") || locLower.contains("turkiye"));

            if (!(matchedTitle && matchedDept && matchedLoc)) {
                logger.warn(
                        "Skipping card(index={}) because criteria not met. matchedTitle={}, matchedDept={}, matchedLoc={}",
                        i,
                        matchedTitle,
                        matchedDept,
                        matchedLoc
                );
                continue;
            }

            valid.add(new JobPreview(title, department, location, href));
        }

        return valid;
    }

    public JobPreview qaJobs_clickRandomValidViewRoleWithFallback(List<JobPreview> validJobs) {
        if (validJobs == null || validJobs.isEmpty()) {
            logger.warn("qaJobs_clickRandomValidViewRoleWithFallback: validJobs is empty; nothing to click.");
            return null;
        }

        List<JobPreview> shuffled = new ArrayList<>(validJobs);
        Collections.shuffle(shuffled);

        qaJobs_waitForJobCardsLoaded();

        for (int candidateIndex = 0; candidateIndex < shuffled.size(); candidateIndex++) {
            JobPreview candidate = shuffled.get(candidateIndex);
            String candidateHref = normalizeWhitespace(candidate.href());

            try {
                List<WebElement> cards = driver.findElements(qaJobs_jobCards);
                if (cards.isEmpty()) {
                    logger.warn("No job cards found while attempting candidate (candidateIndex={}).", candidateIndex);
                    continue;
                }

                WebElement matchingCard = null;
                for (WebElement card : cards) {
                    String hrefInCard = normalizeWhitespace(readAttributeInCard(card, qaJobs_viewRoleInCard, "href"));
                    if (Objects.equals(hrefInCard, candidateHref)) {
                        matchingCard = card;
                        break;
                    }
                }

                if (matchingCard == null) {
                    logger.warn(
                            "Candidate card not found on page by href; skipping (candidateIndex={}, href='{}').",
                            candidateIndex,
                            candidateHref
                    );
                    continue;
                }

                WebElement viewRole = matchingCard.findElement(qaJobs_viewRoleInCard);

                Set<String> handlesBefore = new HashSet<>(driver.getWindowHandles());

                logger.info(
                        "Attempting View Role click (candidateIndex={}, title='{}', dept='{}', loc='{}', href='{}')",
                        candidateIndex,
                        normalizeWhitespace(candidate.title()),
                        normalizeWhitespace(candidate.department()),
                        normalizeWhitespace(candidate.location()),
                        candidateHref
                );

                safeClick(viewRole);

                boolean opened = false;
                try {
                    opened = shortWait(Duration.ofSeconds(5)).until(d -> {
                        Set<String> now = new HashSet<>(d.getWindowHandles());
                        now.removeAll(handlesBefore);
                        return !now.isEmpty();
                    });
                } catch (TimeoutException e) {
                    opened = false;
                }

                if (opened) {
                    logger.info(
                            "New tab opened successfully for candidate (candidateIndex={}, href='{}').",
                            candidateIndex,
                            candidateHref
                    );
                    return candidate;
                }

                logger.warn(
                        "Click did not open a new tab in time; trying next candidate (candidateIndex={}, href='{}').",
                        candidateIndex,
                        candidateHref
                );
            } catch (ElementClickInterceptedException e) {
                logger.warn(
                        "View Role click intercepted; trying next candidate (candidateIndex={}, href='{}').",
                        candidateIndex,
                        candidateHref,
                        e
                );
            } catch (RuntimeException e) {
                logger.warn(
                        "Candidate click attempt failed; trying next candidate (candidateIndex={}, href='{}').",
                        candidateIndex,
                        candidateHref,
                        e
                );
            }
        }

        logger.error("All valid candidates exhausted; could not open any View Role tab (count={}).", validJobs.size());
        return null;
    }

    private boolean waitForJobListToBePopulated(Duration timeout) {
        try {
            return fluentWait(timeout).until(d -> {
                List<WebElement> cards = d.findElements(qaJobs_jobCards);
                if (cards.isEmpty()) {
                    return false;
                }

                for (WebElement card : cards) {
                    String title = normalizeWhitespace(readTextInCard(card, qaJobs_jobTitleInCard));
                    String department = normalizeWhitespace(readTextInCard(card, qaJobs_jobDepartmentInCard));
                    String location = normalizeWhitespace(readTextInCard(card, qaJobs_jobLocationInCard));

                    if (!title.isBlank() && !department.isBlank() && !location.isBlank()) {
                        return true;
                    }
                }

                return false;
            });
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for job list to populate (timeout={}s).", timeout.getSeconds());
            return false;
        } catch (RuntimeException e) {
            logger.warn("waitForJobListToBePopulated failed (non-fatal).", e);
            return false;
        }
    }

    private void waitForJobsListToRefresh(String beforeTextSnapshot, Duration timeout) {
        try {
            fluentWait(timeout).until(d -> {
                String now = readJobsListContainerTextSafe();
                return !Objects.equals(normalizeWhitespace(now), normalizeWhitespace(beforeTextSnapshot));
            });
        } catch (TimeoutException e) {
            logger.info("Jobs list did not change within settle timeout ({}s). Continuing.", timeout.getSeconds());
        } catch (RuntimeException e) {
            logger.debug("waitForJobsListToRefresh failed (non-fatal). Continuing.", e);
        }
    }

    private void waitForDuration(Duration duration) {
        long startNanos = System.nanoTime();
        try {
            fluentWait(duration).until(d -> {
                long elapsedNanos = System.nanoTime() - startNanos;
                return elapsedNanos >= duration.toNanos();
            });
        } catch (RuntimeException e) {
            logger.debug("waitForDuration failed (non-fatal). Continuing.", e);
        }
    }

    private String readJobsListContainerTextSafe() {
        try {
            return driver.findElement(qaJobs_jobsListContainer).getText();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String normalizeWhitespace(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s+", " ").trim();
    }

    private static boolean isDisplayedSafe(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String readTextInCard(WebElement card, By locator) {
        try {
            String t = card.findElement(locator).getText();
            return t == null ? "" : t.trim();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String readAttributeInCard(WebElement card, By locator, String name) {
        try {
            String v = card.findElement(locator).getAttribute(name);
            return v == null ? "" : v.trim();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private final By qaJobs_locationSelect = By.id("filter-by-location");
    private final By qaJobs_departmentSelect = By.id("filter-by-department");
    private final By qaJobs_jobsListContainer = By.id("jobs-list");
    private final By qaJobs_jobCards = By.cssSelector("#jobs-list .position-list-item");
    private final By qaJobs_jobTitleInCard = By.cssSelector("p.position-title");
    private final By qaJobs_jobDepartmentInCard = By.cssSelector("span.position-department");
    private final By qaJobs_jobLocationInCard = By.cssSelector("div.position-location");
    private final By qaJobs_viewRoleInCard = By.cssSelector("a.btn.btn-navy");
}
