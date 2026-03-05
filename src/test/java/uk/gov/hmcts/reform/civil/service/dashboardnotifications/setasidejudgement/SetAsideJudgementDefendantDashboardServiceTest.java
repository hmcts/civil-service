package uk.gov.hmcts.reform.civil.service.dashboardnotifications.setasidejudgement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_SET_ASIDE_ERROR_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    private SetAsideJudgementDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        service = new SetAsideJudgementDefendantDashboardService(dashboardScenariosService, mapper);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenJudgmentErrorAndDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(9876L)
            .respondent1Represented(YesOrNo.NO)
            .build();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);

        service.notifySetAsideJudgement(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_JUDGEMENTS_ONLINE_SET_ASIDE_ERROR_DEFENDANT.getScenario()),
            eq("9876"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenDefendantRepresented() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(6789L)
            .respondent1Represented(YesOrNo.YES)
            .build();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);

        service.notifySetAsideJudgement(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenReasonIsNotJudgmentError() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(2468L)
            .respondent1Represented(YesOrNo.NO)
            .build();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);

        service.notifySetAsideJudgement(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
