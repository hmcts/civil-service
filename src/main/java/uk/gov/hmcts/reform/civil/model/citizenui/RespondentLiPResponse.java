package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {

    private MediationLiP respondent1MediationLiPResponse;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    private String respondent1ResponseLanguage;

    @JsonIgnore
    public boolean isRespondentResponseBilingual(CaseData caseData) {
        boolean responseLanguage = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .filter(respondent1ResponseLanguage -> Language.BOTH.toString().equals(respondent1ResponseLanguage))
            .isPresent();

        return responseLanguage;
    }
}
