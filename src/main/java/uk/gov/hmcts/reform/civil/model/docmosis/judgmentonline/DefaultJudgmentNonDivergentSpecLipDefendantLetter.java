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

    private final Party defendant;
    private final String claimantName;
    private final String claimReferenceNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate letterIssueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate caseSubmittedDate;

    private final String respondToClaimUrl;
    private final String pin;
    private final String varyJudgmentFee;
    private final String judgmentSetAsideFee;
    private final String certifOfSatisfactionFee;

}
