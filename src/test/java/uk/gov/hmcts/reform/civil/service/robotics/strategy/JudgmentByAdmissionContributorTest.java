package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JudgmentByAdmissionContributorTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private RoboticsTimelineHelper timelineHelper;

    private JudgmentByAdmissionContributor contributor;

    private RoboticsEventTextFormatter textFormatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textFormatter = new RoboticsEventTextFormatter();
        contributor = new JudgmentByAdmissionContributor(
            featureToggleService,
            textFormatter,
            sequenceGenerator,
            timelineHelper
        );
    }

    @Test
    void supportsReturnsFalseWhenRequestNotMade() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsOfflineJudgmentEvents() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);
        LocalDateTime responseDate = LocalDateTime.of(2024, 2, 1, 10, 0);
        when(timelineHelper.ensurePresentOrNow(any(LocalDateTime.class))).thenReturn(responseDate);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder()
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1000))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(100))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(50))
                .ccjPaymentPaidSomeAmountInPounds(BigDecimal.valueOf(10))
                .build())
            .applicant1ResponseDate(responseDate)
            .joJudgementByAdmissionIssueDate(null)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getMiscellaneous().get(0).getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(textFormatter.judgmentByAdmissionOffline());

        assertThat(history.getJudgmentByAdmission()).hasSize(1);
        assertThat(history.getJudgmentByAdmission().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getJudgmentByAdmission().get(0).getEventCode())
            .isEqualTo(EventType.JUDGEMENT_BY_ADMISSION.getCode());
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.APPLICANT_ID);
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfJudgment())
            .isEqualByComparingTo(BigDecimal.valueOf(1000).setScale(2));
        assertThat(history.getJudgmentByAdmission().get(0).getEventDetails().getAmountOfCosts())
            .isEqualByComparingTo(BigDecimal.valueOf(150).setScale(2));
    }

    @Test
    void contributeUsesJoIssueDateWhenLiveFeedActive() {
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        LocalDateTime issueDate = LocalDateTime.of(2024, 3, 5, 15, 30);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentImmediately()
            .toBuilder()
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(800))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(120))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(BigDecimal.ZERO)
                .build())
            .joJudgementByAdmissionIssueDate(issueDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetails().getMiscText())
            .isEqualTo(EventHistoryMapper.RECORD_JUDGMENT);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(issueDate);
        assertThat(history.getJudgmentByAdmission().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
    }
}
