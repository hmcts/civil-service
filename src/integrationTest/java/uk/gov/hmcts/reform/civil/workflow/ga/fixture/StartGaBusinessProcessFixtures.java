package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class StartGaBusinessProcessFixtures {

    private static final String APPLICATION_SUBMITTED = "ga/application-submitted";

    private StartGaBusinessProcessFixtures() {
    }

    public static GeneralApplicationCaseData caseData() {
        return CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class, template ->
            CaseDataTemplates.set(
                template,
                "businessProcess",
                BusinessProcess.readyGa(CaseEvent.START_GA_BUSINESS_PROCESS)));
    }
}
