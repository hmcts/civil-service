package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentResponseCui {
    private FinancialDetailsCui respondent1FinancialDetailsFromCui;
    private MediationCUI respondent1MediationFromCui;
}
