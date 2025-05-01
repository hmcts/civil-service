package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AllResponsesReceivedTransitionBuilder extends MidTransitionBuilder {

    public AllResponsesReceivedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.ALL_RESPONSES_RECEIVED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_DEFENCE, transitions).onlyWhen(fullDefence, transitions)
            .moveTo(FULL_DEFENCE, transitions).onlyWhen(fullDefenceSpec, transitions)
            .moveTo(FULL_ADMISSION, transitions).onlyWhen(fullAdmission.and(not(divergentRespondGoOffline)), transitions)
            .moveTo(PART_ADMISSION, transitions).onlyWhen(partAdmission.and(not(divergentRespondGoOffline)), transitions)
            .moveTo(COUNTER_CLAIM, transitions).onlyWhen(counterClaim.and(not(divergentRespondGoOffline)), transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions).onlyWhen(divergentRespondGoOffline, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions).onlyWhen(divergentRespondGoOfflineSpec, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions).onlyWhen(fullAdmissionSpec, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions).onlyWhen(partAdmissionSpec, transitions)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE, transitions).onlyWhen(counterClaimSpec, transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions).onlyWhen(divergentRespondWithDQAndGoOffline, transitions)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, transitions).onlyWhen(divergentRespondWithDQAndGoOfflineSpec, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffAfterClaimDetailsNotified, transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions).onlyWhen(caseDismissedAfterDetailNotified, transitions);
    }

    public static final Predicate<CaseData> fullDefence = caseData ->
        getPredicateForResponseType(caseData, RespondentResponseType.FULL_DEFENCE);

    public static final Predicate<CaseData> fullAdmission = caseData ->
        getPredicateForResponseType(caseData, RespondentResponseType.FULL_ADMISSION);

    public static final Predicate<CaseData> partAdmission = caseData ->
        getPredicateForResponseType(caseData, RespondentResponseType.PART_ADMISSION);

    public static boolean getPredicateForResponseType(CaseData caseData, RespondentResponseType responseType) {
        boolean basePredicate = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == responseType;
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP -> basePredicate && (caseData.getRespondentResponseIsSame() == YES
                || caseData.getRespondent2ClaimResponseType() == responseType);
            case ONE_V_TWO_TWO_LEGAL_REP -> basePredicate && caseData.getRespondent2ClaimResponseType() == responseType;
            case ONE_V_ONE -> basePredicate;
            case TWO_V_ONE -> basePredicate && caseData.getRespondent1ClaimResponseTypeToApplicant2() == responseType;
        };
    }
}
