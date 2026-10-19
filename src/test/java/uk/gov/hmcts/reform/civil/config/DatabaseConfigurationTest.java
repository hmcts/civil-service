package uk.gov.hmcts.reform.civil.config;

import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigurationTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues(
            "reference.database.enabled=true",
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.driver-class-name=org.h2.Driver"
        )
        .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class))
        .withUserConfiguration(DatabaseConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenDatabaseConfigurationIsLoaded() {
        context.run(it -> {
            assertThat(it).hasSingleBean(DataSourceTransactionManager.class);
            assertThat(it).hasSingleBean(Jdbi.class);
            assertThat(it).hasSingleBean(CasemanReferenceNumberRepository.class);

            // Base Hikari pool bean
            assertThat(it).hasBean("hikariDataSource")
                .getBean("hikariDataSource")
                .isInstanceOf(HikariDataSource.class);

            // Primary TransactionAwareDataSourceProxy bean
            assertThat(it).hasBean("dataSource")
                .getBean("dataSource")
                .isInstanceOf(TransactionAwareDataSourceProxy.class);
        });
    }
}
