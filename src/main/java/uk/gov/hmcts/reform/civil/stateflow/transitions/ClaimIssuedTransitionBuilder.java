package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullAdmitReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isRespondentResponseLangIsBilingual;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimIssuedTransitionBuilder extends MidTransitionBuilder {

    public ClaimIssuedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_ISSUED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_NOTIFIED, transitions).onlyWhen(claimNotified, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffAfterClaimIssue, transitions)
            .moveTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED, transitions).onlyWhen(takenOfflineAfterClaimNotified, transitions)
            .moveTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA, transitions).onlyWhen(pastClaimNotificationDeadline, transitions)
            .moveTo(CONTACT_DETAILS_CHANGE, transitions).onlyWhen(contactDetailsChange, transitions)
            .set(flags -> flags.put(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true), transitions)
            .moveTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, transitions)
            .onlyWhen(isRespondentResponseLangIsBilingual.and(not(contactDetailsChange)), transitions)
            .set(flags -> flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true), transitions)
            .moveTo(FULL_DEFENCE, transitions).onlyWhen(fullDefenceSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual))
                .and(not(pastClaimNotificationDeadline)), transitions)
            .moveTo(PART_ADMISSION, transitions)
            .onlyWhen(partAdmissionSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)), transitions)
            .moveTo(FULL_ADMISSION, transitions)
            .onlyWhen(fullAdmissionSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)), transitions)
            .moveTo(COUNTER_CLAIM, transitions)
            .onlyWhen(counterClaimSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(awaitingResponsesFullDefenceReceivedSpec.and(specClaim), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(awaitingResponsesFullAdmitReceived.and(specClaim), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(awaitingResponsesNonFullDefenceReceivedSpec.and(specClaim), transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions)
            .onlyWhen(divergentRespondWithDQAndGoOfflineSpec.and(specClaim), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(divergentRespondGoOfflineSpec.and(specClaim), transitions);
    }

    public static final Predicate<CaseData> claimNotified = caseData ->
        !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getClaimNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimOptions() == null
            || "Both".equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel()));

    public static final Predicate<CaseData> takenOfflineAfterClaimNotified = caseData ->
        caseData.getClaimNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && !"Both".equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel());

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimIssue = caseData ->
        // In case of SPEC claim ClaimNotificationDate will be set even when the case is issued
        // In case of UNSPEC ClaimNotificationDate will be set only after notification step
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isAfter(LocalDateTime.now())
            && ((SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.getClaimNotificationDate() != null)
            || (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.getClaimNotificationDate() == null));

    public static final Predicate<CaseData> pastClaimNotificationDeadline = caseData ->
        caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimNotificationDate() == null;

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec = caseData -> {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        return scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP
            && ((caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent2ClaimResponseTypeForSpec() == null
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (caseData.getRespondent1ClaimResponseTypeForSpec() == null
            && caseData.getRespondent2ClaimResponseTypeForSpec() != null
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())));
    };

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceivedSpec = caseData -> {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        return scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP
            && ((caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent2ClaimResponseTypeForSpec() == null
            && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (caseData.getRespondent1ClaimResponseTypeForSpec() == null
            && caseData.getRespondent2ClaimResponseTypeForSpec() != null
            && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())));
    };

    public static final Predicate<CaseData> contactDetailsChange = caseData ->
        NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired());
}
