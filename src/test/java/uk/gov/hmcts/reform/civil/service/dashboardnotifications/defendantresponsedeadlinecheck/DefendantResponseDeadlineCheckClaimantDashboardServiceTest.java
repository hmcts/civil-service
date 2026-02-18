package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponsedeadlinecheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_DEADLINE_PASSED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineCheckClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private DefendantResponseDeadlineCheckClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordClaimantScenarioWhenNotRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(2345L);

        service.notifyDefendantResponseDeadlineCheck(caseData, AUTH_TOKEN);

        verify(mapper).mapCaseDataToParams(caseData);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_RESPONSE_DEADLINE_PASSED_CLAIMANT.getScenario(),
            "2345",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenClaimantRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);

        service.notifyDefendantResponseDeadlineCheck(caseData, AUTH_TOKEN);

        verify(mapper).mapCaseDataToParams(caseData);
        verifyNoInteractions(dashboardScenariosService);
    }
}
