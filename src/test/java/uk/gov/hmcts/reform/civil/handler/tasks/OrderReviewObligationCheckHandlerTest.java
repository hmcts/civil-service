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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.OrderReviewObligationCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StoredObligationData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.OrderReviewObligationSearchService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OrderReviewObligationCheckHandlerTest {

    @Mock
    private OrderReviewObligationSearchService caseSearchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private OrderReviewObligationCheckHandler handler;

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

    }

    @Test
    void handleTask_shouldPublishEvent_whenObligationDateIsCurrentDateAndWATaskRaisedIsNo() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        StoredObligationData obligationData = mock(StoredObligationData.class);
        Element<StoredObligationData> element = Element.<StoredObligationData>builder().value(obligationData).build();
        List<Element<StoredObligationData>> storedObligationData = Collections.singletonList(element);

        when(caseDetails.getId()).thenReturn(Long.valueOf("1"));
        when(caseSearchService.getCases()).thenReturn(Collections.singletonList(caseDetails));
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        when(coreCaseDataService.getCase(Long.valueOf("1"))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.getStoredObligationData()).thenReturn(storedObligationData);
        when(obligationData.getObligationDate()).thenReturn(LocalDate.now());
        when(obligationData.getObligationWATaskRaised()).thenReturn(YesOrNo.NO);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new OrderReviewObligationCheckEvent(Long.valueOf("1")));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void handleTask_shouldNotPublishEvent_whenObligationDateIsNotCurrentDate() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        StoredObligationData obligationData = mock(StoredObligationData.class);
        Element<StoredObligationData> element = Element.<StoredObligationData>builder().value(obligationData).build();
        List<Element<StoredObligationData>> storedObligationData = Collections.singletonList(element);

        when(caseDetails.getId()).thenReturn(Long.valueOf("1"));
        when(caseSearchService.getCases()).thenReturn(Collections.singletonList(caseDetails));
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        when(coreCaseDataService.getCase(Long.valueOf("1"))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.getStoredObligationData()).thenReturn(storedObligationData);
        when(obligationData.getObligationDate()).thenReturn(LocalDate.now().minusDays(1));
        when(obligationData.getObligationWATaskRaised()).thenReturn(YesOrNo.NO);

        handler.handleTask(mock(ExternalTask.class));

        verify(applicationEventPublisher, never()).publishEvent(any(OrderReviewObligationCheckEvent.class));
    }

    @Test
    void handleTask_shouldNotPublishEvent_whenWATaskRaisedIsYes() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        StoredObligationData obligationData = mock(StoredObligationData.class);
        Element<StoredObligationData> element = Element.<StoredObligationData>builder().value(obligationData).build();
        List<Element<StoredObligationData>> storedObligationData = Collections.singletonList(element);

        when(caseDetails.getId()).thenReturn(Long.valueOf("1"));
        when(caseSearchService.getCases()).thenReturn(Collections.singletonList(caseDetails));
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        when(coreCaseDataService.getCase(Long.valueOf("1"))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.getStoredObligationData()).thenReturn(storedObligationData);
        when(obligationData.getObligationDate()).thenReturn(LocalDate.now());
        when(obligationData.getObligationWATaskRaised()).thenReturn(YesOrNo.YES);

        handler.handleTask(mock(ExternalTask.class));

        verify(applicationEventPublisher, never()).publishEvent(any(OrderReviewObligationCheckEvent.class));
    }

    @Test
    void handleTask_shouldLogError_whenExceptionIsThrown() {
        CaseDetails caseDetails = mock(CaseDetails.class);

        when(caseDetails.getId()).thenReturn(Long.valueOf("1"));
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        when(caseSearchService.getCases()).thenReturn(Collections.singletonList(caseDetails));
        when(coreCaseDataService.getCase(Long.valueOf("1"))).thenThrow(new RuntimeException("Test exception"));

        handler.handleTask(mock(ExternalTask.class));

        verify(applicationEventPublisher, never()).publishEvent(any(OrderReviewObligationCheckEvent.class));
    }
}
