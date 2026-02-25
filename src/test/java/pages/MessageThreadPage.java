package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverFactory;

import java.time.Duration;
import java.util.List;

public class MessageThreadPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String THREAD_URL_TEMPLATE = "http://localhost:8080/support/threads/%s";

    private final By replyBoxLocator       = By.id("reply-box");
    private final By sendButtonLocator     = By.id("btn-send-reply");
    private final By threadHistoryLocator  = By.id("thread-history");
    private final By threadMessagesLocator = By.cssSelector("#thread-history .message-item");
    private final By loadingIndicator      = By.id("suggestions-loading-indicator");
    private final By manualNoticeLocator   = By.id("suggestions-unavailable-notice");
    private final By degradationNotice     = By.id("suggestions-degradation-notice");

    public MessageThreadPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = DriverFactory.getDefaultWait();
    }

    public long openThread(String threadId) {
        driver.get(String.format(THREAD_URL_TEMPLATE, threadId));
        wait.until(ExpectedConditions.visibilityOfElementLocated(replyBoxLocator));
        return System.currentTimeMillis();
    }

    public void typeInReplyBox(String text) {
        WebElement replyBox = wait.until(ExpectedConditions.elementToBeClickable(replyBoxLocator));
        replyBox.clear();
        replyBox.sendKeys(text);
    }

    public void appendToReplyBox(String text) {
        WebElement replyBox = wait.until(ExpectedConditions.elementToBeClickable(replyBoxLocator));
        replyBox.sendKeys(text);
    }

    public void clearReplyBox() {
        wait.until(ExpectedConditions.elementToBeClickable(replyBoxLocator)).clear();
    }

    public void clickSend() {
        wait.until(ExpectedConditions.elementToBeClickable(sendButtonLocator)).click();
    }

    public String getReplyBoxContent() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(replyBoxLocator))
            .getAttribute("value");
    }

    public boolean isReplyBoxEditable() {
        WebElement replyBox = wait.until(ExpectedConditions.visibilityOfElementLocated(replyBoxLocator));
        return replyBox.isEnabled() && !Boolean.parseBoolean(replyBox.getAttribute("readonly"));
    }

    public boolean isReplyBoxEmpty() {
        return getReplyBoxContent().isEmpty();
    }

    public boolean isMessageInThreadHistory(String messageText) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(threadHistoryLocator));
        List<WebElement> messages = driver.findElements(threadMessagesLocator);
        return messages.stream()
            .anyMatch(m -> m.getText().contains(messageText));
    }

    public boolean isLoadingIndicatorVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loadingIndicator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuggestionsUnavailableNoticeVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(manualNoticeLocator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDegradationNoticeVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(degradationNotice)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBlockingOverlayAbsent() {
        return driver.findElements(By.id("blocking-error-modal")).isEmpty() &&
               driver.findElements(By.id("error-overlay")).isEmpty();
    }

    public boolean isSendButtonEnabled() {
        try {
            WebElement send = driver.findElement(sendButtonLocator);
            return send.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public int getThreadHistoryMessageCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(threadHistoryLocator));
        return driver.findElements(threadMessagesLocator).size();
    }
}
