package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

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
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT;

@ExtendWith(MockitoExtension.class)
public class DeleteWrittenRepresentationNotificationDefendantHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private DeleteWrittenRepresentationNotificationDefendantHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build()))
                .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordDeleteWrittenRepsRequiredApplicantScenarioWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.NO)
                    .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDeleteWrittenRepsRequiredRespondentScenarioWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations.builder()
                            .writtenOption(CONCURRENT_REPRESENTATIONS).build())
                    .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordSwitchWrittenRepsRequiredApplicantRespondentScenarioWhenInvoked() {
            LocalDate claimantDeadline = LocalDateTime.now().isAfter(LocalDate.now().atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY))
                    ? LocalDate.now()
                    : LocalDate.now().plusDays(-1);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations.builder()
                            .writtenOption(SEQUENTIAL_REPRESENTATIONS)
                            .sequentialApplicantMustRespondWithin(LocalDate.now().plusDays(1))
                            .writtenSequentailRepresentationsBy(claimantDeadline).build())
                    .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordDeleteWrittenRepsRequiredScenarioWhenGaFlagIsDisabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION.name())
                            .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardScenariosService);
        }

    }
}
