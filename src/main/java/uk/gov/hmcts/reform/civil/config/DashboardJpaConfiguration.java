package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("!contract-test")
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.dashboard"})
@EntityScan("uk.gov.hmcts.reform.dashboard")
public class DashboardJpaConfiguration {
}
