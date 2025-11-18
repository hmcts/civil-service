package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

public abstract class CaseProceedOfflineScenarioService {

    private static final List<CaseState> CASE_PROGRESSION_STATES = List.of(
        CaseState.CASE_PROGRESSION,
        CaseState.HEARING_READINESS,
        CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING,
        CaseState.DECISION_OUTCOME,
        CaseState.All_FINAL_ORDERS_ISSUED
    );

    protected final FeatureToggleService featureToggleService;

    protected CaseProceedOfflineScenarioService(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    protected boolean inCaseProgressionState(CaseData caseData) {
        return caseData.getPreviousCCDState() != null
            && CASE_PROGRESSION_STATES.contains(caseData.getPreviousCCDState());
    }
}
