package uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_CLAIMANT;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecisionOnRequestForReconsiderationClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private DecisionOnRequestForReconsiderationClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForDecisionReconsiderationClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);

        service.notifyDecisionReconsideration(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_CLAIMANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioForDecisionReconsiderationClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);

        service.notifyDecisionReconsideration(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
