package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimResponseForm {
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

    public String getResponseTypeDisplay() {
        return responseType.getDisplayedValue();
    }

    public static SealedClaimResponseForm toSealClaimResponseCommonContent(CaseData caseData) {
        SealedClaimResponseForm.SealedClaimResponseFormBuilder builder = SealedClaimResponseForm.builder();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, caseData.getTotalClaimAmount());
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

    private static void addRepaymentMethod(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, totalAmount);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, totalAmount);
        }
    }

    private static void addPayBySetDate(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
            .amountToPay(totalClaimAmount + "")
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
    }

    private static void addPayByDatePayImmediately(SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(LocalDate.now()
                          .plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY))
            .amountToPay(totalClaimAmount + "");
    }

    private static void addRepaymentPlan(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        if (caseData.getRespondent1RepaymentPlan() != null) {
            builder.repaymentPlan(RepaymentPlanTemplateData.builder()
                                      .paymentFrequencyDisplay(caseData.getRespondent1RepaymentPlan().getPaymentFrequencyDisplay())
                                      .firstRepaymentDate(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate())
                                      .paymentAmount(MonetaryConversions.penniesToPounds(caseData.getRespondent1RepaymentPlan().getPaymentAmount()))
                                      .build())
                .payBy(caseData.getRespondent1RepaymentPlan()
                           .finalPaymentBy(totalClaimAmount))
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        }
    }

    private static void alreadyPaid(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder) {
        RespondToClaim respondToClaim = caseData.getResponseToClaim();
        builder.whyReject("ALREADY_PAID")
            .howMuchWasPaid(MonetaryConversions.penniesToPounds(respondToClaim.getHowMuchWasPaid()) + "")
            .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
            .paymentHow(respondToClaim.getHowWasThisAmountPaid() == PaymentMethod.OTHER
                            ? respondToClaim.getHowWasThisAmountPaidOther()
                            : respondToClaim.getHowWasThisAmountPaid()
                .getHumanFriendly());
    }

    private static void addDetailsOnWhyClaimIsRejected(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder) {
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineComments(Optional.ofNullable(caseData.getCaseDataLiP()).map(CaseDataLiP::getTimeLineComment).orElse(
                ""))
            .timelineEventList(Optional.ofNullable(caseData.getSpecResponseTimelineOfEvents()).map(Collection::stream)
                                   .orElseGet(Stream::empty)
                                   .map(event ->
                                            EventTemplateData.builder()
                                                .date(event.getValue().getTimelineDate())
                                                .explanation(event.getValue().getTimelineDescription())
                                                .build()).collect(Collectors.toList()))
            .evidenceComments(Optional.ofNullable(caseData.getCaseDataLiP()).map(CaseDataLiP::getEvidenceComment).orElse(
                ""))
            .evidenceList(Optional.ofNullable(caseData.getSpecResponselistYourEvidenceList()).map(Collection::stream)
                              .orElseGet(Stream::empty)
                              .map(evidence -> EvidenceTemplateData.toEvidenceTemplateData(evidence))
                              .toList());
    }

    private static void fullDefenceData(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.hasDefendantPayedTheAmountClaimed()) {
            alreadyPaid(caseData, builder);
        } else if (caseData.isClaimBeingDisputed()) {
            builder.whyReject("DISPUTE");
        }
    }

    @JsonIgnore
    private static void partAdmissionData(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
            alreadyPaid(caseData, builder);
        } else {
            addRepaymentMethod(
                caseData,
                builder,
                MonetaryConversions.penniesToPounds(caseData.getRespondToAdmittedClaimOwingAmount())
            );
        }
    }
}
