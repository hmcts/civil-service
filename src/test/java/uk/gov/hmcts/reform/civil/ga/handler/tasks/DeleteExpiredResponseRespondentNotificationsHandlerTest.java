package uk.gov.hmcts.reform.civil.ga.handler.tasks;

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
import uk.gov.hmcts.reform.civil.ga.event.DeleteExpiredResponseRespondentNotificationsEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.service.search.DeleteExpiredResponseRespondentNotificationSearchService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DeleteExpiredResponseRespondentNotificationsHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private DeleteExpiredResponseRespondentNotificationSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DeleteExpiredResponseRespondentNotificationsHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

    }

    @Test
    void shouldEmitRequestForReconsiderationDeadlineEvent_whenDeadlineIsDue() {
        long caseId = 1L;
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        Map<String, Object> data = Map.of("data", caseData);
        final CaseDetails caseDetails = CaseDetails.builder().id(caseId).data(data).build();

        when(searchService.getApplications()).thenReturn((Set.of(caseDetails)));
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new DeleteExpiredResponseRespondentNotificationsEvent(caseId));
        verify(externalTaskService).complete(any(), any());
    }
}
