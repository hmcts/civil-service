package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DQExtraDetailsLip {

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
    private ExpertLiP respondent1DQLiPExpert;
}
