package uk.gov.hmcts.reform.civil.handler.callback.testing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

public class LinkDefendantToClaimCallbackHandlerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";
    private static final String CALLBACK_PAGE_ID_URL = "/cases/callbacks/{callback-type}/{page-id}";

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private CoreCaseUserService coreCaseUserService;
    @MockBean
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @BeforeEach
    void setup() {
        when(featureToggleService.isLinkDefendantTestingEnabled()).thenReturn(true);
        when(systemUpdateUserConfiguration.getUserName()).thenReturn("system-user");
        when(systemUpdateUserConfiguration.getPassword()).thenReturn("password");
    }

    @Test
    void midEvent_shouldReturnNoErrors_whenEmailIsValid() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefendantEmailAddress("test@example.com");

        var result = doPost(
                BEARER_TOKEN,
                buildCallbackRequest(caseData, "LINK_DEFENDANT_TO_CLAIM"),
                CALLBACK_PAGE_ID_URL,
                MID.getValue(),
                "confirm-defendant-email"
            )
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(responseString, AboutToStartOrSubmitCallbackResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void midEvent_shouldReturnErrors_whenEmailIsInvalid() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefendantEmailAddress("invalid-email");

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(
            doPost(
                BEARER_TOKEN,
                buildCallbackRequest(caseData, "LINK_DEFENDANT_TO_CLAIM"),
                CALLBACK_PAGE_ID_URL,
                MID.getValue(),
                "confirm-defendant-email"
            )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).contains("Enter an email address in the correct format, for example john.smith@example.com");
    }

    @Test
    void aboutToSubmit_shouldLinkDefendant_whenUserExists() throws Exception {
        String email = "test@example.com";
        String userId = "user-id-123";
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(12345678L)
            .respondent1(new PartyBuilder().individual().build())
            .build();
        caseData.setDefendantEmailAddress(email);

        when(userService.getAccessToken(anyString(), anyString())).thenReturn("system-token");
        when(idamClient.searchUsers("system-token", "email:\"" + email + "\""))
            .thenReturn(List.of(UserDetails.builder().id(userId).build()));

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(
            doPost(
                BEARER_TOKEN,
                buildCallbackRequest(caseData, "LINK_DEFENDANT_TO_CLAIM"),
                CALLBACK_URL,
                ABOUT_TO_SUBMIT.getValue()
            )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().get("defendantUserDetails")).isNotNull();
        assertThat(response.getData().get("respondent1")).extracting("partyEmail").isEqualTo(email);
    }

    @Test
    void aboutToSubmit_shouldReturnError_whenUserDoesNotExist() throws Exception {
        String email = "nonexistent@example.com";
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(12345678L)
            .build();
        caseData.setDefendantEmailAddress(email);

        when(userService.getAccessToken(anyString(), anyString())).thenReturn("system-token");
        when(idamClient.searchUsers("system-token", "email:\"" + email + "\""))
            .thenReturn(List.of());

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(
            doPost(
                BEARER_TOKEN,
                buildCallbackRequest(caseData, "LINK_DEFENDANT_TO_CLAIM"),
                CALLBACK_URL,
                ABOUT_TO_SUBMIT.getValue()
            )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).contains("No user found with the provided email address");
    }

    private CallbackRequest buildCallbackRequest(CaseData caseData, String eventId) {
        return CallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(CaseDetailsBuilder.builder()
                             .data(caseData.toMap(objectMapper))
                             .id(caseData.getCcdCaseReference())
                             .build())
            .build();
    }
}
