package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
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

    @JsonUnwrapped
    private PartAdmitResponseLiP partAdmitResponseLiP;
    private String timelineComment;
    private String evidenceComment;
    private MediationLiP respondent1MediationLiPResponse;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    private String respondent1LiPContactPerson;
    private Address respondent1LiPCorrespondenceAddress;
    private DQExtraDetailsLip respondent1DQExtraDetails;
    private HearingSupportLip respondent1DQHearingSupportLip;
}
