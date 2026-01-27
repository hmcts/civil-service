package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SetAsideJudgmentInErrorLiPDefendantLetter implements MappableObject {

    private Party defendant;
    private String claimantName;
    private String claimReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate letterIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate issueDate;


}
