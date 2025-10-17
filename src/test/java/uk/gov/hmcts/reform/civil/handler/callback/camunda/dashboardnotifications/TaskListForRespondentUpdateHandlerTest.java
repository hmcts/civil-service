package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

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
public class TaskListForRespondentUpdateHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
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
        assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordDefendantScenarioActionNeeded_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.YES)
                                    .caseState(AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioInProgress_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.YES)
                                    .caseState(HEARING_SCHEDULED.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioAvailable_whenInvoked_claimantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.YES)
                                    .caseState(APPLICATION_DISMISSED.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioActionNeeded_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.NO)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.NO)
                                    .caseState(AWAITING_APPLICATION_PAYMENT.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioInProgress_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.NO)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.NO)
                                    .caseState(HEARING_SCHEDULED.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantScenarioAvailable_whenInvoked_defendantIsApplicant() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.NO)
                    .parentClaimantIsApplicant(YesOrNo.NO)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.NO)
                                    .caseState(APPLICATION_DISMISSED.getDisplayedValue()).build()).build()))
                    .build();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseDataGA(caseDetails)).thenReturn(caseData);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordDefendantScenario_whenDefendantNotLiP() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                    .parentCaseReference(caseData.getCcdCaseReference().toString())
                    .isGaApplicantLip(YesOrNo.YES)
                    .respondent1Represented(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                            .value(GADetailsRespondentSol.builder()
                                    .parentClaimantIsApplicant(YesOrNo.YES)
                                    .caseState(AWAITING_APPLICATION_PAYMENT.getDisplayedValue()).build()).build()))
                    .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(UPDATE_RESPONDENT_TASK_LIST_GA.name())
                            .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardScenariosService);
        }
    }
}
