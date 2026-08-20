package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class StartBusinessProcessFixtures {

    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_TEMPLATE =
        "start-business-process-proceeds-in-heritage-system";
    private static final long CASE_REFERENCE = 1779198401913981L;

    private StartBusinessProcessFixtures() {
    }

    public static CaseData proceedsInHeritageSystemCaseData() {
        return CaseDataTemplates.load(PROCEEDS_IN_HERITAGE_SYSTEM_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdCaseReference", CASE_REFERENCE);
            CaseDataTemplates.set(template, "ccdState", CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
            CaseDataTemplates.set(template, "businessProcess", BusinessProcess.ready(CaseEvent.START_BUSINESS_PROCESS));
        });
    }
}
