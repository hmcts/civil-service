package uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class JudgeDecisionPdfDocument implements MappableObject {

    private final String claimNumber;
    private final String claimantName;
    private final String defendantName;
    private final String applicationType;
    private final String judicialByCourtsInitiative;
    private final String locationName;
    private final String courtName;
    private final String courtNameCy;
    private final String judgeHearingLocation;
    private final String siteName;
    private final String address;
    private final String postcode;
    private final YesOrNo reasonAvailable;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate receivedDate;
    private final String partyName;
    private final String partyAddressAddressLine1;
    private final String partyAddressAddressLine2;
    private final String partyAddressAddressLine3;
    private final String partyAddressPostTown;
    private final String partyAddressPostCode;

    private final String judicialByCourtsInitiativeListForHearing;
    private final String judicialByCourtsInitiativeForWrittenRep;
    private final String applicantName;
    private final String judgeDirection;
    private final String dismissalOrder;
    private final String generalOrder;
    private final String requestOrder;
    private final String writtenOrder;
    private final String hearingPrefType;
    private final String estimatedHearingLength;
    private final String reasonForDecision;
    private final String judgeRecital;
    private final String hearingOrder;
    private final String judgeComments;
    private final String judgeNameTitle;
    private final YesOrNo isMultiParty;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String additionalApplicationFee;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate dateBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate uploadDeadlineDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate  responseDeadlineDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate applicationCreatedDate;

    private final String applicationCreatedDateCy;
}
