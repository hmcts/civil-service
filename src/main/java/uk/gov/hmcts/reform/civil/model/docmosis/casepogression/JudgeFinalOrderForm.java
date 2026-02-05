package uk.gov.hmcts.reform.civil.model.docmosis.casepogression;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class JudgeFinalOrderForm implements MappableObject {

    private String caseNumber;
    private String legacyNumber;
    private String caseName;
    private String claimantReference;
    private String defendantReference;
    private String freeFormRecordedText;
    private String freeFormOrderedText;
    private OrderOnCourtsList orderOnCourtsList;
    private String onInitiativeSelectionText;
    private LocalDate onInitiativeSelectionDate;
    private String withoutNoticeSelectionText;
    private LocalDate withoutNoticeSelectionDate;
    private YesOrNo finalOrderMadeSelection;
    private String otherRepresentedText;
    private String judgeConsideredPapers;
    private boolean recordedToggle;
    private String recordedText;
    private String orderedText;
    private String costSelection;
    private String costsReservedText;
    private String bespokeCostText;
    private boolean furtherHearingToggle;
    private boolean furtherHearingToToggle;
    private LocalDate furtherHearingFromDate;
    private LocalDate furtherHearingToDate;
    private String furtherHearingLength;
    private LocalDate datesToAvoid;
    private String furtherHearingLocationDefault;
    private Boolean showFurtherHearingLocationAlt;
    private String furtherHearingLocationAlt;
    private String furtherHearingMethod;
    private String hearingNotes;
    private String appealGranted;
    private String claimantOrDefendantAppeal;
    private String tableAorB;
    private LocalDate appealDate;
    private String showInitiativeOrWithoutNotice;
    private LocalDate initiativeDate;
    private LocalDate withoutNoticeDate;
    private String reasonsText;
    private String judgeNameTitle;
    private String courtLocation;
    private String courtName;
    private String claimant1Name;
    private String claimant2Name;
    private String defendant1Name;
    private String defendant2Name;
    private String claimantNum;
    private String defendantNum;
    private String orderMadeDate;
    private String claimantAttendsOrRepresented;
    private String claimantTwoAttendsOrRepresented;
    private String defendantAttendsOrRepresented;
    private String defendantTwoAttendsOrRepresented;
    private String summarilyAssessed;
    private LocalDate summarilyAssessedDate;
    private String detailedAssessment;
    private String interimPayment;
    private LocalDate interimPaymentDate;
    private String qcosProtection;
    private String costsProtection;
    private Boolean finalOrderJudgeHeardFrom;
    // Download order
    private YesOrNo claimTrack;
    private String trackAndComplexityBandText;
    private String dateNowPlus7;
    private String orderAfterHearingDate;
}
