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
public class RespondToClaimSpecUtils {

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    private final LocationReferenceDataService locationRefDataService;
    private final UserService userService;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;

    /**
     * WhenWillClaimBePaid has to be shown if the respondent admits (full or part) and say they didn't pay.
     * At the moment of writing, full admit doesn't ask for how much the respondent paid (if they say they paid)
     * and part admit doesn't ask when will the amount be paid even if paid less.
     *
     * @param caseData claim data
     * @return true if pageId WhenWillClaimBePaid must be shown
     */
    public boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        // 1v1 or 1v2 dif sol
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            // admit part not pay or admit full not pay
            return caseData.getSpecDefenceFullAdmittedRequired() == NO
                || caseData.getSpecDefenceAdmittedRequired() == NO;
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            // admit part not pay or admit full not pay
            return caseData.getSpecDefenceFullAdmitted2Required() == NO
                || caseData.getSpecDefenceAdmitted2Required() == NO;
        }

        return false;
    }

    public boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        if (YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES)) {
            return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                && caseData.getSpecDefenceFullAdmittedRequired() != YES
                && caseData.getSpecDefenceAdmittedRequired() != YES;
        }
        return false;
    }

    public boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        if (caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE) {
            if (scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmittedRequired() != YES
                    && caseData.getSpecDefenceAdmittedRequired() != YES;
            } else if (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmitted2Required() != YES
                    && caseData.getSpecDefenceAdmitted2Required() != YES;
            }
        }
        return false;
    }

    public boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
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
    }

    /**
     * Returns the flags that should be active because part admission has been chosen.
     *
     * @param caseData the claim info
     * @return flags to describe who disputes because a response is part admission
     */
    public Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case TWO_V_ONE:
                if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
                    || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                    || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    if (caseData.getRespondentResponseIsSame() == YES
                        || caseData.getRespondent2ClaimResponseTypeForSpec()
                        == RespondentResponseTypeSpec.PART_ADMISSION) {
                        tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
                    } else {
                        tags.add(ONLY_RESPONDENT_1_DISPUTES);
                    }
                } else if (caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }

    public boolean someoneDisputes(CaseData caseData) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return ((caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE)
                || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION);
        } else {
            return someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_1,
                                   caseData.getRespondent1ClaimResponseTypeForSpec()
            )
                || someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_2,
                                   caseData.getRespondent2ClaimResponseTypeForSpec()
            );
        }
    }

    private boolean someoneDisputes(CaseData caseData, DefendantResponseShowTag respondent,
                                    RespondentResponseTypeSpec response) {
        return caseData.getShowConditionFlags().contains(respondent)
            && (response == FULL_DEFENCE
            || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
    }

    public List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    public boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}
