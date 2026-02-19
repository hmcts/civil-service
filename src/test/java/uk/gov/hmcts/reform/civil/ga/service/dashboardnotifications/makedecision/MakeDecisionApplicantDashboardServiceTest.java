package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;

@ExtendWith(MockitoExtension.class)
class MakeDecisionApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @Mock
    private JudicialDecisionHelper judicialDecisionHelper;

    @InjectMocks
    private MakeDecisionApplicantDashboardService service;

    @Test
    void shouldRecordRequestMoreInfoScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
                                                 .build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordAdditionalPaymentScenarioWhenUncloakedWithAdditionalFee() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
                                                 .build())
            .build();
        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)).thenReturn(true);

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordRequestMoreInfoScenarioWhenNoAdditionalFee() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
                                                 .build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordHearingScheduledScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.LISTING_FOR_A_HEARING)
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
            .gaHearingNoticeApplication(GAHearingNoticeApplication.builder().build())
            .gaHearingNoticeDetail(GAHearingNoticeDetail.builder().build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordWrittenRepresentationsScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations.builder().build())
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
                                  .build())
            .build();

        assertScenarioRecorded(
            caseData,
            SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario()
        );
    }

    @Test
    void shouldPreferWrittenRepresentationsOverRequestMoreInfoScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
                                                 .build())
            .judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations.builder().build())
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
                                  .build())
            .build();

        assertScenarioRecorded(
            caseData,
            SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario()
        );
    }

    @Test
    void shouldRecordOrderMadeScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario());
    }

    @Test
    void shouldNotRecordScenarioWhenNoConditionsMatched() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
            .build();

        service.notifyMakeDecision(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }

    private GeneralApplicationCaseData baseCase() {
        return GeneralApplicationCaseData.builder()
            .ccdCaseReference(123L)
            .isGaApplicantLip(YesOrNo.YES)
            .build();
    }

    private void assertScenarioRecorded(GeneralApplicationCaseData caseData, String scenario) {
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyMakeDecision(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            scenario,
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordScenario_true_whenApplicantIsLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenApplicantIsLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenApplicantIsLipNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }
}
