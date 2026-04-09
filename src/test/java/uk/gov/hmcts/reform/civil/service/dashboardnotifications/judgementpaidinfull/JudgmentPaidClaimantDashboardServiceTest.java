package uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MARK_PAID_IN_FULL_CLAIMANT;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JudgmentPaidClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private JudgmentPaidClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForJudgmentPaidClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);

        service.notifyJudgmentPaidInFull(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_CLAIMANT.getScenario(),
            "1234",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldRecordScenario_whenInvoked_whenClaimantRepresented_And_MarkedPaid_And_Def_Mark_Paid_In_Full() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);

        CaseData.CaseDataBuilder<?, ?> caseData2 = caseData.toBuilder();
        caseData2.activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()));
        caseData2.certOfSC(new CertOfSC().setDefendantFinalPaymentDate(LocalDate.now()));

        service.notifyJudgmentPaidInFull(caseData2.build(), AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MARK_PAID_IN_FULL_CLAIMANT.getScenario(),
            "1234",
            new ScenarioRequestParams(new HashMap<>())
        );
    }

    @Test
    void shouldNotRecordScenarioForJudgmentPaidClaimant() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);

        service.notifyJudgmentPaidInFull(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
