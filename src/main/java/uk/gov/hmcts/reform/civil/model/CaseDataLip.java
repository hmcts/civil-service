package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDataLip {

    @JsonProperty("respondent1LiPResponse")
    private RespondentLiPResponse respondent1LiPResponse;
}
