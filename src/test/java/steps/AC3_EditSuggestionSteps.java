package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.DataTable;
import org.openqa.selenium.WebDriver;
import pages.MessageThreadPage;
import pages.SuggestionPanelPage;
import utils.DriverFactory;
import utils.FeedbackLogVerifier;
import utils.ScenarioContext;
import utils.WireMockSetup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AC3_EditSuggestionSteps {

    private final ScenarioContext context;
    private final WebDriver driver;
    private final MessageThreadPage threadPage;
    private final SuggestionPanelPage suggestionPanel;

    public AC3_EditSuggestionSteps(ScenarioContext context) {
        this.context        = context;
        this.driver         = DriverFactory.getDriver();
        this.threadPage     = new MessageThreadPage(driver);
        this.suggestionPanel = new SuggestionPanelPage(driver);
    }

    @Given("suggestion {string} with text {string} has been accepted into the reply box")
    public void suggestionAcceptedIntoReplyBox(String suggId, String originalText) {
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        WireMockSetup.stubNamedSuggestion(threadId, suggId, originalText, 0.90);
        threadPage.openThread(threadId);
        suggestionPanel.acceptSuggestion(suggId);
        context.set(ScenarioContext.ACCEPTED_SUGGESTION_ID, suggId);
        context.set("suggestion.original.text", originalText);
    }

    @When("the agent changes the reply box content to {string}")
    public void agentChangesReplyBoxContent(String newText) {
        threadPage.typeInReplyBox(newText);
        context.set("edited.reply.text", newText);
    }

    @When("the agent appends a trailing space to make the content {string}")
    public void agentAppendsTrailingSpace(String expectedContent) {
        threadPage.appendToReplyBox(" ");
        context.set("edited.reply.text", expectedContent);
    }

    @When("the agent clicks Send")
    public void agentClicksSend() {
        threadPage.clickSend();
        context.set(ScenarioContext.MESSAGE_SENT_FLAG, true);
    }

    @Then("the message {string} SHALL be delivered to the customer")
    public void messageDeliveredToCustomer(String expectedText) {
        assertThat(context.getAs(ScenarioContext.MESSAGE_SENT_FLAG, Boolean.class))
            .as("Message should have been sent")
            .isTrue();
        assertThat(threadPage.isMessageInThreadHistory(expectedText))
            .as("Message '%s' should appear in thread history", expectedText)
            .isTrue();
    }

    @And("a feedback log entry SHALL be created containing:")
    public void feedbackLogEntryCreatedWithFields(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedFields = new HashMap<>();
        for (Map<String, String> row : rows) {
            expectedFields.put(row.get("field"), row.get("value"));
        }
        FeedbackLogVerifier.verifyFeedbackLogEntry(expectedFields);
    }

    @Then("a feedback log entry SHALL be created with edited_text = {string} and action = {string}")
    public void feedbackLogEntryWithEditedTextAndAction(String editedText, String action) {
        String agentId  = context.getAs(ScenarioContext.ACTIVE_AGENT_ID, String.class);
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        String suggId   = context.getAs(ScenarioContext.ACCEPTED_SUGGESTION_ID, String.class);

        Map<String, String> expectedFields = Map.of(
            "edited_text",    editedText,
            "action",         action,
            "agent_id",       agentId,
            "thread_id",      threadId,
            "suggestion_id",  suggId
        );
        FeedbackLogVerifier.verifyFeedbackLogEntry(expectedFields);
    }

    @When("the agent clicks Send without modifying the reply box")
    public void agentClicksSendWithoutModifyingBox() {
        threadPage.clickSend();
        context.set(ScenarioContext.MESSAGE_SENT_FLAG, true);
    }

    @Then("NO edit feedback log entry SHALL be created for this action")
    public void noEditFeedbackLogEntryCreated() {
        String suggId  = context.getAs(ScenarioContext.ACCEPTED_SUGGESTION_ID, String.class);
        String agentId = context.getAs(ScenarioContext.ACTIVE_AGENT_ID, String.class);
        FeedbackLogVerifier.verifyNoEditFeedbackLogEntry(suggId, agentId);
    }

    @And("the action SHALL be logged as {string} only")
    public void actionLoggedAs(String expectedAction) {
        String suggId  = context.getAs(ScenarioContext.ACCEPTED_SUGGESTION_ID, String.class);
        String agentId = context.getAs(ScenarioContext.ACTIVE_AGENT_ID, String.class);
        if ("accepted".equals(expectedAction)) {
            FeedbackLogVerifier.verifyAcceptedOnlyLog(suggId, agentId);
        }
    }
}
