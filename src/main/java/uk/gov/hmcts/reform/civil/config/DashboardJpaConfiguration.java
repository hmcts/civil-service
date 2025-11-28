package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Enables the dashboard JPA infrastructure only when explicitly requested.
 * Contract tests start an application context but stub out any persistence,
 * so we skip wiring JPA unless `app.jpa.enabled` remains true.
 */
@Configuration
@ConditionalOnProperty(value = "app.jpa.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.dashboard"})
@EntityScan("uk.gov.hmcts.reform.dashboard")
public class DashboardJpaConfiguration {
}
