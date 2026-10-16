package uk.gov.hmcts.reform.civil.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty("reference.database.enabled")
public class DatabaseConfiguration {

    @Bean
    @Primary
    public TransactionAwareDataSourceProxy dataSourceProxy(@Qualifier("dataSource") @Lazy DataSource dataSource) {
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
    public CasemanReferenceNumberRepository referenceNumberRepository(Jdbi dbi) {
        return dbi.onDemand(CasemanReferenceNumberRepository.class);
    }

}
