package uk.gov.hmcts.reform.civil.service.dashboardnotifications.discontinueclaimclaimant;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DiscontinueClaimClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private DiscontinueClaimClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForLipDefendant() {
        CaseData caseData = new CaseDataBuilder()
            .caseReference(1234L)
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyDiscontinueClaimClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenDefendantRepresented() {
        CaseData caseData = new CaseDataBuilder()
            .caseReference(5678L)
            .respondent1Represented(YesOrNo.YES)
            .build();

        service.notifyDiscontinueClaimClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
