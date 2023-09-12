package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
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

    protected static void addPayBySetDate(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
            .amountToPay(totalClaimAmount + "")
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
    }

    protected static void addPayByDatePayImmediately(SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(LocalDate.now()
                          .plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY))
            .amountToPay(totalClaimAmount + "");
    }

    protected static void addRepaymentPlan(CaseData caseData, SealedClaimResponseForm.SealedClaimResponseFormBuilder builder, BigDecimal totalClaimAmount) {
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

}
