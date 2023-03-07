package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    @JsonUnwrapped
    private PartAdmitResponseLiP partAdmitResponseLiP;
    private String timelineComment;
    private String evidenceComment;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
}
