package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantLiPResponse {

    private DQExtraDetailsLip applicant1DQExtraDetails;
    private HearingSupportLip applicant1DQHearingSupportLip;
}
