package uk.gov.hmcts.reform.civil.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.workflow.helper.WorkflowBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;

public abstract class WorkflowIntegrationTest extends BaseIntegrationTest {

    protected static final String SYSTEM_USER = "system-user";
    protected static final String SYSTEM_PASSWORD = "password";
    protected static final String SYSTEM_TOKEN = "system-token";

    protected static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";
    protected static final String CALLBACK_PAGE_ID_URL = "/cases/callbacks/{callback-type}/{page-id}";

    @MockBean
    protected SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @BeforeEach
    void setUpWorkflowIntegrationTest() {
        when(systemUpdateUserConfiguration.getUserName()).thenReturn(SYSTEM_USER);
        when(systemUpdateUserConfiguration.getPassword()).thenReturn(SYSTEM_PASSWORD);
        when(userService.getAccessToken(SYSTEM_USER, SYSTEM_PASSWORD)).thenReturn(SYSTEM_TOKEN);
    }

    protected WorkflowBuilder<CaseData> startWorkflow(CaseData caseData) {
        return new WorkflowBuilder<>(this::invokeCallback, caseData);
    }

    public WorkflowBuilder.CallbackResult<CaseData> invokeCallback(
        CaseData caseData,
        CaseData caseDataBefore,
        String eventId,
        CallbackType callbackType,
        String pageId
    ) throws Exception {
        CallbackInvocationResult<CaseData> result = invokeCallback(
            caseData,
            caseDataBefore,
            eventId,
            callbackType,
            pageId,
            CASE_TYPE,
            CaseData.class,
            CaseData::getCcdCaseReference,
            data -> data.getCcdState() != null ? data.getCcdState().name() : null
        );

        return new WorkflowBuilder.CallbackResult<>(
            result.response(),
            result.submittedResponse(),
            result.caseData(),
            result.rawBody()
        );
    }

    @SuppressWarnings("java:S107")
    protected <T extends MappableObject> CallbackInvocationResult<T> invokeCallback(
        T caseData,
        T caseDataBefore,
        String eventId,
        CallbackType callbackType,
        String pageId,
        String caseTypeId,
        Class<T> caseDataClass,
        Function<T, Long> caseReferenceExtractor,
        Function<T, String> stateExtractor
    ) throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(toCaseDetails(caseData, caseTypeId, caseReferenceExtractor, stateExtractor))
            .caseDetailsBefore(caseDataBefore != null
                                   ? toCaseDetails(caseDataBefore, caseTypeId, caseReferenceExtractor, stateExtractor)
                                   : null)
            .build();

        ResultActions response = pageId == null
            ? doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, callbackType.getValue())
            : doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, callbackType.getValue(), pageId);

        MvcResult result = response
            .andExpect(status().isOk())
            .andReturn();

        String body = result.getResponse().getContentAsString();

        if (callbackType == CallbackType.SUBMITTED) {
            JsonNode submittedResponse = body.isBlank()
                ? objectMapper.createObjectNode()
                : objectMapper.readTree(body);

            return new CallbackInvocationResult<>(
                null,
                submittedResponse,
                caseData,
                body
            );
        }

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            body,
            AboutToStartOrSubmitCallbackResponse.class
        );

        return new CallbackInvocationResult<>(
            callbackResponse,
            null,
            toCaseData(callbackResponse, caseData, caseDataClass, caseReferenceExtractor, stateExtractor),
            body
        );
    }

    @SuppressWarnings("java:S4276")
    protected <T extends MappableObject> CaseDetails toCaseDetails(
        T caseData,
        String caseTypeId,
        Function<T, Long> caseReferenceExtractor,
        Function<T, String> stateExtractor
    ) {
        return CaseDetails.builder()
            .id(caseReferenceExtractor.apply(caseData))
            .state(stateExtractor.apply(caseData))
            .caseTypeId(caseTypeId)
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @SuppressWarnings("java:S4276")
    protected <T extends MappableObject> T toCaseData(
        AboutToStartOrSubmitCallbackResponse callbackResponse,
        T currentCaseData,
        Class<T> caseDataClass,
        Function<T, Long> caseReferenceExtractor,
        Function<T, String> stateExtractor
    ) {
        if (callbackResponse.getData() == null) {
            return currentCaseData;
        }

        Map<String, Object> updatedData = new HashMap<>(currentCaseData.toMap(objectMapper));
        updatedData.putAll(callbackResponse.getData());
        updatedData.put("ccdCaseReference", caseReferenceExtractor.apply(currentCaseData));

        String state = resolveState(callbackResponse, currentCaseData, stateExtractor);

        if (state != null) {
            updatedData.put("ccdState", state);
        }

        return objectMapper.convertValue(updatedData, caseDataClass);
    }

    protected <T> String resolveState(
        AboutToStartOrSubmitCallbackResponse callbackResponse,
        T currentCaseData,
        Function<T, String> stateExtractor
    ) {
        if (callbackResponse.getState() != null) {
            return callbackResponse.getState();
        }
        String currentState = stateExtractor.apply(currentCaseData);
        if (currentState != null) {
            return currentState;
        }
        return null;
    }

    protected record CallbackInvocationResult<T>(
        AboutToStartOrSubmitCallbackResponse response,
        JsonNode submittedResponse,
        T caseData,
        String rawBody
    ) {
    }

}
