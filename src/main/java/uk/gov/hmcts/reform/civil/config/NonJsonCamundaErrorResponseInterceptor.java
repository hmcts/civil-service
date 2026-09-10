package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.nio.charset.StandardCharsets;

/**
 * Rewrites a non-JSON error body from the Camunda engine's fronting gateway into the
 * {@code EngineRestExceptionDto} JSON shape the external task client expects.
 *
 * <p>On any {@code >= 300} response the client parses the body as {@code EngineRestExceptionDto}.
 * When an ingress or gateway serves an HTML or plain-text 502/503/504 page instead, Jackson throws
 * {@code JsonParseException} and the client wraps it as
 * {@code EngineClientException TASK/CLIENT-02004 "parsing json object"} - an error that reads like a
 * civil-service bug and carries no HTTP status, so it cannot be categorised or backed off on
 * (EXC-CS-020).
 *
 * <p>Replacing the body with {@code {"type":"...","message":"<snippet> (HTTP <code>)"}} lets the
 * client's normal path run: it builds a {@code RestException}, stamps the real status code on it,
 * and the failure surfaces as "status code 502" - correctly attributed to the upstream dependency,
 * a stable string to alert on, and an error the {@link CamundaErrorAwareBackoffStrategy} can tell
 * apart from an empty long-poll.
 *
 * <p>Responses under 300, and {@code >= 300} bodies that already look like JSON, are passed through
 * untouched.
 */
public class NonJsonCamundaErrorResponseInterceptor implements HttpResponseInterceptor {

    static final String SYNTHETIC_TYPE = "UpstreamGatewayException";

    private static final int MAX_BODY_BYTES = 8 * 1024;
    private static final int MAX_SNIPPET_CHARS = 200;

    @Override
    public void process(HttpResponse response, EntityDetails entityDetails, HttpContext context) {
        if (!(response instanceof ClassicHttpResponse classicResponse)) {
            return;
        }

        int statusCode = classicResponse.getCode();
        if (statusCode < 300 || classicResponse.getEntity() == null) {
            return;
        }

        byte[] body;
        try {
            body = EntityUtils.toByteArray(classicResponse.getEntity(), MAX_BODY_BYTES);
        } catch (Exception e) {
            // Could not buffer the body - leave the original entity in place and let the
            // client handle the read failure exactly as it does today.
            return;
        }
        if (body == null) {
            return;
        }

        if (looksLikeJson(body)) {
            // Reading the entity consumed its stream; hand the same bytes back for parsing.
            classicResponse.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_JSON));
            return;
        }

        String message = jsonEscape(snippet(body)) + " (HTTP " + statusCode + ")";
        String json = "{\"type\":\"" + SYNTHETIC_TYPE + "\",\"message\":\"" + message + "\"}";
        classicResponse.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    }

    private static boolean looksLikeJson(byte[] body) {
        for (byte b : body) {
            if (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
                continue;
            }
            return b == '{' || b == '[';
        }
        return false;
    }

    private static String snippet(byte[] body) {
        String text = new String(body, StandardCharsets.UTF_8)
            .replaceAll("<[^>]*>", " ")
            .replaceAll("[\\p{Cntrl}\\s]+", " ")
            .trim();
        if (text.isEmpty()) {
            return "non-JSON error response";
        }
        return text.length() > MAX_SNIPPET_CHARS ? text.substring(0, MAX_SNIPPET_CHARS).trim() + "..." : text;
    }

    private static String jsonEscape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
