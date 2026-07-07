package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;

@ExtendWith(MockitoExtension.class)
class GAOrderMadeScheduledTaskTest {

    private static final Long CASE_ID = 123L;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Spy
    private ObjectMapper mapper = ObjectMapperFactory.instance();
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private GAOrderMadeScheduledTask task;

    @Test
    void shouldReturnCaseId() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .build();

        assertThat(task.getItemId(caseData)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldTriggerStayOrderDeadlineEventForJudicialOrder() {
        GeneralApplicationCaseData caseData = getJudicialOrderCaseData(LocalDate.now(), YesOrNo.NO);
        GeneralApplicationCaseData expectedCaseData = getJudicialOrderCaseData(LocalDate.now(), YesOrNo.YES);

        task.accept(caseData);

        verify(coreCaseDataService).triggerGaEvent(
            CASE_ID,
            END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
            expectedCaseData.toMap(mapper)
        );
    }

    @Test
    void shouldTriggerStayOrderDeadlineEventForConsentOrder() {
        GeneralApplicationCaseData caseData = getConsentOrderCaseData(LocalDate.now(), YesOrNo.NO);
        GeneralApplicationCaseData expectedCaseData = getConsentOrderCaseData(LocalDate.now(), YesOrNo.YES);

        task.accept(caseData);

        verify(coreCaseDataService).triggerGaEvent(
            CASE_ID,
            END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
            expectedCaseData.toMap(mapper)
        );
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }

    private GeneralApplicationCaseData getJudicialOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(deadline)
                                           .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }

    private GeneralApplicationCaseData getConsentOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .approveConsentOrder(new GAApproveConsentOrder()
                                     .setConsentOrderDescription("Testing prepopulated text")
                                     .setConsentOrderDateToEnd(deadline)
                                     .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }
}
