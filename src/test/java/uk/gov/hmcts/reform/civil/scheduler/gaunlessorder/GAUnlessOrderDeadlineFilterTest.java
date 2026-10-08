package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
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
class GAUnlessOrderDeadlineFilterTest {

    @Mock
    private Time time;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private GAUnlessOrderDeadlineFilter filter;

    @Test
    void shouldIdentifyExpiredUnlessOrderDeadline() {
        LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);
        LocalDate today = now.toLocalDate();

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        GeneralApplicationCaseData caseData = getCaseData(today, YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        assertThat(filter.hasExpiredUnlessOrderDeadline(caseDetails)).isTrue();

        CaseDetails expiredCaseDetails = CaseDetails.builder().id(2L).build();
        GeneralApplicationCaseData expiredCaseData = getCaseData(today.minusDays(1), YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(expiredCaseDetails)).thenReturn(expiredCaseData);
        assertThat(filter.hasExpiredUnlessOrderDeadline(expiredCaseDetails)).isTrue();

        CaseDetails futureCaseDetails = CaseDetails.builder().id(3L).build();
        GeneralApplicationCaseData futureCaseData = getCaseData(today.plusDays(1), YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(futureCaseDetails)).thenReturn(futureCaseData);
        assertThat(filter.hasExpiredUnlessOrderDeadline(futureCaseDetails)).isFalse();

        CaseDetails processedCaseDetails = CaseDetails.builder().id(4L).build();
        GeneralApplicationCaseData processedCaseData = getCaseData(today, YesOrNo.YES);
        when(caseDetailsConverter.toGeneralApplicationCaseData(processedCaseDetails)).thenReturn(processedCaseData);
        assertThat(filter.hasExpiredUnlessOrderDeadline(processedCaseDetails)).isFalse();

        CaseDetails noDateCaseDetails = CaseDetails.builder().id(5L).build();
        GeneralApplicationCaseData noDateCaseData = getCaseData(null, YesOrNo.NO);
        when(caseDetailsConverter.toGeneralApplicationCaseData(noDateCaseDetails)).thenReturn(noDateCaseData);
        assertThat(filter.hasExpiredUnlessOrderDeadline(noDateCaseDetails)).isFalse();
    }

    private GeneralApplicationCaseData getCaseData(LocalDate deadline, YesOrNo isProcessed) {
        return GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .ccdState(ORDER_MADE)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.UNLESS_ORDER)))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDateForUnlessOrder(deadline)
                                           .setIsOrderProcessedByUnlessScheduler(isProcessed))
            .build();
    }
}
