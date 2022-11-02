package uk.gov.hmcts.reform.civil.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JacksonAutoConfiguration.class, JacksonConfiguration.class})
class JacksonConfigurationTest {

    @Autowired
    ObjectMapper mapper;

    @ParameterizedTest()
    @CsvSource({
        "2022-11-02T16:25:37.123456789,2022-11-02T16:25:37.123456789",
        "2022-11-02T16:25:37.123456,2022-11-02T16:25:37.123456000",
        "2022-11-02T16:25:37.123,2022-11-02T16:25:37.123000000",
        "2022-11-02T16:25:37,2022-11-02T16:25:37.000000000",
        "2022-11-02T16:25,2022-11-02T16:25:00.000000000",
        "2022-11-02,2022-11-02T00:00:00.000000000"})
    @SneakyThrows
    void shouldProcessInputDateTimeInATolerantWay(String inputValue, String expectedResult) {
        // When
        Map<String, LocalDateTime> parsed = mapper.readValue(
            "{ \"date\": \"" + inputValue + "\" }",
            new TypeReference<>() {}
        );
        LocalDateTime actualResult = parsed.get("date");

        // Then
        assertThat(actualResult.format(DateTimeFormatter
                                           .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS", Locale.UK)))
            .isEqualTo(expectedResult);
    }
}
