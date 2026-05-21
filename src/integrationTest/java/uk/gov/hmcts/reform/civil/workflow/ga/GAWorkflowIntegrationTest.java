package uk.gov.hmcts.reform.civil.workflow.ga;

import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.helper.WorkflowBuilder;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

@SuppressWarnings("java:S5960")
public abstract class GAWorkflowIntegrationTest extends WorkflowIntegrationTest {

    protected WorkflowBuilder<GeneralApplicationCaseData> startWorkflow(GeneralApplicationCaseData caseData) {
        return new WorkflowBuilder<>(this::invokeCallback, caseData);
    }

    public WorkflowBuilder.CallbackResult<GeneralApplicationCaseData> invokeCallback(
        GeneralApplicationCaseData caseData,
        GeneralApplicationCaseData caseDataBefore,
        String eventId,
        CallbackType callbackType,
        String pageId
    ) throws Exception {
        CallbackInvocationResult<GeneralApplicationCaseData> result = invokeCallback(
            caseData,
            caseDataBefore,
            eventId,
            callbackType,
            pageId,
            GENERALAPPLICATION_CASE_TYPE,
            GeneralApplicationCaseData.class,
            GeneralApplicationCaseData::getCcdCaseReference,
            data -> data.getCcdState() != null ? data.getCcdState().name() : null
        );

        return new WorkflowBuilder.CallbackResult<>(
            result.response(),
            result.submittedResponse(),
            result.caseData(),
            result.rawBody()
        );
    }
}
