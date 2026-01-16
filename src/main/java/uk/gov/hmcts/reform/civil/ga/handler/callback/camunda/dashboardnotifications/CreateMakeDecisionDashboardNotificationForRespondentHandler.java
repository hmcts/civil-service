package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.callback.GaDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;

@Service
public class CreateMakeDecisionDashboardNotificationForRespondentHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION);

    public CreateMakeDecisionDashboardNotificationForRespondentHandler(DashboardApiClient dashboardApiClient,
                                                                       GaDashboardNotificationsParamsMapper mapper,
                                                                       FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        if (isWithoutNotice(caseData)
            && caseData.getApplicationIsUncloakedOnce() != null
            && caseData.getApplicationIsUncloakedOnce().equals(YES)) {
            if (caseData.getMakeAppVisibleToRespondents() != null) {
                return SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT.getScenario();
            } else {
                return getScenarioBasedOnDecision(caseData);
            }
        }

        return getScenarioBasedOnDecision(caseData);
    }

    private String getScenarioBasedOnDecision(GeneralApplicationCaseData caseData) {
        if (caseData.getCcdState().equals(CaseState.LISTING_FOR_A_HEARING) && caseData
            .getJudicialDecision().getDecision().equals(
                GAJudgeDecisionOption.LIST_FOR_A_HEARING) && caseData.getGaHearingNoticeApplication() != null
            && caseData.getGaHearingNoticeDetail() != null) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT.getScenario();
        } else if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null
            && caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision() == GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario();
        } else if (caseData.judgeHasMadeAnOrder()
            && List.of(
                CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
                CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED
            )
            .contains(caseData.getCcdState())) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario();
        } else if (caseData.getJudicialDecisionRequestMoreInfo() != null
            && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY
            && caseData.getCcdState().equals(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario();
        }
        return "";
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private boolean isWithoutNotice(GeneralApplicationCaseData caseData) {
        return NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
    }

}
