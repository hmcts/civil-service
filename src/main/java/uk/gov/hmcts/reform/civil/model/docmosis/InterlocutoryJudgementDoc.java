package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@EqualsAndHashCode
public class InterlocutoryJudgementDoc implements MappableObject {
    private final String claimNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    private final LocalDate claimIssueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate claimantResponseSubmitDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDateTime claimantResponseSubmitTime;

    private final String claimantResponseToDefendantAdmission;
    private final String claimantRequestRepaymentBy;
    private final String claimantRequestRepaymentLastDateBy;

    private final double disposableIncome;
    private final String courtDecisionRepaymentBy;
    private final String courtDecisionRepaymentLastDateBy;
}
