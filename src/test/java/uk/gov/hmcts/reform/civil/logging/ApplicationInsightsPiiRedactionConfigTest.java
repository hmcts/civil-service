package uk.gov.hmcts.reform.civil.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationInsightsPiiRedactionConfigTest {

    private static final Path CONFIG = Path.of("lib/applicationinsights.json");
    private static final Pattern NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    @Test
    void shouldRedactSensitiveValuesAndDeleteExtractedAttributes() throws IOException {
        JsonNode processors = new ObjectMapper().readTree(CONFIG.toFile()).at("/preview/processors");
        JsonNode rules = processors.get(0).at("/body/toAttributes/rules");
        JsonNode actions = processors.get(1).get("actions");

        String body = "Notification for jane.doe@example.com: firstName=Jane, addressLine1=1 High Street, "
            + "claimAmount=250.00, paymentReference=RC-123, caseId=1234567890123456";
        List<String> extractedAttributes = new ArrayList<>();

        for (JsonNode rule : rules) {
            Pattern pattern = Pattern.compile(rule.asText());
            Matcher groupMatcher = NAMED_GROUP.matcher(rule.asText());
            assertThat(groupMatcher.find()).isTrue();
            String groupName = groupMatcher.group(1);
            Matcher valueMatcher = pattern.matcher(body);
            while (valueMatcher.find()) {
                extractedAttributes.add(groupName);
            }
            body = valueMatcher.replaceAll("{" + groupName + "}");
        }

        assertThat(body)
            .doesNotContain("jane.doe@example.com", "Jane", "1 High Street", "250.00", "RC-123")
            .contains("caseId=1234567890123456");
        assertThat(actions)
            .extracting(action -> action.get("key").asText())
            .containsAll(extractedAttributes);
        assertThat(actions)
            .filteredOn(action -> action.get("key").asText().startsWith("redacted"))
            .allSatisfy(action -> assertThat(action.get("action").asText()).isEqualTo("delete"));
    }

    @Test
    void shouldRedactExceptionTelemetryAttributes() throws IOException {
        JsonNode actions = new ObjectMapper().readTree(CONFIG.toFile()).at("/preview/processors/1/actions");
        Map<String, String> attributes = new HashMap<>();
        attributes.put(
            "exception.message",
            "Request for jane.doe@example.com failed: firstName=Jane, claimAmount=250.00"
        );
        attributes.put(
            "exception.stacktrace",
            "java.lang.IllegalStateException: addressLine1=1 High Street, paymentReference=RC-123\n"
                + "at Example.method(Example.java:10)"
        );

        for (JsonNode action : actions) {
            if (!"mask".equals(action.get("action").asText())) {
                continue;
            }
            String key = action.get("key").asText();
            String value = attributes.get(key);
            Pattern pattern = Pattern.compile(action.get("pattern").asText());
            attributes.put(key, pattern.matcher(value).replaceAll(action.get("replace").asText()));
        }

        assertThat(attributes.get("exception.message"))
            .doesNotContain("jane.doe@example.com", "Jane", "250.00")
            .contains("firstName=[REDACTED]", "claimAmount=[REDACTED]");
        assertThat(attributes.get("exception.stacktrace"))
            .doesNotContain("1 High Street", "RC-123")
            .contains("addressLine1=[REDACTED]", "paymentReference=[REDACTED]", "at Example.method");
    }
}
