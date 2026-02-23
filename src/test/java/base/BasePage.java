package base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public abstract class BasePage {

    protected static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);

    protected final WebDriver driver;
    protected final Logger logger;
    protected final WebDriverWait wait;

    protected final By cookieBanner = By.id("wt-cli-cookie-banner");
    protected final By cookieAcceptBtn = By.id("wt-cli-accept-all-btn");
    protected final By marketingPopupClose = By.xpath("//*[starts-with(@id,'close-button-')]");

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.logger = LoggerFactory.getLogger(getClass());
        this.wait = new WebDriverWait(driver, DEFAULT_WAIT);
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(NoSuchElementException.class);
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    protected void open(String url) {
        logger.info("Navigate: {}", url);
        driver.get(url);
        waitForDocumentReady(Duration.ofSeconds(20));
    }

    protected void acceptCookiesIfPresent() {
        try {
            List<WebElement> banners = driver.findElements(cookieBanner);
            if (banners.isEmpty()) {
                logger.debug("Cookie banner not present.");
                return;
            }

            WebElement banner = banners.getFirst();
            if (!banner.isDisplayed()) {
                logger.debug("Cookie banner present but not displayed.");
                return;
            }

            WebDriverWait shortWait = shortWait(Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.elementToBeClickable(cookieAcceptBtn)).click();
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookieBanner));
            logger.info("Accepted cookies (cookie banner closed).");
        } catch (RuntimeException e) {
            logger.warn("acceptCookiesIfPresent failed (non-fatal).", e);
        }
    }

    protected void closeMarketingPopupIfPresentShort() {
        try {
            List<WebElement> closeButtons = driver.findElements(marketingPopupClose);
            if (closeButtons.isEmpty()) {
                return;
            }

            WebElement closeBtn = closeButtons.getFirst();
            if (!closeBtn.isDisplayed()) {
                return;
            }

            WebDriverWait shortWait = shortWait(Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.elementToBeClickable(marketingPopupClose)).click();
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(marketingPopupClose));
            logger.info("Closed marketing popup.");
        } catch (RuntimeException e) {
            logger.warn("closeMarketingPopupIfPresentShort failed (non-fatal).", e);
        }
    }

    protected void beforeActionGuards() {
        acceptCookiesIfPresent();
        closeMarketingPopupIfPresentShort();
    }

    protected FluentWait<WebDriver> fluentWait(Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class);
    }

    protected WebDriverWait shortWait(Duration timeout) {
        WebDriverWait shortWait = new WebDriverWait(driver, timeout);
        shortWait.pollingEvery(Duration.ofMillis(200));
        shortWait.ignoring(NoSuchElementException.class);
        shortWait.ignoring(StaleElementReferenceException.class);
        return shortWait;
    }

    protected WebElement waitForVisible(By locator, Duration timeout) {
        logger.debug("Wait visible: {}", locator);
        return fluentWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator, Duration timeout) {
        logger.debug("Wait clickable: {}", locator);
        return fluentWait(timeout).until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void safeClick(By locator) {
        beforeActionGuards();
        logger.info("Safe click: {}", locator);
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            scrollIntoView(el);
            el.click();
        } catch (ElementClickInterceptedException e) {
            logger.warn("Click intercepted, retrying once: {}", locator, e);
            closeMarketingPopupIfPresentShort();
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                scrollIntoView(el);
                el.click();
            } catch (RuntimeException retryException) {
                logger.error("Safe click failed after retry: {}", locator, retryException);
                throw retryException;
            }
        } catch (RuntimeException e) {
            logger.error("Safe click failed: {}", locator, e);
            throw e;
        }
    }

    protected void safeClick(WebElement el) {
        beforeActionGuards();
        logger.info("Safe click: WebElement");
        try {
            WebElement clickable = wait.until(ExpectedConditions.elementToBeClickable(el));
            scrollIntoView(clickable);
            clickable.click();
        } catch (ElementClickInterceptedException e) {
            logger.warn("Click intercepted, retrying once: WebElement", e);
            closeMarketingPopupIfPresentShort();
            try {
                WebElement clickable = wait.until(ExpectedConditions.elementToBeClickable(el));
                scrollIntoView(clickable);
                clickable.click();
            } catch (RuntimeException retryException) {
                logger.error("Safe click failed after retry: WebElement", retryException);
                throw retryException;
            }
        } catch (RuntimeException e) {
            logger.error("Safe click failed: WebElement", e);
            throw e;
        }
    }

    protected void safeSelectByVisibleText(By selectLocator, String text) {
        Objects.requireNonNull(text, "text");
        beforeActionGuards();
        logger.info("Safe select by visible text: {} -> {}", selectLocator, text);

        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(selectLocator));
            scrollIntoView(el);
            new Select(el).selectByVisibleText(text);
        } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
            logger.warn("Select failed ({}), retrying once: {} -> {}", e.getClass().getSimpleName(), selectLocator, text, e);
            closeMarketingPopupIfPresentShort();
            try {
                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(selectLocator));
                scrollIntoView(el);
                new Select(el).selectByVisibleText(text);
            } catch (RuntimeException retryException) {
                logger.error("Safe select failed after retry: {} -> {}", selectLocator, text, retryException);
                throw retryException;
            }
        } catch (RuntimeException e) {
            logger.error("Safe select failed: {} -> {}", selectLocator, text, e);
            throw e;
        }
    }

    protected String getText(By locator) {
        return getText(locator, Duration.ofSeconds(15));
    }

    protected String getText(By locator, Duration timeout) {
        WebElement el = waitForVisible(locator, timeout);
        String text = el.getText();
        return text == null ? "" : text.trim();
    }

    protected void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", el);
        } catch (JavascriptException ignored) {
            // best-effort
        }
    }

    protected void waitForDocumentReady(Duration timeout) {
        logger.debug("Wait document.readyState=complete");
        fluentWait(timeout).until(d -> {
            Object state = ((JavascriptExecutor) d).executeScript("return document.readyState");
            return "complete".equals(state);
        });
    }

    protected void waitUntilCountAtLeast(By locator, int minCount, Duration timeout) {
        logger.info("Wait elements count >= {} : {}", minCount, locator);
        fluentWait(timeout).until(d -> d.findElements(locator).size() >= minCount);
    }

}
