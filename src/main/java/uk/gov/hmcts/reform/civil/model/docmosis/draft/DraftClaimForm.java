package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class DraftClaimForm implements MappableObject {

    private final LipFormParty claimant;
    private final LipFormParty defendant;
    private final Address claimantCorrespondenceAddress;
    private final Address defendantCorrespondenceAddress;
    private final String descriptionOfClaim;
    private final List<EventTemplateData> timelineEvents;
    private final List<ClaimAmountBreakupDetails> claimAmount;
    private final String totalClaimAmount;

}
