package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

class ClaimantNocOnlineDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private ClaimantNocOnlineDashboardNotificationHandler handler;

    private CallbackParams callbackParams;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        caseData = CaseData.builder().ccdCaseReference(1234567890123456L).build();

        callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder()
                .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_APPLICANT.toString())
                .build()
        ).build();
    }

    @Test
    void shouldNotCallDashboardApiWhenToggleIsOff() {
        // Given
        when(toggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(false);

        handler.handle(callbackParams);
        // Then
        verifyNoInteractions(dashboardApiClient);
        //assertThat(response).isNotNull();
    }

    @Test
    void shouldCallDashboardApiWhenToggleIsOn() {
        // Given
        when(toggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>(Map.of(
            "respondent1PartyName",
            "Mr defendant"
        )));

        // When
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

        // Then
        verify(dashboardApiClient, times(1)).recordScenario(
            eq("1234567890123456"),
            eq("Scenario.AAA6.DefendantNoticeOfChange.ClaimRemainsOnline.Claimant"),
            eq("BEARER_TOKEN"),
            argThat(params ->
                        params.getParams().containsKey("respondent1PartyName")
                            && "Mr defendant".equals(params.getParams().get("respondent1PartyName"))
            )
        );
    }
}
