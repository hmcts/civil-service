package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

class JacksonConfigurationTest {

    private final ObjectMapper objectMapper = configuredObjectMapper();

    @Test
    void shouldSerializeJavaTimeTypes() throws Exception {
        Map<String, Object> payload = Map.of(
            "date", LocalDate.of(2026, 6, 15),
            "dateTime", LocalDateTime.of(2026, 6, 15, 10, 30, 45),
            "offsetDateTime", OffsetDateTime.of(2026, 6, 15, 10, 30, 45, 0, ZoneOffset.UTC)
        );

        String result = objectMapper.writeValueAsString(payload);

        assertThat(result).contains("\"date\":\"2026-06-15\"");
        assertThat(result).contains("\"dateTime\":\"2026-06-15T10:30:45\"");
        assertThat(result).contains("\"offsetDateTime\":\"2026-06-15T10:30:45Z\"");
    }

    @Test
    void shouldDeserializeJavaTimeTypes() throws Exception {
        assertThat(objectMapper.readValue("\"2026-06-15\"", LocalDate.class))
            .isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(objectMapper.readValue("\"2026-06-15T10:30:45\"", LocalDateTime.class))
            .isEqualTo(LocalDateTime.of(2026, 6, 15, 10, 30, 45));
    }

    @Test
    void shouldDeserializeCcdDocumentWithBuilder() throws Exception {
        Document document = objectMapper.readValue(
            readString("document-management/download.success.json"),
            Document.class
        );

        assertThat(document.originalDocumentName).isEqualTo("TEST_DOCUMENT_1.pdf");
        assertThat(document.links.self.href)
            .isEqualTo("http://dm-store:4506/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    private static ObjectMapper configuredObjectMapper() {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        new JacksonConfiguration().jsonDateTimeFormatCustomizer().customize(builder);
        return builder.build();
    }
}
