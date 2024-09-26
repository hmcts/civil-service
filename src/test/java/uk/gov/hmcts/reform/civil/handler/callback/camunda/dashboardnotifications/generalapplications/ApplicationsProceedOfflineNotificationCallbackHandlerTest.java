package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicationsProceedOfflineNotificationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ApplicationsProceedOfflineNotificationCallbackHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    private static final String EVENT_ID_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT";
    private static final String EVENT_ID_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT";
    private static final String TASK_ID_CLAIMANT = "claimantLipApplicationOfflineDashboardNotification";
    private static final String TASK_ID_DEFENDANT = "defendantLipApplicationOfflineDashboardNotification";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            when(toggleService.isGeneralApplicationsEnabled()).thenReturn(true);
        }

        @Test
        void shouldNotCallRecordScenario_whenLipVLipIsDisabled() {
            when(toggleService.isLipVLipEnabled()).thenReturn(false);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldNotCallRecordScenario_whenGeneralApplicationIsDisabled() {
            when(toggleService.isGeneralApplicationsEnabled()).thenReturn(false);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldReturnCorrectActivityId_whenClaimant() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder().build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            assertThat(handler.camundaActivityId(callbackParams)).isEqualTo(TASK_ID_CLAIMANT);
        }

        @Test
        void shouldReturnCorrectActivityId_whenDefendant() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder().build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_DEFENDANT).build())
                .build();
            // THEN
            assertThat(handler.camundaActivityId(callbackParams)).isEqualTo(TASK_ID_DEFENDANT);
        }

        @Test
        void shouldEmptyResponse_whenMainCaseIsNotOffline() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(CASE_ISSUED)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsIsNull() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(null)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsIsEmpty() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(new ArrayList<>())
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenEventIsLrClaimant() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .applicant1Represented(YesOrNo.YES)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenEventIsLrDefendant() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .respondent1Represented(YesOrNo.YES)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_DEFENDANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsForClaimantNull() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .applicant1Represented(YesOrNo.NO)
                .claimantGaAppDetails(null)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsForClaimantEmpty() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .applicant1Represented(YesOrNo.NO)
                .claimantGaAppDetails(new ArrayList<>())
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldReturnResponse_whenGeneralApplicationsForClaimantExist() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());

            List<Element<GeneralApplicationsDetails>> gaApplicationsClaimant = wrapElements(
                GeneralApplicationsDetails.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .caseState("Awaiting Respondent Response")
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .applicant1Represented(YesOrNo.NO)
                .claimantGaAppDetails(gaApplicationsClaimant)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(new HashMap<>()).build()
            );
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsForDefendantNull() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .respondent1Represented(YesOrNo.NO)
                .respondentSolGaAppDetails(null)
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_DEFENDANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldEmptyResponse_whenGeneralApplicationsForDefendantEmpty() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder()
                    .caseLink(CaseLink.builder().caseReference("12345678").build())
                    .build());
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .ccdState(PROCEEDS_IN_HERITAGE_SYSTEM)
                .generalApplications(gaApplications)
                .respondent1Represented(YesOrNo.NO)
                .respondentSolGaAppDetails(new ArrayList<>())
                .build();
            // WHEN
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_DEFENDANT).build())
                .build();
            // THEN
            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }
    }
}
