package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
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
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SHOW_ADMITTED_AMOUNT_SCREEN;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetGenericResponseTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService featureToggleService;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
                .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        handleClaimantResponseTypeForSpec(caseData, updatedData);
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        handleOneVOneScenario(caseData, updatedData, multiPartyScenario);
        handleTwoVOneScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoOneLegalRepScenario(caseData, updatedData, multiPartyScenario);
        handleOneVTwoTwoLegalRepScenario(callbackParams, caseData, updatedData, multiPartyScenario);
        handleRespondentResponseTypeForSpec(caseData, updatedData);
        handleDefenceAdmitPartPaymentTimeRoute(caseData, updatedData);

        Set<DefendantResponseShowTag> updatedShowConditions = whoDisputesPartAdmission(caseData);
        updateShowConditions(caseData, updatedShowConditions);

        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && YES.equals(caseData.getIsRespondent1())
                && featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            updatedData.specDefenceFullAdmittedRequired(NO);
            updatedData.fullAdmissionAndFullAmountPaid(NO);
            updatedData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
            updatedData.specDisputesOrPartAdmission(NO);
            updatedData.specFullAdmitPaid(NO);
            updatedShowConditions.removeAll(EnumSet.of(
                    NEED_FINANCIAL_DETAILS_1,
                    NEED_FINANCIAL_DETAILS_2,
                    WHY_1_DOES_NOT_PAY_IMMEDIATELY,
                    WHY_2_DOES_NOT_PAY_IMMEDIATELY,
                    WHEN_WILL_CLAIM_BE_PAID,
                    SHOW_ADMITTED_AMOUNT_SCREEN
            ));
            if (mustWhenWillClaimBePaidBeShown(updatedData.build())) {
                updatedShowConditions.add(WHEN_WILL_CLAIM_BE_PAID);
            }
        } else if (
                RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                        && YES.equals(caseData.getIsRespondent2())
                        && featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            updatedData.specDefenceFullAdmitted2Required(NO);
            updatedShowConditions.removeAll(EnumSet.of(
                    NEED_FINANCIAL_DETAILS_1,
                    NEED_FINANCIAL_DETAILS_2,
                    WHY_1_DOES_NOT_PAY_IMMEDIATELY,
                    WHY_2_DOES_NOT_PAY_IMMEDIATELY,
                    WHEN_WILL_CLAIM_BE_PAID,
                    SHOW_ADMITTED_AMOUNT_SCREEN
            ));
            if (mustWhenWillClaimBePaidBeShown(updatedData.build())) {
                updatedShowConditions.add(WHEN_WILL_CLAIM_BE_PAID);
            }
        } else {
            updatedShowConditions.add(SHOW_ADMITTED_AMOUNT_SCREEN);
        }

        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
    }

    private void handleClaimantResponseTypeForSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Handling claimant response type for caseId: {}", caseData.getCcdCaseReference());

        if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
                &&
                (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                        || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                        || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            log.debug("CaseId {}: Updated multi-party response type flags to COUNTER_ADMIT_OR_ADMIT_PART", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed handling claimant response type", caseData.getCcdCaseReference());
    }

    private void handleOneVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        log.info("Handling One V One scenario for caseId: {}", caseData.getCcdCaseReference());

        if (ONE_V_ONE.equals(multiPartyScenario)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("CaseId {}: Set respondent claim response type for spec generic", caseData.getCcdCaseReference());

            if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
                log.debug("CaseId {}: Updated multi-party response type flags to FULL_DEFENCE", caseData.getCcdCaseReference());
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.COUNTER_CLAIM) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
                log.debug("CaseId {}: Updated multi-party response type flags to COUNTER_ADMIT_OR_ADMIT_PART", caseData.getCcdCaseReference());
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                    || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
                log.debug("CaseId {}: Updated multi-party response type flags to FULL_ADMISSION", caseData.getCcdCaseReference());
            }
        }

        log.info("CaseId {}: Completed handling One V One scenario", caseData.getCcdCaseReference());
    }

    private void handleTwoVOneScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        log.info("Handling Two V One scenario for caseId: {}", caseData.getCcdCaseReference());

        Set<RespondentResponseTypeSpec> someAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);
        if (TWO_V_ONE.equals(multiPartyScenario)
                && someAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
                && someAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
            log.debug("CaseId {}: Updated specFullAdmissionOrPartAdmission to YES", caseData.getCcdCaseReference());
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
            log.debug("CaseId {}: Updated specFullAdmissionOrPartAdmission to NO", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed handling Two V One scenario", caseData.getCcdCaseReference());
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, MultiPartyScenario multiPartyScenario) {
        log.info("Handling One V Two One Legal Rep scenario for caseId: {}", caseData.getCcdCaseReference());

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)
                && Objects.equals(caseData.getRespondent1ClaimResponseTypeForSpec(), caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.respondentResponseIsSame(YES);
            caseData = caseData.toBuilder().respondentResponseIsSame(YES).build();
            log.debug("CaseId {}: Respondent responses are the same", caseData.getCcdCaseReference());
        }
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario) && caseData.getRespondentResponseIsSame().equals(NO)) {
            updatedData.sameSolicitorSameResponse(NO);
            log.debug("CaseId {}: Same solicitor same response set to NO", caseData.getCcdCaseReference());
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
                log.debug("CaseId {}: Respondent claim response type set to FULL_DEFENCE", caseData.getCcdCaseReference());
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario) && caseData.getRespondentResponseIsSame().equals(YES)) {
            updatedData.sameSolicitorSameResponse(YES);
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("CaseId {}: Same solicitor same response set to YES", caseData.getCcdCaseReference());
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
                log.debug("CaseId {}: Multi-party response type flags set to FULL_DEFENCE", caseData.getCcdCaseReference());
            }
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            log.debug("CaseId {}: Respondent claim response type set to generic", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed handling One V Two One Legal Rep scenario", caseData.getCcdCaseReference());
    }

    private void handleOneVTwoTwoLegalRepScenario(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                                  MultiPartyScenario multiPartyScenario) {
        log.info("Handling One V Two Two Legal Rep scenario for caseId: {}", caseData.getCcdCaseReference());

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
                log.debug("CaseId {}: Set respondent claim response type for spec generic to Respondent 2", caseData.getCcdCaseReference());
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
                log.debug("CaseId {}: Set respondent claim response type for spec generic to Respondent 1", caseData.getCcdCaseReference());
            }
        }

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)
                && ((YES.equals(caseData.getIsRespondent1())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
                || (YES.equals(caseData.getIsRespondent2())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            log.debug("CaseId {}: Updated multi-party response type flags to PART_ADMISSION", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed handling One V Two Two Legal Rep scenario", caseData.getCcdCaseReference());
    }

    private void handleRespondentResponseTypeForSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Handling respondent response type for caseId: {}", caseData.getCcdCaseReference());

        setRespondentClaimResponseTypeForSpecGeneric(caseData, updatedData);
        log.debug("CaseId {}: Set respondent claim response type for spec generic", caseData.getCcdCaseReference());

        setMultiPartyResponseTypeFlags(caseData, updatedData);
        log.debug("CaseId {}: Set multi-party response type flags", caseData.getCcdCaseReference());

        setSpecFullAdmissionOrPartAdmission(caseData, updatedData);
        log.debug("CaseId {}: Set spec full admission or part admission", caseData.getCcdCaseReference());

        setSpecFullDefenceOrPartAdmission(caseData, updatedData);
        log.debug("CaseId {}: Set spec full defence or part admission", caseData.getCcdCaseReference());

        setSpecDefenceFullAdmittedRequired(caseData, updatedData);
        log.debug("CaseId {}: Set spec defence full admitted required", caseData.getCcdCaseReference());

        setShowHowToAddTimeLinePage(caseData, updatedData);
        log.debug("CaseId {}: Set show how to add timeline page", caseData.getCcdCaseReference());

        setPartAdmittedByEitherRespondents(caseData, updatedData);
        log.debug("CaseId {}: Set part admitted by either respondents", caseData.getCcdCaseReference());

        setFullAdmissionAndFullAmountPaid(caseData, updatedData);
        log.debug("CaseId {}: Set full admission and full amount paid", caseData.getCcdCaseReference());

        log.info("CaseId {}: Completed handling respondent response type", caseData.getCcdCaseReference());
    }

    private void setRespondentClaimResponseTypeForSpecGeneric(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent2())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
            log.debug("CaseId {}: Respondent claim response type for spec generic set to Respondent 2", caseData.getCcdCaseReference());
        }

    }

    private void setMultiPartyResponseTypeFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isAnyRespondentOrClaimantFullDefence(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            log.debug("CaseId {}: Multi-party response type flags set to FULL_DEFENCE", caseData.getCcdCaseReference());
        }

        if (isRespondent2AdmitOrCounterClaim(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            log.debug("CaseId {}: Multi-party response type flags set to COUNTER_ADMIT_OR_ADMIT_PART", caseData.getCcdCaseReference());
        }
    }

    private boolean isAnyRespondentOrClaimantFullDefence(CaseData caseData) {
        log.debug("Checking if any respondent or claimant has full defence for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isRespondent2AdmitOrCounterClaim(CaseData caseData) {
        log.debug("Checking if respondent 2 admits or counter claims for caseId: {}", caseData.getCcdCaseReference());
        return RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private void setSpecFullAdmissionOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting spec full admission or part admission for caseId: {}", caseData.getCcdCaseReference());

        if (isRespondent1Admitting(caseData) || isRespondent2Admitting(caseData)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
            log.debug("CaseId {}: Spec full admission or part admission set to YES", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed setting spec full admission or part admission", caseData.getCcdCaseReference());
    }

    private boolean isRespondent1Admitting(CaseData caseData) {
        log.debug("Checking if respondent 1 admits part or full for caseId: {}", caseData.getCcdCaseReference());
        return YES.equals(caseData.getIsRespondent1())
                && (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION);
    }

    private boolean isRespondent2Admitting(CaseData caseData) {
        log.debug("Checking if respondent 2 admits part or full for caseId: {}", caseData.getCcdCaseReference());
        return YES.equals(caseData.getIsRespondent2())
                && (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION);
    }

    private void setSpecFullDefenceOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (isRespondent1DefendingOrAdmitting(caseData)) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
            log.debug("CaseId {}: Spec full defence or part admission 1V1 set to YES", caseData.getCcdCaseReference());
        }
        if (isAnyRespondentDefendingOrAdmitting(caseData)) {
            updatedData.specFullDefenceOrPartAdmission(YES);
            log.debug("CaseId {}: Spec full defence or part admission set to YES", caseData.getCcdCaseReference());
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
            log.debug("CaseId {}: Spec full defence or part admission set to NO", caseData.getCcdCaseReference());
        }
    }

    private boolean isRespondent1DefendingOrAdmitting(CaseData caseData) {
        log.debug("Checking if respondent 1 is defending or admitting for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE;
    }

    private boolean isAnyRespondentDefendingOrAdmitting(CaseData caseData) {
        log.debug("Checking if any respondent is defending or admitting for caseId: {}", caseData.getCcdCaseReference());
        return isRespondent1DefendingOrAdmitting(caseData)
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE;
    }

    private void setSpecDefenceFullAdmittedRequired(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specDefenceFullAdmittedRequired(NO);
            log.debug("CaseId {}: Spec defence full admitted required set to NO", caseData.getCcdCaseReference());
        }
    }

    private void setShowHowToAddTimeLinePage(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getSpecPaidLessAmountOrDisputesOrPartAdmission())
                && !MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART.equals(caseData.getMultiPartyResponseTypeFlags())
                && (!RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.equals(caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()))) {
            updatedData.showHowToAddTimeLinePage(YES);
            log.debug("CaseId {}: Show how to add timeline page set to YES", caseData.getCcdCaseReference());
        }
    }

    private void setPartAdmittedByEitherRespondents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting part admitted by either respondents for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedData.partAdmittedByEitherRespondents(YES);
            log.debug("CaseId {}: Part admitted by either respondents set to YES", caseData.getCcdCaseReference());
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedData.partAdmittedByEitherRespondents(YES);
            log.debug("CaseId {}: Part admitted by either respondents set to YES", caseData.getCcdCaseReference());
        } else {
            updatedData.partAdmittedByEitherRespondents(NO);
            log.debug("CaseId {}: Part admitted by either respondents set to NO", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed setting part admitted by either respondents", caseData.getCcdCaseReference());
    }

    private void setFullAdmissionAndFullAmountPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting full admission and full amount paid for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
            log.debug("CaseId {}: Full admission and full amount paid set to YES", caseData.getCcdCaseReference());
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
            log.debug("CaseId {}: Full admission and full amount paid set to YES", caseData.getCcdCaseReference());
        } else {
            updatedData.fullAdmissionAndFullAmountPaid(NO);
            log.debug("CaseId {}: Full admission and full amount paid set to NO", caseData.getCcdCaseReference());
        }

        log.info("CaseId {}: Completed setting full admission and full amount paid", caseData.getCcdCaseReference());
    }

    private void handleDefenceAdmitPartPaymentTimeRoute(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            log.debug("CaseId {}: Defence admit part payment time route generic set to Respondent 1", caseData.getCcdCaseReference());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
            log.debug("CaseId {}: Defence admit part payment time route generic set to Respondent 2", caseData.getCcdCaseReference());
        } else {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(IMMEDIATELY);
            log.debug("CaseId {}: Defence admit part payment time route generic set to IMMEDIATELY", caseData.getCcdCaseReference());
        }
    }

    private void updateShowConditions(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        log.info("Updating show conditions for caseId: {}", caseData.getCcdCaseReference());

        addRespondent1AdmitsPartOrFull(caseData, updatedShowConditions);
        log.debug("CaseId {}: Added Respondent 1 admits part or full condition", caseData.getCcdCaseReference());

        addRespondent2AdmitsPartOrFull(caseData, updatedShowConditions);
        log.debug("CaseId {}: Added Respondent 2 admits part or full condition", caseData.getCcdCaseReference());

        addSomeoneDisputes(caseData, updatedShowConditions);
        log.debug("CaseId {}: Added someone disputes condition", caseData.getCcdCaseReference());

        addCurrentAdmitsPartOrFull(caseData, updatedShowConditions);
        log.debug("CaseId {}: Added current admits part or full condition", caseData.getCcdCaseReference());

        log.info("CaseId {}: Completed updating show conditions", caseData.getCcdCaseReference());
    }

    private void addRespondent1AdmitsPartOrFull(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        log.info("Adding Respondent 1 admits part or full condition for caseId: {}", caseData.getCcdCaseReference());

        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
                RespondentResponseTypeSpec.PART_ADMISSION,
                RespondentResponseTypeSpec.FULL_ADMISSION
        );
        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
                && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            log.debug("CaseId {}: Respondent 1 admits part or full condition added", caseData.getCcdCaseReference());

            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
                log.debug("CaseId {}: Respondent 2 admits part or full condition added due to same response", caseData.getCcdCaseReference());
            }
        }
    }

    private void addRespondent2AdmitsPartOrFull(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        log.info("Adding Respondent 2 admits part or full condition for caseId: {}", caseData.getCcdCaseReference());

        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
                RespondentResponseTypeSpec.PART_ADMISSION,
                RespondentResponseTypeSpec.FULL_ADMISSION
        );
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
                && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            log.debug("CaseId {}: Respondent 2 admits part or full condition added", caseData.getCcdCaseReference());
        }
    }

    private void addSomeoneDisputes(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        if (someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
            log.debug("CaseId {}: Someone disputes condition added", caseData.getCcdCaseReference());
        }
    }

    private void addCurrentAdmitsPartOrFull(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        log.info("Adding current admits part or full condition for caseId: {}", caseData.getCcdCaseReference());

        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
                RespondentResponseTypeSpec.PART_ADMISSION,
                RespondentResponseTypeSpec.FULL_ADMISSION
        );
        if ((anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
                && YES.equals(caseData.getIsRespondent1()))
                || (anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())
                && YES.equals(caseData.getIsRespondent2()))) {
            updatedShowConditions.removeIf(EnumSet.of(CURRENT_ADMITS_PART_OR_FULL)::contains);
            updatedShowConditions.add(CURRENT_ADMITS_PART_OR_FULL);
            log.debug("CaseId {}: Current admits part or full condition added", caseData.getCcdCaseReference());
        }
    }

    private Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        log.info("Determining who disputes part admission for caseId: {}", caseData.getCcdCaseReference());

        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        log.debug("CaseId {}: Initial show condition flags: {}", caseData.getCcdCaseReference(), tags);

        respondToClaimSpecUtils.removeWhoDisputesAndWhoPaidLess(tags);
        log.debug("CaseId {}: Removed who disputes and who paid less flags", caseData.getCcdCaseReference());

        tags.addAll(respondToClaimSpecUtils.whoDisputesBcoPartAdmission(caseData));
        log.debug("CaseId {}: Added who disputes BCO part admission flags", caseData.getCcdCaseReference());

        log.info("CaseId {}: Completed determining who disputes part admission", caseData.getCcdCaseReference());
        return tags;
    }

    private boolean someoneDisputes(CaseData caseData) {
        log.info("Checking if someone disputes for caseId: {}", caseData.getCcdCaseReference());

        boolean result;
        if (isTwoVOneScenario(caseData)) {
            result = isClaimantOrRespondentDisputing(caseData);
            log.debug("CaseId {}: Two V One scenario - someone disputes result: {}", caseData.getCcdCaseReference(), result);
        } else {
            result = isRespondentDisputing(caseData, CAN_ANSWER_RESPONDENT_1, caseData.getRespondent1ClaimResponseTypeForSpec())
                    || isRespondentDisputing(caseData, CAN_ANSWER_RESPONDENT_2, caseData.getRespondent2ClaimResponseTypeForSpec());
            log.debug("CaseId {}: Non-Two V One scenario - someone disputes result: {}", caseData.getCcdCaseReference(), result);
        }
        return result;
    }

    private boolean isTwoVOneScenario(CaseData caseData) {
        log.info("Checking if caseId {} is a Two V One scenario", caseData.getCcdCaseReference());
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    private boolean isClaimantOrRespondentDisputing(CaseData caseData) {
        log.info("Checking if claimant or respondent is disputing for caseId: {}", caseData.getCcdCaseReference());
        return isClaimantDisputing(caseData) || isRespondentDisputing(caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private boolean isClaimantDisputing(CaseData caseData) {
        log.info("Checking if claimant is disputing for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE;
    }

    private boolean isRespondentDisputing(RespondentResponseTypeSpec responseType) {
        return responseType == FULL_DEFENCE || responseType == PART_ADMISSION;
    }

    private boolean isRespondentDisputing(CaseData caseData, DefendantResponseShowTag respondent, RespondentResponseTypeSpec response) {
        log.info("Checking if respondent {} is disputing for caseId: {}", respondent, caseData.getCcdCaseReference());
        return caseData.getShowConditionFlags().contains(respondent)
                && (response == FULL_DEFENCE || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
    }

    private boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            return mustBeShownForRespondent1(caseData);
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            return mustBeShownForRespondent2(caseData);
        }
        return false;
    }

    private boolean mustBeShownForRespondent1(CaseData caseData) {
        log.info("Checking if When Will Claim Be Paid must be shown for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return isAdmitPartNotPay(caseData.getSpecDefenceAdmittedRequired())
                || isAdmitFullNotPay(caseData.getSpecDefenceFullAdmittedRequired());
    }

    private boolean mustBeShownForRespondent2(CaseData caseData) {
        log.info("Checking if When Will Claim Be Paid must be shown for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        return isAdmitPartNotPay(caseData.getSpecDefenceAdmitted2Required())
                || isAdmitFullNotPay(caseData.getSpecDefenceFullAdmitted2Required());
    }

    private boolean isAdmitPartNotPay(YesOrNo admitPart) {
        return admitPart == NO;
    }

    private boolean isAdmitFullNotPay(YesOrNo admitFull) {
        return admitFull == NO;
    }
}
