package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Configuration
public class JacksonConfiguration {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern(DATE_TIME_FORMAT, Locale.UK);

    @Bean
    public Module jsonDateTimeFormatModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
        module.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        return module;
    }

    @Bean
    public ObjectMapper objectMapper(ObjectProvider<List<Module>> modulesProvider) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        modulesProvider.getIfAvailable(List::of).forEach(mapper::registerModule);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
