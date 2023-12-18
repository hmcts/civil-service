package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Timeline;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class ClaimForm implements MappableObject {

    private final String claimNumber;
    private final LipFormParty claimant;
    private final LipFormParty defendant;
    private final Address claimantCorrespondenceAddress;
    private final Address defendantCorrespondenceAddress;
    private final String descriptionOfClaim;
    private final List<Timeline> timelineEvents;
    private final List<EvidenceTemplateData> evidenceList;
    private final List<ClaimAmountBreakupDetails> claimAmount;
    private final String totalClaimAmount;
    private final String breakdownInterestRate;
    private final String totalInterestAmount;
    private final String howTheInterestWasCalculated;
    private final String interestRate;
    private final String interestExplanationText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate interestFromDate;
    private final String whenAreYouClaimingInterestFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate interestEndDate;
    private final String interestEndDateDescription;
    private final String interestAmount;
    private final String claimFee;
    private final String totalAmountOfClaim;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    private final LocalDate claimIssuedDate;
}
