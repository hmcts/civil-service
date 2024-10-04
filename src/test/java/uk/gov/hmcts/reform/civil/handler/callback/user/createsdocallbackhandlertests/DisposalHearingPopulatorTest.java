package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.DisposalHearingPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisposalHearingPopulatorTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private DisposalHearingPopulator disposalHearingPopulator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .build();
    }

    @Test
    void shouldSetDisposalHearingFields() {
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), any(Integer.class))).thenReturn(LocalDate.now().plusDays(5));

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        disposalHearingPopulator.setDisposalHearingFields(updatedData, caseData);

        CaseData result = updatedData.build();
        assertThat(result.getDisposalHearingJudgesRecital()).isNotNull();
        assertThat(result.getDisposalHearingDisclosureOfDocuments()).isNotNull();
        assertThat(result.getDisposalHearingWitnessOfFact()).isNotNull();
        assertThat(result.getDisposalHearingMedicalEvidence()).isNotNull();
        assertThat(result.getDisposalHearingQuestionsToExperts()).isNotNull();
        assertThat(result.getDisposalHearingSchedulesOfLoss()).isNotNull();
        assertThat(result.getDisposalHearingFinalDisposalHearing()).isNotNull();
        assertThat(result.getDisposalHearingHearingTime()).isNotNull();
        assertThat(result.getDisposalOrderWithoutHearing()).isNotNull();
        assertThat(result.getDisposalHearingBundle()).isNotNull();
        assertThat(result.getDisposalHearingNotes()).isNotNull();
    }

    @Test
    void shouldSetJudgementDeductionValues() {
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), any(Integer.class))).thenReturn(LocalDate.now().plusDays(5));

        CaseData caseDataWithJudgementSum = CaseData.builder()
            .drawDirectionsOrder(JudgementSum.builder().judgementSum(10.0).build())
            .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        disposalHearingPopulator.setDisposalHearingFields(updatedData, caseDataWithJudgementSum);

        CaseData result = updatedData.build();
        assertThat(result.getDisposalHearingJudgementDeductionValue()).isNotNull();
        assertThat(result.getDisposalHearingJudgementDeductionValue().getValue()).isEqualTo("10.0%");
        assertThat(result.getFastTrackJudgementDeductionValue()).isNotNull();
        assertThat(result.getFastTrackJudgementDeductionValue().getValue()).isEqualTo("10.0%");
        assertThat(result.getSmallClaimsJudgementDeductionValue()).isNotNull();
        assertThat(result.getSmallClaimsJudgementDeductionValue().getValue()).isEqualTo("10.0%");
    }
}
