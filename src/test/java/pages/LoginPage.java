package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverFactory;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String LOGIN_URL = "http://localhost:8080/login";

    private final By usernameField  = By.id("username");
    private final By passwordField  = By.id("password");
    private final By loginButton    = By.id("btn-login");
    private final By loginError     = By.id("login-error-message");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = DriverFactory.getDefaultWait();
    }

    public void navigateTo() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
    }

    public void enterUsername(String username) {
        wait.until(ExpectedConditions.elementToBeClickable(usernameField)).clear();
        driver.findElement(usernameField).sendKeys(username);
    }

    public void enterPassword(String password) {
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).clear();
        driver.findElement(passwordField).sendKeys(password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public void loginAs(String username, String password) {
        navigateTo();
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public void loginAsAgent(String agentId) {
        loginAs(agentId, "agent-password");
    }

    public boolean isLoginErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginError)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
