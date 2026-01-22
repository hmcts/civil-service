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
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;

@Service
public class CreateMakeDecisionDashboardNotificationForApplicantHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION);
    private final JudicialDecisionHelper judicialDecisionHelper;

    public CreateMakeDecisionDashboardNotificationForApplicantHandler(DashboardApiClient dashboardApiClient,
                                                                      GaDashboardNotificationsParamsMapper mapper,
                                                                      FeatureToggleService featureToggleService,
                                                                      JudicialDecisionHelper judicialDecisionHelper) {
        super(dashboardApiClient, mapper, featureToggleService);
        this.judicialDecisionHelper = judicialDecisionHelper;
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        if ((caseData.getJudicialDecisionRequestMoreInfo() != null
            && (GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION == caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
            || List.of(
                CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
                CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED
            )
            .contains(caseData.getCcdState())))
            && (Objects.isNull(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations())
            && !caseData.judgeHasMadeAnOrder())) {
            if (GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY == caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
                && judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)) {
                return SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT.getScenario();
            }
            return SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT.getScenario();
        } else if (caseData.getCcdState().equals(CaseState.LISTING_FOR_A_HEARING)
            && caseData.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.LIST_FOR_A_HEARING)
            && caseData.getGaHearingNoticeApplication() != null
            && caseData.getGaHearingNoticeDetail() != null) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT.getScenario();
        } else if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null
            && caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision()
            == GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario();
        } else if (caseData.judgeHasMadeAnOrder()
            && List.of(
                CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION,
                CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED
            )
            .contains(caseData.getCcdState())) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario();
        }
        return "";
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
