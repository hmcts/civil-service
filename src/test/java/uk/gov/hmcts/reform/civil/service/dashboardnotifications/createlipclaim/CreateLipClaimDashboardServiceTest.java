package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createlipclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@ExtendWith(MockitoExtension.class)
class CreateLipClaimDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private CreateLipClaimDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordClaimFeeScenarioWhenHelpWithFeesNotRequested() {
        CaseData caseData = buildCaseData(1234L, YesOrNo.NO, YesOrNo.NO, BigDecimal.valueOf(5_000));

        service.notifyCreateLipClaim(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED.getScenario(),
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
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordHwfScenarioWhenHelpWithFeesRequested() {
        CaseData caseData = buildCaseData(2345L, YesOrNo.NO, YesOrNo.YES, BigDecimal.valueOf(5_000));

        service.notifyCreateLipClaim(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED.getScenario(),
            "2345",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "2345",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "2345",
            params
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordExtraScenarioForFastTrackClaims() {
        CaseData caseData = buildCaseData(3456L, YesOrNo.NO, YesOrNo.NO, BigDecimal.valueOf(15_000));

        service.notifyCreateLipClaim(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED.getScenario(),
            "3456",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_CLAIMANT.getScenario(),
            "3456",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "3456",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "3456",
            params
        );
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = buildCaseData(4567L, YesOrNo.YES, YesOrNo.NO, BigDecimal.valueOf(15_000));

        service.notifyCreateLipClaim(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    private CaseData buildCaseData(long reference, YesOrNo applicantRepresented, YesOrNo helpWithFees,
                                   BigDecimal totalClaimAmount) {
        return CaseDataBuilder.builder()
            .ccdCaseReference(reference)
            .applicant1Represented(applicantRepresented)
            .caseDataLip(CaseDataLiP.builder()
                .helpWithFees(HelpWithFees.builder().helpWithFee(helpWithFees).build())
                .build())
            .totalClaimAmount(totalClaimAmount)
            .build();
    }
}
