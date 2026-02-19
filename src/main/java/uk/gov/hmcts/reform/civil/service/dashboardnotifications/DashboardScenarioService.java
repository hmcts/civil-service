package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class DashboardScenarioService {

    protected final DashboardScenariosService dashboardScenariosService;
    protected final DashboardNotificationsParamsMapper mapper;

    protected static final String DEFENDANT_ROLE = "DEFENDANT";
    protected static final String CLAIMANT_ROLE = "CLAIMANT";

    protected DashboardScenarioService(DashboardScenariosService dashboardScenariosService,
                                       DashboardNotificationsParamsMapper mapper) {
        this.dashboardScenariosService = dashboardScenariosService;
        this.mapper = mapper;
    }

    protected void recordScenario(CaseData caseData, String authToken) {
        String caseReference = resolveCaseReference(caseData);
        log.info("Evaluating dashboard scenarios for case {}", caseReference);

        ScenarioRequestParams scenarioParams = scenarioRequestParamsFrom(caseData);

        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            log.info("Recording primary dashboard scenario {} for case {}", scenario, caseReference);
            beforeRecordScenario(caseData, authToken);
            dashboardScenariosService.recordScenarios(
                authToken,
                scenario,
                caseReference,
                scenarioParams
            );
        } else if (!Strings.isNullOrEmpty(scenario)) {
            log.debug("Primary scenario {} not recorded for case {} due to eligibility", scenario, caseReference);
        } else {
            log.debug("No primary dashboard scenario resolved for case {}", caseReference);
        }

        String extraScenario = getExtraScenario();
        if (!Strings.isNullOrEmpty(extraScenario) && shouldRecordExtraScenario(caseData)) {
            log.info("Recording extra dashboard scenario {} for case {}", extraScenario, caseReference);
            dashboardScenariosService.recordScenarios(
                authToken,
                extraScenario,
                caseReference,
                scenarioParams
            );
        } else if (!Strings.isNullOrEmpty(extraScenario)) {
            log.debug("Extra scenario {} not recorded for case {} due to eligibility", extraScenario, caseReference);
        }

        Optional.ofNullable(getScenarios(caseData)).orElse(new HashMap<>())
            .forEach((scenarioName, shouldRecord) -> {
                if (Strings.isNullOrEmpty(scenarioName)) {
                    return;
                }
                if (Boolean.TRUE.equals(shouldRecord)) {
                    log.info("Recording additional dashboard scenario {} for case {}", scenarioName, caseReference);
                    dashboardScenariosService.recordScenarios(
                        authToken,
                        scenarioName,
                        caseReference,
                        scenarioParams
                    );
                } else {
                    log.debug("Additional scenario {} not recorded for case {} due to eligibility", scenarioName, caseReference);
                }
            });
    }

    private String resolveCaseReference(CaseData caseData) {
        if (caseData != null && caseData.getCcdCaseReference() != null) {
            return caseData.getCcdCaseReference().toString();
        }
        return "unknown";
    }

    protected abstract String getScenario(CaseData caseData);

    protected String getExtraScenario() {
        return null;
    }

    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        return new HashMap<>();
    }

    protected boolean shouldRecordScenario(CaseData caseData) {
        return true;
    }

    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        return false;
    }

    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        // hook for subclasses
    }

    protected ScenarioRequestParams scenarioRequestParamsFrom(CaseData caseData) {
        return ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
    }
}
