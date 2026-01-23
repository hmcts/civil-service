package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GaStartGeneralApplicationBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    public static final String BUSINESS_PROCESS = "businessProcess";
    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final GaStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        GeneralApplicationCaseData caseData = startGeneralApplicationBusinessProcess(externalTask);
        log.debug("Started General Application Business Process for case ID: {}", caseData.getCcdCaseReference());
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(caseData);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        if (caseData.getGeneralAppParentCaseLink() != null) {
            variables.putValue("generalAppParentCaseLink", caseData.getGeneralAppParentCaseLink().getCaseReference());
        }
        return ExternalTaskData.builder().parentCaseData(caseData).variables(variables).build();
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        return externalTaskData.getVariables();
    }

    private GeneralApplicationCaseData startGeneralApplicationBusinessProcess(ExternalTask externalTask) {
        ExternalTaskInput externalTaskInput = mapper.convertValue(
            externalTask.getAllVariables(),
            ExternalTaskInput.class
        );
        String caseId = externalTaskInput.getCaseId();
        CaseEvent caseEvent = externalTaskInput.getCaseEvent();
        log.info("Starting process for Case ID: {}, Event: {}", caseId, caseEvent);
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);
        GeneralApplicationCaseData data = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = data.getBusinessProcess();
        log.debug("Business Process Status: {}", businessProcess.getStatusOrDefault());
        switch (businessProcess.getStatusOrDefault()) {
            case READY, DISPATCHED -> {
                return updateBusinessProcess(caseId, externalTask, startEventResponse, businessProcess);
            }
            case STARTED -> {
                if (businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())) {
                    log.warn("Process instance ID already exists. Aborting for Case ID: {}", caseId);
                    throw new BpmnError("ABORT");
                }
                log.debug("Process already started for Case ID: {}", caseId);
                return data;
            }
            default -> {
                log.error("----------------CAMUNDAERROR -START------------------");
                log.error("CAMUNDAERROR CaseId ({})", caseId);
                log.error("CAMUNDAERROR CaseEvent ({})", caseEvent);
                log.error("CAMUNDAERROR BusinessProcessStatus ({})", businessProcess.getStatusOrDefault());
                log.error("----------------CAMUNDAERROR -END------------------");
                throw new BpmnError("ABORT");
            }
        }
    }

    private GeneralApplicationCaseData updateBusinessProcess(
        String ccdId,
        ExternalTask externalTask,
        StartEventResponse startEventResponse,
        BusinessProcess businessProcess
    ) {
        businessProcess = businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
        log.info("Business process updated and submitted for Case ID: {}", ccdId);
        return coreCaseDataService.submitGaUpdate(ccdId, caseDataContent(startEventResponse, businessProcess));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put(BUSINESS_PROCESS, businessProcess);
        log.debug("Prepared case data content with updated BusinessProcess for Case ID: {}", startEventResponse.getCaseDetails().getId());

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}
