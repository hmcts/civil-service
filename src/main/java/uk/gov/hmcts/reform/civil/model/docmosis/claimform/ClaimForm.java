package uk.gov.hmcts.reform.civil.model.docmosis.claimform;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Timeline;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimForm implements MappableObject {

    private String claimNumber;
    private String ccdCaseReference;
    private LipFormParty claimant;
    private LipFormParty defendant;
    private Address claimantCorrespondenceAddress;
    private Address defendantCorrespondenceAddress;
    private String descriptionOfClaim;
    private List<Timeline> timelineEvents;
    private List<EvidenceTemplateData> evidenceList;
    private List<ClaimAmountBreakupDetails> claimAmount;
    private String totalClaimAmount;
    private String breakdownInterestRate;
    private String totalInterestAmount;
    private String howTheInterestWasCalculated;
    private String interestRate;
    private String interestExplanationText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate interestFromDate;
    private String whenAreYouClaimingInterestFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate interestEndDate;
    private String interestEndDateDescription;
    private String interestPerDayBreakdown;
    private String interestAmount;
    private String claimFee;
    private String totalAmountOfClaim;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy 'at' HH:mm a")
    private LocalDateTime generationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    private LocalDate claimIssuedDate;
    private FlightDelayDetails flightDelayDetails;
    private StatementOfTruth uiStatementOfTruth;
}
