package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Component
@RequiredArgsConstructor
@Slf4j
public class RespondToClaimSpecUtils {

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    private final LocationReferenceDataService locationRefDataService;
    private final UserService userService;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;

    public boolean isWhenWillClaimBePaidShown(CaseData caseData) {
        log.debug("Evaluating if 'When Will Claim Be Paid' should be shown for case ID: {}", caseData.getCcdCaseReference());
        boolean result = isRespondent1AdmitsAndNotPaid(caseData) || isRespondent2AdmitsAndNotPaid(caseData);
        log.debug("'When Will Claim Be Paid' visibility for case ID {}: {}", caseData.getCcdCaseReference(), result);
        return result;
    }

    private boolean isRespondent1AdmitsAndNotPaid(CaseData caseData) {
        boolean condition = caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)
            && (caseData.getSpecDefenceFullAdmittedRequired() == NO
            || caseData.getSpecDefenceAdmittedRequired() == NO);
        log.debug("Respondent 1 admits and not paid condition for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isRespondent2AdmitsAndNotPaid(CaseData caseData) {
        boolean condition = caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && (caseData.getSpecDefenceFullAdmitted2Required() == NO
            || caseData.getSpecDefenceAdmitted2Required() == NO);
        log.debug("Respondent 2 admits and not paid condition for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    public boolean isRespondent1DoesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        log.debug("Checking if Respondent 1 does not pay immediately for case ID: {}", caseData.getCcdCaseReference());
        boolean condition = isRespondent1(caseData) && isNotCounterClaimOrFullDefence(caseData)
            && (isOneVTwoOneLegalRepAndSameResponse(caseData, scenario) || isNotOneVTwoOneLegalRep(scenario))
            && isPaymentNotImmediate(caseData) && isSpecDefenceNotAdmitted(caseData);
        log.debug("Respondent 1 does not pay immediately condition for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isRespondent1(CaseData caseData) {
        boolean condition = YES.equals(caseData.getIsRespondent1());
        log.debug("Is Respondent 1 for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isNotCounterClaimOrFullDefence(CaseData caseData) {
        boolean condition = caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
        log.debug("Is not counter claim or full defence for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isOneVTwoOneLegalRepAndSameResponse(CaseData caseData, MultiPartyScenario scenario) {
        boolean condition = scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES;
        log.debug("Is One vs Two One Legal Representative and same response for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isNotOneVTwoOneLegalRep(MultiPartyScenario scenario) {
        boolean condition = scenario != ONE_V_TWO_ONE_LEGAL_REP;
        log.debug("Is not One vs Two One Legal Representative scenario: {}", condition);
        return condition;
    }

    private boolean isNotOneVTwoOneLegalRep(CaseData caseData, MultiPartyScenario scenario) {
        boolean condition = caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP;
        log.debug("Is not One vs Two One Legal Representative and same response or scenario is One vs Two Two Legal Representative: {}", condition);
        return condition;
    }

    private boolean isPaymentNotImmediate(CaseData caseData) {
        boolean condition = caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY;
        log.debug("Is payment not immediate for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isSpecDefenceNotAdmitted(CaseData caseData) {
        boolean condition = caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getSpecDefenceAdmittedRequired() != YES;
        log.debug("Is speculative defence not admitted for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    public boolean isRespondent2DoesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        log.debug("Checking if Respondent 2 does not pay immediately for case ID: {}", caseData.getCcdCaseReference());
        boolean condition = isNotCounterClaimOrFullDefence(caseData)
            && (isOneVTwoOneLegalRepAndSameResponse(caseData, scenario) || isNotOneVTwoOneLegalRep(caseData, scenario))
            && isPaymentNotImmediateForRespondent2(caseData)
            && isSpecDefenceNotAdmittedForRespondent2(caseData);
        log.debug("Respondent 2 does not pay immediately condition for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData) {
        boolean condition = caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY;
        log.debug("Is payment not immediate for Respondent 2 in case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isSpecDefenceNotAdmittedForRespondent2(CaseData caseData) {
        boolean condition = caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getSpecDefenceAdmitted2Required() != YES;
        log.debug("Is speculative defence not admitted for Respondent 2 in case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    public boolean isRespondent2HasSameLegalRep(CaseData caseData) {
        boolean condition = caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
        log.debug("Does Respondent 2 have the same legal representative for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
        log.debug("Removing specific dispute and payment tags from the set");
        tags.removeIf(EnumSet.of(
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
            DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE,
            SOMEONE_DISPUTES,
            DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS,
            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS,
            WHEN_WILL_CLAIM_BE_PAID,
            RESPONDENT_1_ADMITS_PART_OR_FULL,
            RESPONDENT_2_ADMITS_PART_OR_FULL,
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            REPAYMENT_PLAN_2,
            DefendantResponseShowTag.MEDIATION
        )::contains);
        log.debug("Removed specified dispute and payment tags");
    }

    public Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        log.debug("Determining who disputes based on part admission for case ID: {}", caseData.getCcdCaseReference());
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        log.debug("Multi-party scenario for case ID {}: {}", caseData.getCcdCaseReference(), mpScenario);

        switch (mpScenario) {
            case ONE_V_ONE:
                handleOneVOneScenario(caseData, tags);
                break;
            case TWO_V_ONE:
                handleTwoVOneScenario(caseData, tags);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRepScenario(caseData, tags);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRepScenario(caseData, tags);
                break;
            default:
                log.error("Unsupported multi-party scenario '{}' for case ID: {}", mpScenario, caseData.getCcdCaseReference());
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        log.debug("Determined dispute tags for case ID {}: {}", caseData.getCcdCaseReference(), tags);
        return tags;
    }

    private void handleOneVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.debug("Handling One vs One scenario for case ID: {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("Respondent 1 has partially admitted the claim in One vs One scenario");
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleTwoVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.debug("Handling Two vs One scenario for case ID: {}", caseData.getCcdCaseReference());
        if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
            || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("At least one claimant has partially admitted the claim in Two vs One scenario");
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.debug("Handling One vs Two One Legal Representative scenario for case ID: {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            if (caseData.getRespondentResponseIsSame() == YES
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                log.debug("Both respondents dispute the claim in One vs Two One Legal Representative scenario");
                tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
            } else {
                log.debug("Only Respondent 1 disputes the claim in One vs Two One Legal Representative scenario");
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("Only Respondent 2 disputes the claim in One vs Two One Legal Representative scenario");
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.debug("Handling One vs Two Two Legal Representative scenario for case ID: {}", caseData.getCcdCaseReference());
        if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("Respondent 1 disputes the claim in One vs Two Two Legal Representative scenario");
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
            && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("Respondent 2 disputes the claim in One vs Two Two Legal Representative scenario");
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    public boolean isSomeoneDisputes(CaseData caseData) {
        log.debug("Determining if someone disputes the claim for case ID: {}", caseData.getCcdCaseReference());
        if (isTwoVOneScenario(caseData)) {
            boolean condition = isClaimantDisputes(caseData) || isRespondent1Disputes(caseData);
            log.debug("Is someone disputes in Two vs One scenario for case ID {}: {}", caseData.getCcdCaseReference(), condition);
            return condition;
        } else {
            boolean condition = isRespondentDisputes(caseData, CAN_ANSWER_RESPONDENT_1, caseData.getRespondent1ClaimResponseTypeForSpec())
                || isRespondentDisputes(caseData, CAN_ANSWER_RESPONDENT_2, caseData.getRespondent2ClaimResponseTypeForSpec());
            log.debug("Is someone disputes in other scenarios for case ID {}: {}", caseData.getCcdCaseReference(), condition);
            return condition;
        }
    }

    private boolean isTwoVOneScenario(CaseData caseData) {
        boolean condition = TWO_V_ONE.equals(getMultiPartyScenario(caseData));
        log.debug("Is Two vs One scenario for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isClaimantDisputes(CaseData caseData) {
        boolean condition = caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE;
        log.debug("Does claimant dispute in case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isRespondent1Disputes(CaseData caseData) {
        boolean condition = caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION;
        log.debug("Does Respondent 1 dispute in case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }

    private boolean isRespondentDisputes(CaseData caseData, DefendantResponseShowTag respondent, RespondentResponseTypeSpec response) {
        boolean condition = caseData.getShowConditionFlags().contains(respondent)
            && (response == FULL_DEFENCE || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
        log.debug("Is Respondent {} disputes with response type {} for case ID {}: {}", respondent, response, caseData.getCcdCaseReference(), condition);
        return condition;
    }

    public List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        log.debug("Fetching location data with auth token for callback");
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
        log.debug("Retrieved {} location data entries", locations.size());
        return locations;
    }

    public boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        log.debug("Checking if solicitor represents only one of the respondents for case role '{}' and callback ID: {}",
                  caseRole, callbackParams.getCaseData().getCcdCaseReference());
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        boolean condition = stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
        log.debug("Solicitor represents only one respondent condition for case ID {}: {}", caseData.getCcdCaseReference(), condition);
        return condition;
    }
}
