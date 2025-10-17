package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class AssistedOrderForm implements MappableObject {

    private final String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate receivedDate;
    private final String claimantReference;
    private final String defendantReference;
    private final YesOrNo isMultiParty;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String judgeNameTitle;
    private final String courtLocation;
    private final String siteName;
    private final String postcode;
    private final String address;
    //Order Made
    private final YesOrNo isOrderMade;
    private final Boolean isSingleDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate orderMadeSingleDate;
    private final Boolean isDateRange;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate orderMadeDateRangeFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate orderMadeDateRangeTo;
    private final Boolean isBeSpokeRange;
    private final String orderMadeBeSpokeText;
    //Judge HeardFrom Section
    private final Boolean judgeHeardFromShowHide;
    private final String judgeHeardSelection;
    private final String claimantRepresentation;
    private final String defendantRepresentation;
    private final String defendantTwoRepresentation;
    private final Boolean isOtherRepresentation;
    private final String otherRepresentationText;
    private final String heardClaimantNotAttend;
    private final String heardDefendantNotAttend;
    private final Boolean isDefendantTwoExists;
    private final String heardDefendantTwoNotAttend;
    private final Boolean isJudgeConsidered;
    //Ordered
    private final String orderedText;
    //Recitals
    private final Boolean showRecitals;
    private final String recitalRecordedText;
    //Further Hearing
    private final Boolean showFurtherHearing;
    private final YesOrNo checkListToDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate furtherHearingListFromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate furtherHearingListToDate;
    private final String furtherHearingDuration;
    private final Boolean checkDatesToAvoid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate furtherHearingDatesToAvoid;
    private final String furtherHearingLocation;
    private final String furtherHearingMethod;
    //Costs
    private final String costSelection;
    private final String costsReservedText;
    private final String summarilyAssessed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate summarilyAssessedDate;
    private final String detailedAssessment;
    private final String interimPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate interimPaymentDate;
    private final String beSpokeCostsText;
    private final YesOrNo costsProtection;
    private final String beSpokeCostDetailsText;
    private final Boolean isQocsProtectionEnabled;
    //Appeal
    private final Boolean showAppeal;
    private final String claimantOrDefendantAppeal;
    private final Boolean isAppealGranted;
    private final String tableAorB;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate appealDate;
    //Order Made
    private final Boolean showInitiativeOrWithoutNotice;
    private final Boolean showInitiative;
    private final String orderMadeOnText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate initiativeDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate withoutNoticeDate;
    //Reasons
    private final String reasonsText;

    private final String partyName;
    private final String partyAddressAddressLine1;
    private final String partyAddressAddressLine2;
    private final String partyAddressAddressLine3;
    private final String partyAddressPostTown;
    private final String partyAddressPostCode;
}
