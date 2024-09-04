package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC;
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

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(
            Optional.ofNullable(caseData.getRespondToAdmittedClaim()).orElseGet(RespondToClaim::new)
        );

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updateAdmissionAndPaymentFields(caseData, updatedCaseData, callbackParams);

        Set<DefendantResponseShowTag> currentShowFlags = new HashSet<>(caseData.getShowConditionFlags());
        updateShowConditionFlags(caseData, currentShowFlags);
        updatedCaseData.showConditionFlags(currentShowFlags);

        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData.build().toMap(objectMapper)).build();
    }

    private void updateAdmissionAndPaymentFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, CallbackParams callbackParams) {
        updateFullAdmissionAndFullAmountPaid(caseData, updatedCaseData);
        updateDefenceAdmitPartPaymentTimeRouteGeneric(caseData, updatedCaseData);
        updatePartAdmittedByEitherRespondents(caseData, updatedCaseData);
        updateEmploymentTypeFields(caseData, updatedCaseData);
        updateAdmittedClaimOwingAmount(caseData, updatedCaseData);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData, updatedCaseData);
        updateSpecDisputesOrPartAdmission(caseData, updatedCaseData);
        updateSpecPartAdmitOrFullAdmitPaid(caseData, updatedCaseData);

        if (DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
        }
    }

    private void updateFullAdmissionAndFullAmountPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if ((YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required()))
            || (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired()))) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }
    }

    private void updateDefenceAdmitPartPaymentTimeRouteGeneric(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }
    }

    private void updatePartAdmittedByEitherRespondents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if ((YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required()))
            || (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired()))) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
        }
    }

    private void updateEmploymentTypeFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
    }

    private void updateAdmittedClaimOwingAmount(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
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
            || PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
        }
    }

    private void updateSpecPartAdmitOrFullAdmitPaid(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION && caseData.getSpecDefenceAdmittedRequired() == NO) {
            updatedCaseData.specPartAdmitPaid(NO);
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_ADMISSION && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            updatedCaseData.specFullAdmitPaid(NO);
        }
    }

    private void updateShowConditionFlags(CaseData caseData, Set<DefendantResponseShowTag> currentShowFlags) {
        currentShowFlags.removeAll(EnumSet.of(NEED_FINANCIAL_DETAILS_1, NEED_FINANCIAL_DETAILS_2, WHY_1_DOES_NOT_PAY_IMMEDIATELY, WHY_2_DOES_NOT_PAY_IMMEDIATELY, WHEN_WILL_CLAIM_BE_PAID));
        currentShowFlags.addAll(checkNecessaryFinancialDetails(caseData));
        if (mustWhenWillClaimBePaidBeShown(caseData)) {
            currentShowFlags.add(WHEN_WILL_CLAIM_BE_PAID);
        }
    }

    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        Set<DefendantResponseShowTag> necessary = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            addNecessaryFinancialDetailsForRespondent1(caseData, necessary, scenario);
        }

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            addNecessaryFinancialDetailsForRespondent2(caseData, necessary, scenario);
        }

        return necessary;
    }

    private void addNecessaryFinancialDetailsForRespondent1(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (caseData.getRespondent1().getType() != Party.Type.COMPANY && caseData.getRespondent1().getType() != Party.Type.ORGANISATION && needFinancialInfo1(caseData)) {
            necessary.add(NEED_FINANCIAL_DETAILS_1);
        }

        if (RespondToClaimSpecUtilsDisputeDetails.respondent1doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
        }
    }

    private void addNecessaryFinancialDetailsForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        if (caseData.getRespondent2().getType() != Party.Type.COMPANY && caseData.getRespondent2().getType() != Party.Type.ORGANISATION) {
            if ((scenario == ONE_V_TWO_TWO_LEGAL_REP && RespondToClaimSpecUtilsDisputeDetails.needFinancialInfo21v2ds(caseData))
                || (scenario == ONE_V_TWO_ONE_LEGAL_REP && ((caseData.getRespondentResponseIsSame() != YES && RespondToClaimSpecUtilsDisputeDetails.needFinancialInfo21v2ds(caseData))
                || (needFinancialInfo1(caseData) && caseData.getRespondentResponseIsSame() == YES)))) {
                necessary.add(NEED_FINANCIAL_DETAILS_2);
            }

            if (RespondToClaimSpecUtilsDisputeDetails.respondent2doesNotPayImmediately(caseData, scenario)) {
                necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
            }
        }

        if (RespondToClaimSpecUtilsDisputeDetails.respondent2doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
        }

        if ((caseData.getRespondentResponseIsSame() == YES && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN) {
            necessary.add(REPAYMENT_PLAN_2);
        }
    }

    public boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        return RespondToClaimSpecUtilsDisputeDetails.mustWhenWillClaimBePaidBeShown(caseData);
    }

    private boolean needFinancialInfo1(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
            && caseData.getSpecDefenceAdmittedRequired() != YES
            && caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM
            && caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART
            && (caseData.getSameSolicitorSameResponse() != NO || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP)
            && caseData.getDefendantSingleResponseToBothClaimants() != NO;
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }
}
