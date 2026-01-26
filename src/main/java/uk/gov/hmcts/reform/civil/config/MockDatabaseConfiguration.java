package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

@Configuration
@ConditionalOnProperty(value = "reference.database.enabled", havingValue = "false")
public class MockDatabaseConfiguration {

    @Bean
    public CasemanReferenceNumberRepository referenceNumberRepository() {
        return series -> "spec".equals(series) ? "000MC001" : "000DC001";
    }

}
