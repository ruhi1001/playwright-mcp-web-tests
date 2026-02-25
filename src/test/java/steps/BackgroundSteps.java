package steps;

import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import pages.LoginPage;
import pages.SupportPortalPage;
import utils.DriverFactory;
import utils.ScenarioContext;
import utils.WireMockSetup;

import static org.assertj.core.api.Assertions.assertThat;

public class BackgroundSteps {

    private final ScenarioContext context;
    private final WebDriver driver;

    public BackgroundSteps(ScenarioContext context) {
        this.context = context;
        this.driver  = DriverFactory.getDriver();
    }

    @Given("the Customer Support Portal is running and accessible")
    public void customerSupportPortalIsRunning() {
        SupportPortalPage portalPage = new SupportPortalPage(driver);
        portalPage.navigateToPortal();
        assertThat(portalPage.isPortalAccessible())
            .as("Customer Support Portal should be accessible")
            .isTrue();
    }

    @Given("the AI suggestion service endpoint is available")
    public void aiSuggestionServiceIsAvailable() {
        WireMockSetup.stubValidSuggestions("health-check", 1);
    }

    @Given("the feature flag for AI suggestions is enabled")
    public void featureFlagForAISuggestionsIsEnabled() {
        context.set(ScenarioContext.FEATURE_FLAG_STATE, true);
    }

    @Given("an authenticated Customer Support Agent with id {string} is logged in")
    public void authenticatedAgentIsLoggedIn(String agentId) {
        context.set(ScenarioContext.ACTIVE_AGENT_ID, agentId);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAsAgent(agentId);
    }

    @Given("a customer message has been received in the support portal for thread {string}")
    public void customerMessageReceivedForThread(String threadId) {
        context.set(ScenarioContext.ACTIVE_THREAD_ID, threadId);
    }
}
