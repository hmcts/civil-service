package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT;

@Service
public class TaskListForRespondentUpdateHandler extends TaskListUpdateHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.UPDATE_RESPONDENT_TASK_LIST_GA);

    public TaskListForRespondentUpdateHandler(DashboardApiClient dashboardApiClient,
                                              GaDashboardNotificationsParamsMapper mapper,
                                              GaCoreCaseDataService coreCaseDataService,
                                              CaseDetailsConverter caseDetailsConverter,
                                              FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, coreCaseDataService, caseDetailsConverter, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData parentCaseData = getParentCaseData(caseData.getParentCaseReference());
        boolean someGaActionNeeded = Optional.ofNullable(parentCaseData.getRespondentSolGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? RESPONDENT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState())
                : APPLICANT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState()));
        if (someGaActionNeeded) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario();
        }
        boolean someGaInProgress = Optional.ofNullable(parentCaseData.getRespondentSolGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? RESPONDENT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState())
                : APPLICANT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState()));
        if (someGaInProgress) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario();
        }
        if (parentCaseData.getRespondentSolGaAppDetails() != null && parentCaseData.getRespondentSolGaAppDetails().size() > 0) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario();
        } else {
            return "";
        }
    }

    protected boolean shouldRecordScenario(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
        GeneralApplicationCaseData parentCaseData = getParentCaseData(caseData.getParentCaseReference());
        return parentCaseData.isRespondent1NotRepresented();
    }

}
