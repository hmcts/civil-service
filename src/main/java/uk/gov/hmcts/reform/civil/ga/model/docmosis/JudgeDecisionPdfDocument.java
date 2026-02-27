package uk.gov.hmcts.reform.civil.ga.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgeDecisionPdfDocument implements MappableObject {

    private String claimNumber;
    private String claimantName;
    private String defendantName;
    private String applicationType;
    private String judicialByCourtsInitiative;
    private String locationName;
    private String courtName;
    private String courtNameCy;
    private String judgeHearingLocation;
    private String siteName;
    private String address;
    private String postcode;
    private YesOrNo reasonAvailable;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate receivedDate;
    private String partyName;
    private String partyAddressAddressLine1;
    private String partyAddressAddressLine2;
    private String partyAddressAddressLine3;
    private String partyAddressPostTown;
    private String partyAddressPostCode;

    private String judicialByCourtsInitiativeListForHearing;
    private String judicialByCourtsInitiativeForWrittenRep;
    private String applicantName;
    private String judgeDirection;
    private String dismissalOrder;
    private String generalOrder;
    private String requestOrder;
    private String writtenOrder;
    private String hearingPrefType;
    private String estimatedHearingLength;
    private String reasonForDecision;
    private String judgeRecital;
    private String hearingOrder;
    private String judgeComments;
    private String judgeNameTitle;
    private YesOrNo isMultiParty;
    private String defendant1Name;
    private String defendant2Name;
    private String claimant1Name;
    private String claimant2Name;
    private String additionalApplicationFee;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate uploadDeadlineDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate  responseDeadlineDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate applicationCreatedDate;

    private String applicationCreatedDateCy;
}
