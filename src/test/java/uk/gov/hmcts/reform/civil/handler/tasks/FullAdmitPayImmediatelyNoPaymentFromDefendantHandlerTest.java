package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantHandlerTest {

    private static final String TOPIC = "full-admit-no-payment";
    private static final String EVENT_SUMMARY =
        "Updating case - Full Admit No Payment Dashboard notification created successfully";
    private static final String EVENT_DESCRIPTION =
        "Updating case - Full Admit No Payment Dashboard notification created successfully";

    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService caseSearchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ExternalTask externalTask;

    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantHandler handler;

    @BeforeEach
    void setUp() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
    }

    @Test
    void shouldMarkCaseAsProcessedAndPublishEventForEachResult() {
        long caseId = 1234567890123456L;
        Set<CaseDetails> cases = Set.of(new CaseDetailsBuilder().id(caseId).build());
        when(caseSearchService.getCases()).thenReturn(cases);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(caseId),
            eq(UPDATE_CASE_DATA),
            argThat(payload -> YesOrNo.YES.equals(payload.get("fullAdmitNoPaymentSchedulerProcessed"))),
            eq(EVENT_SUMMARY),
            eq(EVENT_DESCRIPTION)
        );

        verify(applicationEventPublisher).publishEvent(
            new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(caseId));
    }

    @Test
    void shouldContinueProcessingRemainingCasesWhenOneUpdateFails() {
        long failingCaseId = 1111111111111111L;
        long successfulCaseId = 2222222222222222L;

        LinkedHashSet<CaseDetails> cases = new LinkedHashSet<>();
        cases.add(new CaseDetailsBuilder().id(failingCaseId).build());
        cases.add(new CaseDetailsBuilder().id(successfulCaseId).build());
        when(caseSearchService.getCases()).thenReturn(cases);

        doAnswer(invocation -> {
            Long caseId = invocation.getArgument(0);
            if (caseId.equals(failingCaseId)) {
                throw new RuntimeException("triggerEvent failure");
            }
            return null;
        }).when(coreCaseDataService).triggerEvent(anyLong(), any(), anyMap(), anyString(), anyString());

        handler.handleTask(externalTask);

        verify(coreCaseDataService, times(2)).triggerEvent(anyLong(), any(), anyMap(), anyString(), anyString());
        verify(coreCaseDataService).triggerEvent(
            successfulCaseId,
            UPDATE_CASE_DATA,
            Map.of("fullAdmitNoPaymentSchedulerProcessed", YesOrNo.YES),
            EVENT_SUMMARY,
            EVENT_DESCRIPTION
        );
        verify(applicationEventPublisher).publishEvent(
            new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(successfulCaseId));
        verifyNoMoreInteractions(applicationEventPublisher);
    }

    @Test
    void shouldDoNothingWhenNoCasesAreReturned() {
        when(caseSearchService.getCases()).thenReturn(Set.of());

        handler.handleTask(externalTask);

        verifyNoInteractions(coreCaseDataService);
        verifyNoInteractions(applicationEventPublisher);
    }
}
