package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DivergencePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.NotificationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.util.function.Predicate.not;
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
        this.moveTo(CLAIM_NOTIFIED, transitions)
            .onlyWhen(ClaimPredicate.isSpec.negate().and(NotificationPredicate.hasClaimNotifiedToBoth), transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(ClaimPredicate.afterIssued), transitions)
            .moveTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED, transitions)
            .onlyWhen(TakenOfflinePredicate.afterClaimNotified, transitions)
            .moveTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(DismissedPredicate.pastClaimNotificationDeadline, transitions)
            .moveTo(CONTACT_DETAILS_CHANGE, transitions)
            .onlyWhen(ClaimantPredicate.correspondenceAddressNotRequired, transitions)
            .set(flags -> flags.put(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true), transitions)
            .moveTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, transitions)
            .onlyWhen(LanguagePredicate.onlyInitialResponseIsBilingual
                  .and(not(ClaimantPredicate.correspondenceAddressNotRequired)), transitions)
            .set(flags -> flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true), transitions)
            .moveTo(FULL_DEFENCE, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE)
                  .and(not(ClaimantPredicate.correspondenceAddressNotRequired))
                  .and(not(LanguagePredicate.onlyInitialResponseIsBilingual))
                  .and(not(DismissedPredicate.pastClaimNotificationDeadline)), transitions)
            .moveTo(PART_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.PART_ADMISSION)
                  .and(not(ClaimantPredicate.correspondenceAddressNotRequired))
                  .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)
            .moveTo(FULL_ADMISSION, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_ADMISSION)
                  .and(not(ClaimantPredicate.correspondenceAddressNotRequired))
                  .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)
            .moveTo(COUNTER_CLAIM, transitions)
            .onlyWhen(ResponsePredicate.isType(RespondentResponseTypeSpec.COUNTER_CLAIM)
                  .and(not(ClaimantPredicate.correspondenceAddressNotRequired))
                  .and(not(LanguagePredicate.onlyInitialResponseIsBilingual)), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.and(ClaimPredicate.isSpec), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.and(ClaimPredicate.isSpec), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.and(ClaimPredicate.isSpec), transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec.and(ClaimPredicate.isSpec), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions)
            .onlyWhen(DivergencePredicate.divergentRespondGoOfflineSpec.and(ClaimPredicate.isSpec), transitions);
    }

}
