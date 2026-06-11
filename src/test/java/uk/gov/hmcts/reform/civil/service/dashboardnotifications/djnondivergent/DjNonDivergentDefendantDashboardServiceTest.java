package uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGMENT_ONLINE_DEFAULT_JUDGMENT_ISSUED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private DjNonDivergentDefendantDashboardService service;

    @Test
    void shouldRecordScenarioWhenDefendantIsLip() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        CaseData caseData = new CaseDataBuilder()
            .atStateClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyDjNonDivergent(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_JUDGMENT_ONLINE_DEFAULT_JUDGMENT_ISSUED_DEFENDANT.getScenario(),
            "1594901956117591",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldNotRecordScenarioWhenBufferEnabledAndCaseInJudgmentRequestedState() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.JUDGMENT_REQUESTED);

        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenarioWhenBufferDisabledAndCaseInJudgmentRequestedState() {
        when(toggleService.isJudgmentBufferEnabled()).thenReturn(false);

        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.JUDGMENT_REQUESTED);

        assertTrue(service.shouldRecordScenario(caseData));
    }
}
