package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedbackLogVerifier {

    private static final String FEEDBACK_LOG_API = "http://localhost:8080/api/v1/feedback-logs";
    private static final String AUDIT_LOG_API     = "http://localhost:8080/api/v1/audit/pii-suppression";
    private static final String REJECTION_LOG_API = "http://localhost:8080/api/v1/feedback-logs/rejections";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private FeedbackLogVerifier() {}

    public static void verifyFeedbackLogEntry(Map<String, String> expectedFields) {
        JsonNode logs = fetchLogs(FEEDBACK_LOG_API);
        SoftAssertions soft = new SoftAssertions();

        boolean entryFound = false;
        for (JsonNode entry : logs) {
            boolean matches = expectedFields.entrySet().stream()
                .allMatch(e -> entry.has(e.getKey()) &&
                    entry.get(e.getKey()).asText().equals(e.getValue()));
            if (matches) {
                entryFound = true;
                break;
            }
        }

        soft.assertThat(entryFound)
            .as("Expected feedback log entry with fields %s was not found", expectedFields)
            .isTrue();
        soft.assertAll();
    }

    public static void verifyNoEditFeedbackLogEntry(String suggestionId, String agentId) {
        JsonNode logs = fetchLogs(FEEDBACK_LOG_API);
        for (JsonNode entry : logs) {
            if (entry.has("suggestion_id") &&
                entry.get("suggestion_id").asText().equals(suggestionId) &&
                entry.has("agent_id") &&
                entry.get("agent_id").asText().equals(agentId)) {
                assertThat(entry.get("action").asText())
                    .as("Action for suggestion %s should not be 'edited'", suggestionId)
                    .isNotEqualTo("edited");
            }
        }
    }

    public static void verifyAcceptedOnlyLog(String suggestionId, String agentId) {
        JsonNode logs = fetchLogs(FEEDBACK_LOG_API);
        boolean acceptedFound = false;
        for (JsonNode entry : logs) {
            if (entry.has("suggestion_id") &&
                entry.get("suggestion_id").asText().equals(suggestionId)) {
                assertThat(entry.get("action").asText())
                    .as("Only 'accepted' action should exist for suggestion %s", suggestionId)
                    .isEqualTo("accepted");
                acceptedFound = true;
            }
        }
        assertThat(acceptedFound)
            .as("An 'accepted' log entry should exist for suggestion %s", suggestionId)
            .isTrue();
    }

    public static void verifyRejectionLogCount(String agentId, String threadId, int expectedCount) {
        JsonNode logs = fetchLogs(REJECTION_LOG_API);
        long count = 0;
        for (JsonNode entry : logs) {
            if (entry.has("agent_id") && entry.get("agent_id").asText().equals(agentId) &&
                entry.has("thread_id") && entry.get("thread_id").asText().equals(threadId) &&
                entry.has("action") && entry.get("action").asText().equals("rejected")) {
                count++;
            }
        }
        assertThat(count)
            .as("Expected %d rejection log entries for agent %s on thread %s",
                expectedCount, agentId, threadId)
            .isEqualTo(expectedCount);
    }

    public static void verifyRejectionLogFields(List<String> suggestionIds, String agentId, String threadId) {
        JsonNode logs = fetchLogs(REJECTION_LOG_API);
        for (String suggId : suggestionIds) {
            boolean found = false;
            for (JsonNode entry : logs) {
                if (entry.has("suggestion_id") &&
                    entry.get("suggestion_id").asText().equals(suggId) &&
                    entry.has("agent_id") && entry.get("agent_id").asText().equals(agentId) &&
                    entry.has("thread_id") && entry.get("thread_id").asText().equals(threadId) &&
                    entry.has("action") && entry.get("action").asText().equals("rejected")) {
                    found = true;
                    break;
                }
            }
            assertThat(found)
                .as("Rejection log entry not found for suggestion_id=%s", suggId)
                .isTrue();
        }
    }

    public static void verifyPIIAuditRecord(String agentId, String threadId, String piiType) {
        JsonNode records = fetchLogs(AUDIT_LOG_API);
        boolean found = false;
        for (JsonNode record : records) {
            if (record.has("agent_id") && record.get("agent_id").asText().equals(agentId) &&
                record.has("thread_id") && record.get("thread_id").asText().equals(threadId) &&
                record.has("pii_type") && record.get("pii_type").asText().equals(piiType) &&
                record.has("timestamp")) {
                found = true;
                break;
            }
        }
        assertThat(found)
            .as("PII audit record not found for pii_type=%s, agent=%s, thread=%s",
                piiType, agentId, threadId)
            .isTrue();
    }

    public static void verifyAcceptLogWithConfidence(String suggestionId, double confidence, boolean lowConfidenceFlag) {
        JsonNode logs = fetchLogs(FEEDBACK_LOG_API);
        for (JsonNode entry : logs) {
            if (entry.has("suggestion_id") &&
                entry.get("suggestion_id").asText().equals(suggestionId)) {
                assertThat(entry.get("confidence").asDouble())
                    .as("Confidence score mismatch for suggestion %s", suggestionId)
                    .isEqualTo(confidence, org.assertj.core.data.Offset.offset(0.001));
                assertThat(entry.get("low_confidence_flag").asBoolean())
                    .as("low_confidence_flag mismatch for suggestion %s", suggestionId)
                    .isEqualTo(lowConfidenceFlag);
            }
        }
    }

    private static JsonNode fetchLogs(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch logs from " + endpoint, e);
        }
    }
}
