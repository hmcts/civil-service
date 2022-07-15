package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import javax.validation.constraints.NotNull;

import static java.lang.String.format;
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
    private final RoboticsDataMapper roboticsDataMapper;
    private final StateFlowEngine stateFlowEngine;
    private final JsonSchemaValidationService jsonSchemaValidationService;

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

    //Option 1 (Current): Pass caseId and grab case data from ccd and generate robotics data.
    //Option 2: Change to a POST and pass the case data to the endpoint to generate the robotics data.
    @GetMapping("/testing-support/case/{caseId}/robotics")
    public ResponseEntity<RoboticsCaseData> getRoboticsData(@PathVariable("caseId") Long caseId) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        var roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);

        try {
            var errors = jsonSchemaValidationService.validate(roboticsCaseData.toJsonString());
            if (errors == null || errors.isEmpty()) {
                return new ResponseEntity<>(roboticsCaseData, HttpStatus.OK);
            } else {
                throw new JsonSchemaValidationException(
                    format("Invalid RPA Json payload for %s", caseData.getLegacyCaseReference()), errors);
            }
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Data
    private static class BusinessProcessInfo {
        private BusinessProcess businessProcess;
        private String incidentMessage;

        private BusinessProcessInfo(BusinessProcess businessProcess) {
            this.businessProcess = businessProcess;
        }
    }
}
