package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class GaDashboardScenarioService {

    protected final DashboardApiClient dashboardApiClient;
    protected final GaDashboardNotificationsParamsMapper mapper;

    protected GaDashboardScenarioService(DashboardApiClient dashboardApiClient,
                                         GaDashboardNotificationsParamsMapper mapper) {
        this.dashboardApiClient = dashboardApiClient;
        this.mapper = mapper;
    }

    protected void recordScenario(GeneralApplicationCaseData caseData, String authToken) {
        if (caseData == null) {
            log.warn("Case data is null, skipping dashboard scenario recording");
            return;
        }

        String caseReference = resolveCaseReference(caseData);
        log.info("Evaluating dashboard scenarios for case {}", caseReference);

        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();

        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            log.info("Recording primary dashboard scenario {} for case {}", scenario, caseReference);
            beforeRecordScenario(caseData, authToken);
            dashboardApiClient.recordScenario(caseReference, scenario, authToken, scenarioParams);
        } else if (!Strings.isNullOrEmpty(scenario)) {
            log.debug("Primary scenario {} not recorded for case {} due to eligibility", scenario, caseReference);
        } else {
            log.debug("No primary dashboard scenario resolved for case {}", caseReference);
        }

        String extraScenario = getExtraScenario();
        if (!Strings.isNullOrEmpty(extraScenario) && shouldRecordExtraScenario(caseData)) {
            log.info("Recording extra dashboard scenario {} for case {}", extraScenario, caseReference);
            dashboardApiClient.recordScenario(caseReference, extraScenario, authToken, scenarioParams);
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
                    dashboardApiClient.recordScenario(caseReference, scenarioName, authToken, scenarioParams);
                } else {
                    log.debug("Additional scenario {} not recorded for case {} due to eligibility", scenarioName, caseReference);
                }
            });
    }

    protected String resolveCaseReference(GeneralApplicationCaseData caseData) {
        if (caseData != null && caseData.getCcdCaseReference() != null) {
            return caseData.getCcdCaseReference().toString();
        }
        return "unknown";
    }

    protected abstract String getScenario(GeneralApplicationCaseData caseData);

    protected String getExtraScenario() {
        return null;
    }

    protected Map<String, Boolean> getScenarios(GeneralApplicationCaseData caseData) {
        return new HashMap<>();
    }

    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        return true;
    }

    protected boolean shouldRecordExtraScenario(GeneralApplicationCaseData caseData) {
        return false;
    }

    protected void beforeRecordScenario(GeneralApplicationCaseData caseData, String authToken) {
        // hook for subclasses
    }
}
