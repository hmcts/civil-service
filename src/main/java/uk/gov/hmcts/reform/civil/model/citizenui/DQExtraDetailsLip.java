package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private ExpertLiP applicant1DQLiPExpert;

    @JsonIgnore
    public List<ExpertReportLiP> getReportExpertDetails() {
        return Optional.ofNullable(respondent1DQLiPExpert).map(ExpertLiP::getUnwrappedDetails).orElse(Collections.emptyList());
    }
}
