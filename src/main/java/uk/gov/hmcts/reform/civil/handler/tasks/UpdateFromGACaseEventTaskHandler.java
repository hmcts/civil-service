package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Case;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.*;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.*;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@RequiredArgsConstructor
@Component
public class UpdateFromGACaseEventTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final StateFlowEngine stateFlowEngine;

    private CaseData data;

    @Override
    public void handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, variables.getCaseEvent());
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
    }
}
