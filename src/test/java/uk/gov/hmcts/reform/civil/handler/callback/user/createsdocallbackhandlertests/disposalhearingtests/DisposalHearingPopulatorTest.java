package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.DisposalHearingPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DisposalHearingPopulatorTest {

    @Mock
    private List<SdoCaseFieldBuilder> sdoCaseFieldBuilder;

    @InjectMocks
    private DisposalHearingPopulator disposalHearingPopulator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        disposalHearingPopulator = new DisposalHearingPopulator(sdoCaseFieldBuilder);
    }

    @Test
    void shouldSetDisposalHearingFields() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        disposalHearingPopulator.setDisposalHearingFields(updatedData, caseData);

        CaseData result = updatedData.build();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSetJudgementDeductionValues() {
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
