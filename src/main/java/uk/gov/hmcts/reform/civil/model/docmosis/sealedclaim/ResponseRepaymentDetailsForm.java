package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
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

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class ResponseRepaymentDetailsForm {

    private final String amountToPay;
    private final String howMuchWasPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentDate;
    private final String paymentHow;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate payBy;
    private final String whyNotPayImmediately;
    private final RepaymentPlanTemplateData repaymentPlan;
    private final RespondentResponseTypeSpec responseType;
    private final String freeTextWhyReject;
    private final String whyReject;
    private final List<EventTemplateData> timelineEventList;
    private final String timelineComments;
    private final List<EvidenceTemplateData> evidenceList;
    private final String evidenceComments;
    private final boolean mediation;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec howToPay;
    private final BigDecimal admittedAmount;

    public String getResponseTypeDisplay() {
        return Optional.ofNullable(responseType).map(RespondentResponseTypeSpec::getDisplayedValue).orElse("");
    }

    public static ResponseRepaymentDetailsForm toSealedClaimResponseCommonContent(CaseData caseData,
                                                                                  BigDecimal admittedAmount,
                                                                                  boolean isLRAdmissionBulkToggleEnabled) {
        ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder = ResponseRepaymentDetailsForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, builder, getTotalClaimAmountWithInterest(caseData, isLRAdmissionBulkToggleEnabled), admittedAmount);
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, builder, getTotalClaimAmountWithInterest(caseData, isLRAdmissionBulkToggleEnabled), admittedAmount);
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

    public static ResponseRepaymentDetailsForm toSealedClaimResponseCommonContent(CaseData caseData,
                                                                                  boolean isLRAdmissionBulkToggleEnabled) {
        ResponseRepaymentDetailsForm.ResponseRepaymentDetailsFormBuilder builder = ResponseRepaymentDetailsForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, getTotalClaimAmountWithInterest(caseData, isLRAdmissionBulkToggleEnabled));
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            builder.responseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, getTotalClaimAmountWithInterest(caseData, isLRAdmissionBulkToggleEnabled));
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

    private static BigDecimal getTotalClaimAmountWithInterest(CaseData caseData, boolean isLRAdmissionBulkToggleEnabled) {
        if (isLRAdmissionBulkToggleEnabled) {
            return caseData.getTotalClaimAmountPlusInterest();
        }
        return caseData.getTotalClaimAmount();
    }

    private static void addRepaymentMethodLip(CaseData caseData, ResponseRepaymentDetailsFormBuilder builder,
                                              BigDecimal totalAmount,
                                              BigDecimal admittedAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, admittedAmount, caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid());
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
            builder.admittedAmount(admittedAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, admittedAmount);
        }
    }

    private static void addRepaymentMethod(CaseData caseData, ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, totalAmount, caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid());
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

    private static void addPayByDatePayImmediately(ResponseRepaymentDetailsFormBuilder builder, BigDecimal totalClaimAmount, LocalDate responseDate) {
        builder.payBy(responseDate).amountToPay(totalClaimAmount + "");
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

    private static void partAdmissionData(CaseData caseData, ResponseRepaymentDetailsFormBuilder builder) {
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
