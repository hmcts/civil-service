package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantsignsettlementagreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_ACCEPTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DefendantSignSettlementAgreementDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantSignSettlementAgreementDefendantDashboardService service;

    private Map<String, Object> params;

    @BeforeEach
    void setUp() {
        params = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn((HashMap<String, Object>) params);
    }

    @Test
    void shouldRecordAcceptedScenarioForDefendant() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = baseCaseData(YesOrNo.YES, YesOrNo.NO);

        service.notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_ACCEPTED_DEFENDANT.getScenario(),
            "4567",
            ScenarioRequestParams.builder().params((HashMap<String, Object>) params).build()
        );
    }

    @Test
    void shouldRecordRejectedScenarioForDefendant() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = baseCaseData(YesOrNo.NO, YesOrNo.NO);

        service.notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_SETTLEMENT_AGREEMENT_DEFENDANT_REJECTED_DEFENDANT.getScenario(),
            "4567",
            ScenarioRequestParams.builder().params((HashMap<String, Object>) params).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenDefendantRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = baseCaseData(YesOrNo.YES, YesOrNo.YES);

        service.notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldNotRecordScenarioWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        CaseData caseData = baseCaseData(YesOrNo.YES, YesOrNo.NO);

        service.notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    private CaseData baseCaseData(YesOrNo settlementDecision, YesOrNo respondentRepresented) {
        CaseDataLiP caseDataLip = new CaseDataLiP();
        caseDataLip.setRespondentSignSettlementAgreement(settlementDecision);
        return CaseDataBuilder.builder()
            .caseDataLip(caseDataLip)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(respondentRepresented)
            .ccdCaseReference(4567L)
            .build();
    }
}
