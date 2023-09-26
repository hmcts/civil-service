package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    private String timelineComment;
    private String evidenceComment;
    private MediationLiP respondent1MediationLiPResponse;
    private String respondent1LiPContactPerson;
    private Address respondent1LiPCorrespondenceAddress;
    private DQExtraDetailsLip respondent1DQExtraDetails;
    private HearingSupportLip respondent1DQHearingSupportLip;
    private String respondent1ResponseLanguage;
    private String helpWithFeesReferenceNumberLip;
}
