package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class InterlocutoryJudgementDoc implements MappableObject {
    
    private final String claimNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private final LocalDate claimIssueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy 'at' HH:mm a")
    private final LocalDateTime claimantResponseSubmitDateTime;

    private final String claimantResponseToDefendantAdmission;
    private final String claimantRequestRepaymentBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private final LocalDate claimantRequestRepaymentLastDateBy;

    private final String formattedDisposableIncome;
    private final String courtDecisionRepaymentBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private final LocalDate courtDecisionRepaymentLastDateBy;
    private final String formalisePaymentBy;
    private final String rejectionReason;
}
