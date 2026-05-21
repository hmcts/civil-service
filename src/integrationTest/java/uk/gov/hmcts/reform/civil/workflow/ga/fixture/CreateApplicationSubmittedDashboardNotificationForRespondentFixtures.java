package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class CreateApplicationSubmittedDashboardNotificationForRespondentFixtures {

    private static final String APPLICATION_SUBMITTED = "ga/application-submitted";

    private CreateApplicationSubmittedDashboardNotificationForRespondentFixtures() {
    }

    public static GeneralApplicationCaseData caseData() {
        return CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class);
    }

    public static GeneralApplicationCaseData urgentCaseData() {
        return CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class, template ->
            CaseDataTemplates.set(
                template,
                "generalAppUrgencyRequirement",
                new GAUrgencyRequirement().setGeneralAppUrgency(YesOrNo.YES)));
    }
}
