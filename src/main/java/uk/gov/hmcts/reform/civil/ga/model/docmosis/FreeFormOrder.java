package uk.gov.hmcts.reform.civil.ga.model.docmosis;

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

    private String caseNumber;
    private String caseName;
    private String receivedDate;
    private String freeFormRecitalText;
    private String freeFormOrderedText;
    private String freeFormOrderValue;
    private String courtName;
    private String locationName;
    private String siteName;
    private String address;
    private String postcode;
    private YesOrNo isMultiParty;
    private String judgeNameTitle;
    private String defendant1Name;
    private String defendant2Name;
    private String claimant1Name;
    private String claimant2Name;

    private String partyName;
    private String partyAddressAddressLine1;
    private String partyAddressAddressLine2;
    private String partyAddressAddressLine3;
    private String partyAddressPostTown;
    private String partyAddressPostCode;
}
