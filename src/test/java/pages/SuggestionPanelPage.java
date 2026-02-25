package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestionPanelPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait shortWait;

    private final By suggestionPanel            = By.id("ai-suggestion-panel");
    private final By allSuggestionCards         = By.cssSelector(".ai-suggestion-card");
    private final By lowConfidenceIndicator     = By.cssSelector(".low-confidence-indicator");
    private final By unsupportedLangNotice      = By.id("unsupported-language-notice");

    private static final int SUGGESTION_TIMEOUT_MS = 2000;

    public SuggestionPanelPage(WebDriver driver) {
        this.driver    = driver;
        this.wait      = DriverFactory.getDefaultWait();
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
    }

    // ── Panel state ──────────────────────────────────────────────────────────

    public boolean isPanelVisible() {
        try {
            WebElement panel = driver.findElement(suggestionPanel);
            return panel.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isPanelHidden() {
        List<WebElement> panels = driver.findElements(suggestionPanel);
        if (panels.isEmpty()) return true;
        return !panels.get(0).isDisplayed();
    }

    // ── Suggestion count ─────────────────────────────────────────────────────

    public int getVisibleSuggestionCount() {
        try {
            shortWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(allSuggestionCards));
        } catch (Exception e) {
            return 0;
        }
        return (int) driver.findElements(allSuggestionCards).stream()
            .filter(WebElement::isDisplayed)
            .count();
    }

    public int waitForSuggestionsAndGetCount(long threadOpenTimestamp) {
        try {
            shortWait.until(d -> !d.findElements(allSuggestionCards).isEmpty()
                || isPanelHidden());
        } catch (Exception e) {
            // panel may be hidden due to failure
        }
        return getVisibleSuggestionCount();
    }

    // ── Suggestion retrieval ─────────────────────────────────────────────────

    public List<String> getAllSuggestionTexts() {
        return driver.findElements(allSuggestionCards).stream()
            .filter(WebElement::isDisplayed)
            .map(card -> card.findElement(By.cssSelector(".suggestion-text")).getText())
            .collect(Collectors.toList());
    }

    public String getSuggestionTextById(String suggestionId) {
        WebElement card = getSuggestionCardById(suggestionId);
        return card.findElement(By.cssSelector(".suggestion-text")).getText();
    }

    public boolean isSuggestionVisible(String suggestionId) {
        try {
            return getSuggestionCardById(suggestionId).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ── Accept / Reject actions ──────────────────────────────────────────────

    public void acceptSuggestion(String suggestionId) {
        WebElement card = getSuggestionCardById(suggestionId);
        WebElement acceptButton = card.findElement(By.cssSelector("[data-action='accept']"));
        wait.until(ExpectedConditions.elementToBeClickable(acceptButton)).click();
    }

    public void rejectSuggestion(String suggestionId) {
        WebElement card = getSuggestionCardById(suggestionId);
        WebElement rejectButton = card.findElement(By.cssSelector("[data-action='reject']"));
        wait.until(ExpectedConditions.elementToBeClickable(rejectButton)).click();
    }

    public void rejectAllVisibleSuggestions() {
        List<WebElement> cards = driver.findElements(allSuggestionCards).stream()
            .filter(WebElement::isDisplayed)
            .collect(Collectors.toList());
        for (WebElement card : cards) {
            WebElement rejectButton = card.findElement(By.cssSelector("[data-action='reject']"));
            wait.until(ExpectedConditions.elementToBeClickable(rejectButton)).click();
        }
    }

    // ── Confidence indicator ─────────────────────────────────────────────────

    public boolean isLowConfidenceIndicatorVisible(String suggestionId) {
        WebElement card = getSuggestionCardById(suggestionId);
        try {
            return card.findElement(lowConfidenceIndicator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isLowConfidenceIndicatorAbsent(String suggestionId) {
        WebElement card = getSuggestionCardById(suggestionId);
        return card.findElements(lowConfidenceIndicator).isEmpty();
    }

    // ── Language support ─────────────────────────────────────────────────────

    public boolean isUnsupportedLanguageNoticeVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(unsupportedLangNotice)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Timing validation ────────────────────────────────────────────────────

    public long measureSuggestionRenderTime(long threadOpenTimestamp) {
        try {
            shortWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(allSuggestionCards));
        } catch (Exception ignored) {}
        return System.currentTimeMillis() - threadOpenTimestamp;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private WebElement getSuggestionCardById(String suggestionId) {
        By locator = By.cssSelector("[data-suggestion-id='" + suggestionId + "']");
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
}
