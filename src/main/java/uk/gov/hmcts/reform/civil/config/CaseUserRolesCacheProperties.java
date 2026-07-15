package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "case-user-roles-cache")
public class CaseUserRolesCacheProperties {

    private boolean enabled = true;
    private long ttlSeconds = 30;
    private long negativeTtlSeconds = 10;
    private String keyPrefix = "civil:v1:case-user-roles";
    private long caffeineMaxSize = 10000;
}
