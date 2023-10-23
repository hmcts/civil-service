package uk.gov.hmcts.reform.civil.handler.tasks;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.DEFENDANT_DOES_NOT_CONSENT;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.JUDGEMENT_REQUEST;
import static uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper.OTHER;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
        try {
            ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
            String caseId = ofNullable(variables.getCaseId())
                .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));
            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, variables.getCaseEvent());
            CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
            BusinessProcess businessProcess = startEventData.getBusinessProcess()
                .updateActivityId(externalTask.getActivityId());

            if ((featureToggleService.isAutomatedHearingNoticeEnabled()
                || featureToggleService.isNextHearingDateEnabled())
                && !businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())) {
                businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
            }

            String flowState = externalTask.getVariable(FLOW_STATE);
            CaseDataContent caseDataContent = caseDataContent(
                startEventResponse,
                businessProcess,
                flowState,
                startEventData
            );
            data = coreCaseDataService.submitUpdate(caseId, caseDataContent);
        } catch (ValueMapperException | IllegalArgumentException e) {
            throw new InvalidCaseDataException("Mapper conversion failed due to incompatible types", e);
        }
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
                                            String flowState,
                                            CaseData caseData) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(getSummary(startEventResponse.getEventId(), flowState))
                       .description(getDescription(startEventResponse.getEventId(), data, flowState, caseData))
                       .build())
            .data(data)
            .build();
    }

    private String getSummary(String eventId, String state) {
        if (Objects.equals(eventId, CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM.name())) {
            FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state);
            return switch (flowState) {
                case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, DIVERGENT_RESPOND_GO_OFFLINE ->
                    "RPA Reason: Divergent respond.";
                case FULL_ADMISSION -> "RPA Reason: Defendant fully admits.";
                case PART_ADMISSION -> "RPA Reason: Defendant partial admission.";
                case COUNTER_CLAIM -> "RPA Reason: Defendant rejects and counter claims.";
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC, PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT ->
                    "RPA Reason: Unrepresented defendant(s).";
                case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT ->
                    "RPA Reason: Unregistered defendant solicitor firm(s).";
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT ->
                    "RPA Reason: Unrepresented defendant and unregistered defendant solicitor firm";
                case FULL_DEFENCE_PROCEED, FULL_ADMIT_PROCEED, FULL_ADMIT_PAY_IMMEDIATELY, PART_ADMIT_PAY_IMMEDIATELY, PART_ADMIT_PROCEED ->
                    "RPA Reason: Claimant(s) proceeds.";
                case FULL_DEFENCE_NOT_PROCEED, FULL_ADMIT_NOT_PROCEED, PART_ADMIT_NOT_PROCEED ->
                    "RPA Reason: Claimant(s) intends not to proceed.";
                case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED, TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED ->
                    "RPA Reason: Only one of the defendants is notified.";
                case TAKEN_OFFLINE_BY_STAFF -> "RPA Reason: Case taken offline by staff.";
                case CLAIM_DETAILS_NOTIFIED, NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, NOTIFICATION_ACKNOWLEDGED,
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA ->
                    "RPA Reason: Not suitable for SDO.";
                case FULL_ADMIT_AGREE_REPAYMENT, PART_ADMIT_AGREE_REPAYMENT, FULL_ADMIT_JUDGMENT_ADMISSION ->
                    "RPA Reason: Judgement by Admission requested and claim moved offline.";
                default -> {
                    log.info("Unexpected flow state " + flowState.fullName());
                    yield null;
                }
            };
        }
        return null;
    }

    private String getDescription(String eventId, Map data, String state, CaseData caseData) {
        Object claimProceedsInCaseman = data.get("claimProceedsInCaseman");
        FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state);

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

            switch (flowState) {
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC:
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                    return format("Unrepresented defendant: %s",
                                      StringUtils.join(
                                          getDefendantNames(UNREPRESENTED, caseData), " and "
                                      ));
                case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                    return format("Unregistered defendant solicitor firm: %s",
                                     StringUtils.join(
                                         getDefendantNames(featureToggleService.isNoticeOfChangeEnabled()
                                                               ? UNREGISTERED_NOTICE_OF_CHANGE : UNREGISTERED,
                                                           caseData), " and "
                                     ));
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                    return format("Unrepresented defendant and unregistered defendant solicitor firm. "
                                      + "Unrepresented defendant: %s. "
                                      + "Unregistered defendant solicitor firm: %s.",
                                        StringUtils.join(getDefendantNames(UNREPRESENTED, caseData), " and "),
                                        StringUtils.join(
                                            getDefendantNames(featureToggleService.isNoticeOfChangeEnabled()
                                                                  ? UNREGISTERED_NOTICE_OF_CHANGE : UNREGISTERED,
                                                              caseData), " and "
                                        ));
                case FULL_DEFENCE_PROCEED:
                    return !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                        ? getDescriptionFullDefenceProceed(caseData) : null;
                default:
                    break;
            }
        }
        return null;
    }

    private String getDescriptionFullDefenceProceed(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
            case ONE_V_TWO_TWO_LEGAL_REP: {
                return format(
                    "Claimant has provided intention: %s against defendant: %s and %s against defendant: %s",
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        ? "proceed" : "not proceed",
                    caseData.getRespondent1().getPartyName(),
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
                        ? "proceed" : "not proceed",
                    caseData.getRespondent2().getPartyName()
                );
            }
            case TWO_V_ONE: {
                return format(
                    "Claimant: %s has provided intention: %s. Claimant: %s has provided intention: %s.",
                    caseData.getApplicant1().getPartyName(),
                    YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1()) ? "proceed" : "not proceed",
                    caseData.getApplicant2().getPartyName(),
                    YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1()) ? "proceed" : "not proceed"
                );
            }
            default: {
                return null;
            }
        }
    }
}
