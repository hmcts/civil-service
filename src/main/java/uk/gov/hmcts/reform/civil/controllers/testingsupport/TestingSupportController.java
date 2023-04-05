package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;

@Api
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
    @ApiOperation("Check if a feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkFeatureToggle(
        @PathVariable("toggle") String toggle) {
        boolean featureEnabled = featureToggleService.isFeatureEnabled(toggle);
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/feature-toggle/noc")
    @ApiOperation("Check if noc feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkNoCToggleEnabled() {
        boolean featureEnabled = featureToggleService.isNoticeOfChangeEnabled();
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/feature-toggle/court-locations")
    @ApiOperation("Check if court location dynamic list feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkCourtLocationsDynamicListEnabled() {
        boolean featureEnabled = featureToggleService.isCourtLocationDynamicListEnabled();
        FeatureToggleInfo featureToggleInfo = new FeatureToggleInfo(featureEnabled);
        return new ResponseEntity<>(featureToggleInfo, HttpStatus.OK);
    }

    @GetMapping("/testing-support/feature-toggle/access-profiles")
    @ApiOperation("Check if access profiles feature toggle is enabled")
    public ResponseEntity<FeatureToggleInfo> checkAccessProfilesEnabled() {
        boolean featureEnabled = featureToggleService.isAccessProfilesEnabled();
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
        return roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();
    }
}
