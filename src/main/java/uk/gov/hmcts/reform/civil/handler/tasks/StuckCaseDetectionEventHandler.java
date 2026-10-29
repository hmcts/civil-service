package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;
import uk.gov.hmcts.reform.civil.service.search.CasesStuckCheckSearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class StuckCaseDetectionEventHandler extends BaseExternalTaskHandler {

    private record StuckCaseSearchEnrichment(
        String processInstanceId,
        String incidentId,
        String incidentMessage,
        String stateId,
        String lastEventId,
        String failedActivityId,
        String retryStatus
    ) {
    }

    private final CasesStuckCheckSearchService casesStuckCheckSearchService;
    private final CamundaRuntimeApi camundaRuntimeApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseTaskTrackingService caseTaskTrackingService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public StuckCaseDetectionEventHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CasesStuckCheckSearchService casesStuckCheckSearchService,
        CamundaRuntimeApi camundaRuntimeApi,
        AuthTokenGenerator authTokenGenerator,
        CaseTaskTrackingService caseTaskTrackingService,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.casesStuckCheckSearchService = casesStuckCheckSearchService;
        this.camundaRuntimeApi = camundaRuntimeApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseTaskTrackingService = caseTaskTrackingService;
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    private static final String UNKNOWN = "UNKNOWN";
    private static final String HAS_PROCESS_INSTANCE = "has-process-instance";
    private static final String NO_PROCESS_INSTANCE = "no-process-instance";
    private static final String UNKNOWN_PROCESS_INSTANCE = "unknown";
    private static final String STUCK_CASE_ENRICHMENT_FAILED = "stuck_case_enrichment_failed";
    private static final String STUCK_CASES_FROM_PAST_DAYS = "stuckCasesFromPastDays";
    private static final String DEFAULT_LOOKBACK_DAYS = "7";
    private static final String STATE_ID_VARIABLE = "stateId";
    private static final String INCIDENT_TIMESTAMP = "incidentTimestamp";
    private static final String STUCK_CASE_EVENT_NAME = "StuckCaseDetected";
    private static final String SEARCH_RESULT_INCIDENT_MESSAGE = "Detected by CasesStuckCheckSearchService";

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        log.info("Call cases stuck check search service to log cases being stuck in app insights");
        String pastDays = externalTask.getVariable(STUCK_CASES_FROM_PAST_DAYS);
        final Set<CaseDetails> cases = casesStuckCheckSearchService.getCases(StringUtils.isBlank(pastDays) ? DEFAULT_LOOKBACK_DAYS : pastDays);
        final String serviceAuthorization = authTokenGenerator.generate();
        Map<String, List<String>> populations = new HashMap<>();
        populations.put(HAS_PROCESS_INSTANCE, new ArrayList<>());
        populations.put(NO_PROCESS_INSTANCE, new ArrayList<>());
        populations.put(UNKNOWN_PROCESS_INSTANCE, new ArrayList<>());
        cases.stream().map(CaseDetails::getId).filter(Objects::nonNull).map(String::valueOf).distinct().sorted()
            .forEach(caseId -> {
                StuckCaseSearchEnrichment enrichment = enrichStuckCaseFromSearch(caseId, serviceAuthorization);
                populations.get(processInstanceStatus(enrichment)).add(caseId);
                trackStuckCaseSearchResultEvent(caseId, enrichment);
            });
        Map<String, String> summary = new HashMap<>();
        populations.forEach((status, ids) -> {
            String prefix = switch (status) {
                case HAS_PROCESS_INSTANCE -> "withProcessInstance";
                case NO_PROCESS_INSTANCE -> "withoutProcessInstance";
                default -> "unknownProcessInstance";
            };
            summary.put(prefix + "Count", String.valueOf(ids.size()));
            summary.put(prefix + "CaseIds", String.join(",", ids));
        });
        summary.put("stuckCaseCount", String.valueOf(populations.values().stream().mapToInt(List::size).sum()));
        summary.put(STUCK_CASES_FROM_PAST_DAYS, StringUtils.isBlank(pastDays) ? DEFAULT_LOOKBACK_DAYS : pastDays);
        log.info("Stuck case detection summary: {}", summary);
        caseTaskTrackingService.trackCaseTask("MULTIPLE", "stuckCaseDetectionSummary", "StuckCasesDetectionSummary", summary);
        return new ExternalTaskData();
    }

    private String processInstanceStatus(StuckCaseSearchEnrichment enrichment) {
        if (STUCK_CASE_ENRICHMENT_FAILED.equals(enrichment.retryStatus())) {
            return UNKNOWN_PROCESS_INSTANCE;
        }
        return UNKNOWN.equals(enrichment.processInstanceId()) ? NO_PROCESS_INSTANCE : HAS_PROCESS_INSTANCE;
    }

    private void trackStuckCaseSearchResultEvent(String caseId, StuckCaseSearchEnrichment enrichment) {
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("processInstanceId", enrichment.processInstanceId());
        additionalProperties.put("processInstanceStatus", processInstanceStatus(enrichment));
        additionalProperties.put("incidentId", enrichment.incidentId());
        additionalProperties.put("incidentMessage", enrichment.incidentMessage());
        additionalProperties.put(STATE_ID_VARIABLE, enrichment.stateId());
        additionalProperties.put("lastEventId", enrichment.lastEventId());
        additionalProperties.put("failedActivityId", enrichment.failedActivityId());
        additionalProperties.put("retryStatus", enrichment.retryStatus());
        additionalProperties.put("retryExhausted", Boolean.FALSE.toString());
        additionalProperties.put("jobId", UNKNOWN);

        caseTaskTrackingService.trackCaseTask(
            caseId,
            "stuckCaseDetection",
            STUCK_CASE_EVENT_NAME,
            additionalProperties
        );
    }

    private StuckCaseSearchEnrichment enrichStuckCaseFromSearch(String caseId, String serviceAuthorization) {
        try {
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseId));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            BusinessProcess businessProcess = caseData.getBusinessProcess();

            String processInstanceId = businessProcess != null
                ? defaultIfBlank(businessProcess.getProcessInstanceId())
                : UNKNOWN;
            String failedActivityId = businessProcess != null
                ? defaultIfBlank(businessProcess.getActivityId())
                : UNKNOWN;
            String lastEventId = businessProcess != null
                ? defaultIfBlank(businessProcess.getCamundaEvent())
                : UNKNOWN;
            String stateId = caseData.getCcdState() != null ? caseData.getCcdState().name() : UNKNOWN;

            if (UNKNOWN.equals(processInstanceId)) {
                return new StuckCaseSearchEnrichment(
                    UNKNOWN,
                    UNKNOWN,
                    SEARCH_RESULT_INCIDENT_MESSAGE,
                    stateId,
                    lastEventId,
                    failedActivityId,
                    "stuck_case_no_process_instance"
                );
            }

            return enrichStuckCaseWithIncident(
                caseId, serviceAuthorization, processInstanceId, stateId, lastEventId, failedActivityId
            );
        } catch (Exception e) {
            log.warn("Could not enrich stuck case {} from search results", caseId, e);
            return new StuckCaseSearchEnrichment(
                UNKNOWN,
                UNKNOWN,
                SEARCH_RESULT_INCIDENT_MESSAGE,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                STUCK_CASE_ENRICHMENT_FAILED
            );
        }
    }

    private StuckCaseSearchEnrichment enrichStuckCaseWithIncident(
        String caseId,
        String serviceAuthorization,
        String processInstanceId,
        String stateId,
        String lastEventId,
        String failedActivityId
    ) {
        List<CamundaIncident> incidents;
        try {
            incidents = camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                serviceAuthorization, true, processInstanceId, INCIDENT_TIMESTAMP, "desc", 1
            );
        } catch (Exception e) {
            log.warn("Could not fetch incident for stuck case {}", caseId, e);
            return new StuckCaseSearchEnrichment(
                processInstanceId, UNKNOWN, SEARCH_RESULT_INCIDENT_MESSAGE,
                stateId, lastEventId, failedActivityId, "stuck_case_incident_lookup_failed"
            );
        }

        if (incidents.isEmpty()) {
            return new StuckCaseSearchEnrichment(
                processInstanceId,
                UNKNOWN,
                SEARCH_RESULT_INCIDENT_MESSAGE,
                stateId,
                lastEventId,
                failedActivityId,
                "stuck_case_no_open_incident"
            );
        }

        CamundaIncident incident = incidents.getFirst();
        String resolvedFailedActivityId = !UNKNOWN.equals(defaultIfBlank(incident.getActivityId()))
            ? defaultIfBlank(incident.getActivityId())
            : failedActivityId;
        return new StuckCaseSearchEnrichment(
            processInstanceId,
            defaultIfBlank(incident.getId()),
            defaultIfBlank(incident.getIncidentMessage()),
            stateId,
            lastEventId,
            resolvedFailedActivityId,
            "stuck_case_open_incident"
        );
    }

    private String defaultIfBlank(String value) {
        return StringUtils.isBlank(value) ? UNKNOWN : value;
    }
}
