package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class LipExtraDQ {

    private YesOrNo wantPhoneOrVideoHearing;
    private String whyPhoneOrVideoHearing;
    private String whyUnavailableForHearing;
    private YesOrNo giveEvidenceYourSelf;
    private YesOrNo triedToSettle;
    private YesOrNo determinationWithoutHearingRequired;
    private String determinationWithoutHearingReason;
    private YesOrNo requestExtra4weeks;
    private YesOrNo considerClaimantDocuments;
    private String considerClaimantDocumentsDetails;
}
