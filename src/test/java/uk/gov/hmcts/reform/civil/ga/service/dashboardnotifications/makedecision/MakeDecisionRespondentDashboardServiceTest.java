package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class MakeDecisionRespondentDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private MakeDecisionRespondentDashboardService service;

    @Test
    void shouldRecordJudgeUncloakScenarioWhenWithoutNoticeAndUncloaked() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .makeAppVisibleToRespondents(GAMakeApplicationAvailableCheck.builder()
                                              .makeAppAvailableCheck(List.of(MakeAppAvailableCheckGAspec.CONSENT_AGREEMENT_CHECKBOX))
                                              .build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT.getScenario());
    }

    @Test
    void shouldFallBackToDecisionScenarioWhenWithoutNoticeAndUncloakedButNotVisibleToRespondent() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
                                                 .build())
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario());
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

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT.getScenario());
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
            SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario()
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
            SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario()
        );
    }

    @Test
    void shouldRecordOrderMadeScenario() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario());
    }

    @Test
    void shouldRecordRequestMoreInfoScenarioWhenOptionIsNotSendToOtherParty() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)
                                                 .build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario());
    }

    @Test
    void shouldNotRecordScenarioWhenOptionIsSendToOtherParty() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)
                                                 .build())
            .build();

        service.notifyMakeDecision(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }

    private GeneralApplicationCaseData baseCase() {
        return GeneralApplicationCaseData.builder()
            .ccdCaseReference(456L)
            .isGaRespondentOneLip(YesOrNo.YES)
            .isMultiParty(YesOrNo.NO)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
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
    void shouldRecordScenario_true_whenRespondentOneLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenSinglePartyAndRespondentOneLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenSinglePartyAndRespondentOneLipNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_true_whenMultiPartyAndRespondentTwoLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .isGaRespondentTwoLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenMultiPartyAndRespondentTwoLipNoAndRespondentOneLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .isGaRespondentTwoLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_true_whenMultiPartyRespondentOneLipYesEvenIfRespondentTwoLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .isGaRespondentTwoLip(YesOrNo.NO)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }
}
