package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;

import static org.assertj.core.api.Assertions.assertThat;

class MockDatabaseConfigurationTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues("reference.database.enabled:false")
        .withUserConfiguration(MockDatabaseConfiguration.class);

    @Test
    void shouldReturnMockReferenceNumbersForSpecAndUnspec() {
        context.run(it -> {
            CasemanReferenceNumberRepository repository = it.getBean(CasemanReferenceNumberRepository.class);
            assertThat(repository.next("spec")).isEqualTo("000MC001");
            assertThat(repository.next("unspec")).isEqualTo("000DC001");
        });
    }
}
