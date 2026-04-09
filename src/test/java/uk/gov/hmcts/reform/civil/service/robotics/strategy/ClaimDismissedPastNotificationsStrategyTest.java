package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClaimDismissedPastNotificationsStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;

    private ClaimDismissedPastNotificationsStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new ClaimDismissedPastNotificationsStrategy(sequenceGenerator, textFormatter);
        when(sequenceGenerator.nextSequence(any())).thenReturn(40, 41);
        when(textFormatter.claimDismissedNoActionSinceIssue())
            .thenReturn("RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.");
        when(textFormatter.claimDismissedNoClaimDetailsWithinWindow())
            .thenReturn("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    @Test
    void supportsReturnsTrueWhenCaseDataPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenCaseDataNull() {
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenDismissedDatePresent() {
        LocalDateTime dismissedDate = LocalDateTime.of(2024, 2, 6, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(dismissedDate)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventForNotificationDeadline() {
        LocalDateTime dismissedDate = LocalDateTime.of(2024, 2, 6, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(dismissedDate)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null, FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(40);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.");
    }

    @Test
    void contributeAddsEventForDetailsDeadline() {
        LocalDateTime dismissedDate = LocalDateTime.of(2024, 2, 6, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(dismissedDate)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null, FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(40);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    @Test
    void contributeNoOpOverloadDoesNotAddEvents() {
        EventHistory builder = new EventHistory();
        strategy.contribute(builder, CaseDataBuilder.builder().build(), null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeReturnsWhenFlowStateNull() {
        EventHistory builder = new EventHistory();
        strategy.contribute(builder, CaseDataBuilder.builder().build(), null, null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeIgnoresUnrelatedState() {
        EventHistory builder = new EventHistory();
        strategy.contribute(builder, CaseDataBuilder.builder().build(), null, FlowState.Main.CLAIM_ISSUED);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
    }
}
