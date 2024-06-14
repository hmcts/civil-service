package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Objects;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
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
public class AllResponsesReceivedTransitionBuilder extends MidTransitionBuilder {

    public AllResponsesReceivedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.ALL_RESPONSES_RECEIVED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_DEFENCE).onlyWhen(fullDefence)
            .moveTo(FULL_DEFENCE).onlyWhen(fullDefenceSpec)
            .moveTo(FULL_ADMISSION).onlyWhen(fullAdmission.and(not(divergentRespondGoOffline)))
            .moveTo(PART_ADMISSION).onlyWhen(partAdmission.and(not(divergentRespondGoOffline)))
            .moveTo(COUNTER_CLAIM).onlyWhen(counterClaim.and(not(divergentRespondGoOffline)))
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(divergentRespondGoOffline)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(divergentRespondGoOfflineSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(fullAdmissionSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(partAdmissionSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(counterClaimSpec)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyWhen(divergentRespondWithDQAndGoOffline)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyWhen(divergentRespondWithDQAndGoOfflineSpec)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterClaimDetailsNotified)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA).onlyWhen(caseDismissedAfterDetailNotified);
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

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = AllResponsesReceivedTransitionBuilder::isDivergentResponsesWithDQAndGoOffline;

    public static boolean isDivergentResponsesWithDQAndGoOffline(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP ->
                //scenario: either of them have submitted full defence response
                !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    || caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE));
            case ONE_V_TWO_TWO_LEGAL_REP ->
                //scenario: latest response is full defence
                !Objects.equals(caseData.getRespondent1ClaimResponseType(), caseData.getRespondent2ClaimResponseType())
                    && ((caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()))
                    || (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate())));
            case TWO_V_ONE ->
                (RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                    || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()))
                    && !(RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                    && RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default -> false;
        };
    }

    public static final Predicate<CaseData> divergentRespondGoOffline = AllResponsesReceivedTransitionBuilder::isDivergentResponsesGoOffline;

    private static boolean isDivergentResponsesGoOffline(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP ->
                !Objects.equals(caseData.getRespondent1ClaimResponseType(), caseData.getRespondent2ClaimResponseType())
                    && ((!caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate())
                    || !caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate()))
                    || (!caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && !caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)));
            case ONE_V_TWO_ONE_LEGAL_REP ->
                !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (!caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
                    && !caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE));
            case TWO_V_ONE ->
                !(RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                    || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default -> false;
        };
    }
}
