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
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class HearingScheduledDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private HearingScheduledDefendantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private UserService userService;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    public static final String TASK_ID = "GenerateDashboardNotificationHearingScheduledDefendant";

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";

    HashMap<String, Object> params = new HashMap<>();

    @BeforeEach
    void init() {
        clearInvocations(userService);
    }

    @Nested
    class CaseProgression {

        private static final String USER_ID = "User1";

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT);
        }

        @Test
        void shouldNotCallRecordScenario_whenCaseProgressionIsDisabled() {
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                    .request(CallbackRequest.builder()
                                 .eventId(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT.name())
                                 .build())
                    .build()))
                .isEqualTo(TASK_ID);
        }

        @Test
        void createDashboardNotifications() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(
                USER_AUTH_TOKEN);
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);

            DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
            DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .ccdCaseReference(1234L)
                .respondent1Represented(YesOrNo.NO)
                .build().toBuilder().hearingLocation(list).build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void doNotCreateDashboardNotifications() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            when(userService.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
            when(userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(
                USER_AUTH_TOKEN);
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);

            DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
            DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
            CaseData caseData = CaseData.builder()
                .legacyCaseReference("reference")
                .ccdCaseReference(1234L)
                .respondent1Represented(YesOrNo.YES)
                .build().toBuilder().hearingLocation(list).build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verifyNoInteractions(dashboardApiClient);
        }
    }
}
