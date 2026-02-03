package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
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
    private LipExtraDQEvidenceConfirmDetails giveEvidenceConfirmDetails;
}
