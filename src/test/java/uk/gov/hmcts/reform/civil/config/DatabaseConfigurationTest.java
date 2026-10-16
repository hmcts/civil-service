package uk.gov.hmcts.reform.civil.config;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues("reference.database.enabled:true")
        .withBean("dataSource", DataSource.class, () -> Mockito.mock(DataSource.class))
        .withUserConfiguration(DatabaseConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenDatabaseConfigurationIsLoaded() {
        context.run(it -> {
            assertThat(it).hasSingleBean(DataSourceTransactionManager.class);
            assertThat(it).hasSingleBean(Jdbi.class);
            assertThat(it).hasSingleBean(CasemanReferenceNumberRepository.class);
            assertThat(it).hasBean("dataSource");
            assertThat(it).hasBean("dataSourceProxy");
        });
    }
}
