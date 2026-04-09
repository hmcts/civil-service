package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class JudgementServiceTest {

    @InjectMocks
    private JudgementService judgementService;
    @Mock
    private FeatureToggleService featureToggleService;

    @ParameterizedTest
    @EnumSource(RespondentResponsePartAdmissionPaymentTimeLRspec.class)
    void ccjJudgementSubTotal(RespondentResponsePartAdmissionPaymentTimeLRspec selection) {
        HelpWithFeesDetails hearingHwfDetails = new HelpWithFeesDetails();
        hearingHwfDetails.setOutstandingFeeInPounds(new BigDecimal(255));
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentFixedCostOption(YesOrNo.NO);
        CaseData mockData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(selection)
            .hwfFeeType(FeeType.HEARING)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        mockData.setHearingHwfDetails(hearingHwfDetails);
        mockData.setCcjPaymentDetails(ccjPaymentDetails);
        BigDecimal expected = new BigDecimal(805).setScale(2, RoundingMode.UNNECESSARY);
        assertEquals(expected, judgementService.ccjJudgementSubTotal(mockData));
    }
}
