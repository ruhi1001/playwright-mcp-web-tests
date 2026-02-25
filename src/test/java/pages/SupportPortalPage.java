package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverFactory;

public class SupportPortalPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String PORTAL_BASE_URL = "http://localhost:8080/support";

    private final By portalHeader         = By.id("support-portal-header");
    private final By agentDashboard       = By.id("agent-dashboard");
    private final By threadListContainer  = By.id("thread-list-container");
    private final By emailTicketContainer = By.id("email-ticket-container");
    private final By accessDeniedMessage  = By.id("access-denied-message");
    private final By loginRedirectElement = By.id("login-form");

    public SupportPortalPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = DriverFactory.getDefaultWait();
    }

    public void navigateToPortal() {
        driver.get(PORTAL_BASE_URL);
    }

    public boolean isPortalAccessible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(portalHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDashboardVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(agentDashboard)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessDenied() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(accessDeniedMessage)).isDisplayed();
        } catch (Exception e) {
            return isLoginRedirected();
        }
    }

    public boolean isLoginRedirected() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginRedirectElement)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void openEmailTicket(String ticketId) {
        driver.get(PORTAL_BASE_URL + "/email/" + ticketId);
    }

    public boolean isEmailTicketSuggestionPanelAbsent() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(emailTicketContainer));
            return driver.findElements(By.id("ai-suggestion-panel")).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isThreadListVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(threadListContainer)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
