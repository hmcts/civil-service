package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.DEFENDANT_DOES_NOT_CONSENT;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.JUDGEMENT_REQUEST;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.OTHER;

@RequiredArgsConstructor
@Component
public class CaseEventTaskHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final StateFlowEngine stateFlowEngine;
    private final FeatureToggleService featureToggleService;

    private CaseData data;

    @Override
    public void handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, variables.getCaseEvent());
        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = startEventData.getBusinessProcess()
            .updateActivityId(externalTask.getActivityId());

        String flowState = externalTask.getVariable(FLOW_STATE);
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, businessProcess, flowState);
        data = coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    @Override
    public VariableMap getVariableMap() {
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        variables.putValue(FLOW_STATE, stateFlow.getState().getName());
        variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
        return variables;
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse,
                                            BusinessProcess businessProcess,
                                            String flowState) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(getSummary(startEventResponse.getEventId(), flowState, data))
                       .description(getDescription(startEventResponse.getEventId(), data))
                       .build())
            .data(data)
            .build();
    }

    private String getSummary(String eventId, String state, Map data) {
        if (Objects.equals(eventId, CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM.name())) {
            FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state);

            if (featureToggleService.isMultipartyEnabled()) {
                try {
                    if (Objects.nonNull(data.get("addRespondent2").equals("Yes"))) {
                        if (Objects.nonNull(data.get("respondent1ClaimResponseType"))
                            != Objects.nonNull(data.get("respondent2ClaimResponseType"))) {
                            return "RPA Reason: Divergent response";
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Invalid data " + e);
                }
            }

            switch (flowState) {
                case FULL_ADMISSION:
                    return "RPA Reason: Defendant fully admits.";
                case PART_ADMISSION:
                    return "RPA Reason: Defendant partial admission.";
                case COUNTER_CLAIM:
                    return "RPA Reason: Defendant rejects and counter claims.";
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                    return "RPA Reason: Unrepresented defendant.";
                case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                    return "RPA Reason: Unregistered defendant solicitor firm.";
                case FULL_DEFENCE_PROCEED:
                    return "RPA Reason: Applicant proceeds.";
                case FULL_DEFENCE_NOT_PROCEED:
                    return "RPA Reason: Claimant intends not to proceed.";
                case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                    return "RPA Reason: Only one of the respondent is notified.";
                case TAKEN_OFFLINE_BY_STAFF:
                    return "RPA Reason: Case taken offline by staff.";
                default:
                    throw new IllegalStateException("Unexpected flow state " + flowState.fullName());
            }
        }
        return null;
    }

    private String getDescription(String eventId, Map data) {
        Object claimProceedsInCaseman = data.get("claimProceedsInCaseman");
        if (Objects.equals(eventId, CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM.name())) {
            if (Objects.nonNull(claimProceedsInCaseman)) {
                String claimString = claimProceedsInCaseman.toString();
                String[] claimArray = claimString.split(",");
                for (String value : claimArray) {
                    if (value.contains(APPLICATION.name())) {
                        return "Application.";
                    } else if (value.contains(CASE_SETTLED.name())) {
                        return "Case settled.";
                    } else if (value.contains(DEFENDANT_DOES_NOT_CONSENT.name())) {
                        return "Defendant does not consent to accept service.";
                    } else if (value.contains(JUDGEMENT_REQUEST.name())) {
                        return "Judgement request.";
                    } else if (value.contains(OTHER.name())) {
                        for (String description : claimArray) {
                            if (description.contains("other=")) {
                                return format("Other: %s", description.substring(description.indexOf("=") + 1));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
