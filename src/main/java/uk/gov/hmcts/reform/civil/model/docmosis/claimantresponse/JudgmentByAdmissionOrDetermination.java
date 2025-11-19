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

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class JudgmentByAdmissionOrDetermination implements MappableObject {

    private String formHeader;
    private String formName;
    private String claimReferenceNumber;
    private LipFormParty claimant;
    private LipFormParty defendant;
    private Address claimantCorrespondenceAddress;
    private Address defendantCorrespondenceAddress;
    private String totalClaimAmount;
    private String totalInterestAmount;
    private RespondentResponseTypeSpec defendantResponse;
    private ApplicantResponsePaymentPlan paymentType;
    private String paymentTypeDisplayValue;
    private RepaymentPlanTemplateData repaymentPlan;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate payBy;
    private String ccjJudgmentAmount;
    private String ccjInterestToDate;
    private String claimFee;
    private String ccjSubtotal;
    private String ccjAlreadyPaidAmount;
    private String ccjFinalTotal;
    private String generationDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate generationDate;
    private String respondent1Name;
    private String respondent2Name;
    private String respondent1Ref;
    private String respondent2Ref;
    private Party applicant;
    private Party respondent;
    private List<Party> applicants;
    private String paymentPlan;
    private String repaymentFrequency;
    private String repaymentDate;
    private String paymentStr;
    private String installmentAmount;
    private String payByDate;
    private String applicantReference;
    private String welshDate;
    private String welshPayByDate;
    private String welshRepaymentDate;
    private String welshRepaymentFrequency;
    private String welshPaymentStr;
}
