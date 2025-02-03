package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class JudgmentByAdmissionOrDetermination implements MappableObject {

    private final String formHeader;
    private String formName;
    private final String claimReferenceNumber;
    private final LipFormParty claimant;
    private final LipFormParty defendant;
    private final Address claimantCorrespondenceAddress;
    private final Address defendantCorrespondenceAddress;
    private final String totalClaimAmount;
    private final String totalInterestAmount;
    private final RespondentResponseTypeSpec defendantResponse;
    private final ApplicantResponsePaymentPlan paymentType;
    private final String paymentTypeDisplayValue;
    private final RepaymentPlanTemplateData repaymentPlan;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate payBy;
    private final String ccjJudgmentAmount;
    private final String ccjInterestToDate;
    private final String claimFee;
    private final String ccjSubtotal;
    private final String ccjAlreadyPaidAmount;
    private final String ccjFinalTotal;
    private final String generationDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
    private final String respondent1Name;
    private final String respondent2Name;
    private final String respondent1Ref;
    private final String respondent2Ref;
    private final Party applicant;
    private final Party respondent;
    private final List<Party> applicants;
    private final String paymentPlan;
    private final String repaymentFrequency;
    private final String repaymentDate;
    private final String paymentStr;
    private final String installmentAmount;
    private final String payByDate;
    private final String applicantReference;
}
