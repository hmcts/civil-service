package uk.gov.hmcts.reform.civil.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class IdamScopeLoggingConfiguration {

    private final String idamScope;

    public IdamScopeLoggingConfiguration(@Value("${idam.client.scope}") String idamScope) {
        this.idamScope = idamScope;
    }

    @PostConstruct
    public void logIdamScope() {
        log.info("IDAM Scope: {}", idamScope);
    }
}
