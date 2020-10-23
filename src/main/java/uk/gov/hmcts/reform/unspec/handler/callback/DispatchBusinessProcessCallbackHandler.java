package uk.gov.hmcts.reform.unspec.handler.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISPATCH_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.DISPATCHED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@Service
@RequiredArgsConstructor
public class DispatchBusinessProcessCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DISPATCH_BUSINESS_PROCESS);

    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::dispatchBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse dispatchBusinessProcess(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        BusinessProcess businessProcess = caseData.getBusinessProcess();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        if (businessProcess.getStatus() == READY) {
            caseDataBuilder
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent(businessProcess.getCamundaEvent())
                                     .status(DISPATCHED)
                                     .build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataBuilder.build()))
            .build();
    }
}
