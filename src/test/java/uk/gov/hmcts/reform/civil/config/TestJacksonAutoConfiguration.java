package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import(JacksonConfiguration.class)
public class TestJacksonAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper(ObjectProvider<List<Module>> modulesProvider) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        modulesProvider.getIfAvailable(List::of).forEach(mapper::registerModule);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
