package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_ONE;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_ONE;

@Component
@AllArgsConstructor
public class CaseQueriesInitializer {

    public void initialiseCaseQueries(CaseData.CaseDataBuilder dataBuilder) {

        CaseData caseData = dataBuilder.build();
        initialiseQueriesCollection(dataBuilder, caseData);

    }

    private void initialiseQueriesCollection(CaseData.CaseDataBuilder dataBuilder, CaseData caseData) {
        dataBuilder.qmApplicantSolicitorQueries(
                CaseQueriesUtuil.createCaseQueries(APPLICANT_ONE, caseData.getApplicant1().getPartyName()))
            .qmRespondentSolicitor1Queries(
                CaseQueriesUtuil.createCaseQueries(RESPONDENT_ONE, caseData.getRespondent1().getPartyName()));
    }

}
