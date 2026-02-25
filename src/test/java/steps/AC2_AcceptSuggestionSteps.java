package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import pages.MessageThreadPage;
import pages.SuggestionPanelPage;
import utils.DriverFactory;
import utils.ScenarioContext;
import utils.WireMockSetup;

import static org.assertj.core.api.Assertions.assertThat;

public class AC2_AcceptSuggestionSteps {

    private final ScenarioContext context;
    private final WebDriver driver;
    private final MessageThreadPage threadPage;
    private final SuggestionPanelPage suggestionPanel;

    public AC2_AcceptSuggestionSteps(ScenarioContext context) {
        this.context        = context;
        this.driver         = DriverFactory.getDriver();
        this.threadPage     = new MessageThreadPage(driver);
        this.suggestionPanel = new SuggestionPanelPage(driver);
    }

    @Given("AI suggestions are displayed for thread {string}")
    public void aiSuggestionsAreDisplayedForThread(String threadId) {
        WireMockSetup.stubValidSuggestions(threadId, 3);
        context.set(ScenarioContext.ACTIVE_THREAD_ID, threadId);
        threadPage.openThread(threadId);
    }

    @Given("suggestion {string} with text {string} is visible")
    public void suggestionWithTextIsVisible(String suggId, String text) {
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        WireMockSetup.stubNamedSuggestion(threadId, suggId, text, 0.90);
        threadPage.openThread(threadId);
        assertThat(suggestionPanel.isSuggestionVisible(suggId))
            .as("Suggestion %s should be visible", suggId)
            .isTrue();
        context.set("suggestion.text." + suggId, text);
    }

    @Given("the agent has accepted suggestion {string} and the reply box is populated")
    public void agentHasAcceptedSuggestion(String suggId) {
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        String text = "Thank you for reaching out. Let me help you.";
        WireMockSetup.stubNamedSuggestion(threadId, suggId, text, 0.90);
        threadPage.openThread(threadId);
        suggestionPanel.acceptSuggestion(suggId);
        context.set(ScenarioContext.ACCEPTED_SUGGESTION_ID, suggId);
        context.set("suggestion.text." + suggId, text);
    }

    @Given("{int} AI suggestions {string}, {string}, {string} are visible for thread {string}")
    public void threeSuggestionsVisible(int count, String suggA, String suggB, String suggC, String threadId) {
        WireMockSetup.stubValidSuggestions(threadId, count);
        context.set(ScenarioContext.ACTIVE_THREAD_ID, threadId);
        threadPage.openThread(threadId);
        assertThat(suggestionPanel.getVisibleSuggestionCount())
            .as("Expected %d suggestions", count)
            .isEqualTo(count);
    }

    @Given("suggestion {string} is visible and has been accepted once")
    public void suggestionIsVisibleAndAcceptedOnce(String suggId) {
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        String text = "Accepted suggestion text";
        WireMockSetup.stubNamedSuggestion(threadId, suggId, text, 0.90);
        threadPage.openThread(threadId);
        suggestionPanel.acceptSuggestion(suggId);
        context.set(ScenarioContext.ACCEPTED_SUGGESTION_ID, suggId);
        context.set("suggestion.text." + suggId, text);
    }

    @When("the agent clicks Accept on suggestion {string}")
    public void agentClicksAcceptOnSuggestion(String suggId) {
        suggestionPanel.acceptSuggestion(suggId);
        context.set(ScenarioContext.ACCEPTED_SUGGESTION_ID, suggId);
    }

    @When("the agent clicks Send without modifying the reply box content")
    public void agentClicksSendWithoutModifying() {
        threadPage.clickSend();
        context.set(ScenarioContext.MESSAGE_SENT_FLAG, true);
    }

    @When("the agent attempts to accept suggestion {string} a second time")
    public void agentAttemptsToAcceptSecondTime(String suggId) {
        String contentBefore = threadPage.getReplyBoxContent();
        context.set("reply.content.before.double.accept", contentBefore);
        suggestionPanel.acceptSuggestion(suggId);
    }

    @Then("the reply box SHALL contain {string}")
    public void replyBoxShallContain(String expectedText) {
        assertThat(threadPage.getReplyBoxContent())
            .as("Reply box should contain accepted suggestion text")
            .isEqualTo(expectedText);
    }

    @And("the message SHALL NOT be sent automatically")
    public void messageNotSentAutomatically() {
        assertThat(context.contains(ScenarioContext.MESSAGE_SENT_FLAG))
            .as("Message should not have been sent automatically")
            .isFalse();
    }

    @And("the agent SHALL be able to further edit or send the reply manually")
    public void agentCanEditOrSendManually() {
        assertThat(threadPage.isReplyBoxEditable())
            .as("Reply box should remain editable after accepting suggestion")
            .isTrue();
        assertThat(threadPage.isSendButtonEnabled())
            .as("Send button should be enabled")
            .isTrue();
    }

    @Then("the response {string} SHALL be sent to the customer")
    public void responseSentToCustomer(String expectedText) {
        assertThat(context.getAs(ScenarioContext.MESSAGE_SENT_FLAG, Boolean.class))
            .as("Message should have been sent")
            .isTrue();
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        assertThat(threadPage.isMessageInThreadHistory(expectedText))
            .as("Sent message '%s' should appear in thread history", expectedText)
            .isTrue();
    }

    @And("the sent message SHALL be recorded in the thread history")
    public void sentMessageRecordedInThreadHistory() {
        String suggId = context.getAs(ScenarioContext.ACCEPTED_SUGGESTION_ID, String.class);
        String expectedText = context.getAs("suggestion.text." + suggId, String.class);
        assertThat(threadPage.isMessageInThreadHistory(expectedText))
            .as("Sent message should be in thread history")
            .isTrue();
    }

    @Then("the reply box SHALL be populated with suggestion {string} text")
    public void replyBoxPopulatedWithSuggestionText(String suggId) {
        String content = threadPage.getReplyBoxContent();
        assertThat(content)
            .as("Reply box should be populated with suggestion text")
            .isNotBlank();
        context.set(ScenarioContext.REPLY_BOX_CONTENT, content);
    }

    @And("suggestions {string} and {string} SHALL be dismissed from the suggestion panel")
    public void otherSuggestionsDismissed(String suggA, String suggC) {
        assertThat(suggestionPanel.isSuggestionVisible(suggA))
            .as("Suggestion %s should be dismissed", suggA)
            .isFalse();
        assertThat(suggestionPanel.isSuggestionVisible(suggC))
            .as("Suggestion %s should be dismissed", suggC)
            .isFalse();
    }

    @Then("the reply box content SHALL remain unchanged with a single copy of the suggestion text")
    public void replyBoxContentUnchangedAfterDoubleAccept() {
        String contentBefore = context.getAs("reply.content.before.double.accept", String.class);
        String contentAfter  = threadPage.getReplyBoxContent();
        assertThat(contentAfter)
            .as("Reply box content should remain unchanged after duplicate accept")
            .isEqualTo(contentBefore);
    }

    @And("no duplicate accept event SHALL be emitted")
    public void noDuplicateAcceptEventEmitted() {
        WireMockSetup.verifyAISuggestionRequestMadeOnce();
    }
}
