package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helpwithhearingfees;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT;

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
class HelpWithHearingFeesClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private HelpWithHearingFeesClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForHelpWithHearingFeesClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);

        service.notifyHelpWithHearingFees(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioForHelpWithHearingFeesClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);

        service.notifyHelpWithHearingFees(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
