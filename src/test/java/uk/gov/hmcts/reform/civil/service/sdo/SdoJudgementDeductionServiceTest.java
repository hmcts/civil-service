package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import static org.assertj.core.api.Assertions.assertThat;

class SdoJudgementDeductionServiceTest {

    private final SdoJudgementDeductionService service = new SdoJudgementDeductionService();

    @Test
    void shouldPopulateDeductionValuesWhenJudgementSumPresent() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrder(JudgementSum.builder().judgementSum(15d).build())
            .build();

        service.populateJudgementDeductionValues(caseData);

        assertThat(caseData.getDisposalHearingJudgementDeductionValue().getValue()).isEqualTo("15.0%");
        assertThat(caseData.getFastTrackJudgementDeductionValue().getValue()).isEqualTo("15.0%");
        assertThat(caseData.getSmallClaimsJudgementDeductionValue().getValue()).isEqualTo("15.0%");
    }

    @Test
    void shouldSkipPopulationWhenJudgementSumMissing() {
        CaseData caseData = CaseData.builder().build();

        service.populateJudgementDeductionValues(caseData);

        assertThat(caseData.getDisposalHearingJudgementDeductionValue()).isNull();
        assertThat(caseData.getFastTrackJudgementDeductionValue()).isNull();
        assertThat(caseData.getSmallClaimsJudgementDeductionValue()).isNull();
    }
}
