package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class JacksonConfiguration {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern(DATE_TIME_FORMAT, Locale.UK);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonDateTimeFormatCustomizer() {
        return builder -> {
            builder.modulesToInstall(JavaTimeModule.class);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.simpleDateFormat(DATE_TIME_FORMAT);
            builder.serializers(new LocalDateSerializer(DATE_FORMATTER));
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        };
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .modulesToInstall(JavaTimeModule.class)
            .build();
    }
}
