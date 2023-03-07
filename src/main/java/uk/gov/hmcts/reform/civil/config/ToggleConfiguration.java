package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ToggleConfiguration {

    private final String featureToggle;

    public ToggleConfiguration(
        @Value("${wa.feature-toggle}") String featureToggle) {
        this.featureToggle = featureToggle;
    }
}
