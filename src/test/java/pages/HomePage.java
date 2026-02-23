package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class HomePage extends BasePage {

    private static final String URL = "https://insiderone.com/";
    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        open(URL);
        return this;
    }

    public boolean isNavbarVisible() {
        List<WebElement> elements = driver.findElements(navBar);
        boolean exists = !elements.isEmpty();
        boolean visible = exists && isDisplayedSafe(elements.getFirst());

        if (visible) {
            logger.info("isNavbarVisible ok (locator={})", navBar);
        } else {
            logger.error("isNavbarVisible failed (exists={}, visible={}) (locator={})", exists, visible, navBar);
        }
        return visible;
    }

    public boolean isLogoValid() {
        List<WebElement> elements = driver.findElements(navBarLogo);
        boolean exists = !elements.isEmpty();
        boolean visible = false;
        String href = null;
        boolean hrefContains = false;

        if (exists) {
            WebElement el = elements.getFirst();
            visible = isDisplayedSafe(el);
            href = getAttributeSafe(el, "href");
            hrefContains = href != null && href.contains("insiderone.com");
        }

        boolean ok = exists && visible && hrefContains;
        if (ok) {
            logger.info("isLogoValid ok (href='{}', hrefContains={}) (locator={})", href, hrefContains, navBarLogo);
        } else {
            logger.error(
                    "isLogoValid failed (exists={}, visible={}, href='{}', hrefContains={}) (locator={})",
                    exists,
                    visible,
                    href,
                    hrefContains,
                    navBarLogo
            );
        }
        return ok;
    }

    public boolean isNavbarGetDemoClickable() {
        boolean exists = !driver.findElements(navBarGetDemo).isEmpty();
        boolean clickable = exists && isClickable(navBarGetDemo, Duration.ofSeconds(10));

        if (clickable) {
            logger.info("isNavbarGetDemoClickable ok (locator={})", navBarGetDemo);
        } else {
            logger.error(
                    "isNavbarGetDemoClickable failed (exists={}, clickable={}) (locator={})",
                    exists,
                    clickable,
                    navBarGetDemo
            );
        }
        return clickable;
    }

    public boolean isEmailInputVisible() {
        List<WebElement> elements = driver.findElements(homePageEmailInput);
        boolean exists = !elements.isEmpty();
        boolean visible = false;
        boolean enabled = false;

        if (exists) {
            WebElement el = elements.getFirst();
            visible = isDisplayedSafe(el);
            enabled = isEnabledSafe(el);
        }

        boolean ok = visible && enabled;
        if (ok) {
            logger.info("isEmailInputVisible ok (visible={}, enabled={}) (locator={})", visible, enabled, homePageEmailInput);
        } else {
            logger.error(
                    "isEmailInputVisible failed (exists={}, visible={}, enabled={}) (locator={})",
                    exists,
                    visible,
                    enabled,
                    homePageEmailInput
            );
        }
        return ok;
    }

    public boolean isHeroGetDemoClickable() {
        boolean exists = !driver.findElements(homePageGetDemo).isEmpty();
        boolean clickable = exists && isClickable(homePageGetDemo, Duration.ofSeconds(10));

        if (clickable) {
            logger.info("isHeroGetDemoClickable ok (locator={})", homePageGetDemo);
        } else {
            logger.error(
                    "isHeroGetDemoClickable failed (exists={}, clickable={}) (locator={})",
                    exists,
                    clickable,
                    homePageGetDemo
            );
        }
        return clickable;
    }

    private boolean isClickable(By locator, Duration timeout) {
        try {
            waitForClickable(locator, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean isDisplayedSafe(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean isEnabledSafe(WebElement el) {
        try {
            return el.isEnabled();
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

    private static final By navBar = By.id("navigation");
    private static final By navBarLogo = By.cssSelector("#navigation .header-logo a");
    private static final By navBarGetDemo =
            By.xpath("//header[@id='navigation']//a[contains(normalize-space(.),'Get a demo')]");
    private static final By homePageEmailInput = By.id("email");
    private static final By homePageGetDemo = By.cssSelector("section.homepage-hero form .redirect-button");
}
