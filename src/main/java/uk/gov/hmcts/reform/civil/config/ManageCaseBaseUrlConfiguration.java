package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ManageCaseBaseUrlConfiguration {

    private final String manageCaseBaseUrl;

    public ManageCaseBaseUrlConfiguration(
        @Value("${manage-case-ui.baseurl}") String manageCaseBaseUrl
    ) {
        this.manageCaseBaseUrl = manageCaseBaseUrl;
    }
}
