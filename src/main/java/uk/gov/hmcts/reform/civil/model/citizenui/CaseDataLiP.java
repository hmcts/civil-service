package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDataLiP {

    @JsonProperty("respondent1LiPResponse")
    private RespondentLiPResponse respondent1LiPResponse;

    @JsonProperty("translationDocument")
    private CaseDocument translationDocument;

}
