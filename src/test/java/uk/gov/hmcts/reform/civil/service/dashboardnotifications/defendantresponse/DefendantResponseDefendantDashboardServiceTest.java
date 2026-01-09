package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantResponseDefendantDashboardService service;

    private final HashMap<String, Object> params = new HashMap<>();

    @Test
    void shouldRecordScenarioWhenEligible() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(1234L);
        when(caseData.isRespondent1NotRepresented()).thenReturn(true);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM.getScenario()),
            eq("1234"),
            eq(ScenarioRequestParams.builder().params(params).build())
        );
    }

    @Test
    void shouldNotRecordScenarioWhenNotEligible() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
