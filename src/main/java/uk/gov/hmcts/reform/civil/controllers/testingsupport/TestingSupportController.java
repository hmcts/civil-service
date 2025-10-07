package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.community.rest.client.model.HistoricProcessInstanceDto;
import org.camunda.community.rest.client.model.ProcessInstanceWithVariablesDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.model.TestCamundaProcess;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.TrialReadyNotificationEvent;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeePaidEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeeUnpaidEventHandler;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.handler.event.BundleCreationTriggerEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.TrialReadyNotificationEventHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.CheckStayOrderDeadlineEndTaskHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.CheckUnlessOrderDeadlineEndTaskHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.GAJudgeRevisitTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.judgments.CjesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;

@Tag(name = "Testing Support Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${testing.support.enabled:false}")
public class TestingSupportController {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final CamundaRestEngineClient camundaRestEngineClient;
    private final FeatureToggleService featureToggleService;
    private final IStateFlowEngine stateFlowEngine;
    private final EventHistoryMapper eventHistoryMapper;
    private final RoboticsDataMapper roboticsDataMapper;
    private final RoboticsDataMapperForSpec roboticsSpecDataMapper;
    private final CjesMapper cjesMapper;

    private final ClaimDismissedHandler claimDismissedHandler;
    private final HearingFeePaidEventHandler hearingFeePaidHandler;
    private final HearingFeeUnpaidEventHandler hearingFeeUnpaidHandler;
    private final TrialReadyNotificationEventHandler trialReadyNotificationHandler;
    private final BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;

    private final CheckStayOrderDeadlineEndTaskHandler checkStayOrderDeadlineEndTaskHandler;
    private final CheckUnlessOrderDeadlineEndTaskHandler checkUnlessOrderDeadlineEndTaskHandler;
    private final UserService userService;
    private final OrganisationService organisationService;
    private final CoreCaseUserService coreCaseUserService;
    private final GAJudgeRevisitTaskHandler gaJudgeRevisitTaskHandler;

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";


    @GetMapping("/testing-support/case/{caseId}/business-process")
    public ResponseEntity<BusinessProcessInfo> getBusinessProcess(@PathVariable("caseId") Long caseId) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        var businessProcess = caseData.getBusinessProcess();
        var businessProcessInfo = new BusinessProcessInfo(businessProcess);

        if (businessProcess.getStatus() == STARTED) {
            try {
                camundaRestEngineClient.findIncidentByProcessInstanceId(businessProcess.getProcessInstanceId())
                    .map(camundaRestEngineClient::getIncidentMessage)
                    .ifPresent(businessProcessInfo::setIncidentMessage);
            } catch (FeignException e) {
                if (e.status() != 404) {
                    businessProcessInfo.setIncidentMessage(e.contentUTF8());
                }
            }
        }

