package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JudgmentByAdmissionStrategyNullCourtFeeTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;

    private JudgmentByAdmissionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new JudgmentByAdmissionStrategy(
            featureToggleService, new RoboticsEventTextFormatter(), sequenceGenerator, timelineHelper);
        LocalDateTime responseDate = LocalDateTime.of(2024, 5, 1, 9, 0);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);
    }

    @Test
    void shouldThrowNullPointerExceptionForLipOneVOneWhenClaimFeeIsMissing() {
        CaseData caseData = lipOneVOneCaseData();
        caseData.setClaimFee(null);

        assertThatThrownBy(() -> strategy.contribute(new EventHistory(), caseData, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("court fee must not be null");
    }

    @Test
    void shouldUseCourtFeeForLipOneVOneWhenClaimFeeIsPresent() {
        CaseData caseData = lipOneVOneCaseData();
        Fee claimFee = new Fee();
        claimFee.setCalculatedAmountInPence(new BigDecimal("9900"));
        caseData.setClaimFee(claimFee);

        EventHistory history = new EventHistory();
        strategy.contribute(history, caseData, null);

        assertThat(history.getJudgmentByAdmission().getFirst().getEventDetails().getAmountOfCosts())
            .isEqualByComparingTo("99.00");
    }

    @Test
    void shouldUseCcjFixedCostAndClaimFeeForLegalRepClaimEvenWithoutClaimFee() {
        CaseData caseData = baseCaseData();
        caseData.setClaimFee(null);

        EventHistory history = new EventHistory();
        strategy.contribute(history, caseData, null);

        assertThat(history.getJudgmentByAdmission().getFirst().getEventDetails().getAmountOfCosts())
            .isEqualByComparingTo("130.00");
    }

    private CaseData lipOneVOneCaseData() {
        CaseData caseData = baseCaseData();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        return caseData;
    }

    private CaseData baseCaseData() {
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(500));
        ccjPaymentDetails.setCcjJudgmentAmountClaimFee(BigDecimal.valueOf(80));
        ccjPaymentDetails.setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(50));
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YesOrNo.NO);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 5, 1, 9, 0));
        return caseData;
    }
}
