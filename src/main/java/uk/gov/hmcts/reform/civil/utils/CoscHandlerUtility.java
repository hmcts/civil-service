package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;

@Slf4j
public class CoscHandlerUtility {

    private CoscHandlerUtility() {
        //NO-OP
    }

    public static Map<String, Boolean> addScenario(boolean isEnabled) {
        return Map.of(
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            isEnabled
        );
    }

    public static void addBeforeRecordScenario(CaseData caseData, DashboardNotificationService dashboardNotificationService) {
        if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
            caseData.getGeneralApplications().stream()
                .filter(application ->
                            application.getValue().getGeneralAppType().getTypes().contains(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID))
                .findFirst()
                .ifPresent(coscApplication -> {
                    CaseLink caseLink = coscApplication.getValue().getCaseLink();
                    if (caseLink == null || caseLink.getCaseReference() == null) {
                        log.warn(
                            "CoSC application on case {} has no caseLink; skipping notification cleanup",
                            caseData.getCcdCaseReference()
                        );
                        return;
                    }
                    dashboardNotificationService.deleteByReferenceAndCitizenRole(
                        caseLink.getCaseReference(),
                        "APPLICANT"
                    );
                });
        }
    }
}
