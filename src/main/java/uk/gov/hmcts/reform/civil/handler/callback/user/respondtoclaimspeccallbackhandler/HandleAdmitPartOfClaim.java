package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String caseId = String.valueOf(caseData.getCcdCaseReference());

        log.info("Executing HandleAdmitPartOfClaim for caseId: {}", caseId);

        List<String> errors = validatePaymentDate(caseData);
        if (!errors.isEmpty()) {
            log.warn("Validation failed for caseId: {}. Errors: {}", caseId, errors);
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updateFullAdmissionAndFullAmountPaid(caseData, updatedCaseData, caseId);
        updateDefenceAdmitPartPaymentTimeRoute(caseData, updatedCaseData, caseId);
        updatePartAdmittedByEitherRespondents(caseData, updatedCaseData, caseId);
        updateEmploymentType(caseData, updatedCaseData, caseId);
        updateOwingAmount(caseData, updatedCaseData, caseId);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData, updatedCaseData, caseId);
        updateSpecDisputesOrPartAdmission(caseData, updatedCaseData, caseId);
        updateSpecPartAdmitPaid(caseData, updatedCaseData, caseId);
        updateSpecFullAdmitPaid(caseData, updatedCaseData, caseId);
        updateResponseClaimTrack(callbackParams, caseData, updatedCaseData, caseId);
        updateShowConditionFlags(caseData, updatedCaseData, caseId);

        log.info("Successfully executed HandleAdmitPartOfClaim for caseId: {}", caseId);
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

    private void updateFullAdmissionAndFullAmountPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
            log.debug("Set fullAdmissionAndFullAmountPaid to YES for caseId: {}", caseId);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
            log.debug("Set fullAdmissionAndFullAmountPaid to YES for caseId: {}", caseId);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
            log.debug("Set fullAdmissionAndFullAmountPaid to NO for caseId: {}", caseId);
        }
    }

    private void updateDefenceAdmitPartPaymentTimeRoute(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            log.debug("Updated defenceAdmitPartPaymentTimeRouteGeneric for respondent1, caseId: {}", caseId);
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
            log.debug("Updated defenceAdmitPartPaymentTimeRouteGeneric for respondent2, caseId: {}", caseId);
        }
    }

    private void updatePartAdmittedByEitherRespondents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
            log.debug("Set partAdmittedByEitherRespondents to YES for respondent2, caseId: {}", caseId);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
            log.debug("Set partAdmittedByEitherRespondents to YES for respondent1, caseId: {}", caseId);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
            log.debug("Set partAdmittedByEitherRespondents to NO for caseId: {}", caseId);
        }
    }

    private void updateEmploymentType(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
            log.debug("Updated respondToClaimAdmitPartEmploymentTypeLRspec for respondent1, caseId: {}", caseId);
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
            log.debug("Updated respondToClaimAdmitPartEmploymentTypeLRspec for respondent2, caseId: {}", caseId);
        }
    }

    private void updateOwingAmount(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(amount -> {
                updatedCaseData.respondToAdmittedClaimOwingAmountPounds(amount);
                log.debug("Updated respondToAdmittedClaimOwingAmountPounds for respondent1, caseId: {}", caseId);
            });
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(amount -> {
                updatedCaseData.respondToAdmittedClaimOwingAmountPounds2(amount);
                log.debug("Updated respondToAdmittedClaimOwingAmountPounds2 for respondent2, caseId: {}", caseId);
            });
    }

    private void updateSpecPaidLessAmountOrDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
            log.debug("Set specPaidLessAmountOrDisputesOrPartAdmission to YES for caseId: {}", caseId);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
            log.debug("Set specPaidLessAmountOrDisputesOrPartAdmission to NO for caseId: {}", caseId);
        }
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
            log.debug("Set specDisputesOrPartAdmission to YES for caseId: {}", caseId);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
            log.debug("Set specDisputesOrPartAdmission to NO for caseId: {}", caseId);
        }
    }

    private void updateSpecPartAdmitPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO) {
            updatedCaseData.specPartAdmitPaid(NO);
            log.debug("Set specPartAdmitPaid to NO for caseId: {}", caseId);
        }
    }

    private void updateSpecFullAdmitPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            updatedCaseData.specFullAdmitPaid(NO);
            log.debug("Set specFullAdmitPaid to NO for caseId: {}", caseId);
        }
    }

    private void updateResponseClaimTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
            log.debug("Set responseClaimTrack to {} for caseId: {}", allocatedTrack.name(), caseId);
        }
    }

    private void updateShowConditionFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, String caseId) {
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
            log.debug("Added WHEN_WILL_CLAIM_BE_PAID to showConditionFlags for caseId: {}", caseId);
        }
        updatedCaseData.showConditionFlags(currentShowFlags);
        log.debug("Updated showConditionFlags for caseId: {}", caseId);
    }

    private CallbackResponse buildSuccessResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
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
            log.debug("Added NEED_FINANCIAL_DETAILS_1 for respondent1, caseId: {}", caseData.getCcdCaseReference());
        }

        if (respondent1doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
            log.debug("Added WHY_1_DOES_NOT_PAY_IMMEDIATELY for respondent1, caseId: {}", caseData.getCcdCaseReference());
        }
    }

    private void checkRespondent2FinancialDetails(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (isRespondent2Individual(caseData)) {
            addFinancialDetailsForRespondent2(caseData, necessary, scenario);
        }

        if (respondent2doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
            log.debug("Added WHY_2_DOES_NOT_PAY_IMMEDIATELY for respondent2, caseId: {}", caseData.getCcdCaseReference());
        }

        if (isRepaymentPlanRequired(caseData)) {
            necessary.add(REPAYMENT_PLAN_2);
            log.debug("Added REPAYMENT_PLAN_2 for respondent2, caseId: {}", caseData.getCcdCaseReference());
        }
    }

    private boolean isRespondent2Individual(CaseData caseData) {
        return caseData.getRespondent2().getType() != Party.Type.COMPANY
            && caseData.getRespondent2().getType() != Party.Type.ORGANISATION;
    }

    private void addFinancialDetailsForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if ((scenario == ONE_V_TWO_TWO_LEGAL_REP && needFinancialInfo21v2ds(caseData))
            || (scenario == ONE_V_TWO_ONE_LEGAL_REP
            && ((caseData.getRespondentResponseIsSame() != YES && needFinancialInfo21v2ds(caseData))
            || (needFinancialInfo1(caseData) && caseData.getRespondentResponseIsSame() == YES)))) {
            necessary.add(NEED_FINANCIAL_DETAILS_2);
            log.debug("Added NEED_FINANCIAL_DETAILS_2 for respondent2, caseId: {}", caseData.getCcdCaseReference());
        }
    }

    private boolean isRepaymentPlanRequired(CaseData caseData) {
        return (caseData.getRespondentResponseIsSame() == YES
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN;
    }

    private boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            return isClaimNotFullyAdmitted(caseData.getSpecDefenceFullAdmittedRequired(), caseData.getSpecDefenceAdmittedRequired());
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            return isClaimNotFullyAdmitted(caseData.getSpecDefenceFullAdmitted2Required(), caseData.getSpecDefenceAdmitted2Required());
        }
        return false;
    }

    private boolean isClaimNotFullyAdmitted(YesOrNo fullAdmittedRequired, YesOrNo admittedRequired) {
        return fullAdmittedRequired == NO || admittedRequired == NO;
    }

    private boolean needFinancialInfo1(CaseData caseData) {
        return isPaymentNotImmediate(caseData)
            && isNotFullyAdmitted(caseData)
            && isNotFullDefenceOrCounterClaim(caseData)
            && isNotCounterAdmitOrAdmitPart(caseData)
            && isSameSolicitorOrOneVTwoTwoLegalRep(caseData)
            && isNotSingleResponseToBothClaimants(caseData);
    }

    private boolean isPaymentNotImmediate(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY;
    }

    private boolean isNotFullyAdmitted(CaseData caseData) {
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

    private boolean isSameSolicitorOrOneVTwoTwoLegalRep(CaseData caseData) {
        return caseData.getSameSolicitorSameResponse() != NO
            || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP;
    }

    private boolean isNotSingleResponseToBothClaimants(CaseData caseData) {
        return caseData.getDefendantSingleResponseToBothClaimants() != NO;
    }

    private boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        return isRespondent1Involved(caseData)
            && isNotCounterClaimOrFullDefence(caseData)
            && isPaymentNotImmediateForRespondent1(caseData, scenario);
    }

    private boolean isRespondent1Involved(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent1());
    }

    private boolean isNotCounterClaimOrFullDefence(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }

    private boolean isPaymentNotImmediateForRespondent1(CaseData caseData, MultiPartyScenario scenario) {
        return (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES)
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
            && caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getSpecDefenceAdmittedRequired() != YES;
    }

    private boolean needFinancialInfo21v2ds(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
            && caseData.getSpecDefenceAdmitted2Required() != YES
            && caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        return isNotCounterClaimOrFullDefence(caseData)
            && (isOneVTwoOneLegalRepAndSameResponse(caseData, scenario) || isOneVTwoTwoLegalRepOrDifferentResponse(caseData, scenario));
    }

    private boolean isOneVTwoOneLegalRepAndSameResponse(CaseData caseData, MultiPartyScenario scenario) {
        return scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES
            && isPaymentNotImmediateForRespondent1(caseData, scenario);
    }

    private boolean isOneVTwoTwoLegalRepOrDifferentResponse(CaseData caseData, MultiPartyScenario scenario) {
        return (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP)
            && isPaymentNotImmediateForRespondent2(caseData);
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
            && caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getSpecDefenceAdmitted2Required() != YES;
    }

}
