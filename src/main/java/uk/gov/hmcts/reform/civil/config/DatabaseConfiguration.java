package uk.gov.hmcts.reform.civil.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty("reference.database.enabled")
public class DatabaseConfiguration {

    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource();
    }

    @Bean
    public TransactionAwareDataSourceProxy dataSourceProxy(DataSource dataSource) {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(TransactionAwareDataSourceProxy dataSourceProxy) {
        return new DataSourceTransactionManager(dataSourceProxy);
    }

    @Bean
    public Jdbi dbi(TransactionAwareDataSourceProxy dataSourceProxy) {
        Jdbi jdbi = Jdbi.create(dataSourceProxy);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Bean
    public ReferenceNumberRepository referenceNumberRepository(Jdbi dbi) {
        return dbi.onDemand(ReferenceNumberRepository.class);
    }
}
