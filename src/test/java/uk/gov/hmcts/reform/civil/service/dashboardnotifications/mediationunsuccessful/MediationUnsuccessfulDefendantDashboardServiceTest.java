package uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_WHEN_CLAIMANT_NOT_CONTACTABLE;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediationUnsuccessfulDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private uk.gov.hmcts.reform.civil.service.FeatureToggleService featureToggleService;

    @InjectMocks
    private MediationUnsuccessfulDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordIntentScenarioWhenCarmDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        service.notifyMediationUnsuccessful(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordNonAttendanceScenarioWhenCarmEnabledAndClaimantNotContactable() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
        caseData.setMediation(mediation);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyMediationUnsuccessful(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordDefendantNotContactableScenarioWhenCarmEnabledAndDefendantNotContactable() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
        caseData.setMediation(mediation);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyMediationUnsuccessful(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_MEDIATION_WHEN_CLAIMANT_NOT_CONTACTABLE.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordGenericScenarioWhenCarmEnabledAndOtherReason() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(APPOINTMENT_NO_AGREEMENT));
        caseData.setMediation(mediation);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyMediationUnsuccessful(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(APPOINTMENT_NO_AGREEMENT));
        caseData.setMediation(mediation);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyMediationUnsuccessful(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
