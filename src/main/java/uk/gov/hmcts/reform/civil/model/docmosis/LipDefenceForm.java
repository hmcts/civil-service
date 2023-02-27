package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class LipDefenceForm implements MappableObject {

    private final String claimReferenceNumber;
    private final LocalDate generationDate;
    private final String amountToPay;
    private final String howMuchWasPaid;
    private final LocalDate paymentDate;
    private final String paymentHow;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec howToPay;
    private final LocalDate payBy;
    private final String whyNotPayImmediately;
    private final RepaymentPlanLRspec repaymentPlan;

    private final RespondentResponseTypeSpec responseType;
    // TODO enum, ALREADY_PAID, DISPUTE, COUNTER_CLAIM
    private final String whyReject;
    private final LipDefenceFormParty claimant1;
    private final LipDefenceFormParty defendant1;
    private final LipDefenceFormParty defendant2;
    private final List<EventTemplateData> timelineEventList;
    private final String timelineComments;
    private final List<EvidenceTemplateData> evidenceList;
    private final String evidenceComments;
    private final boolean mediation;
    private final String whereTheyLive;

    public String getResponseTypeDisplay() {
        // TODO localization?
        return responseType.getDisplayedValue();
    }
}
