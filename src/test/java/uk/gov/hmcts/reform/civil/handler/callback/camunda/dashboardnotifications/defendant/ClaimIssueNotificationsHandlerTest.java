package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;

@ExtendWith(MockitoExtension.class)
public class ClaimIssueNotificationsHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimIssueNotificationsHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;
    @Mock
    private FeatureToggleService toggleService;

    public static final String TASK_ID = "CreateIssueClaimDashboardNotificationsForDefendant1";

    Map<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    public void createDashboardNotifications() {

        params.put("ccdCaseReference", "123");
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1ResponseDeadline", "11 March 2024");

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(toggleService.isDashboardServiceEnabled()).thenReturn(true);

        LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1ResponseDeadline(dateTime)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