        return new ResponseEntity<>(businessProcessInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/ga/case/{caseId}/business-process")
    public ResponseEntity<BusinessProcessInfo> getBusinessProcessGa(@PathVariable("caseId") Long caseId) {
        log.info("Get business process for caseId: {}", caseId);
        CaseData caseData = caseDetailsConverter.toCaseDataGA(coreCaseDataService.getCase(caseId));
        var businessProcess = caseData.getBusinessProcess();
        var caseState = caseData.getCcdState();
        var businessProcessInfo = new BusinessProcessInfo(businessProcess);

        if (businessProcess.getStatus() == STARTED) {
            try {
                camundaRestEngineClient.findIncidentByProcessInstanceId(businessProcess.getProcessInstanceId())
                        .map(camundaRestEngineClient::getIncidentMessage)
                        .ifPresent(businessProcessInfo::setIncidentMessage);
            } catch (FeignException e) {
                if (e.status() != 404) {
                    businessProcessInfo.setIncidentMessage(e.contentUTF8());
                }
            }
        }

        businessProcessInfo.setCcdState(caseState.toString());

        return new ResponseEntity<>(businessProcessInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/feature-toggle/{toggle}")
    @Operation(summary = "Check if a feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkFeatureToggle(
        @PathVariable("toggle") String toggle) {
        boolean featureEnabled = featureToggleService.isFeatureEnabled(toggle);
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @PostMapping(
        value = "/testing-support/is-dashboard-toggle-enabled",
        produces = "application/json")
    public ResponseEntity<FeatureToggleInfo> checkDashboardFeatureToggle(
        @RequestBody CaseData caseData) {
        boolean featureEnabled = featureToggleService.isDashboardEnabledForCase(caseData);
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @Data
    private static class BusinessProcessInfo {
        private BusinessProcess businessProcess;
        private String incidentMessage;
        private String ccdState;

        private BusinessProcessInfo(BusinessProcess businessProcess) {
            this.businessProcess = businessProcess;
        }
    }

    @Data
    private static class FeatureToggleInfo {
        private boolean isToggleEnabled;

        private FeatureToggleInfo(boolean isToggleEnabled) {
            this.isToggleEnabled = isToggleEnabled;
        }
    }

    @PostMapping(
        value = "/testing-support/flowstate",
        produces = "application/json")
    public StateFlow getFlowStateInformationForCaseData(
        @RequestBody CaseData caseData) {
        return stateFlowEngine.evaluate(caseData);
    }

    @PostMapping(
        value = "/testing-support/eventHistory",
        produces = "application/json")
    public EventHistory getEventHistoryInformationForCaseData(
        @RequestBody CaseData caseData) {
        return eventHistoryMapper.buildEvents(caseData);
    }

    @PostMapping(
        value = "/testing-support/rpaJson",
        produces = "application/json")
    public String getRPAJsonInformationForCaseData(
        @RequestBody CaseData caseData) throws JsonProcessingException {
        return roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();
    }

    @PostMapping(
        value = "/testing-support/rpaJsonSpec",
        produces = "application/json")
    public String getRPAJsonInformationForSpecCaseData(
        @RequestBody CaseData caseData) throws JsonProcessingException {
        return roboticsSpecDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();
    }

    @PostMapping(
        value = "/testing-support/rtlActiveJudgment",
        produces = "application/json")
    public String getRTLJudgment(
        @RequestBody CaseData caseData) {
        return cjesMapper.toJudgmentDetailsCJES(caseData, true).toString();
    }

    @GetMapping("/testing-support/trigger-case-dismissal-scheduler")
    public ResponseEntity<String> getCaseDismissalScheduler() {

        String responseMsg = SUCCESS;
        ExternalTaskImpl externalTask = new ExternalTaskImpl();
        try {
            claimDismissedHandler.handleTask(externalTask);
        } catch (Exception e) {
            responseMsg = FAILED;
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-trial-bundle")
    public ResponseEntity<String> getTrialBundleEvent(@PathVariable("caseId") Long caseId) {
        String responseMsg = SUCCESS;
        var event = new BundleCreationTriggerEvent(caseId);
        try {
            bundleCreationTriggerEventHandler.sendBundleCreationTrigger(event);
        } catch (Exception e) {
            responseMsg = FAILED;
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/case/{caseId}")
    public ResponseEntity<CaseData> getCaseData(@PathVariable("caseId") Long caseId) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }

    @GetMapping("/testing-support/ga/case/{caseId}")
    public ResponseEntity<CaseData> getCaseDataGa(@PathVariable("caseId") Long caseId) {
        CaseData caseData = caseDetailsConverter.toCaseDataGA(coreCaseDataService.getCase(caseId));
        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-hearing-fee-paid")
    public ResponseEntity<String> getHearingFeePaidEvent(@PathVariable("caseId") Long caseId) {

        String responseMsg = SUCCESS;
        var event = new HearingFeePaidEvent(caseId);
        try {
            hearingFeePaidHandler.moveCaseToPrepareForHearing(event);
        } catch (Exception e) {
            responseMsg = FAILED;
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-hearing-fee-unpaid")
    public ResponseEntity<String> getHearingFeeUnpaidEvent(@PathVariable("caseId") Long caseId) {

        String responseMsg = SUCCESS;
        var event = new HearingFeeUnpaidEvent(caseId);
        try {
            hearingFeeUnpaidHandler.moveCaseToStruckOut(event);
        } catch (Exception e) {
            responseMsg = FAILED;
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-trial-arrangements")
    public ResponseEntity<String> getTrialReadyNotificationsEvent(@PathVariable("caseId") Long caseId) {

        String responseMsg = SUCCESS;
        var event = new TrialReadyNotificationEvent(caseId);
        try {
            trialReadyNotificationHandler.sendTrialReadyNotification(event);
        } catch (Exception e) {
            log.error("Error triggering trial arrangement notification for case {}: {}", caseId, e.getMessage(), e);
            responseMsg = FAILED;
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @PostMapping(
        value = "/testing-support/trigger-camunda-process",
        produces = "application/json")
    public ResponseEntity<ProcessInstanceWithVariablesDto> triggerCamundaProcess(
        @RequestBody TestCamundaProcess camundaProcess) {
        var response = camundaRestEngineClient.startProcessByKey(camundaProcess.getName(), camundaProcess.getVariables());
        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/testing-support/camunda-processes",
            produces = "application/json")
    public ResponseEntity<List<HistoricProcessInstanceDto>> getCamundaProcesses(
            @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
            @RequestParam(value = "definitionKey", required = false) String definitionKey,
            @RequestParam(value = "variables", required = false) String variables
    ) {
        ResponseEntity<List<HistoricProcessInstanceDto>> response =
                camundaRestEngineClient.getProcessInstances(processInstanceId, definitionKey, variables);
        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
    }


    /*Check if Camunda Event CREATE_GENERAL_APPLICATION_CASE is Finished
    if so, generalApplicationsDetails object will be populated with GA case references*/
    @GetMapping("/testing-support/case/{caseId}/business-process/ga")
    public ResponseEntity<BusinessProcessInfo> getGACaseReference(@PathVariable("caseId") Long caseId) {
        log.info("Get GA case reference for caseId: {}", caseId);
        CaseData caseData = caseDetailsConverter.toCaseDataGA(coreCaseDataService.getCase(caseId));

        int size = caseData.getGeneralApplications().size();

        /**
         * Check the business process status of latest GA case
         * if caseData.getGeneralApplications() collection size is more than 1
         */

        var generalApplication = caseData.getGeneralApplications().get(size - 1);

        var businessProcess = Objects.requireNonNull(generalApplication).getValue().getBusinessProcess();
        var businessProcessInfo = new BusinessProcessInfo(businessProcess);

        log.info("GA Business process status: " + businessProcess.getStatus() + " Camunda Event: " + businessProcess
                .getCamundaEvent());

        if (businessProcess.getStatus() == STARTED) {
            try {
                camundaRestEngineClient.findIncidentByProcessInstanceId(businessProcess.getProcessInstanceId())
                        .map(camundaRestEngineClient::getIncidentMessage)
                        .ifPresent(businessProcessInfo::setIncidentMessage);
            } catch (FeignException e) {
                if (e.status() != 404) {
                    businessProcessInfo.setIncidentMessage(e.contentUTF8());
                }
            }
        }

        return new ResponseEntity<>(businessProcessInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/trigger-judge-revisit-process-event/{state}/{genAppType}")
    public ResponseEntity<String> getJudgeRevisitProcessEvent(@PathVariable("state") String ccdState,
                                                              @PathVariable("genAppType") String genAppType) {

        String responseMsg = "success";
        ExternalTaskImpl externalTask = new ExternalTaskImpl();
        try {
            if (ccdState.equals("ORDER_MADE")) {
                if (genAppType.equals("STAY_THE_CLAIM")) {
                    checkStayOrderDeadlineEndTaskHandler.handleTask(externalTask);
                } else {
                    checkUnlessOrderDeadlineEndTaskHandler.handleTask(externalTask);
                }
            } else {
                gaJudgeRevisitTaskHandler.handleTask(externalTask);
            }
        } catch (Exception e) {
            responseMsg = "failed";
        }

        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @PostMapping(value = {"/user-roles/{caseId}", "/user-roles/{caseId}"})
    @Operation(summary = "user roles for the cases")
    public CaseAssignmentUserRolesResource getUserRoles(
            @PathVariable("caseId") String caseId) {
        return coreCaseUserService.getUserRoles(caseId);
    }

    @PostMapping(value = {"/assignCase/{caseId}", "/assignCase/{caseId}/{caseRole}"})
    @Operation(summary = "Assign case to user")
    public void assignCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                           @PathVariable("caseId") String caseId,
                           @PathVariable("caseRole") CaseRole caseRole) {
        String userId = userService.getUserInfo(authorisation).getUid();
        String organisationId = organisationService.findOrganisation(authorisation)
                .map(Organisation::getOrganisationIdentifier).orElse(null);
        coreCaseUserService.assignCase(caseId, userId, organisationId, caseRole);
        log.info("Assign caseId: {}", caseId);
    }

    @GetMapping(value = {"/getOrgDetails"})
    @Operation(summary = "Assign case to user")
    public String getOrgDetailsByUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        String userId = userService.getUserInfo(authorisation).getUid();
        return organisationService.findOrganisationByUserId(userId)
                .map(Organisation::getOrganisationIdentifier).orElse(null);

    }
}
