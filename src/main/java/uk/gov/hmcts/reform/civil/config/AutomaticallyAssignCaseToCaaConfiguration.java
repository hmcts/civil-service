package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AutomaticallyAssignCaseToCaaConfiguration {

    private final boolean assignCaseToCaa;

    public AutomaticallyAssignCaseToCaaConfiguration(@Value("${auto-assign-caa}") boolean assignCaseToCaa) {
        this.assignCaseToCaa = assignCaseToCaa;
    }
}
