package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DQExtraDetailsLip {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo wantPhoneOrVideoHearing;
    @CCD(label = " ", searchable = false)
    private String whyPhoneOrVideoHearing;
    @CCD(label = " ", searchable = false)
    private String whyUnavailableForHearing;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo giveEvidenceYourSelf;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo triedToSettle;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo determinationWithoutHearingRequired;
    @CCD(label = " ", searchable = false)
    private String determinationWithoutHearingReason;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo requestExtra4weeks;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo considerClaimantDocuments;
    @CCD(label = " ", searchable = false)
    private String considerClaimantDocumentsDetails;
    @CCD(label = " ", searchable = false)
    private ExpertLiP respondent1DQLiPExpert;
    @CCD(label = " ", searchable = false)
    private ExpertLiP applicant1DQLiPExpert;

    @JsonIgnore
    public List<ExpertReportLiP> getReportExpertDetails() {
        return Optional.ofNullable(respondent1DQLiPExpert).map(ExpertLiP::getUnwrappedDetails).orElse(Collections.emptyList());
    }
}
