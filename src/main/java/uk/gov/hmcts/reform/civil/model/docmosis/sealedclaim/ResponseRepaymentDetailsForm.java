package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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
        ResponseRepaymentDetailsFormData data = new ResponseRepaymentDetailsFormData();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            data.setHowToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            data.setResponseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, data, getTotalClaimAmountWithInterest(caseData), admittedAmount);
                case PART_ADMISSION -> partAdmissionData(caseData, data);
                case FULL_DEFENCE -> fullDefenceData(caseData, data);
                case COUNTER_CLAIM -> data.setWhyReject(COUNTER_CLAIM.name());
                default -> data.setWhyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            data.setHowToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            data.setResponseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethodLip(caseData, data, getTotalClaimAmountWithInterest(caseData), admittedAmount);
                case PART_ADMISSION -> partAdmissionData(caseData, data);
                case FULL_DEFENCE -> fullDefenceData(caseData, data);
                case COUNTER_CLAIM -> data.setWhyReject(COUNTER_CLAIM.name());
                default -> data.setWhyReject(null);
            }
        }

        return data
            .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .setResponseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .setMediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .toForm();
    }

    public static ResponseRepaymentDetailsForm toSealedClaimResponseCommonContent(CaseData caseData) {
        ResponseRepaymentDetailsFormData data = new ResponseRepaymentDetailsFormData();

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null && !useRespondent2(caseData)) {
            data.setHowToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            data.setResponseType(caseData.getRespondent1ClaimResponseTypeForSpec());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, data, getTotalClaimAmountWithInterest(caseData));
                case PART_ADMISSION -> partAdmissionData(caseData, data);
                case FULL_DEFENCE -> fullDefenceData(caseData, data);
                case COUNTER_CLAIM -> data.setWhyReject(COUNTER_CLAIM.name());
                default -> data.setWhyReject(null);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() != null && useRespondent2(caseData)) {
            data.setHowToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            data.setResponseType(caseData.getRespondent2ClaimResponseTypeForSpec());
            switch (caseData.getRespondent2ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, data, getTotalClaimAmountWithInterest(caseData));
                case PART_ADMISSION -> partAdmissionData(caseData, data);
                case FULL_DEFENCE -> fullDefenceData(caseData, data);
                case COUNTER_CLAIM -> data.setWhyReject(COUNTER_CLAIM.name());
                default -> data.setWhyReject(null);
            }
        }

        return data
            .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .setResponseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .setMediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .toForm();
    }

    private static BigDecimal getTotalClaimAmountWithInterest(CaseData caseData) {
        return caseData.getTotalClaimAmountPlusInterest() != null ? caseData.getTotalClaimAmountPlusInterest() : caseData.getTotalClaimAmount();
    }

    private static void addRepaymentMethodLip(CaseData caseData, ResponseRepaymentDetailsFormData data,
                                              BigDecimal totalAmount,
                                              BigDecimal admittedAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(data, admittedAmount, caseData);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, data, totalAmount);
            data.setAdmittedAmount(admittedAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, data, admittedAmount);
        } else {
            log.error("No repayment method selected for LIP");
        }
    }

    private static void addRepaymentMethod(CaseData caseData, ResponseRepaymentDetailsFormData data, BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(data, totalAmount, caseData);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, data, totalAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, data, totalAmount);
        }
    }

    private static void addPayBySetDate(CaseData caseData, ResponseRepaymentDetailsFormData data, BigDecimal totalClaimAmount) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            data.setPayBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                .setAmountToPay(totalClaimAmount + "")
                .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        } else {
            data.setAmountToPay(totalClaimAmount + "")
                .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        }
    }

    private static void addPayByDatePayImmediately(ResponseRepaymentDetailsFormData data, BigDecimal totalClaimAmount, CaseData caseData) {
        LocalDate whenWillThisAmountBePaid = Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec()).map(
            RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
        if (whenWillThisAmountBePaid == null) {
            log.info("When will this amount be paid is not set.");
        }
        data.setPayBy(whenWillThisAmountBePaid).setAmountToPay(totalClaimAmount + "");
    }

    private static void addRepaymentPlan(CaseData caseData, ResponseRepaymentDetailsFormData data, BigDecimal totalClaimAmount) {
        RepaymentPlanLRspec repaymentPlan = caseData.getRespondent1RepaymentPlan();
        if (repaymentPlan != null) {
            data.setRepaymentPlan(new RepaymentPlanTemplateData()
                                      .setPaymentFrequencyDisplay(repaymentPlan.getPaymentFrequencyDisplay())
                                      .setFirstRepaymentDate(repaymentPlan.getFirstRepaymentDate())
                                      .setPaymentAmount(MonetaryConversions.penniesToPounds(repaymentPlan.getPaymentAmount())))
                .setPayBy(repaymentPlan.finalPaymentBy(totalClaimAmount))
                .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
                .setAmountToPay(totalClaimAmount + "");
        } else if (caseData.getRespondent2RepaymentPlan() != null) {
            repaymentPlan = caseData.getRespondent2RepaymentPlan();
            data.setRepaymentPlan(new RepaymentPlanTemplateData()
                                      .setPaymentFrequencyDisplay(repaymentPlan.getPaymentFrequencyDisplay())
                                      .setFirstRepaymentDate(repaymentPlan.getFirstRepaymentDate())
                                      .setPaymentAmount(MonetaryConversions.penniesToPounds(repaymentPlan.getPaymentAmount())))
                .setPayBy(repaymentPlan.finalPaymentBy(totalClaimAmount))
                .setWhyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
                .setAmountToPay(totalClaimAmount + "");
        }
    }

    private static void alreadyPaid(CaseData caseData, ResponseRepaymentDetailsFormData data) {
        RespondToClaim respondToClaim = caseData.getResponseToClaim();
        String howMuchWasPaidAsString = MonetaryConversions.penniesToPounds(respondToClaim.getHowMuchWasPaid()) + "";
        data.setWhyReject("ALREADY_PAID")
            .setHowMuchWasPaid(howMuchWasPaidAsString)
            .setPaymentDate(respondToClaim.getWhenWasThisAmountPaid())
            .setPaymentHow(respondToClaim.getExplanationOnHowTheAmountWasPaid());
    }

    private static void addDetailsOnWhyClaimIsRejected(CaseData caseData, ResponseRepaymentDetailsFormData data) {
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.ofNullable(caseData.getCaseDataLiP());
        data.setFreeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .setTimelineComments(caseDataLiPOptional.map(CaseDataLiP::getTimeLineComment).orElse(
                ""))
            .setTimelineEventList(EventTemplateData.toEventTemplateDataList(caseData.getSpecResponseTimelineOfEvents()))
            .setEvidenceComments(caseDataLiPOptional.map(CaseDataLiP::getEvidenceComment).orElse(
                ""))
            .setEvidenceList(EvidenceTemplateData.toEvidenceTemplateDataList(caseData.getSpecResponselistYourEvidenceList()));
    }

    private static void fullDefenceData(CaseData caseData, ResponseRepaymentDetailsFormData data) {
        addDetailsOnWhyClaimIsRejected(caseData, data);
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            alreadyPaid(caseData, data);
        } else if (caseData.isClaimBeingDisputed()) {
            data.setWhyReject("DISPUTE");
        }
    }

    private static void partAdmissionData(CaseData caseData, ResponseRepaymentDetailsFormData data) {
        addDetailsOnWhyClaimIsRejected(caseData, data);
        if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
            alreadyPaid(caseData, data);
        } else {
            BigDecimal amountInPennies =
                useRespondent2(caseData) ? caseData.getRespondToAdmittedClaimOwingAmount2() :
                    caseData.getRespondToAdmittedClaimOwingAmount();

            addRepaymentMethod(
                caseData,
                data,
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

    private static final class ResponseRepaymentDetailsFormData {
        private String amountToPay;
        private String howMuchWasPaid;
        private LocalDate paymentDate;
        private String paymentHow;
        private LocalDate payBy;
        private String whyNotPayImmediately;
        private RepaymentPlanTemplateData repaymentPlan;
        private RespondentResponseTypeSpec responseType;
        private String freeTextWhyReject;
        private String whyReject;
        private List<EventTemplateData> timelineEventList;
        private String timelineComments;
        private List<EvidenceTemplateData> evidenceList;
        private String evidenceComments;
        private boolean mediation;
        private RespondentResponsePartAdmissionPaymentTimeLRspec howToPay;
        private BigDecimal admittedAmount;

        private ResponseRepaymentDetailsFormData setAmountToPay(String amountToPay) {
            this.amountToPay = amountToPay;
            return this;
        }

        private ResponseRepaymentDetailsFormData setHowMuchWasPaid(String howMuchWasPaid) {
            this.howMuchWasPaid = howMuchWasPaid;
            return this;
        }

        private ResponseRepaymentDetailsFormData setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        private ResponseRepaymentDetailsFormData setPaymentHow(String paymentHow) {
            this.paymentHow = paymentHow;
            return this;
        }

        private ResponseRepaymentDetailsFormData setPayBy(LocalDate payBy) {
            this.payBy = payBy;
            return this;
        }

        private ResponseRepaymentDetailsFormData setWhyNotPayImmediately(String whyNotPayImmediately) {
            this.whyNotPayImmediately = whyNotPayImmediately;
            return this;
        }

        private ResponseRepaymentDetailsFormData setRepaymentPlan(RepaymentPlanTemplateData repaymentPlan) {
            this.repaymentPlan = repaymentPlan;
            return this;
        }

        private ResponseRepaymentDetailsFormData setResponseType(RespondentResponseTypeSpec responseType) {
            this.responseType = responseType;
            return this;
        }

        private ResponseRepaymentDetailsFormData setFreeTextWhyReject(String freeTextWhyReject) {
            this.freeTextWhyReject = freeTextWhyReject;
            return this;
        }

        private ResponseRepaymentDetailsFormData setWhyReject(String whyReject) {
            this.whyReject = whyReject;
            return this;
        }

        private ResponseRepaymentDetailsFormData setTimelineEventList(List<EventTemplateData> timelineEventList) {
            this.timelineEventList = timelineEventList;
            return this;
        }

        private ResponseRepaymentDetailsFormData setTimelineComments(String timelineComments) {
            this.timelineComments = timelineComments;
            return this;
        }

        private ResponseRepaymentDetailsFormData setEvidenceList(List<EvidenceTemplateData> evidenceList) {
            this.evidenceList = evidenceList;
            return this;
        }

        private ResponseRepaymentDetailsFormData setEvidenceComments(String evidenceComments) {
            this.evidenceComments = evidenceComments;
            return this;
        }

        private ResponseRepaymentDetailsFormData setMediation(boolean mediation) {
            this.mediation = mediation;
            return this;
        }

        private ResponseRepaymentDetailsFormData setHowToPay(RespondentResponsePartAdmissionPaymentTimeLRspec howToPay) {
            this.howToPay = howToPay;
            return this;
        }

        private ResponseRepaymentDetailsFormData setAdmittedAmount(BigDecimal admittedAmount) {
            this.admittedAmount = admittedAmount;
            return this;
        }

        private ResponseRepaymentDetailsForm toForm() {
            return new ResponseRepaymentDetailsForm(
                amountToPay,
                howMuchWasPaid,
                paymentDate,
                paymentHow,
                payBy,
                whyNotPayImmediately,
                repaymentPlan,
                responseType,
                freeTextWhyReject,
                whyReject,
                timelineEventList,
                timelineComments,
                evidenceList,
                evidenceComments,
                mediation,
                howToPay,
                admittedAmount
            );
        }
    }
}
