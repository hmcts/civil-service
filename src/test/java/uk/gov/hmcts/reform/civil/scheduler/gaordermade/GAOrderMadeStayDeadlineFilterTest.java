package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;

@ExtendWith(MockitoExtension.class)
class GAOrderMadeStayDeadlineFilterTest {

    @Mock
    private Time time;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private GAOrderMadeStayDeadlineFilter filter;

    @Test
    void shouldIdentifyExpiredStayDeadline() {
        LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);
        LocalDate today = now.toLocalDate();

        CaseDetails judicialOrderCase = CaseDetails.builder().id(1L).build();
        GeneralApplicationCaseData judicialData = getJudicialOrderCaseData(today, YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(judicialOrderCase)).thenReturn(judicialData);
        assertThat(filter.hasExpiredStayDeadline(judicialOrderCase)).isTrue();

        CaseDetails judicialOrderExpiredCase = CaseDetails.builder().id(2L).build();
        GeneralApplicationCaseData judicialExpiredData = getJudicialOrderCaseData(today.minusDays(1), YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(judicialOrderExpiredCase)).thenReturn(judicialExpiredData);
        assertThat(filter.hasExpiredStayDeadline(judicialOrderExpiredCase)).isTrue();

        CaseDetails consentOrderCase = CaseDetails.builder().id(3L).build();
        GeneralApplicationCaseData consentData = getConsentOrderCaseData(today, YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(consentOrderCase)).thenReturn(consentData);
        assertThat(filter.hasExpiredStayDeadline(consentOrderCase)).isTrue();

        CaseDetails judicialOrderFutureCase = CaseDetails.builder().id(4L).build();
        GeneralApplicationCaseData judicialFutureData = getJudicialOrderCaseData(today.plusDays(1), YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(judicialOrderFutureCase)).thenReturn(judicialFutureData);
        assertThat(filter.hasExpiredStayDeadline(judicialOrderFutureCase)).isFalse();

        CaseDetails judicialOrderNoDateCase = CaseDetails.builder().id(5L).build();
        GeneralApplicationCaseData judicialNoDateData = getJudicialOrderCaseData(null, YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(judicialOrderNoDateCase)).thenReturn(judicialNoDateData);
        assertThat(filter.hasExpiredStayDeadline(judicialOrderNoDateCase)).isFalse();
    }

    private GeneralApplicationCaseData getJudicialOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(deadline)
                                           .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }

    private GeneralApplicationCaseData getConsentOrderCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)))
            .approveConsentOrder(new GAApproveConsentOrder()
                                     .setConsentOrderDescription("Testing prepopulated text")
                                     .setConsentOrderDateToEnd(deadline)
                                     .setIsOrderProcessedByStayScheduler(isProcessed))
            .build();
    }
}
