package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.recordjudgment;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.recordjudgment.RecordJudgmentDefendantDashboardService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class RecordJudgmentDefendantDashboardTaskTest {

    @Mock
    private DashboardScenariosService service;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Test
    void shouldRecordDeterminationIssuedForLipPayingInInstalments() {
        CaseData caseData = caseData(YesOrNo.NO, PaymentPlanSelection.PAY_IN_INSTALMENTS);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>(Map.of("name", "Defendant")));

        new RecordJudgmentDefendantDashboardTask(new RecordJudgmentDefendantDashboardService(service, mapper)).execute(DashboardTaskContext.civil(caseData, "token"));

        verify(service).recordScenarios(eq("token"),
            eq(SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT.getScenario()),
            eq("123"), refEq(new ScenarioRequestParams(new HashMap<>(Map.of("name", "Defendant")))));
    }

    @Test
    void shouldSkipRepresentedDefendant() {
        new RecordJudgmentDefendantDashboardTask(new RecordJudgmentDefendantDashboardService(service, mapper)).execute(
            DashboardTaskContext.civil(caseData(YesOrNo.YES, PaymentPlanSelection.PAY_IN_INSTALMENTS), "token"));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentPlanSelection.class, names = {"PAY_BY_DATE", "PAY_IMMEDIATELY"})
    void shouldSkipOtherPaymentPlans(PaymentPlanSelection paymentPlan) {
        new RecordJudgmentDefendantDashboardTask(new RecordJudgmentDefendantDashboardService(service, mapper)).execute(
            DashboardTaskContext.civil(caseData(YesOrNo.NO, paymentPlan), "token"));

        verifyNoInteractions(service);
    }

    private CaseData caseData(YesOrNo represented, PaymentPlanSelection type) {
        return CaseData.builder().ccdCaseReference(123L).respondent1Represented(represented)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(type)).build();
    }
}
