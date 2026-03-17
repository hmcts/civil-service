package uk.gov.hmcts.reform.civil.repositories;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class CasemanReferenceNumberRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine")
        .withDatabaseName("civil")
        .withUsername("civil")
        .withPassword("civil");

    private static CasemanReferenceNumberRepository repository;

    @BeforeAll
    static void setup() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .placeholderReplacement(false)
            .load()
            .migrate();

        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        repository = jdbi.onDemand(CasemanReferenceNumberRepository.class);
    }

    @Test
    void shouldGenerateSpecReferencesInOrder() {
        assertThat(repository.next("spec")).isEqualTo("000JE001");
        assertThat(repository.next("spec")).isEqualTo("000JE002");
    }

    @Test
    void shouldGenerateUnspecReferencesFromConfiguredPrefixes() {
        assertThat(repository.next("unspec")).isEqualTo("000KA001");
    }
}
