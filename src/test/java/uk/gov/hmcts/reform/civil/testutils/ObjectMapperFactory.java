package uk.gov.hmcts.reform.civil.testutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.config.JacksonConfiguration.DATE_FORMATTER;
import static uk.gov.hmcts.reform.civil.config.JacksonConfiguration.DATE_TIME_FORMAT;
import static uk.gov.hmcts.reform.civil.config.JacksonConfiguration.DATE_TIME_FORMATTER;

public class ObjectMapperFactory {

    private ObjectMapperFactory() {
        // utility class
    }

    public static ObjectMapper instance() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(javaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT, Locale.UK));
        return objectMapper;
    }

    private static JavaTimeModule javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
        module.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        return module;
    }
}
