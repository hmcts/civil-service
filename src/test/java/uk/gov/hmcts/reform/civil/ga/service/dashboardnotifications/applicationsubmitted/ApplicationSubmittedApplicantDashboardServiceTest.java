package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmittedApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private ApplicationSubmittedApplicantDashboardService service;

    @Test
    void shouldRecordSubmittedAndFeePaidScenariosWhenHwfNoRemission() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .gaHwfDetails(HelpWithFeesDetails.builder()
                              .hwfCaseEvent(CaseEvent.NO_REMISSION_HWF_GA)
                              .build())
            .build();

        assertScenarioRecorded(
            caseData,
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario()
        );
    }

    @Test
    void shouldRecordSubmittedAndFullRemissionScenariosWhenHwfFullRemission() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .gaHwfDetails(HelpWithFeesDetails.builder()
                              .hwfCaseEvent(CaseEvent.FULL_REMISSION_HWF_GA)
                              .build())
            .build();

        assertScenarioRecorded(
            caseData,
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT.getScenario()
        );
    }

    @Test
    void shouldRecordSubmittedAndFeePaidScenariosWhenFeePaymentOutcomeNoRemission() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForGa(YesOrNo.NO))
            .gaHwfDetails(HelpWithFeesDetails.builder()
                              .hwfCaseEvent(CaseEvent.FEE_PAYMENT_OUTCOME_GA)
                              .build())
            .build();

        assertScenarioRecorded(
            caseData,
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario()
        );
    }

    @Test
    void shouldRecordOnlySubmittedScenarioWhenNoHwfDetails() {
        GeneralApplicationCaseData caseData = baseCase();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationSubmitted(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
        verifyNoMoreInteractions(dashboardApiClient);
    }

    private GeneralApplicationCaseData baseCase() {
        return GeneralApplicationCaseData.builder()
            .ccdCaseReference(123456L)
            .build();
    }

    private void assertScenarioRecorded(GeneralApplicationCaseData caseData,
                                        String primaryScenario,
                                        String extraScenario) {
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationSubmitted(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            primaryScenario,
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            extraScenario,
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
        verifyNoMoreInteractions(dashboardApiClient);
    }
}
