package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;

@Configuration
@ConditionalOnProperty(value = "reference.database.enabled", havingValue = "false")
public class MockDatabaseConfiguration {

    @Bean
    public ReferenceNumberRepository referenceNumberRepository() {
        return () -> "000DC001";
    }
}
