package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.service.search.SettlementNoResponseFromDefendantSearchService;

import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettlementNoResponseFromDefendantHandlerTest {

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private SettlementNoResponseFromDefendantSearchService caseSearchService;
    @InjectMocks
    private SettlementNoResponseFromDefendantHandler handler;

    @Test
    void shouldEmit_SettlementNoResponseFromDefendantEvent_whenCasesFound() {
        // Given: one case found from search service
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        given(caseSearchService.getCases()).willReturn(caseDetails);

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: task should be completed
        verify(applicationEventPublisher).publishEvent(new SettlementNoResponseFromDefendantEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotEmitSettlementNoResponseFromDefendantEvent_WhenNoCasesFound() {
        // Given: no case found from search service
        when(caseSearchService.getCases()).thenReturn(Set.of());

        // When: handler is called
        handler.execute(mockTask, externalTaskService);

        // Then: publish event should not get called
        verifyNoInteractions(applicationEventPublisher);
    }

}
