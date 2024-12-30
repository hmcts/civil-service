package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.*;

@Component
@AllArgsConstructor
public class CaseQueriesInitializer {

    private final OrganisationService organisationService;

    public void initialiseCaseQueries(CaseData.CaseDataBuilder dataBuilder) {

        CaseData caseData = dataBuilder.build();
        initialiseApplicantAndRespondentFlags(dataBuilder, caseData);

    }

    private void initialiseApplicantAndRespondentFlags(CaseData.CaseDataBuilder dataBuilder, CaseData caseData) {
        dataBuilder.qmApplicantSolicitorQueries(
            CaseQueriesUtuil.createCaseQueries(APPLICANT_ONE, caseData.getApplicant1().getPartyName()))
        .qmApplicantSolicitorQueries(
            CaseQueriesUtuil.createCaseQueries(RESPONDENT_ONE, caseData.getRespondent1().getPartyName()));
    }
}
