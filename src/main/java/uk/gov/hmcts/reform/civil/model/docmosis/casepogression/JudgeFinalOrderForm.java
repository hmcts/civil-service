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
    private final String freeFormRecitalText;
    private final String freeFormRecordedText;
    private final String freeFormOrderedText;
    private final OrderOnCourtsList orderOnCourtsList;
    private final String onInitiativeSelectionText;
    private final LocalDate onInitiativeSelectionDate;
    private final String withoutNoticeSelectionText;
    private final LocalDate withoutNoticeSelectionDate;
    private final YesOrNo finalOrderMadeSelection;
    private final LocalDate finalOrderHeardDate;
    private final String finalOrderRepresented;
    private final boolean claimantAttended;
    private final boolean defendantAttended;
    private final String judgeHeardFromClaimantTextIfAttended;
    private final String judgeHeardFromDefendantTextIfAttended;
    private final String judgeHeardClaimantNotAttendedText;
    private final String judgeHeardDefendantNotAttendedText;
    private final String otherRepresentedText;
    private final boolean judgeConsideredPapers;
    private final boolean recordedToggle;
    private final String recordedText;
    private final String orderedText;
    private final String costSelection;
    private final String costReservedText;
    private final LocalDate paidByDate;
    private final YesOrNo costProtection;
    private final String costAmount;
    private final String bespokeText;
    private final boolean furtherHearingToggle;
    private final LocalDate furtherHearingFromDate;
    private final LocalDate furtherHearingToDate;
    private final String furtherHearingLength;
    private final String furtherHearingLocation;
    private final String furtherHearingMethod;
    private final boolean appealToggle;
    private final String appealFor;
    private final boolean appealGranted;
    private final String appealReason;
    private final String orderWithoutNotice;
    private final LocalDate orderInitiativeOrWithoutNoticeDate;
    private final YesOrNo isReason;
    private final String reasonText;
}
