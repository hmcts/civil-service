package uk.gov.hmcts.reform.unspec.handler.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_CLAIM_TO_STRUCK_OUT;

@Service
@RequiredArgsConstructor
public class MoveClaimToStruckOutCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MOVE_CLAIM_TO_STRUCK_OUT);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::updateBusinessStatusToReady
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private final CaseDetailsConverter caseDetailsConverter;

    private CallbackResponse updateBusinessStatusToReady(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        CaseData.CaseDataBuilder caseDataBuilder = data.toBuilder()
            .businessProcess(BusinessProcess.ready(MOVE_CLAIM_TO_STRUCK_OUT));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataBuilder.build()))
            .build();
    }
}
