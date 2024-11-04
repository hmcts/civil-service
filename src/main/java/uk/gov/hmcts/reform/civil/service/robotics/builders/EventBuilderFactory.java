package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventBuilderFactory {

    private final Map<FlowState.Main, EventBuilder> flowStateBuilderMap;

    @Autowired
    public EventBuilderFactory(
        UnrepresentedDefendantBuilder unrepresentedDefendantBuilder,
        UnregisteredAndUnrepresentedBuilder unregisteredAndUnrepresentedBuilder,
        UnregisteredDefendantBuilder unregisteredDefendantBuilder,
        ClaimIssuedBuilder claimIssuedBuilder,
        ClaimNotifiedBuilder claimNotifiedBuilder,
        ClaimDetailsNotifiedBuilder claimDetailsNotifiedBuilder,
        TakenOfflineAfterClaimNotifiedBuilder takenOfflineAfterClaimNotifiedBuilder,
        TakenOfflineAfterClaimDetailsNotifiedBuilder takenOfflineAfterClaimDetailsNotifiedBuilder,
        NotificationAcknowledgedBuilder notificationAcknowledgedBuilder,
        ClaimDetailsNotifiedTimeExtensionBuilder claimDetailsNotifiedTimeExtensionBuilder,
        FullAdmissionBuilder fullAdmissionBuilder,
        PartAdmissionBuilder partAdmissionBuilder,
        FullDefenceBuilder fullDefenceBuilder,
        FullDefenceProceedBuilder fullDefenceProceedBuilder,
        TakenOfflineByStaffBuilder takenOfflineByStaffBuilder,
        ClaimDismissedPastNotificationDeadlineBuilder claimDismissedPastNotificationDeadlineBuilder,
        TakenOfflinePastResponseDeadlineBuilder takenOfflinePastResponseDeadlineBuilder,
        TakenOfflineSdoNotDrawnBuilder takenOfflineSdoNotDrawnBuilder,
        TakenOfflineAfterSdoBuilder takenOfflineAfterSdoBuilder,
        InMediationBuilder inMediationBuilder,
        FullDefenceNotProceedBuilder fullDefenceNotProceedBuilder,
        AdmitRejectPaymentBuilder admitRejectPaymentBuilder,
        ClaimDismissedPastDeadlineBuilder claimDismissedPastDeadlineBuilder,
        DivergentResponseBuilder divergentResponseBuilder,
        CounterClaimBuilder counterClaimBuilder,
        TakenOfflineDueToDependantNocBuilder takenOfflineDueToDependantNocBuilder
    ) {
        flowStateBuilderMap = Map.ofEntries(
            Map.entry(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT, unrepresentedDefendantBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT, unregisteredAndUnrepresentedBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT, unregisteredDefendantBuilder),
            Map.entry(FlowState.Main.CLAIM_ISSUED, claimIssuedBuilder),
            Map.entry(FlowState.Main.CLAIM_NOTIFIED, claimNotifiedBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED, takenOfflineAfterClaimNotifiedBuilder),
            Map.entry(FlowState.Main.CLAIM_DETAILS_NOTIFIED, claimDetailsNotifiedBuilder),
            Map.entry(FlowState.Main.NOTIFICATION_ACKNOWLEDGED, notificationAcknowledgedBuilder),
            Map.entry(FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, claimDetailsNotifiedTimeExtensionBuilder),
            Map.entry(FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, claimDetailsNotifiedTimeExtensionBuilder),
            Map.entry(FlowState.Main.FULL_DEFENCE, fullDefenceBuilder),
            Map.entry(FlowState.Main.FULL_ADMISSION, fullAdmissionBuilder),
            Map.entry(FlowState.Main.PART_ADMISSION, partAdmissionBuilder),
            Map.entry(FlowState.Main.COUNTER_CLAIM, counterClaimBuilder),
            Map.entry(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, divergentResponseBuilder),
            Map.entry(FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED, divergentResponseBuilder),
            Map.entry(FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, divergentResponseBuilder),
            Map.entry(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE, divergentResponseBuilder),
            Map.entry(FlowState.Main.FULL_DEFENCE_NOT_PROCEED, fullDefenceNotProceedBuilder),
            Map.entry(FlowState.Main.FULL_DEFENCE_PROCEED, fullDefenceProceedBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_BY_STAFF, takenOfflineByStaffBuilder),
            Map.entry(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE, claimDismissedPastDeadlineBuilder),
            Map.entry(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE, claimDismissedPastNotificationDeadlineBuilder),
            Map.entry(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE, claimDismissedPastNotificationDeadlineBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED, takenOfflineAfterClaimDetailsNotifiedBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE, takenOfflinePastResponseDeadlineBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN, takenOfflineSdoNotDrawnBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO, takenOfflineAfterSdoBuilder),
            Map.entry(FlowState.Main.PART_ADMIT_REJECT_REPAYMENT, admitRejectPaymentBuilder),
            Map.entry(FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT, admitRejectPaymentBuilder),
            Map.entry(FlowState.Main.IN_MEDIATION, inMediationBuilder),
            Map.entry(FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC, admitRejectPaymentBuilder)
        );
    }

    public EventBuilder getBuilder(FlowState.Main scenario) {
        return Optional.ofNullable(flowStateBuilderMap.get(scenario))
            .orElse(null);
    }
}
