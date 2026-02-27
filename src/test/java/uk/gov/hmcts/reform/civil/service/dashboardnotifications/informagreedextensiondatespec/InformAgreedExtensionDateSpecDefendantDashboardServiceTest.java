package uk.gov.hmcts.reform.civil.service.dashboardnotifications.informagreedextensiondatespec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFRESPONSE_MORETIMEREQUESTED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    private InformAgreedExtensionDateSpecDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        service = new InformAgreedExtensionDateSpecDefendantDashboardService(dashboardScenariosService, mapper);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(9999L)
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFRESPONSE_MORETIMEREQUESTED_DEFENDANT.getScenario()),
            eq("9999"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenDefendantRepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1111L)
            .respondent1Represented(YesOrNo.YES)
            .build();

        service.notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
