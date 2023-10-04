package uk.gov.hmcts.reform.civil.model.docmosis.casepogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class JudgeFinalOrderForm implements MappableObject {

    private final String caseNumber;
    private final String caseName;
    private final String claimantReference;
    private final String defendantReference;
    private final String freeFormRecordedText;
    private final String freeFormOrderedText;
    private final OrderOnCourtsList orderOnCourtsList;
    private final String onInitiativeSelectionText;
    private final LocalDate onInitiativeSelectionDate;
    private final String withoutNoticeSelectionText;
    private final LocalDate withoutNoticeSelectionDate;
    private final YesOrNo finalOrderMadeSelection;
    private final String otherRepresentedText;
    private final String judgeConsideredPapers;
    private final boolean recordedToggle;
    private final String recordedText;
    private final String orderedText;
    private final String costSelection;
    private final String costsReservedText;
    private final String bespokeCostText;
    private final boolean furtherHearingToggle;
    private final boolean furtherHearingToToggle;
    private final LocalDate furtherHearingFromDate;
    private final LocalDate furtherHearingToDate;
    private final String furtherHearingLength;
    private LocalDate datesToAvoid;
    private final String furtherHearingLocationDefault;
    private final Boolean showFurtherHearingLocationAlt;
    private final String furtherHearingLocationAlt;
    private final String furtherHearingMethod;
    private final String hearingNotes;
    private final String appealGranted;
    private final String claimantOrDefendantAppeal;
    private final String tableAorB;
    private final LocalDate appealDate;
    private final String showInitiativeOrWithoutNotice;
    private final LocalDate initiativeDate;
    private final LocalDate withoutNoticeDate;
    private final String reasonsText;
    private final String judgeNameTitle;
    private final String courtLocation;
    private final String courtName;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String orderMadeDate;
    private final String claimantAttendsOrRepresented;
    private final String claimantTwoAttendsOrRepresented;
    private final String defendantAttendsOrRepresented;
    private final String defendantTwoAttendsOrRepresented;
    private final String summarilyAssessed;
    private final LocalDate summarilyAssessedDate;
    private final String detailedAssessment;
    private final String interimPayment;
    private final LocalDate interimPaymentDate;
    private final String qcosProtection;
    private final String costsProtection;
    private final Boolean finalOrderJudgeHeardFrom;
}
