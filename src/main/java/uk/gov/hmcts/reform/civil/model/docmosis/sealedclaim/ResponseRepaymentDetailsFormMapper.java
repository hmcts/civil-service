package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAndSettlementAmountsCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;

@Component
@RequiredArgsConstructor
public class ResponseRepaymentDetailsFormMapper {

    private final JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator;

    public ResponseRepaymentDetailsForm toResponsePaymentDetails(CaseData caseData) {
        ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder = ResponseRepaymentDetailsForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            buildRepaymentDetails(builder, caseData, caseData.getRespondent1ClaimResponseTypeForSpec(),
                judgmentAndSettlementAmountsCalculator.getTotalClaimAmount(caseData));
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            buildRepaymentDetails(builder, caseData, caseData.getRespondent2ClaimResponseTypeForSpec(),
                caseData.getTotalClaimAmount());
        }

        return builder
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .build();
    }

    private void buildRepaymentDetails(ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, CaseData caseData,
                                       RespondentResponseTypeSpec respondentResponseTypeSpec,
                                       BigDecimal judgmentAndSettlementAmountsCalculator) {
        builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        builder.responseType(respondentResponseTypeSpec);
        switch (respondentResponseTypeSpec) {
            case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, judgmentAndSettlementAmountsCalculator);
            case PART_ADMISSION -> partAdmissionData(caseData, builder);
            case FULL_DEFENCE -> fullDefenceData(caseData, builder);
            case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
            default -> builder.whyReject(null);
        }
    }

    private void addRepaymentMethod(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder,
                                    BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, totalAmount, caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid());
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, totalAmount);
        }
    }

    private void addPayBySetDate(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder,
                                 BigDecimal totalClaimAmount) {
        builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
            .amountToPay(totalClaimAmount + "")
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
    }

    private void addPayByDatePayImmediately(ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalClaimAmount,
                                            LocalDate responseDate) {
        builder.payBy(responseDate).amountToPay(totalClaimAmount + "");
    }

    private void addRepaymentPlan(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder,
                                  BigDecimal totalClaimAmount) {
        RepaymentPlanLRspec repaymentPlan = caseData.getRespondent1RepaymentPlan();
        if (repaymentPlan != null) {
            builder.repaymentPlan(RepaymentPlanTemplateData.builder()
                    .paymentFrequencyDisplay(repaymentPlan.getPaymentFrequencyDisplay())
                    .firstRepaymentDate(repaymentPlan.getFirstRepaymentDate())
                    .paymentAmount(MonetaryConversions.penniesToPounds(repaymentPlan.getPaymentAmount()))
                    .build())
                .payBy(repaymentPlan.finalPaymentBy(totalClaimAmount))
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
            builder.amountToPay(totalClaimAmount + "");
        } else if (caseData.getRespondent2RepaymentPlan() != null) {
            repaymentPlan = caseData.getRespondent2RepaymentPlan();
            builder.repaymentPlan(RepaymentPlanTemplateData.builder()
                    .paymentFrequencyDisplay(repaymentPlan.getPaymentFrequencyDisplay())
                    .firstRepaymentDate(repaymentPlan.getFirstRepaymentDate())
                    .paymentAmount(MonetaryConversions.penniesToPounds(repaymentPlan.getPaymentAmount()))
                    .build())
                .payBy(repaymentPlan.finalPaymentBy(totalClaimAmount))
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
                .amountToPay(totalClaimAmount + "");
        }
    }

    private void alreadyPaid(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        RespondToClaim respondToClaim = caseData.getResponseToClaim();
        String howMuchWasPaidAsString = MonetaryConversions.penniesToPounds(respondToClaim.getHowMuchWasPaid()) + "";
        builder.whyReject("ALREADY_PAID")
            .howMuchWasPaid(howMuchWasPaidAsString)
            .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
            .paymentHow(respondToClaim.getExplanationOnHowTheAmountWasPaid());
    }

    private void addDetailsOnWhyClaimIsRejected(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.ofNullable(caseData.getCaseDataLiP());
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineComments(caseDataLiPOptional.map(CaseDataLiP::getTimeLineComment).orElse(
                ""))
            .timelineEventList(EventTemplateData.toEventTemplateDataList(caseData.getSpecResponseTimelineOfEvents()))
            .evidenceComments(caseDataLiPOptional.map(CaseDataLiP::getEvidenceComment).orElse(
                ""))
            .evidenceList(EvidenceTemplateData.toEvidenceTemplateDataList(caseData.getSpecResponselistYourEvidenceList()));
    }

    private void fullDefenceData(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            alreadyPaid(caseData, builder);
        } else if (caseData.isClaimBeingDisputed()) {
            builder.whyReject("DISPUTE");
        }
    }

    private void partAdmissionData(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
            alreadyPaid(caseData, builder);
        } else {
            BigDecimal amountInPennies =
                useRespondent2(caseData) ? caseData.getRespondToAdmittedClaimOwingAmount2() :
                    caseData.getRespondToAdmittedClaimOwingAmount();

            addRepaymentMethod(
                caseData,
                builder,
                MonetaryConversions.penniesToPounds(amountInPennies)
            );
        }
    }

    private static boolean useRespondent2(CaseData caseData) {
        return MultiPartyScenario.getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP
            && caseData.getRespondent1ResponseDate() == null
            || (caseData.getRespondent2ResponseDate() != null
            && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }
}
