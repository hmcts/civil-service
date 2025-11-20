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
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateJudgementDeductionValues(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalHearingJudgementDeductionValue().getValue()).isEqualTo("15.0%");
        assertThat(result.getFastTrackJudgementDeductionValue().getValue()).isEqualTo("15.0%");
        assertThat(result.getSmallClaimsJudgementDeductionValue().getValue()).isEqualTo("15.0%");
    }

    @Test
    void shouldSkipPopulationWhenJudgementSumMissing() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateJudgementDeductionValues(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalHearingJudgementDeductionValue()).isNull();
        assertThat(result.getFastTrackJudgementDeductionValue()).isNull();
        assertThat(result.getSmallClaimsJudgementDeductionValue()).isNull();
    }
}
