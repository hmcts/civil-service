package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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

}
