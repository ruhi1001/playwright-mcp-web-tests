package steps;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import utils.WireMockSetup;

public class Hooks {

    @BeforeAll
    public static void globalSetup() {
        WireMockSetup.startServer();
    }

    @Before
    public void beforeEach(Scenario scenario) {
        WireMockSetup.resetAllStubs();
        DriverFactory.getDriver();
    }

    @After
    public void afterEach(Scenario scenario) {
        if (scenario.isFailed()) {
            WebDriver driver = DriverFactory.getDriver();
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Failure Screenshot: " + scenario.getName());
        }
        DriverFactory.quitDriver();
    }

    @AfterAll
    public static void globalTeardown() {
        WireMockSetup.stopServer();
    }
}
