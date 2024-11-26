package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

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
public class ShowConditionFlagsCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
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

    private boolean isPaymentNotImmediateForRespondent1(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                && caseData.getSpecDefenceFullAdmittedRequired() != YES
                && caseData.getSpecDefenceAdmittedRequired() != YES;
    }

    private boolean isPaymentNotImmediateForRespondent2Case(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
                && caseData.getSpecDefenceFullAdmitted2Required() != YES
                && caseData.getSpecDefenceAdmitted2Required() != YES;
    }

    private boolean isRespondent2EligibleForNonImmediatePayment(CaseData caseData) {
        return caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
                && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE;
    }
}
