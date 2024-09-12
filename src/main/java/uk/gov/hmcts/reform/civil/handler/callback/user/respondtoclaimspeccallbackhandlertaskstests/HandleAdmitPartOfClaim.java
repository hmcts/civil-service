package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
public class HandleAdmitPartOfClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final RespondToClaimSpecUtils disputeDetailsUtil;

    // called on full_admit, also called after whenWillClaimBePaid
    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = validatePayments(caseData);
        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updateAdmissionFlags(caseData, updatedCaseData);
        updatePaymentRouteFlags(caseData, updatedCaseData);
        updateRespondentsAdmissionStatus(caseData, updatedCaseData);
        updateEmploymentType(caseData, updatedCaseData);
        updateClaimOwingAmounts(caseData, updatedCaseData);
        updateSpecPaidOrDisputeStatus(caseData, updatedCaseData);
        updateAllocatedTrack(callbackParams, caseData, updatedCaseData);
        updateShowConditionFlags(caseData, updatedCaseData);

        return buildCallbackResponse(updatedCaseData);
    }

    private List<String> validatePayments(CaseData caseData) {
        return paymentDateValidator.validate(
            Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElse(RespondToClaim.builder().build())
        );
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private void updateAdmissionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }
    }

    private void updatePaymentRouteFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }
    }

    private void updateRespondentsAdmissionStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
        }
    }

    private void updateEmploymentType(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
    }

    private void updateClaimOwingAmounts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
    }

    private void updateSpecPaidOrDisputeStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (shouldMarkSpecPaidLessOrDispute(caseData)) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }

        if (shouldMarkSpecDisputeOrPartAdmission(caseData)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
        }

        if (shouldMarkPartAdmitNotPaid(caseData)) {
            updatedCaseData.specPartAdmitPaid(NO);
        }

        if (shouldMarkFullAdmitNotPaid(caseData)) {
            updatedCaseData.specFullAdmitPaid(NO);
        }
    }

    private boolean shouldMarkSpecPaidLessOrDispute(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private boolean shouldMarkSpecDisputeOrPartAdmission(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private boolean shouldMarkPartAdmitNotPaid(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO;
    }

    private boolean shouldMarkFullAdmitNotPaid(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO;
    }

    private void updateAllocatedTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            updatedCaseData.responseClaimTrack(getAllocatedTrack(caseData).name());
        }
    }

    private void updateShowConditionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        Set<DefendantResponseShowTag> currentShowFlags = new HashSet<>(caseData.getShowConditionFlags());
        currentShowFlags.removeAll(EnumSet.of(
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            WHEN_WILL_CLAIM_BE_PAID
        ));
        currentShowFlags.addAll(checkNecessaryFinancialDetails(caseData));
        if (mustWhenWillClaimBePaidBeShown(caseData)) {
            currentShowFlags.add(WHEN_WILL_CLAIM_BE_PAID);
        }
        updatedCaseData.showConditionFlags(currentShowFlags);
    }

    private boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        return disputeDetailsUtil.mustWhenWillClaimBePaidBeShown(caseData);
    }


    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        Set<DefendantResponseShowTag> necessary = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (shouldAddFinancialDetailsForRespondent1(caseData, scenario)) {
            necessary.add(NEED_FINANCIAL_DETAILS_1);
        }
        if (disputeDetailsUtil.respondent1doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
        }

        if (shouldAddFinancialDetailsForRespondent2(caseData, scenario)) {
            necessary.add(NEED_FINANCIAL_DETAILS_2);
        }
        if (disputeDetailsUtil.respondent2doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
        }
        if (shouldAddRepaymentPlan2(caseData)) {
            necessary.add(REPAYMENT_PLAN_2);
        }

        return necessary;
    }

    private boolean shouldAddFinancialDetailsForRespondent1(CaseData caseData, MultiPartyScenario scenario) {
        return caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1) && needsFinancialDetails(caseData, caseData.getRespondent1(), scenario);
    }

    private boolean shouldAddFinancialDetailsForRespondent2(CaseData caseData, MultiPartyScenario scenario) {
        return caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2) && needsFinancialDetails(caseData, caseData.getRespondent2(), scenario);
    }

    private boolean needsFinancialDetails(CaseData caseData, Party respondent, MultiPartyScenario scenario) {
        return isNonCorporateParty(respondent) && (isScenarioTwoLegalRepAndNeedsInfo(caseData, scenario)
            || isScenarioOneLegalRepAndNeedsInfo(caseData, scenario));
    }

    private boolean isNonCorporateParty(Party respondent) {
        return respondent.getType() != Party.Type.COMPANY && respondent.getType() != Party.Type.ORGANISATION;
    }

    private boolean isScenarioTwoLegalRepAndNeedsInfo(CaseData caseData, MultiPartyScenario scenario) {
        return scenario == ONE_V_TWO_TWO_LEGAL_REP && needFinancialInfo21v2ds(caseData);
    }

    private boolean isScenarioOneLegalRepAndNeedsInfo(CaseData caseData, MultiPartyScenario scenario) {
        return scenario == ONE_V_TWO_ONE_LEGAL_REP && needFinancialInfo1(caseData);
    }


    /**
     * this condition has been copied from ccd's on the moment of writing.
     *
     * @param caseData the case data
     * @return true if the financial details for r1 are needed. Doesn't consider if the r1
     */
    private boolean needFinancialInfo1(CaseData caseData) {
        return isPaymentNotImmediate(caseData)
            && isSpecDefenceNotAdmitted(caseData)
            && isFullDefenceNotClaimed(caseData)
            && isNotCounterClaim(caseData)
            && hasMultiplePartyResponseFlags(caseData)
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

    private boolean hasMultiplePartyResponseFlags(CaseData caseData) {
        return caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART;
    }

    private boolean isSameSolicitorAndResponseValid(CaseData caseData) {
        return caseData.getSameSolicitorSameResponse() != NO
            || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP;
    }

    private boolean needFinancialInfo21v2ds(CaseData caseData) {
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

    private boolean shouldAddRepaymentPlan2(CaseData caseData) {
        return (caseData.getRespondentResponseIsSame() == YES
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN;
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }
}
