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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AC1_SuggestionDisplaySteps {

    private final ScenarioContext context;
    private final WebDriver driver;
    private final MessageThreadPage threadPage;
    private final SuggestionPanelPage suggestionPanel;

    public AC1_SuggestionDisplaySteps(ScenarioContext context) {
        this.context        = context;
        this.driver         = DriverFactory.getDriver();
        this.threadPage     = new MessageThreadPage(driver);
        this.suggestionPanel = new SuggestionPanelPage(driver);
    }

    @Given("the AI service will return valid suggestions for thread {string}")
    public void aiServiceWillReturnValidSuggestions(String threadId) {
        WireMockSetup.stubValidSuggestions(threadId, 3);
    }

    @Given("the AI service returns exactly {int} suggestion(s) for thread {string}")
    public void aiServiceReturnsExactlySuggestions(int count, String threadId) {
        WireMockSetup.stubValidSuggestions(threadId, count);
        context.set(ScenarioContext.AI_SERVICE_STUB_COUNT, count);
    }

    @Given("the AI service returns an empty suggestion list for thread {string}")
    public void aiServiceReturnsEmptySuggestions(String threadId) {
        WireMockSetup.stubEmptySuggestions(threadId);
        context.set(ScenarioContext.AI_SERVICE_STUB_COUNT, 0);
    }

    @Given("the AI service returns {int} suggestions for thread {string}")
    public void aiServiceReturnsNSuggestions(int count, String threadId) {
        WireMockSetup.stubValidSuggestions(threadId, count);
        context.set(ScenarioContext.AI_SERVICE_STUB_COUNT, count);
    }

    @Given("the AI service will return suggestions in under 2 seconds")
    public void aiServiceWillReturnSuggestionsUnder2Seconds() {
        String threadId = context.getAs(ScenarioContext.ACTIVE_THREAD_ID, String.class);
        WireMockSetup.stubValidSuggestions(threadId, 2);
    }

    @When("the agent opens message thread {string}")
    public void agentOpensMessageThread(String threadId) {
        context.set(ScenarioContext.ACTIVE_THREAD_ID, threadId);
        long openTimestamp = threadPage.openThread(threadId);
        context.set(ScenarioContext.THREAD_OPEN_TIMESTAMP, openTimestamp);
    }

    @Then("between 1 and 3 AI-generated suggestions SHALL be displayed")
    public void between1And3SuggestionsDisplayed() {
        long openTimestamp = context.getAs(ScenarioContext.THREAD_OPEN_TIMESTAMP, Long.class);
        int count = suggestionPanel.waitForSuggestionsAndGetCount(openTimestamp);
        assertThat(count)
            .as("Suggestion count should be between 1 and 3")
            .isBetween(1, 3);
    }

    @And("all suggestions SHALL appear within 2 seconds from the thread open event")
    public void allSuggestionsAppearWithin2Seconds() {
        long openTimestamp = context.getAs(ScenarioContext.THREAD_OPEN_TIMESTAMP, Long.class);
        long elapsedMs = suggestionPanel.measureSuggestionRenderTime(openTimestamp);
        assertThat(elapsedMs)
            .as("Suggestions should appear within 2000ms, but took %dms", elapsedMs)
            .isLessThanOrEqualTo(2000L);
    }

    @Then("exactly {int} AI suggestion(s) SHALL be displayed within 2 seconds")
    public void exactlyNSuggestionsDisplayedWithin2Seconds(int expectedCount) {
        long openTimestamp = context.getAs(ScenarioContext.THREAD_OPEN_TIMESTAMP, Long.class);
        int count = suggestionPanel.waitForSuggestionsAndGetCount(openTimestamp);
        long elapsedMs = suggestionPanel.measureSuggestionRenderTime(openTimestamp);
        assertThat(count).as("Displayed suggestion count").isEqualTo(expectedCount);
        assertThat(elapsedMs).as("Render time in ms").isLessThanOrEqualTo(2000L);
    }

    @And("the suggestion SHALL contain non-empty response text")
    public void suggestionShallContainNonEmptyText() {
        List<String> texts = suggestionPanel.getAllSuggestionTexts();
        assertThat(texts).as("Suggestion texts should not be empty").isNotEmpty();
        texts.forEach(text -> assertThat(text).as("Each suggestion text should not be blank").isNotBlank());
    }

    @And("each suggestion SHALL contain distinct non-empty response text")
    public void eachSuggestionShallContainDistinctNonEmptyText() {
        List<String> texts = suggestionPanel.getAllSuggestionTexts();
        assertThat(texts).as("Suggestions list").isNotEmpty();
        texts.forEach(text -> assertThat(text).as("Suggestion text should not be blank").isNotBlank());
        assertThat(texts).as("All suggestion texts should be distinct").doesNotHaveDuplicates();
    }

    @Then("no suggestions SHALL be displayed")
    public void noSuggestionsDisplayed() {
        assertThat(suggestionPanel.getVisibleSuggestionCount())
            .as("No suggestions should be visible")
            .isZero();
    }

    @And("the suggestion panel SHALL be hidden from the agent view")
    public void suggestionPanelHidden() {
        assertThat(suggestionPanel.isPanelHidden())
            .as("Suggestion panel should be hidden")
            .isTrue();
    }

    @And("the reply box SHALL remain editable for manual input")
    public void replyBoxEditableForManualInput() {
        assertThat(threadPage.isReplyBoxEditable())
            .as("Reply box should be editable")
            .isTrue();
    }

    @Then("at most 3 AI suggestions SHALL be displayed")
    public void atMost3SuggestionsDisplayed() {
        long openTimestamp = context.getAs(ScenarioContext.THREAD_OPEN_TIMESTAMP, Long.class);
        int count = suggestionPanel.waitForSuggestionsAndGetCount(openTimestamp);
        assertThat(count)
            .as("At most 3 suggestions should be visible")
            .isLessThanOrEqualTo(3);
    }

    @And("no more than 3 suggestions SHALL be visible to the agent at any time")
    public void noMoreThan3SuggestionsVisible() {
        assertThat(suggestionPanel.getVisibleSuggestionCount())
            .as("Visible suggestion count should not exceed 3")
            .isLessThanOrEqualTo(3);
    }

    @Then("the elapsed time from thread open to first suggestion rendered SHALL be less than 2000 milliseconds")
    public void elapsedTimeLessThan2000Ms() {
        long openTimestamp = context.getAs(ScenarioContext.THREAD_OPEN_TIMESTAMP, Long.class);
        long elapsed = suggestionPanel.measureSuggestionRenderTime(openTimestamp);
        assertThat(elapsed)
            .as("Elapsed time from thread open to suggestion render should be < 2000ms, was %dms", elapsed)
            .isLessThan(2000L);
    }
}
