package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_RESPONDENT_TASK_LIST_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class TaskListForRespondentUpdateHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private GaDashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private TaskListForRespondentUpdateHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_RESPONDENT_TASK_LIST_GA);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(new CallbackParams())).isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordDefendantScenarioActionNeeded_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                  .value(GADetailsRespondentSol.builder()
                                                             .parentClaimantIsApplicant(YesOrNo.YES)
                                                             .caseState(AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioInProgress_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                       .value(GADetailsRespondentSol.builder()
                                                                  .parentClaimantIsApplicant(YesOrNo.YES)
                                                                  .caseState(HEARING_SCHEDULED.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioAvailable_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                       .value(GADetailsRespondentSol.builder()
                                                                  .parentClaimantIsApplicant(YesOrNo.YES)
                                                                  .caseState(APPLICATION_DISMISSED.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioActionNeeded_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.NO)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                       .value(GADetailsRespondentSol.builder()
                                                                  .parentClaimantIsApplicant(YesOrNo.NO)
                                                                  .caseState(AWAITING_APPLICATION_PAYMENT.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioInProgress_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.NO)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                       .value(GADetailsRespondentSol.builder()
                                                                  .parentClaimantIsApplicant(YesOrNo.NO)
                                                                  .caseState(HEARING_SCHEDULED.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioAvailable_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .parentClaimantIsApplicant(YesOrNo.NO)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                       .value(GADetailsRespondentSol.builder()
                                                                  .parentClaimantIsApplicant(YesOrNo.NO)
                                                                  .caseState(APPLICATION_DISMISSED.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordDefendantScenario_whenDefendantNotLiP() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .respondent1Represented(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                  .value(GADetailsRespondentSol.builder()
                                                             .parentClaimantIsApplicant(YesOrNo.YES)
                                                             .caseState(AWAITING_APPLICATION_PAYMENT.getDisplayedValue()).build()).build()))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardApiClient);
        }
    }
}
