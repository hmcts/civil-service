package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class InterlocutoryJudgementDoc implements MappableObject {

    private String claimNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private LocalDate claimIssueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy 'at' HH:mm a")
    private LocalDateTime claimantResponseSubmitDateTime;

    private String claimantResponseToDefendantAdmission;
    private String claimantRequestRepaymentBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private LocalDate claimantRequestRepaymentLastDateBy;

    private String formattedDisposableIncome;
    private String courtDecisionRepaymentBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private LocalDate courtDecisionRepaymentLastDateBy;
    private String formalisePaymentBy;
    private String rejectionReason;
}
