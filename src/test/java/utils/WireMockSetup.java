package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockSetup {

    private static final int AI_SERVICE_PORT = 9090;
    private static final String AI_SUGGESTIONS_PATH = "/api/v1/ai/suggestions";
    private static WireMockServer wireMockServer;

    private WireMockSetup() {}

    public static void startServer() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(AI_SERVICE_PORT)
            );
            wireMockServer.start();
            WireMock.configureFor("localhost", AI_SERVICE_PORT);
        }
    }

    public static void stopServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public static void resetAllStubs() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
    }

    // ── Stub builders ────────────────────────────────────────────────────────

    public static void stubValidSuggestions(String threadId, int count) {
        List<String> suggestions = IntStream.rangeClosed(1, count)
            .mapToObj(i -> buildSuggestionJson("sugg-" + (char)('A' + i - 1),
                "AI suggestion number " + i + " for thread " + threadId,
                0.90))
            .collect(Collectors.toList());

        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[" + String.join(",", suggestions) + "]}")));
    }

    public static void stubNamedSuggestion(String threadId, String suggId, String text, double confidence) {
        String suggestionJson = buildSuggestionJson(suggId, text, confidence);
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[" + suggestionJson + "]}")));
    }

    public static void stubEmptySuggestions(String threadId) {
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[]}")));
    }

    public static void stubHttpError(String threadId, int statusCode) {
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Service error " + statusCode + "\"}")));
    }

    public static void stubTimeout(String threadId) {
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)));
    }

    public static void stubWithNetworkDelay(String threadId, int delayMs, int count) {
        List<String> suggestions = IntStream.rangeClosed(1, count)
            .mapToObj(i -> buildSuggestionJson("sugg-delay-" + i,
                "Delayed suggestion " + i, 0.85))
            .collect(Collectors.toList());

        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withFixedDelay(delayMs)
                .withBody("{\"suggestions\":[" + String.join(",", suggestions) + "]}")));
    }

    public static void stubWithConfidenceScore(String threadId, double score) {
        String suggJson = buildSuggestionJson("sugg-conf-1", "Confidence-scored suggestion text", score);
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[" + suggJson + "]}")));
    }

    public static void stubWithPIIContent(String threadId, String piiValue) {
        String suggJson = buildSuggestionJson("sugg-pii-1",
            "Please contact us at " + piiValue + " for further assistance.", 0.88);
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[" + suggJson + "]}")));
    }

    public static void stubForLanguage(String threadId, String language, String suggestionText) {
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .withRequestBody(matchingJsonPath("$.language", equalTo(language)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"suggestions\":[" + buildSuggestionJson("sugg-lang-1",
                    suggestionText, 0.90) + "],\"language\":\"" + language + "\"}")));
    }

    public static void stubUnsupportedLanguage(String threadId, String language) {
        wireMockServer.stubFor(post(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.threadId", equalTo(threadId)))
            .withRequestBody(matchingJsonPath("$.language", equalTo(language)))
            .willReturn(aResponse()
                .withStatus(422)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Unsupported language: " + language + "\"}")));
    }

    public static void verifyAISuggestionRequestNotMade() {
        WireMock.verify(0, postRequestedFor(urlEqualTo(AI_SUGGESTIONS_PATH)));
    }

    public static void verifyAISuggestionRequestMadeOnce() {
        WireMock.verify(1, postRequestedFor(urlEqualTo(AI_SUGGESTIONS_PATH)));
    }

    public static void verifyRequestPayloadDoesNotContain(String forbiddenContent) {
        WireMock.verify(postRequestedFor(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(notContaining(forbiddenContent)));
    }

    public static void verifyRequestPayloadContainsLanguage(String language) {
        WireMock.verify(postRequestedFor(urlEqualTo(AI_SUGGESTIONS_PATH))
            .withRequestBody(matchingJsonPath("$.language", equalTo(language))));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static String buildSuggestionJson(String id, String text, double confidence) {
        return String.format(
            "{\"id\":\"%s\",\"text\":\"%s\",\"confidence\":%.2f}",
            id, text, confidence
        );
    }
}
