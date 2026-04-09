package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JudgementServiceTest {

    @Mock
    private InterestCalculator interestCalculator;

    @InjectMocks
    private JudgementService judgementService;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        lenient().when(interestCalculator.calculateInterest(any())).thenReturn(BigDecimal.TEN);
    }

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

    @Test
    void shouldReturnClaimAmountWithInterest_whenFullAdmit() {
        CaseData mockData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .hwfFeeType(FeeType.HEARING)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        mockData.setTotalClaimAmount(BigDecimal.valueOf(100));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(mockData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(11000, 2)); // 100 + 10 interest
    }

    @Test
    void shouldReturnClaimAmountWithInterest_whenPayImmediatelyFullAdmitConditionsAreMet() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .hwfFeeType(FeeType.HEARING)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(200));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(21000, 2)); // 200 + 10 interest
    }

    @Test
    void shouldReturnClaimAmountWithInterest_whenLipFullAdmitPayImmediately() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .hwfFeeType(FeeType.HEARING)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(300));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(31000, 2)); // 300 + 10 interest
    }

    @Test
    void shouldReturnClaimAmountWithInterest_whenLipOneVOneConditionIsMet() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .hwfFeeType(FeeType.HEARING)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(400));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(41000, 2)); // 400 + 10 interest
    }

    @Test
    void shouldReturnAdmittedClaimAmount_whenPartAdmitClaimSpecIsTrue() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .hwfFeeType(FeeType.HEARING)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        caseData.setRespondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(500));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(50000, 2));
    }

    @Test
    void shouldReturnTotalClaimAmount_whenNoConditionsAreMet() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hwfFeeType(FeeType.HEARING)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(550))
            .build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(600));

        BigDecimal result = judgementService.ccjJudgmentClaimAmountWithInterest(caseData);

        assertThat(result).isEqualTo(BigDecimal.valueOf(60000, 2));
    }
}
