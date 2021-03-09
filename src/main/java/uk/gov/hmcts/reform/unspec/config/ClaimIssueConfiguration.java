package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ClaimIssueConfiguration {

    private final String responsePackLink;

    public ClaimIssueConfiguration(@Value("${unspecified.response-pack-url}") String responsePackLink) {
        this.responsePackLink = responsePackLink;
    }
}
