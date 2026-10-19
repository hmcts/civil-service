package uk.gov.hmcts.reform.civil.config;

import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty("reference.database.enabled")
public class DatabaseConfiguration {

    @Bean
    @FlywayDataSource
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource hikariDataSource() {
        return new HikariDataSource();
    }

    @Primary
    @Bean(name = "dataSource")
    public TransactionAwareDataSourceProxy dataSource(@Qualifier("hikariDataSource") DataSource hikariDataSource) {
        return new TransactionAwareDataSourceProxy(hikariDataSource);
    }

    @Bean
    public Jdbi dbi(TransactionAwareDataSourceProxy dataSourceProxy) {
        Jdbi jdbi = Jdbi.create(dataSourceProxy);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Bean
    public CasemanReferenceNumberRepository referenceNumberRepository(Jdbi dbi) {
        return dbi.onDemand(CasemanReferenceNumberRepository.class);
    }

}
