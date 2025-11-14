package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;

class SpecScenarioTest {

    @Test
    void delegatesToCorrectFile() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        var scenario = new SpecScenario(repo);

        when(repo.getAllowedEvents("allowed-spec-events.yml", "MAIN.DRAFT"))
            .thenReturn(Set.of(CREATE_CLAIM_SPEC));

        assertThat(scenario.loadBaseEvents("MAIN.DRAFT"))
            .contains(CREATE_CLAIM_SPEC);
        verify(repo).getAllowedEvents("allowed-spec-events.yml", "MAIN.DRAFT");
    }
}
