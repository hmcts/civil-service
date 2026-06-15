package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

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
            builder.modulesToInstall(javaTimeModule(), new Jdk8Module(), new ParameterNamesModule());
            builder.simpleDateFormat(DATE_TIME_FORMAT);
            builder.serializers(
                new LocalDateSerializer(DATE_FORMATTER),
                new LocalDateTimeSerializer(DATE_TIME_FORMATTER)
            );
            builder.mixIn(Document.class, DocumentMixin.class);
            builder.mixIn(Document.DocumentBuilder.class, DocumentBuilderMixin.class);
        };
    }

    private static Module javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        return javaTimeModule;
    }

    @JsonDeserialize(builder = Document.DocumentBuilder.class)
    private abstract static class DocumentMixin {
    }

    @JsonPOJOBuilder(withPrefix = "")
    private abstract static class DocumentBuilderMixin {
    }
}
