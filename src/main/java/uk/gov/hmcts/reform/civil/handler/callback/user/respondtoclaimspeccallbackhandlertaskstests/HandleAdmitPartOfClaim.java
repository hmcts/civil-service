package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleAdmitPartOfClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing HandleAdmitPartOfClaim task for case ID: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        log.debug("Retrieved case data: {}", caseData);

        List<String> errors = validatePayments(caseData);
        if (!errors.isEmpty()) {
            log.warn("Validation errors found: {}", errors);
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        log.info("Updating admission flags for case ID: {}", caseData.getCcdCaseReference());
        updateAdmissionFlags(caseData, updatedCaseData);

        log.info("Updating payment route flags for case ID: {}", caseData.getCcdCaseReference());
        updatePaymentRouteFlags(caseData, updatedCaseData);

        log.info("Updating respondent's admission status for case ID: {}", caseData.getCcdCaseReference());
        updateRespondentsAdmissionStatus(caseData, updatedCaseData);

        log.info("Updating employment type for case ID: {}", caseData.getCcdCaseReference());
        updateEmploymentType(caseData, updatedCaseData);

        log.info("Updating claim owing amounts for case ID: {}", caseData.getCcdCaseReference());
        updateClaimOwingAmounts(caseData, updatedCaseData);

        log.info("Updating spec paid or dispute status for case ID: {}", caseData.getCcdCaseReference());
        updateSpecPaidOrDisputeStatus(caseData, updatedCaseData);

        log.info("Updating allocated track for case ID: {}", caseData.getCcdCaseReference());
        updateAllocatedTrack(callbackParams, caseData, updatedCaseData);

        log.info("Updating show condition flags for case ID: {}", caseData.getCcdCaseReference());
        updateShowConditionFlags(caseData, updatedCaseData);

        log.info("Task completed successfully for case ID: {}", caseData.getCcdCaseReference());
        return buildCallbackResponse(updatedCaseData);
    }

    private List<String> validatePayments(CaseData caseData) {
        log.debug("Validating payments for case ID: {}", caseData.getCcdCaseReference());
        return paymentDateValidator.validate(
            Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElse(RespondToClaim.builder().build())
        );
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        log.warn("Building error response with errors: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private void updateAdmissionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating admission flags for case ID: {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }
    }

    private void updatePaymentRouteFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating payment route flags for case ID: {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }
    }

    private void updateRespondentsAdmissionStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating respondents' admission status for case ID: {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
        }
    }

    private void updateEmploymentType(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating employment type for case ID: {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
    }

    private void updateClaimOwingAmounts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating claim owing amounts for case ID: {}", caseData.getCcdCaseReference());
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
    }

    private void updateSpecPaidOrDisputeStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating spec paid or dispute status for case ID: {}", caseData.getCcdCaseReference());
        if (isSpecPaidLessOrDispute(caseData)) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }

        if (isSpecDisputeOrPartAdmission(caseData)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
        }

        if (isPartAdmitNotPaid(caseData)) {
            updatedCaseData.specPartAdmitPaid(NO);
        }

        if (isFullAdmitNotPaid(caseData)) {
            updatedCaseData.specFullAdmitPaid(NO);
        }
    }

    private boolean isSpecPaidLessOrDispute(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private boolean isSpecDisputeOrPartAdmission(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private boolean isPartAdmitNotPaid(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO;
    }

    private boolean isFullAdmitNotPaid(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO;
    }

    private void updateAllocatedTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating allocated track for case ID: {}", caseData.getCcdCaseReference());
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            updatedCaseData.responseClaimTrack(getAllocatedTrack(caseData).name());
        }
    }

    private void updateShowConditionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Updating show condition flags for case ID: {}", caseData.getCcdCaseReference());
        Set<DefendantResponseShowTag> currentShowFlags = new HashSet<>(caseData.getShowConditionFlags());
        currentShowFlags.removeAll(EnumSet.of(
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            WHEN_WILL_CLAIM_BE_PAID
        ));
        currentShowFlags.addAll(checkNecessaryFinancialDetails(caseData));
        if (isWhenWillClaimBePaidShown(caseData)) {
            currentShowFlags.add(WHEN_WILL_CLAIM_BE_PAID);
        }
        updatedCaseData.showConditionFlags(currentShowFlags);
    }

    private boolean isWhenWillClaimBePaidShown(CaseData caseData) {
        return respondToClaimSpecUtils.isWhenWillClaimBePaidShown(caseData);
    }

    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        log.debug("Checking necessary financial details for case ID: {}", caseData.getCcdCaseReference());
        Set<DefendantResponseShowTag> necessary = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (isFinancialDetailsNeededForRespondent1(caseData, scenario)) {
            necessary.add(NEED_FINANCIAL_DETAILS_1);
        }
        if (respondToClaimSpecUtils.isRespondent1DoesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
        }

        if (isFinancialDetailsNeededForRespondent2(caseData, scenario)) {
            necessary.add(NEED_FINANCIAL_DETAILS_2);
        }
        if (respondToClaimSpecUtils.isRespondent2DoesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
        }
        if (isRepaymentPlan2Needed(caseData)) {
            necessary.add(REPAYMENT_PLAN_2);
        }

        return necessary;
    }

    private boolean isFinancialDetailsNeededForRespondent1(CaseData caseData, MultiPartyScenario scenario) {
        return caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1) && isFinancialDetailsNeeded(caseData, caseData.getRespondent1(), scenario);
    }

    private boolean isFinancialDetailsNeededForRespondent2(CaseData caseData, MultiPartyScenario scenario) {
        return caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2) && isFinancialDetailsNeeded(caseData, caseData.getRespondent2(), scenario);
    }

    private boolean isFinancialDetailsNeeded(CaseData caseData, Party respondent, MultiPartyScenario scenario) {
        return isNonCorporateParty(respondent) && (isScenarioTwoLegalRepAndNeedsInfo(caseData, scenario)
            || isScenarioOneLegalRepAndNeedsInfo(caseData, scenario));
    }

    private boolean isNonCorporateParty(Party respondent) {
        return respondent.getType() != Party.Type.COMPANY && respondent.getType() != Party.Type.ORGANISATION;
    }

    private boolean isScenarioTwoLegalRepAndNeedsInfo(CaseData caseData, MultiPartyScenario scenario) {
        return scenario == ONE_V_TWO_TWO_LEGAL_REP && isFinancialInfo21v2dsNeeded(caseData);
    }

    private boolean isScenarioOneLegalRepAndNeedsInfo(CaseData caseData, MultiPartyScenario scenario) {
        return scenario == ONE_V_TWO_ONE_LEGAL_REP && isFinancialInfo1Needed(caseData);
    }

    private boolean isFinancialInfo1Needed(CaseData caseData) {
        return isPaymentNotImmediate(caseData)
            && isSpecDefenceNotAdmitted(caseData)
            && isFullDefenceNotClaimed(caseData)
            && isNotCounterClaim(caseData)
            && isMultiplePartyResponseFlagsPresent(caseData)
            && isSameSolicitorAndResponseValid(caseData);
    }

    private boolean isPaymentNotImmediate(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY;
    }

    private boolean isSpecDefenceNotAdmitted(CaseData caseData) {
        return caseData.getSpecDefenceAdmittedRequired() != YES
            && caseData.getSpecDefenceFullAdmittedRequired() != YES;
    }

    private boolean isFullDefenceNotClaimed(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }

    private boolean isNotCounterClaim(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean isMultiplePartyResponseFlagsPresent(CaseData caseData) {
        return caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART;
    }

    private boolean isSameSolicitorAndResponseValid(CaseData caseData) {
        return caseData.getSameSolicitorSameResponse() != NO
            || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP;
    }

    private boolean isFinancialInfo21v2dsNeeded(CaseData caseData) {
        return isPaymentRouteRequiredForRespondent2(caseData)
            && isSpecDefenceNotAdmittedForRespondent2(caseData)
            && isFullDefenceNotClaimedForRespondent2(caseData)
            && isNotCounterClaimForRespondent2(caseData);
    }

    private boolean isPaymentRouteRequiredForRespondent2(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY;
    }

    private boolean isSpecDefenceNotAdmittedForRespondent2(CaseData caseData) {
        return caseData.getSpecDefenceAdmitted2Required() != YES
            && caseData.getSpecDefenceFullAdmitted2Required() != YES;
    }

    private boolean isFullDefenceNotClaimedForRespondent2(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }

    private boolean isNotCounterClaimForRespondent2(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean isRepaymentPlan2Needed(CaseData caseData) {
        return (caseData.getRespondentResponseIsSame() == YES
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN;
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        log.debug("Getting allocated track for case ID: {}", caseData.getCcdCaseReference());
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Building callback response for case ID: {}", updatedCaseData.build().getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }
}
