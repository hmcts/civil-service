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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
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

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validatePaymentDate(caseData);
        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updateFullAdmissionAndFullAmountPaid(caseData, updatedCaseData);
        updateDefenceAdmitPartPaymentTimeRoute(caseData, updatedCaseData);
        updatePartAdmittedByEitherRespondents(caseData, updatedCaseData);
        updateEmploymentType(caseData, updatedCaseData);
        updateOwingAmount(caseData, updatedCaseData);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData, updatedCaseData);
        updateSpecDisputesOrPartAdmission(caseData, updatedCaseData);
        updateSpecPartAdmitPaid(caseData, updatedCaseData);
        updateSpecFullAdmitPaid(caseData, updatedCaseData);
        updateResponseClaimTrack(callbackParams, caseData, updatedCaseData);
        updateShowConditionFlags(caseData, updatedCaseData);

        return buildSuccessResponse(updatedCaseData);
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                                                 .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private void updateFullAdmissionAndFullAmountPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }
    }

    private void updateDefenceAdmitPartPaymentTimeRoute(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }
    }

    private void updatePartAdmittedByEitherRespondents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
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

    private void updateOwingAmount(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
    }

    private void updateSpecPaidLessAmountOrDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
        }
    }

    private void updateSpecPartAdmitPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO) {
            updatedCaseData.specPartAdmitPaid(NO);
        }
    }

    private void updateSpecFullAdmitPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            updatedCaseData.specFullAdmitPaid(NO);
        }
    }

    private void updateResponseClaimTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
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

    private CallbackResponse buildSuccessResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                                                toggleService, caseData);
    }

    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        Set<DefendantResponseShowTag> necessary = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            checkRespondent1FinancialDetails(caseData, necessary, scenario);
        }

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            checkRespondent2FinancialDetails(caseData, necessary, scenario);
        }

        return necessary;
    }

    private void checkRespondent1FinancialDetails(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (caseData.getRespondent1().getType() != Party.Type.COMPANY
            && caseData.getRespondent1().getType() != Party.Type.ORGANISATION && needFinancialInfo1(caseData)) {
            necessary.add(NEED_FINANCIAL_DETAILS_1);
        }

        if (respondent1doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
        }
    }

    private void checkRespondent2FinancialDetails(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (isRespondent2Individual(caseData)) {
            checkFinancialInfoForRespondent2(caseData, necessary, scenario);
            checkImmediatePaymentForRespondent2(caseData, necessary, scenario);
            checkRepaymentPlanForRespondent2(caseData, necessary);
        }
    }

    private boolean isRespondent2Individual(CaseData caseData) {
        return caseData.getRespondent2().getType() != Party.Type.COMPANY
            && caseData.getRespondent2().getType() != Party.Type.ORGANISATION;
    }

    private void checkFinancialInfoForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if ((scenario == ONE_V_TWO_TWO_LEGAL_REP && needFinancialInfo21v2ds(caseData))
            || (scenario == ONE_V_TWO_ONE_LEGAL_REP
            && ((caseData.getRespondentResponseIsSame() != YES && needFinancialInfo21v2ds(caseData))
            || (needFinancialInfo1(caseData) && caseData.getRespondentResponseIsSame() == YES)))) {
            necessary.add(NEED_FINANCIAL_DETAILS_2);
        }
    }

    private void checkImmediatePaymentForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (respondent2doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
        }
    }

    private void checkRepaymentPlanForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary) {
        if ((caseData.getRespondentResponseIsSame() == YES
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN) {
            necessary.add(REPAYMENT_PLAN_2);
        }
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
        return isAdmitPartNotPay(caseData.getSpecDefenceAdmittedRequired())
            || isAdmitFullNotPay(caseData.getSpecDefenceFullAdmittedRequired());
    }

    private boolean mustBeShownForRespondent2(CaseData caseData) {
        return isAdmitPartNotPay(caseData.getSpecDefenceAdmitted2Required())
            || isAdmitFullNotPay(caseData.getSpecDefenceFullAdmitted2Required());
    }

    private boolean isAdmitPartNotPay(YesOrNo admitPart) {
        return admitPart == NO;
    }

    private boolean isAdmitFullNotPay(YesOrNo admitFull) {
        return admitFull == NO;
    }

    private boolean needFinancialInfo1(CaseData caseData) {
        return isPaymentNotImmediate(caseData)
            && isNotAdmitted(caseData)
            && isNotFullDefenceOrCounterClaim(caseData)
            && isNotCounterAdmitOrAdmitPart(caseData)
            && isSameSolicitorOrTwoLegalRep(caseData)
            && isNotSingleResponseToBothClaimants(caseData);
    }

    private boolean isPaymentNotImmediate(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY;
    }

    private boolean isNotAdmitted(CaseData caseData) {
        return caseData.getSpecDefenceAdmittedRequired() != YES
            && caseData.getSpecDefenceFullAdmittedRequired() != YES;
    }

    private boolean isNotFullDefenceOrCounterClaim(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean isNotCounterAdmitOrAdmitPart(CaseData caseData) {
        return caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART;
    }

    private boolean isSameSolicitorOrTwoLegalRep(CaseData caseData) {
        return caseData.getSameSolicitorSameResponse() != NO
            || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP;
    }

    private boolean isNotSingleResponseToBothClaimants(CaseData caseData) {
        return caseData.getDefendantSingleResponseToBothClaimants() != NO;
    }

    private boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        return isRespondent1Eligible(caseData, scenario) && isPaymentNotImmediateForRespondent1(caseData);
    }

    private boolean isRespondent1Eligible(CaseData caseData, MultiPartyScenario scenario) {
        return YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES);
    }

    private boolean isPaymentNotImmediateForRespondent1(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
            && caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getSpecDefenceAdmittedRequired() != YES;
    }

    private boolean needFinancialInfo21v2ds(CaseData caseData) {
        return isPaymentNotImmediateForRespondent2(caseData)
            && isNotAdmittedForRespondent2(caseData)
            && isNotFullDefenceOrCounterClaimForRespondent2(caseData);
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY;
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData, MultiPartyScenario scenario) {
        if (scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES) {
            return isPaymentNotImmediateForRespondent1(caseData);
        } else if (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
            return isPaymentNotImmediateForRespondent2Case(caseData);
        }
        return false;
    }

    private boolean isPaymentNotImmediateForRespondent2Case(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
            && caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getSpecDefenceAdmitted2Required() != YES;
    }

    private boolean isNotAdmittedForRespondent2(CaseData caseData) {
        return caseData.getSpecDefenceAdmitted2Required() != YES
            && caseData.getSpecDefenceFullAdmitted2Required() != YES;
    }

    private boolean isNotFullDefenceOrCounterClaimForRespondent2(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        return isRespondent2EligibleForNonImmediatePayment(caseData)
            && isPaymentNotImmediateForRespondent2(caseData, scenario);
    }

    private boolean isRespondent2EligibleForNonImmediatePayment(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }
}
