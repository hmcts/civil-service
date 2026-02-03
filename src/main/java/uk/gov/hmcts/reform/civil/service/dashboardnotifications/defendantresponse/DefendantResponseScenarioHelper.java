package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

public final class DefendantResponseScenarioHelper {

    private DefendantResponseScenarioHelper() {
    }

    public static boolean isCarmApplicable(FeatureToggleService featureToggleService, CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData)
            && caseData.isSmallClaim();
    }

    public static String scenarioForRespondentPartyType(CaseData caseData,
                                                        String companyOrOrganisationScenario,
                                                        String individualScenario) {
        Party respondent = caseData.getRespondent1();
        if (respondent != null && respondent.isCompanyOROrganisation()) {
            return companyOrOrganisationScenario;
        }
        return individualScenario;
    }
}
