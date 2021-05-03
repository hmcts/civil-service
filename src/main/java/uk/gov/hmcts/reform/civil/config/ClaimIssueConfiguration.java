package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ClaimIssueConfiguration {

    private final String responsePackLink;

    public ClaimIssueConfiguration(@Value("${civil.response-pack-url}") String responsePackLink) {
        this.responsePackLink = responsePackLink;
    }
}
