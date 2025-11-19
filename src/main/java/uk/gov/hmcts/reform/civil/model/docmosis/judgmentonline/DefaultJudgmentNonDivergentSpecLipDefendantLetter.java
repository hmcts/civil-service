package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultJudgmentNonDivergentSpecLipDefendantLetter implements MappableObject {

    private Party defendant;
    private String claimantName;
    private String claimReferenceNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate letterIssueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate caseSubmittedDate;

    private String respondToClaimUrl;
    private String pin;
    private String varyJudgmentFee;
    private String judgmentSetAsideFee;
    private String certifOfSatisfactionFee;

}
