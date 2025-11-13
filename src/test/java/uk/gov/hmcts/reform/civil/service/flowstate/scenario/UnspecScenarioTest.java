package uk.gov.hmcts.reform.civil.service.flowstate.scenario;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

class UnspecScenarioTest {

    @Test
    void appliesOnlyToUnspec() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        var scenario = new UnspecScenario(repo);

        CaseData unspec = mock(CaseData.class);
        when(unspec.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        CaseData spec = mock(CaseData.class);
        when(spec.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);

        assertThat(scenario.appliesTo(unspec)).isTrue();
        assertThat(scenario.appliesTo(spec)).isFalse();
    }

    @Test
    void delegatesToCorrectFile() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        var scenario = new UnspecScenario(repo);

        when(repo.getAllowedEvents("allowed-unspec-events.yml", "MAIN.DRAFT"))
            .thenReturn(Set.of(CaseEvent.CREATE_CLAIM));

        assertThat(scenario.loadBaseEvents("MAIN.DRAFT"))
            .contains(CaseEvent.CREATE_CLAIM);
        verify(repo).getAllowedEvents("allowed-unspec-events.yml", "MAIN.DRAFT");
    }
}
