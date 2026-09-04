package uk.gov.hmcts.reform.civil.workflow.ga;

import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaEventEmitterService;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.helper.WorkflowBuilder;

import java.util.function.Function;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

@SuppressWarnings("java:S5960")
public abstract class GAWorkflowIntegrationTest extends WorkflowIntegrationTest {

    @MockBean
    protected GaEventEmitterService gaEventEmitterService;

    protected WorkflowBuilder<GeneralApplicationCaseData> startWorkflow(GeneralApplicationCaseData caseData) {
        return new WorkflowBuilder<>(this::invokeCallback, caseData);
    }

    @Override
    protected <T extends MappableObject> CaseDetails toCaseDetails(
        T caseData,
        String caseTypeId,
        Function<T, Long> caseReferenceExtractor,
        Function<T, String> stateExtractor
    ) {
        CaseDetails.CaseDetailsBuilder builder = CaseDetails.builder()
            .id(caseReferenceExtractor.apply(caseData))
            .state(stateExtractor.apply(caseData))
            .caseTypeId(caseTypeId)
            .data(caseData.toMap(objectMapper));

        if (caseData instanceof GeneralApplicationCaseData generalApplicationCaseData) {
            builder.createdDate(generalApplicationCaseData.getCreatedDate());
        }

        return builder.build();
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
