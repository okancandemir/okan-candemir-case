package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LeverJobPage extends BasePage {

    public LeverJobPage(WebDriver driver) {
        super(driver);
    }

    public boolean lever_isAt() {
        String url = driver.getCurrentUrl();
        boolean ok = url != null && url.contains("jobs.lever.co");
        if (!ok) {
            logger.warn("lever_isAt failed (currentUrl='{}')", url);
        }
        return ok;
    }

    public String lever_getTitle() {
        return getText(lever_title);
    }

    public String lever_getLocation() {
        return getText(lever_location);
    }

    public String lever_getDepartment() {
        return getText(lever_department);
    }

    private final By lever_title = By.cssSelector(".posting-headline h2");
    private final By lever_location = By.cssSelector(".posting-categories .location");
    private final By lever_department = By.cssSelector(".posting-categories .department");
}

