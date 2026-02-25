package utils;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

    private final Map<String, Object> context = new HashMap<>();

    public void set(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> type) {
        return type.cast(context.get(key));
    }

    public boolean contains(String key) {
        return context.containsKey(key);
    }

    public void remove(String key) {
        context.remove(key);
    }

    public void clear() {
        context.clear();
    }

    // ── Shared context keys ──────────────────────────────────────────────────
    public static final String THREAD_OPEN_TIMESTAMP   = "THREAD_OPEN_TIMESTAMP";
    public static final String SUGGESTION_PANEL_HIDDEN = "SUGGESTION_PANEL_HIDDEN";
    public static final String REPLY_BOX_CONTENT       = "REPLY_BOX_CONTENT";
    public static final String ACCEPTED_SUGGESTION_ID  = "ACCEPTED_SUGGESTION_ID";
    public static final String ACTIVE_AGENT_ID         = "ACTIVE_AGENT_ID";
    public static final String ACTIVE_THREAD_ID        = "ACTIVE_THREAD_ID";
    public static final String AI_SERVICE_STUB_COUNT   = "AI_SERVICE_STUB_COUNT";
    public static final String MESSAGE_SENT_FLAG       = "MESSAGE_SENT_FLAG";
    public static final String FEEDBACK_LOG_ENTRIES    = "FEEDBACK_LOG_ENTRIES";
    public static final String FEATURE_FLAG_STATE      = "FEATURE_FLAG_STATE";
    public static final String PII_AUDIT_RECORDS       = "PII_AUDIT_RECORDS";
    public static final String DETECTED_LANGUAGE       = "DETECTED_LANGUAGE";
    public static final String CONFIDENCE_SCORE        = "CONFIDENCE_SCORE";
    public static final String NETWORK_LATENCY_MS      = "NETWORK_LATENCY_MS";
    public static final String CHANNEL_TYPE            = "CHANNEL_TYPE";
}
