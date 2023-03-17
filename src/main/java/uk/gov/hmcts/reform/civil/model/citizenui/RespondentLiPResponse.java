package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.dq.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    private MediationLiP respondent1MediationLiPResponse;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    private String respondent1ResponseLanguage;
}
