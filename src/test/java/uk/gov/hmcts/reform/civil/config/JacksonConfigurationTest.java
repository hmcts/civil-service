package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
        .withUserConfiguration(JacksonConfiguration.class);

    @Test
    void shouldConfigureSpringObjectMapperForJavaTimeTypes() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObjectMapper.class);

            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            Map<String, Object> payload = Map.of(
                "date", LocalDate.of(2026, 6, 15),
                "dateTime", LocalDateTime.of(2026, 6, 15, 10, 30, 45),
                "offsetDateTime", OffsetDateTime.of(2026, 6, 15, 10, 30, 45, 0, ZoneOffset.UTC)
            );

            String result = objectMapper.writeValueAsString(payload);

            assertThat(result).contains("\"date\":\"2026-06-15\"");
            assertThat(result).contains("\"dateTime\":\"2026-06-15T10:30:45\"");
            assertThat(result).contains("\"offsetDateTime\":\"2026-06-15T10:30:45Z\"");
            assertThat(objectMapper.readValue("\"2026-06-15\"", LocalDate.class))
                .isEqualTo(LocalDate.of(2026, 6, 15));
            assertThat(objectMapper.readValue("\"2026-06-15T10:30:45\"", LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2026, 6, 15, 10, 30, 45));
        });
    }
}
