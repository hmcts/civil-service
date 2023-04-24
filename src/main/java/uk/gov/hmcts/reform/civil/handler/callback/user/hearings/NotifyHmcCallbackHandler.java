package uk.gov.hmcts.reform.civil.handler.callback.user.hearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HMC;

@Service
@RequiredArgsConstructor
public class NotifyHmcCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_HMC);

    private final ObjectMapper mapper;

    private final RuntimeService camundaService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyHmc,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyHmc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder().build();

        System.out.println("Getting variables from camunda...\n");
        var variables = camundaService.getVariables(caseData.getBusinessProcess().getProcessInstanceId());
        System.out.println(String.format("Hearing Id: %s \n", variables.get("hearingId")));
        System.out.println(String.format("Hearing Fee: %s \n", variables.get("hearingFee")));

        System.out.println("Notifying HMC about hearing...\n");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .build();
    }
}
