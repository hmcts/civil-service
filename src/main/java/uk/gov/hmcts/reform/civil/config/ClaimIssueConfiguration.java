package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ClaimIssueConfiguration {

    private final String responsePackLink;
    private final String n9aLink;
    private final String n9bLink;
    private final String n215Link;

    public ClaimIssueConfiguration(@Value("${civil.response-pack-url}") String responsePackLink,
                                   @Value("${civil.n9a-url}") String n9aLink,
                                   @Value("${civil.n9b-url}") String n9bLink,
                                   @Value("${civil.n215-url}") String n215Link) {
        this.responsePackLink = responsePackLink;
        this.n9aLink = n9aLink;
        this.n9bLink = n9bLink;
        this.n215Link = n215Link;
    }
}

