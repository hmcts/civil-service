package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;

@Builder(toBuilder = true)
@Slf4j
public record ResponseRepaymentDetailsForm(String amountToPay,
                                           String howMuchWasPaid,
                                           @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
                                           @JsonSerialize(using = LocalDateSerializer.class) LocalDate paymentDate,
                                           String paymentHow,
                                           @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
                                           @JsonSerialize(using = LocalDateSerializer.class)
                                           LocalDate payBy,
                                           String whyNotPayImmediately,
                                           RepaymentPlanTemplateData repaymentPlan,
                                           RespondentResponseTypeSpec responseType,
                                           String freeTextWhyReject,
                                           String whyReject,
                                           List<EventTemplateData> timelineEventList,
                                           String timelineComments,
                                           List<EvidenceTemplateData> evidenceList,
                                           String evidenceComments,
                                           boolean mediation,
                                           RespondentResponsePartAdmissionPaymentTimeLRspec howToPay,
                                           BigDecimal admittedAmount) {

    public String getResponseTypeDisplay() {
        return Optional.ofNullable(responseType).map(RespondentResponseTypeSpec::getDisplayedValue).orElse("");
    }

    public static ResponseRepaymentDetailsForm toSealedClaimResponseCommonContent(CaseData caseData,
                                                                                  BigDecimal admittedAmount) {
        ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder = ResponseRepaymentDetailsForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, builder, getTotalClaimAmountWithInterest(caseData), admittedAmount);
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, builder, getTotalClaimAmountWithInterest(caseData), admittedAmount);
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        }

        return builder
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .build();
    }

    public static ResponseRepaymentDetailsForm toSealedClaimResponseCommonContent(CaseData caseData) {
        ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder = ResponseRepaymentDetailsForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, getTotalClaimAmountWithInterest(caseData));
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, getTotalClaimAmountWithInterest(caseData));
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        }

        return builder
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .build();
    }

    private static BigDecimal getTotalClaimAmountWithInterest(CaseData caseData) {
        return caseData.getTotalClaimAmountPlusInterest() != null ? caseData.getTotalClaimAmountPlusInterest() : caseData.getTotalClaimAmount();
    }

    private static void addRepaymentMethodLip(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder,
                                              BigDecimal totalAmount,
                                              BigDecimal admittedAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, admittedAmount, caseData);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
            builder.admittedAmount(admittedAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, admittedAmount);
        } else {
            log.error("No repayment method selected for LIP");
        }
    }

    private static void addRepaymentMethod(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, totalAmount, caseData);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, totalAmount);
        }
    }

    private static void addPayBySetDate(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalClaimAmount) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                .amountToPay(totalClaimAmount + "")
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        } else {
            builder.amountToPay(totalClaimAmount + "")
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        }
    }

    private static void addPayByDatePayImmediately(ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalClaimAmount, CaseData caseData) {
        LocalDate whenWillThisAmountBePaid = Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec()).map(
            RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
        if (whenWillThisAmountBePaid == null) {
            log.info("When will this amount be paid is not set.");
        }
        builder.payBy(whenWillThisAmountBePaid).amountToPay(totalClaimAmount + "");
    }

    private static void addRepaymentPlan(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalClaimAmount) {
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
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
            builder.amountToPay = (totalClaimAmount + "");
        }
    }

    private static void alreadyPaid(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        RespondToClaim respondToClaim = caseData.getResponseToClaim();
        String howMuchWasPaidAsString = MonetaryConversions.penniesToPounds(respondToClaim.getHowMuchWasPaid()) + "";
        builder.whyReject("ALREADY_PAID")
            .howMuchWasPaid(howMuchWasPaidAsString)
            .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
            .paymentHow(respondToClaim.getExplanationOnHowTheAmountWasPaid());
    }

    private static void addDetailsOnWhyClaimIsRejected(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.ofNullable(caseData.getCaseDataLiP());
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineComments(caseDataLiPOptional.map(CaseDataLiP::getTimeLineComment).orElse(
                ""))
            .timelineEventList(EventTemplateData.toEventTemplateDataList(caseData.getSpecResponseTimelineOfEvents()))
            .evidenceComments(caseDataLiPOptional.map(CaseDataLiP::getEvidenceComment).orElse(
                ""))
            .evidenceList(EvidenceTemplateData.toEvidenceTemplateDataList(caseData.getSpecResponselistYourEvidenceList()));
    }

    private static void fullDefenceData(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            alreadyPaid(caseData, builder);
        } else if (caseData.isClaimBeingDisputed()) {
            builder.whyReject("DISPUTE");
        }
    }

    private static void partAdmissionData(CaseData caseData, ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder) {
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
