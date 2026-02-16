package uk.gov.hmcts.reform.civil.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class ObjectMapperBuilder {

    private ObjectMapperBuilder() {
        // utility class
    }

    public static ObjectMapper instance() {
        return Jackson2ObjectMapperBuilder.json().build()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
