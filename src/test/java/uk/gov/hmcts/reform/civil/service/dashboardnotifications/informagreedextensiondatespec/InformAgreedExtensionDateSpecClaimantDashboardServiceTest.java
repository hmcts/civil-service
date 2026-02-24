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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_MORE_TIME_REQUESTED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    private InformAgreedExtensionDateSpecClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        service = new InformAgreedExtensionDateSpecClaimantDashboardService(dashboardScenariosService, mapper);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenApplicantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .build();

        service.notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_MORE_TIME_REQUESTED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(5678L)
            .applicant1Represented(YesOrNo.YES)
            .build();

        service.notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
