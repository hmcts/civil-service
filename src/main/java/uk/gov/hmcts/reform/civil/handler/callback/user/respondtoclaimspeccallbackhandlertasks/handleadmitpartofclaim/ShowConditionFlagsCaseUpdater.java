package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

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
public class ShowConditionFlagsCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    private final FeatureToggleService featureToggleService;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating show condition flags for caseId: {}", caseData.getCcdCaseReference());
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
        check1v1PartAdmitLRBulkAdmission(updatedCaseData);

    }

    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        log.info("Checking necessary financial details for caseId: {}", caseData.getCcdCaseReference());
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
        log.info("Checking Respondent 1 financial details for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1().getType() != Party.Type.COMPANY
                && caseData.getRespondent1().getType() != Party.Type.ORGANISATION && needFinancialInfo1(caseData)) {
            log.debug("Adding NEED_FINANCIAL_DETAILS_1 for caseId: {}", caseData.getCcdCaseReference());
            necessary.add(NEED_FINANCIAL_DETAILS_1);
        }

        if (respondent1doesNotPayImmediately(caseData, scenario)) {
            log.debug("Adding WHY_1_DOES_NOT_PAY_IMMEDIATELY for caseId: {}", caseData.getCcdCaseReference());
            necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
        }
    }

    private void checkRespondent2FinancialDetails(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        log.info("Checking Respondent 2 financial details for caseId: {}", caseData.getCcdCaseReference());
        if (isRespondent2Individual(caseData)) {
            checkFinancialInfoForRespondent2(caseData, necessary, scenario);
            checkImmediatePaymentForRespondent2(caseData, necessary, scenario);
            checkRepaymentPlanForRespondent2(caseData, necessary);
        }
    }

    private boolean isRespondent2Individual(CaseData caseData) {
        log.info("Checking if Respondent 2 is an individual for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondent2().getType() != Party.Type.COMPANY
                && caseData.getRespondent2().getType() != Party.Type.ORGANISATION;
    }

    private void checkFinancialInfoForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        log.info("Checking financial info for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());

        if ((scenario == ONE_V_TWO_TWO_LEGAL_REP && needFinancialInfo21v2ds(caseData))
                || (scenario == ONE_V_TWO_ONE_LEGAL_REP
                && ((caseData.getRespondentResponseIsSame() != YES && needFinancialInfo21v2ds(caseData))
                || (needFinancialInfo1(caseData) && caseData.getRespondentResponseIsSame() == YES)))) {
            log.debug("Adding NEED_FINANCIAL_DETAILS_2 for caseId: {}", caseData.getCcdCaseReference());
            necessary.add(NEED_FINANCIAL_DETAILS_2);
        }
    }

    private void checkImmediatePaymentForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary, MultiPartyScenario scenario) {
        log.info("Checking immediate payment for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        if (respondent2doesNotPayImmediately(caseData, scenario)) {
            necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
        }
    }

    private void checkRepaymentPlanForRespondent2(CaseData caseData, Set<DefendantResponseShowTag> necessary) {
        log.info("Checking repayment plan for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        if ((caseData.getRespondentResponseIsSame() == YES
                && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
                || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN) {
            necessary.add(REPAYMENT_PLAN_2);
        }
    }

    private boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        log.info("Checking if 'When Will Claim Be Paid' must be shown for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            log.debug("Checking conditions for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
            return mustBeShownForRespondent1(caseData);
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            log.debug("Checking conditions for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
            return mustBeShownForRespondent2(caseData);
        }

        log.debug("No conditions met for 'When Will Claim Be Paid' for caseId: {}", caseData.getCcdCaseReference());
        return false;
    }

    private boolean mustBeShownForRespondent1(CaseData caseData) {
        log.info("Checking conditions for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return isAdmitPartNotPay(caseData.getSpecDefenceAdmittedRequired())
                || isAdmitFullNotPay(caseData.getSpecDefenceFullAdmittedRequired());
    }

    private boolean mustBeShownForRespondent2(CaseData caseData) {
        log.info("Checking conditions for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
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
        log.info("Checking if financial info is needed for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return isPaymentNotImmediate(caseData)
                && isNotAdmitted(caseData)
                && isNotFullDefenceOrCounterClaim(caseData)
                && isNotCounterAdmitOrAdmitPart(caseData)
                && isSameSolicitorOrTwoLegalRep(caseData)
                && isNotSingleResponseToBothClaimants(caseData);
    }

    private boolean isPaymentNotImmediate(CaseData caseData) {
        log.info("Checking if payment is not immediate for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY;
    }

    private boolean isNotAdmitted(CaseData caseData) {
        log.info("Checking if Respondent 1 is not admitted for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getSpecDefenceAdmittedRequired() != YES
                && caseData.getSpecDefenceFullAdmittedRequired() != YES;
    }

    private boolean isNotFullDefenceOrCounterClaim(CaseData caseData) {
        log.info("Checking if Respondent 1 is not full defence or counter claim for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean isNotCounterAdmitOrAdmitPart(CaseData caseData) {
        log.info("Checking if Respondent 1 is not counter admit or admit part for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART;
    }

    private boolean isSameSolicitorOrTwoLegalRep(CaseData caseData) {
        log.info("Checking if Respondent 1 has same solicitor or two legal representatives for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getSameSolicitorSameResponse() != NO
                || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP;
    }

    private boolean isNotSingleResponseToBothClaimants(CaseData caseData) {
        log.info("Checking if Respondent 1 is not single response to both claimants for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getDefendantSingleResponseToBothClaimants() != NO;
    }

    private boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        log.info("Checking if Respondent 1 does not pay immediately for caseId: {}", caseData.getCcdCaseReference());
        return isRespondent1Eligible(caseData, scenario) && isPaymentNotImmediateForRespondent1(caseData);
    }

    private boolean isRespondent1Eligible(CaseData caseData, MultiPartyScenario scenario) {
        log.info("Checking if Respondent 1 is eligible for non-immediate payment for caseId: {}", caseData.getCcdCaseReference());
        return YES.equals(caseData.getIsRespondent1())
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
                && (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES);
    }

    private boolean needFinancialInfo21v2ds(CaseData caseData) {
        log.info("Checking if financial info is needed for Respondent 2 in 1v2 scenario for caseId: {}", caseData.getCcdCaseReference());
        return isPaymentNotImmediateForRespondent2(caseData)
                && isNotAdmittedForRespondent2(caseData)
                && isNotFullDefenceOrCounterClaimForRespondent2(caseData);
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData) {
        log.info("Checking if payment is not immediate for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY;
    }

    private boolean isPaymentNotImmediateForRespondent2(CaseData caseData, MultiPartyScenario scenario) {
        log.info("Checking if payment is not immediate for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());

        if (scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES) {
            log.debug("Scenario is ONE_V_TWO_ONE_LEGAL_REP and Respondent response is same for caseId: {}", caseData.getCcdCaseReference());
            return isPaymentNotImmediateForRespondent1(caseData);
        } else if (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
            log.debug("Scenario is ONE_V_TWO_TWO_LEGAL_REP or Respondent response is not null for caseId: {}", caseData.getCcdCaseReference());
            return isPaymentNotImmediateForRespondent2Case(caseData);
        }

        log.debug("No conditions met for payment not being immediate for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
        return false;
    }

    private boolean isNotAdmittedForRespondent2(CaseData caseData) {
        log.info("Checking if Respondent 2 is not admitted for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getSpecDefenceAdmitted2Required() != YES
                && caseData.getSpecDefenceFullAdmitted2Required() != YES;
    }

    private boolean isNotFullDefenceOrCounterClaimForRespondent2(CaseData caseData) {
        log.info("Checking if Respondent 2 is not full defence or counter claim for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    private boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        log.info("Checking if Respondent 2 does not pay immediately for caseId: {}", caseData.getCcdCaseReference());
        return isRespondent2EligibleForNonImmediatePayment(caseData)
                && isPaymentNotImmediateForRespondent2(caseData, scenario);
    }

    private boolean isPaymentNotImmediateForRespondent1(CaseData caseData) {
        log.info("Checking if payment is not immediate for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                && caseData.getSpecDefenceFullAdmittedRequired() != YES
                && caseData.getSpecDefenceAdmittedRequired() != YES;
    }

    private boolean isPaymentNotImmediateForRespondent2Case(CaseData caseData) {
        log.info("Checking if payment is not immediate for Respondent 2 case for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
                && caseData.getSpecDefenceFullAdmitted2Required() != YES
                && caseData.getSpecDefenceAdmitted2Required() != YES;
    }

    private boolean isRespondent2EligibleForNonImmediatePayment(CaseData caseData) {
        log.info("Checking if Respondent 2 is eligible for non-immediate payment for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }

    private void check1v1PartAdmitLRBulkAdmission(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Checking 1v1 Part Admit LR Bulk Admission");
        CaseData caseData = updatedCaseData.build();
        if (featureToggleService.isLrAdmissionBulkEnabled()) {
            updatedCaseData.partAdmit1v1Defendant(MultiPartyScenario.isOneVOne(caseData)
                    && caseData.isPartAdmitClaimSpec() ? YES : NO);
        }
    }
}
