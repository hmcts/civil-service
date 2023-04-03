package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Address;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class RespondentLiPResponse {

    private MediationLiP respondent1MediationLiPResponse;
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    private String respondent1LiPContactPerson;
    private Address respondent1LiPCorrespondenceAddress;
    private DQExtraDetailsLip respondent1DQExtraDetails;
    private HearingSupportLip respondent1DQHearingSupportLip;
    private String respondent1ResponseLanguage;

    @JsonIgnore
    public boolean isRespondentResponseBilingual(CaseData caseData) {
        boolean responseLanguage = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .filter(Language.BOTH.toString()::equals)
            .isPresent();

        return responseLanguage;
    }
}
