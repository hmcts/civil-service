package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateClaimAfterPaymentDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordResponseRequiredAndFastTrackWhenUnrepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(15000));

        service.notifyDefendant(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_DEFENDANT.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "1234",
            params
        );
    }

    @Test
    void shouldNotRecordAnythingWhenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenDashboardDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenNotLipCase() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenRespondentRepresentedEvenIfLipCase() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.YES);

        service.notifyDefendant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
