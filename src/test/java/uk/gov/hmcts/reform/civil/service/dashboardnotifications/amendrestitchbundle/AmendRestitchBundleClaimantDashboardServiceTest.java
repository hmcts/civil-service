package uk.gov.hmcts.reform.civil.service.dashboardnotifications.amendrestitchbundle;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_BUNDLE_UPDATED_TRIAL_READY_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class AmendRestitchBundleClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private AmendRestitchBundleClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForLipClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .build();

        service.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_BUNDLE_UPDATED_TRIAL_READY_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldSkipScenarioForRepresentedClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .ccdCaseReference(1234L)
            .build();

        service.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
