package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimSettledDefendantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private ClaimSettledDefendantDashboardNotificationHandler handler;

    public static final String TASK_ID = "CreateClaimSettledDashboardNotificationsForDefendant1";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .caseDataLip(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.YES)
                                 .applicant1ClaimSettledDate(
                                     LocalDate.now()).build()).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("applicant1ClaimSettledDateEn", caseData.getApplicant1ClaimSettleDate());
            scenarioParams.put("applicant1ClaimSettledDateCy", caseData.getApplicant1ClaimSettleDate());

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
                caseData.getCcdCaseReference().toString(),
                "DEFENDANT");
            verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                "DEFENDANT",
                null);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
