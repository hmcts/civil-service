package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT;

@Service
public class DeleteWrittenRepresentationNotificationDefendantHandler extends DeleteWrittenRepresentationNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);

    public DeleteWrittenRepresentationNotificationDefendantHandler(DashboardApiClient dashboardApiClient,
                                                                   GaDashboardNotificationsParamsMapper mapper,
                                                                   FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        if (caseData.getParentClaimantIsApplicant() == YES) {
            if (shouldTriggerApplicantNotification(caseData)) {
                return SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT.getScenario();
            } else {
                return SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario();
            }
        } else {
            return SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected boolean shouldRecordScenario(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
        if (caseData.getParentClaimantIsApplicant() == YES) {
            return caseData.getIsGaRespondentOneLip() == YES;
        } else {
            return caseData.getIsGaApplicantLip() == YES;
        }
    }
}
