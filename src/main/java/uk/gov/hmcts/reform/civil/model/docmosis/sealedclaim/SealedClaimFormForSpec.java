package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimFormForSpec implements MappableObject {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private final String referenceNumber;
    private final String caseName;
    private final String applicantExternalReference;
    private final String respondentExternalReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate issueDate;
    private final List<Party> applicants;
    private final List<Party> respondents;
    private final String descriptionOfClaim;
    private final List<TimelineOfEventDetails> timeline;
    private final List<ClaimAmountBreakupDetails> claimAmount;
    private final String sameInterestRate;
    private final String breakdownInterestRate;
    private final String totalInterestAmount;
    private final String howTheInterestWasCalculated;
    private final String interestRate;
    // Static text - The claimant reserves the right to claim interest under Section 69 of the County Courts Act 1984
    private final String interestExplanationText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate interestFromDate;
    private final String whenAreYouClaimingInterestFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate interestEndDate;
    private final String interestEndDateDescription;
    private final String totalClaimAmount;
    private final String interestAmount;
    private final String claimFee;
    private final String totalAmountOfClaim;
    private final StatementOfTruth statementOfTruth;
    private final String applicantRepresentativeOrganisationName;
    private final String defendantResponseDeadlineDate;
}
