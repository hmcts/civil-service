package uk.gov.hmcts.reform.civil.model.citizenui;

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

    private YesOrNo partialAdmissionAlreadyPaid;
    private String timelineComment;
    private String evidenceComment;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
}
