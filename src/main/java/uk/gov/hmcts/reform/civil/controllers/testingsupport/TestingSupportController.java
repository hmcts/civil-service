package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeePaidEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeeUnpaidEventHandler;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.handler.event.BundleCreationTriggerEventHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

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
    private final StateFlowEngine stateFlowEngine;
    private final EventHistoryMapper eventHistoryMapper;
    private final RoboticsDataMapper roboticsDataMapper;

    private final ClaimDismissedHandler claimDismissedHandler;
    private final HearingFeePaidEventHandler hearingFeePaidHandler;
    private final HearingFeeUnpaidEventHandler hearingFeeUnpaidHandler;
    private final BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;

    private static final String BEARER_TOKEN = "Bearer Token";

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

    @GetMapping("/testing-support/feature-toggle/{toggle}")
    @Operation(summary = "Check if a feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkFeatureToggle(
        @PathVariable("toggle") String toggle) {
        boolean featureEnabled = featureToggleService.isFeatureEnabled(toggle);
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @Data
    private static class BusinessProcessInfo {
        private BusinessProcess businessProcess;
        private String incidentMessage;

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

    @RequestMapping(
        value = "/testing-support/flowstate",
        method = RequestMethod.POST,
        produces = "application/json")
    public StateFlow getFlowStateInformationForCaseData(
        @RequestBody CaseData caseData) {
        return stateFlowEngine.evaluate(caseData);
    }

    @RequestMapping(
        value = "/testing-support/eventHistory",
        method = RequestMethod.POST,
        produces = "application/json")
    public EventHistory getEventHistoryInformationForCaseData(
        @RequestBody CaseData caseData) {
        return eventHistoryMapper.buildEvents(caseData);
    }

    @RequestMapping(
        value = "/testing-support/rpaJson",
        method = RequestMethod.POST,
        produces = "application/json")
    public String getRPAJsonInformationForCaseData(
        @RequestBody CaseData caseData) throws JsonProcessingException {
        return roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();
    }

    @GetMapping("/testing-support/trigger-case-dismissal-scheduler")
    public ResponseEntity<String> getCaseDismissalScheduler() {

        String responseMsg = "success";
        ExternalTaskImpl externalTask = new ExternalTaskImpl();
        try {
            claimDismissedHandler.handleTask(externalTask);
        } catch (Exception e) {
            responseMsg = "failed";
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-trial-bundle")
    public ResponseEntity<String> getTrialBundleEvent(@PathVariable("caseId") Long caseId) {
        String responseMsg = "success";
        var event = new BundleCreationTriggerEvent(caseId);
        try {
            bundleCreationTriggerEventHandler.sendBundleCreationTrigger(event);
        } catch (Exception e) {
            responseMsg = "failed";
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/case/{caseId}")
    public ResponseEntity<CaseData> getCaseData(@PathVariable("caseId") Long caseId) {

        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-hearing-fee-paid")
    public ResponseEntity<String> getHearingFeePaidEvent(@PathVariable("caseId") Long caseId) {

        String responseMsg = "success";
        var event = new HearingFeePaidEvent(caseId);
        try {
            hearingFeePaidHandler.moveCaseToPrepareForHearing(event);
        } catch (Exception e) {
            responseMsg = "failed";
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }

    @GetMapping("/testing-support/{caseId}/trigger-hearing-fee-unpaid")
    public ResponseEntity<String> getHearingFeeUnpaidEvent(@PathVariable("caseId") Long caseId) {

        String responseMsg = "success";
        var event = new HearingFeeUnpaidEvent(caseId);
        try {
            hearingFeeUnpaidHandler.moveCaseToStruckOut(event);
        } catch (Exception e) {
            responseMsg = "failed";
        }
        return new ResponseEntity<>(responseMsg, HttpStatus.OK);
    }
}
