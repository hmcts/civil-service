package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    private DQExtraDetailsLip respondent1DQExtraDetails;
    private HearingSupportLip respondent1DQHearingSupportLip;
}
