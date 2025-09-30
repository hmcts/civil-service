package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class FreeFormOrder implements MappableObject {

    private final String caseNumber;
    private final String caseName;
    private final String receivedDate;
    private final String freeFormRecitalText;
    private final String freeFormOrderedText;
    private final String freeFormOrderValue;
    private final String courtName;
    private final String locationName;
    private final String siteName;
    private final String address;
    private final String postcode;
    private final YesOrNo isMultiParty;
    private final String judgeNameTitle;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String claimant1Name;
    private final String claimant2Name;

    private final String partyName;
    private final String partyAddressAddressLine1;
    private final String partyAddressAddressLine2;
    private final String partyAddressAddressLine3;
    private final String partyAddressPostTown;
    private final String partyAddressPostCode;
}
