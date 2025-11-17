package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimFormForSpec implements MappableObject {

    @JsonProperty("courtseal")
    private String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private String referenceNumber;
    private String ccdCaseReference;
    private String caseName;
    private String applicantExternalReference;
    private String respondentExternalReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate issueDate;
    private List<SpecifiedParty> applicants;
    private List<SpecifiedParty> respondents;
    private String descriptionOfClaim;
    private List<TimelineEventDetailsDocmosis> timeline;
    private List<ClaimAmountBreakupDetails> claimAmount;
    private String sameInterestRate;
    private String breakdownInterestRate;
    private String interestPerDayBreakdown;
    private String totalInterestAmount;
    private String howTheInterestWasCalculated;
    private String interestRate;
    // Static text - The claimant reserves the right to claim interest under Section 69 of the County Courts Act 1984
    private String interestExplanationText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate interestFromDate;
    private String whenAreYouClaimingInterestFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate interestEndDate;
    private String interestEndDateDescription;
    private String totalClaimAmount;
    private String interestAmount;
    private String claimFee;
    private String totalAmountOfClaim;
    private StatementOfTruth statementOfTruth;
    private String applicantRepresentativeOrganisationName;
    private String defendantResponseDeadlineDate;
    private YesOrNo claimFixedCosts;
    private String fixedCostAmount;
    private YesOrNo respondentsOrgRegistered;
}
