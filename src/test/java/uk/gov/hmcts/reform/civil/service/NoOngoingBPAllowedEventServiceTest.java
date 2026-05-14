package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoOngoingBPAllowedEventServiceTest {

    private static final String ALLOWED_EVENTS_FILE = "no-ongoing-bp-allowed-events.yml";

    @Test
    void returnsTrueWhenEventIsConfiguredAsAllowed() {
        AllowedEventRepository repository = mock(AllowedEventRepository.class);
        when(repository.getNoOngoingBPAllowedEvents(ALLOWED_EVENTS_FILE))
            .thenReturn(Set.of(CaseEvent.UPDATE_CASE_DATA));

        NoOngoingBPAllowedEventService service = new NoOngoingBPAllowedEventService(repository);

        boolean allowed = service.isAllowed(CaseEvent.UPDATE_CASE_DATA);

        assertThat(allowed).isTrue();
        verify(repository).getNoOngoingBPAllowedEvents(ALLOWED_EVENTS_FILE);
    }

    @Test
    void returnsFalseWhenEventIsNotConfiguredAsAllowed() {
        AllowedEventRepository repository = mock(AllowedEventRepository.class);
        when(repository.getNoOngoingBPAllowedEvents(ALLOWED_EVENTS_FILE))
            .thenReturn(Set.of(CaseEvent.UPDATE_CASE_DATA));

        NoOngoingBPAllowedEventService service = new NoOngoingBPAllowedEventService(repository);

        boolean allowed = service.isAllowed(CaseEvent.CREATE_CLAIM);

        assertThat(allowed).isFalse();
        verify(repository).getNoOngoingBPAllowedEvents(ALLOWED_EVENTS_FILE);
    }
}
