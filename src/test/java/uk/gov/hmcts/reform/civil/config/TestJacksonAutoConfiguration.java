package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JacksonConfiguration.class)
public class TestJacksonAutoConfiguration {
}
