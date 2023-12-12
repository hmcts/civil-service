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
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;

import java.time.LocalDate;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
}
