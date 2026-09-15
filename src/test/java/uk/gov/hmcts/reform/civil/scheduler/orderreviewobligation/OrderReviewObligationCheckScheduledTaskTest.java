package uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.OrderReviewObligationCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StoredObligationData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReviewObligationCheckScheduledTaskTest {

    private static final Long CASE_ID = 123L;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private OrderReviewObligationCheckScheduledTask task;

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();

        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishOrderReviewObligationCheckEventForDueUnraisedObligation() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        CaseData caseData = CaseData.builder()
            .storedObligationData(List.of(obligation(LocalDate.now(), YesOrNo.NO)))
            .build();
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new OrderReviewObligationCheckEvent(CASE_ID));
    }

    @Test
    void shouldNotPublishOrderReviewObligationCheckEventForFutureObligation() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        CaseData caseData = CaseData.builder()
            .storedObligationData(List.of(obligation(LocalDate.now().plusDays(1), YesOrNo.NO)))
            .build();
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(applicationEventPublisher, never()).publishEvent(new OrderReviewObligationCheckEvent(CASE_ID));
    }

    @Test
    void shouldNotPublishOrderReviewObligationCheckEventWhenWaTaskAlreadyRaised() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        CaseData caseData = CaseData.builder()
            .storedObligationData(List.of(obligation(LocalDate.now(), YesOrNo.YES)))
            .build();
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(applicationEventPublisher, never()).publishEvent(new OrderReviewObligationCheckEvent(CASE_ID));
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }

    private static Element<StoredObligationData> obligation(LocalDate obligationDate, YesOrNo waTaskRaised) {
        StoredObligationData obligationData = new StoredObligationData()
            .setObligationDate(obligationDate)
            .setObligationWATaskRaised(waTaskRaised);
        return new Element<StoredObligationData>().setValue(obligationData);
    }
}
