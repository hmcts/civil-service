package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.RequestForReconsiderationNotificationDeadlineSearchService;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RequestForReconsiderationNotificationDeadlineHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private RequestForReconsiderationNotificationDeadlineSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RequestForReconsiderationNotificationDeadlineHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

    }

    @Test
    void shouldEmitRequestForReconsiderationDeadlineEvent_whenDeadlineIsDue() {
        long caseId = 1L;
        CaseData caseData = new CaseDataBuilder().atStateHearingFeeDuePaid().build();
        Set<CaseDetails> caseDetails = Set.of(new CaseDetailsBuilder().id(caseId).data(caseData).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.iterator().next());
        when(caseDetailsConverter.toCaseData(caseDetails.iterator().next())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new RequestForReconsiderationNotificationDeadlineEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }
}
