package uk.gov.hmcts.reform.unspec.config;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.gov.hmcts.reform.unspec.repositories.ReferenceNumberRepository;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues("reference.database.enabled:true")
        .withUserConfiguration(DatabaseConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenDatabaseConfigurationIsLoaded() {
        context.run(it -> {
            assertThat(it).hasSingleBean(DataSourceTransactionManager.class);
            assertThat(it).hasSingleBean(Jdbi.class);
            assertThat(it).hasSingleBean(ReferenceNumberRepository.class);
            assertThat(it).hasBean("dataSource");
            assertThat(it).hasBean("dataSourceProxy");
        });
    }
}
