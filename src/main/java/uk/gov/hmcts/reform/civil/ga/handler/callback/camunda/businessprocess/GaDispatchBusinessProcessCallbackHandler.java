package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISPATCH_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.DISPATCHED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaDispatchBusinessProcessCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DISPATCH_BUSINESS_PROCESS_GASPEC);

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::checkIfBusinessProcessStarted,
            callbackKey(ABOUT_TO_SUBMIT), this::dispatchBusinessProcess
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse checkIfBusinessProcessStarted(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Check if business process started for caseId: {}", caseData.getCcdCaseReference());
        List<String> errors = new ArrayList<>();

        if (caseData.getBusinessProcess().getStatusOrDefault() != READY) {
            errors.add("Business process already started");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse dispatchBusinessProcess(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Dispatch business process for caseId: {}", caseData.getCcdCaseReference());
        BusinessProcess businessProcess = caseData.getBusinessProcess();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        if (businessProcess.getStatus() == READY) {
            caseDataBuilder
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(businessProcess.getCamundaEvent())
                                     .setStatus(DISPATCHED));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
