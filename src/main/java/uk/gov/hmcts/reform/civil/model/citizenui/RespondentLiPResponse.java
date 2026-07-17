package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.model.citizenui.FinancialDetailsLiP;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    @CCD(label = " ", searchable = false)
    private String timelineComment;
    @CCD(label = " ", searchable = false)
    private String evidenceComment;
    @CCD(label = " ", searchable = false)
    private MediationLiP respondent1MediationLiPResponse;
    @CCD(label = " ", searchable = false)
    private String respondent1LiPContactPerson;
    @CCD(label = " ", searchable = false)
    private Address respondent1LiPCorrespondenceAddress;
    @CCD(label = " ", searchable = false)
    private DQExtraDetailsLip respondent1DQExtraDetails;
    @CCD(label = " ", searchable = false)
    private EvidenceConfirmDetails respondent1DQEvidenceConfirmDetails;
    @CCD(label = " ", searchable = false)
    private HearingSupportLip respondent1DQHearingSupportLip;
    @CCD(label = " ", searchable = false)
    private String respondent1ResponseLanguage;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false)
  private FinancialDetailsLiP respondent1LiPFinancialDetails;
  // ==== end synthesised definition-only fields ====
}
