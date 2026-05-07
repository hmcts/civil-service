package uk.gov.hmcts.reform.civil.workflow;

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
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;

public abstract class WorkflowIntegrationTest extends BaseIntegrationTest {

    protected static final String SYSTEM_USER = "system-user";
    protected static final String SYSTEM_PASSWORD = "password";
    protected static final String SYSTEM_TOKEN = "system-token";

    static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";
    static final String CALLBACK_PAGE_ID_URL = "/cases/callbacks/{callback-type}/{page-id}";

    @MockBean
    protected IdamClient idamClient;
    @MockBean
    protected CoreCaseUserService coreCaseUserService;
    @MockBean
    protected SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @BeforeEach
    void setUpWorkflowIntegrationTest() {
        when(systemUpdateUserConfiguration.getUserName()).thenReturn(SYSTEM_USER);
        when(systemUpdateUserConfiguration.getPassword()).thenReturn(SYSTEM_PASSWORD);
        when(userService.getAccessToken(SYSTEM_USER, SYSTEM_PASSWORD)).thenReturn(SYSTEM_TOKEN);
    }

    protected WorkflowBuilder startWorkflow(CaseData caseData) {
        return new WorkflowBuilder(this, caseData);
    }

    protected void stubIdamUserLookup(String email, String userId) {
        when(idamClient.searchUsers(SYSTEM_TOKEN, idamEmailSearch(email)))
            .thenReturn(List.of(UserDetails.builder().id(userId).build()));
    }

    protected void stubIdamUserNotFound(String email) {
        when(idamClient.searchUsers(SYSTEM_TOKEN, idamEmailSearch(email))).thenReturn(List.of());
    }

    WorkflowBuilder.CallbackResult invokeCallback(
        CaseData caseData,
        CaseData caseDataBefore,
        String eventId,
        CallbackType callbackType,
        String pageId
    ) throws Exception {
        // Build the same CCD callback payload shape that the controller receives in production.
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(toCaseDetails(caseData))
            .caseDetailsBefore(caseDataBefore != null ? toCaseDetails(caseDataBefore) : null)
            .build();

        ResultActions response = pageId == null
            ? doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, callbackType.getValue())
            : doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, callbackType.getValue(), pageId);

        MvcResult result = response
            .andExpect(status().isOk())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            body,
            AboutToStartOrSubmitCallbackResponse.class
        );

        return new WorkflowBuilder.CallbackResult(
            callbackResponse,
            toCaseData(callbackResponse, caseData),
            body
        );
    }

    private CaseDetails toCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getCcdCaseReference())
            .state(caseData.getCcdState() != null ? caseData.getCcdState().name() : null)
            .caseTypeId(CASE_TYPE)
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CaseData toCaseData(AboutToStartOrSubmitCallbackResponse callbackResponse, CaseData currentCaseData) {
        if (callbackResponse.getData() == null) {
            return currentCaseData;
        }

        // Rehydrate CaseData from callback response data while preserving CCD metadata used by later steps.
        Map<String, Object> updatedData = new HashMap<>(callbackResponse.getData());
        updatedData.put("ccdCaseReference", currentCaseData.getCcdCaseReference());

        String state = callbackResponse.getState() != null
            ? callbackResponse.getState()
            : currentCaseData.getCcdState() != null ? currentCaseData.getCcdState().name() : null;

        if (state != null) {
            updatedData.put("ccdState", state);
        }

        return objectMapper.convertValue(updatedData, CaseData.class);
    }

    private String idamEmailSearch(String email) {
        return String.format("email:\"%s\"", email);
    }
}
