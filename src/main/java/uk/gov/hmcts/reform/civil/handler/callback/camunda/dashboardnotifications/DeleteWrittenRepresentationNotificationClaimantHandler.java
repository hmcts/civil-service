package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT;

@Service
public class DeleteWrittenRepresentationNotificationClaimantHandler extends DeleteWrittenRepresentationNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);

    public DeleteWrittenRepresentationNotificationClaimantHandler(DashboardScenariosService dashboardScenariosService,
                                                                  DashboardNotificationsParamsMapper mapper,
                                                                  FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (caseData.getParentClaimantIsApplicant() == YES) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT.getScenario();
        } else {
            if (shouldTriggerApplicantNotification(caseData)) {
                return SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT.getScenario();
            } else {
                return SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario();
            }
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        if (caseData.getParentClaimantIsApplicant() == YES) {
            return caseData.getIsGaApplicantLip() == YES;
        } else {
            return caseData.getIsGaRespondentOneLip() == YES
                || (shouldTriggerApplicantNotification(caseData) && caseData.getIsGaApplicantLip() == YES);
        }
    }
}
