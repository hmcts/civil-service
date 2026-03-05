package uk.gov.hmcts.reform.civil.ga.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
@Accessors(chain = true)
public class AssistedOrderForm implements MappableObject {

    private String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate receivedDate;
    private String claimantReference;
    private String defendantReference;
    private YesOrNo isMultiParty;
    private String claimant1Name;
    private String claimant2Name;
    private String defendant1Name;
    private String defendant2Name;
    private String judgeNameTitle;
    private String courtLocation;
    private String siteName;
    private String postcode;
    private String address;
    //Order Made
    private YesOrNo isOrderMade;
    private Boolean isSingleDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate orderMadeSingleDate;
    private Boolean isDateRange;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate orderMadeDateRangeFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate orderMadeDateRangeTo;
    private Boolean isBeSpokeRange;
    private String orderMadeBeSpokeText;
    //Judge HeardFrom Section
    private Boolean judgeHeardFromShowHide;
    private String judgeHeardSelection;
    private String claimantRepresentation;
    private String defendantRepresentation;
    private String defendantTwoRepresentation;
    private Boolean isOtherRepresentation;
    private String otherRepresentationText;
    private String heardClaimantNotAttend;
    private String heardDefendantNotAttend;
    private Boolean isDefendantTwoExists;
    private String heardDefendantTwoNotAttend;
    private Boolean isJudgeConsidered;
    //Ordered
    private String orderedText;
    //Recitals
    private Boolean showRecitals;
    private String recitalRecordedText;
    //Further Hearing
    private Boolean showFurtherHearing;
    private YesOrNo checkListToDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate furtherHearingListFromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate furtherHearingListToDate;
    private String furtherHearingDuration;
    private Boolean checkDatesToAvoid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate furtherHearingDatesToAvoid;
    private String furtherHearingLocation;
    private String furtherHearingMethod;
    //Costs
    private String costSelection;
    private String costsReservedText;
    private String summarilyAssessed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate summarilyAssessedDate;
    private String detailedAssessment;
    private String interimPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate interimPaymentDate;
    private String beSpokeCostsText;
    private YesOrNo costsProtection;
    private String beSpokeCostDetailsText;
    private Boolean isQocsProtectionEnabled;
    //Appeal
    private Boolean showAppeal;
    private String claimantOrDefendantAppeal;
    private Boolean isAppealGranted;
    private String tableAorB;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate appealDate;
    //Order Made
    private Boolean showInitiativeOrWithoutNotice;
    private Boolean showInitiative;
    private String orderMadeOnText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate initiativeDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate withoutNoticeDate;
    //Reasons
    private String reasonsText;

    private String partyName;
    private String partyAddressAddressLine1;
    private String partyAddressAddressLine2;
    private String partyAddressAddressLine3;
    private String partyAddressPostTown;
    private String partyAddressPostCode;
}
