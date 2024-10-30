package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetGenericResponseTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    private static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData =
            caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        setFlagBasedOnClaimants(caseData, updatedData);
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        handleMultiPartyScenario(caseData, updatedData, multiPartyScenario);
        setSpecFullAdmissionOrPartAdmission(caseData, updatedData, multiPartyScenario);
        handleLegalRepScenario(callbackParams, caseData, updatedData, multiPartyScenario);
        setFullDefenceFlags(caseData, updatedData);
        setShowHowToAddTimeLinePage(caseData, updatedData);
        setPartOrFullAdmissionFlags(caseData, updatedData);
        updateShowConditionFlags(caseData, updatedData);
        additionalFlagSettings(caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private void setFlagBasedOnClaimants(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isClaimant1ConditionMet(caseData) && isClaimant2ConditionMet(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
    }

    private boolean isClaimant1ConditionMet(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getClaimant1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getClaimant1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.COUNTER_CLAIM == caseData.getClaimant1ClaimResponseTypeForSpec();
    }

    private boolean isClaimant2ConditionMet(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getClaimant2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getClaimant2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.COUNTER_CLAIM == caseData.getClaimant2ClaimResponseTypeForSpec();
    }

    private void handleMultiPartyScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario scenario) {
        if (ONE_V_ONE == scenario) {
            handleOneVOneScenario(caseData, updatedData);
        }
    }

    private void handleOneVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        RespondentResponseTypeSpec respondent1Response = caseData.getRespondent1ClaimResponseTypeForSpec();

        if (RespondentResponseTypeSpec.FULL_DEFENCE == respondent1Response) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        } else if (RespondentResponseTypeSpec.COUNTER_CLAIM == respondent1Response) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        } else if (RespondentResponseTypeSpec.FULL_ADMISSION == respondent1Response
            || RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
        }
    }

    private void handleOneVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleTwoVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
            || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            if (caseData.getRespondentResponseIsSame() == YES
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
            } else {
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isSameResponse(caseData)) {
            handleSameResponse(caseData, updatedData);
        } else {
            handleDifferentResponses(caseData, updatedData);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
            && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }

        RespondentResponseTypeSpec respondent1Response = caseData.getRespondent1ClaimResponseTypeForSpec();
        RespondentResponseTypeSpec respondent2Response = caseData.getRespondent2ClaimResponseTypeForSpec();
        if ((YES.equals(caseData.getIsRespondent1()) && RespondentResponseTypeSpec.PART_ADMISSION == respondent1Response)
            || (YES.equals(caseData.getIsRespondent2()) && RespondentResponseTypeSpec.PART_ADMISSION == respondent2Response)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
        }
    }

    private void setSpecFullAdmissionOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario scenario) {
        Set<RespondentResponseTypeSpec> someAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);
        if (TWO_V_ONE == scenario
            && someAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && someAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }
    }

    private void handleLegalRepScenario(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario scenario) {
        if (ONE_V_TWO_ONE_LEGAL_REP == scenario) {
            handleOneVTwoOneLegalRepScenario(caseData, updatedData);
        } else if (ONE_V_TWO_TWO_LEGAL_REP == scenario) {
            handleOneVTwoTwoLegalRepScenario(callbackParams, caseData, updatedData);
        }
    }

    private boolean isSameResponse(CaseData caseData) {
        return Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(), caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private void handleSameResponse(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondentResponseIsSame(YES);
        caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
        updatedData.sameSolicitorSameResponse(YES);
        updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        setMultiPartyResponseTypeFlags(updatedData, caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private void handleDifferentResponses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sameSolicitorSameResponse(NO);
        RespondentResponseTypeSpec respondent1Response = caseData.getRespondent1ClaimResponseTypeForSpec();
        RespondentResponseTypeSpec respondent2Response = caseData.getRespondent2ClaimResponseTypeForSpec();
        if (RespondentResponseTypeSpec.FULL_DEFENCE == respondent1Response || RespondentResponseTypeSpec.FULL_DEFENCE == respondent2Response) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);
        }
    }

    private void setMultiPartyResponseTypeFlags(CaseData.CaseDataBuilder<?, ?> updatedData, RespondentResponseTypeSpec response) {
        if (RespondentResponseTypeSpec.FULL_DEFENCE == response || RespondentResponseTypeSpec.COUNTER_CLAIM == response) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }
    }

    private void setFullDefenceFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getClaimant1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getClaimant2ClaimResponseTypeForSpec()) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }
    }

    private void setShowHowToAddTimeLinePage(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (shouldShowHowToAddTimeLinePage(caseData)) {
            updatedData.showHowToAddTimeLinePage(YES);
        }

        if (shouldHideHowToAddTimeLinePageForRespondent1(caseData)) {
            updatedData.showHowToAddTimeLinePage(NO);
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        } else if (shouldHideHowToAddTimeLinePageForRespondent2(caseData)) {
            updatedData.showHowToAddTimeLinePage(NO);
        }
    }

    private boolean shouldShowHowToAddTimeLinePage(CaseData caseData) {
        return YES.equals(caseData.getSpecPaidLessAmountOrDisputesOrPartAdmission())
            && !MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART.equals(caseData.getMultiPartyResponseTypeFlags())
            && !RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT
            .equals(caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec());
    }

    private boolean shouldHideHowToAddTimeLinePageForRespondent1(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent1())
            && RespondentResponseTypeSpec.COUNTER_CLAIM == caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private boolean shouldHideHowToAddTimeLinePageForRespondent2(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent2())
            && RespondentResponseTypeSpec.COUNTER_CLAIM == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec();
    }

    private void setPartOrFullAdmissionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isPartAdmittedByEitherRespondents(caseData)) {
            updatedData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedData.partAdmittedByEitherRespondents(NO);
        }

        if (isFullAdmissionAndFullAmountPaid(caseData)) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedData.fullAdmissionAndFullAmountPaid(NO);
        }

        if (isDefenceAdmitPartPaymentTimeRouteRequired(caseData)) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (isDefenceAdmitPartPaymentTimeRouteRequired2(caseData)) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        } else {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(IMMEDIATELY);
        }
    }

    private boolean isPartAdmittedByEitherRespondents(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmittedRequired()))
            || (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmitted2Required()));
    }

    private boolean isFullAdmissionAndFullAmountPaid(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required()))
            || (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired()));
    }

    private boolean isDefenceAdmitPartPaymentTimeRouteRequired(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null;
    }

    private boolean isDefenceAdmitPartPaymentTimeRouteRequired2(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null;
    }

    private void updateShowConditionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Set<DefendantResponseShowTag> updatedShowConditions = whoDisputesPartAdmission(caseData);
        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
            RespondentResponseTypeSpec.PART_ADMISSION,
            RespondentResponseTypeSpec.FULL_ADMISSION
        );

        if (shouldAddRespondent1AdmitsPartOrFull(caseData, updatedShowConditions, anyAdmission)) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            if (YES.equals(caseData.getRespondentResponseIsSame())) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            }
        }

        if (shouldAddRespondent2AdmitsPartOrFull(caseData, anyAdmission)) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
        }

        if (someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
        }

        if (shouldAddCurrentAdmitsPartOrFull(caseData, anyAdmission)) {
            updatedShowConditions.removeIf(condition -> condition == CURRENT_ADMITS_PART_OR_FULL);
            updatedShowConditions.add(CURRENT_ADMITS_PART_OR_FULL);
        }

        updatedData.showConditionFlags(updatedShowConditions);
    }

    private boolean shouldAddRespondent1AdmitsPartOrFull(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions, EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        return updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
            && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private boolean shouldAddRespondent2AdmitsPartOrFull(CaseData caseData, EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        return caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean shouldAddCurrentAdmitsPartOrFull(CaseData caseData, EnumSet<RespondentResponseTypeSpec> anyAdmission) {
        return (anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent1()))
            || (anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent2()));
    }

    private void additionalFlagSettings(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isSpecFullAdmissionOrPartAdmission(caseData)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }

        if (isCounterAdmitOrAdmitPart(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }

        if (isSpecFullDefenceOrPartAdmission1V1(caseData)) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
        }

        if (isSpecFullDefenceOrPartAdmission(caseData)) {
            updatedData.specFullDefenceOrPartAdmission(YES);
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
        }

        if (isSpecDefenceFullAdmittedRequired(caseData)) {
            updatedData.specDefenceFullAdmittedRequired(NO);
        }
    }

    private boolean isSpecFullAdmissionOrPartAdmission(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent1())
            && (RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()))
            || (YES.equals(caseData.getIsRespondent2())
            && (RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private boolean isCounterAdmitOrAdmitPart(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.COUNTER_CLAIM == caseData.getRespondent2ClaimResponseTypeForSpec();
    }

    private boolean isSpecFullDefenceOrPartAdmission1V1(CaseData caseData) {
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private boolean isSpecFullDefenceOrPartAdmission(CaseData caseData) {
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent2ClaimResponseTypeForSpec();
    }

    private boolean isSpecDefenceFullAdmittedRequired(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION != caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.FULL_ADMISSION != caseData.getRespondent2ClaimResponseTypeForSpec();
    }

    private Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        removeWhoDisputesAndWhoPaidLess(tags);
        tags.addAll(whoDisputesBcoPartAdmission(caseData));
        return tags;
    }

    private boolean someoneDisputes(CaseData caseData) {
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

    private void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
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

    private Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

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
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }
}
