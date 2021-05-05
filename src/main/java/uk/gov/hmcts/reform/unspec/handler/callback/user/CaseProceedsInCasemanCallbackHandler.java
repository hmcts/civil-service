package uk.gov.hmcts.reform.unspec.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.validation.groups.CasemanTransferDateGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;

@Service
@RequiredArgsConstructor
public class CaseProceedsInCasemanCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CASE_PROCEEDS_IN_CASEMAN);

    private final Validator validator;
    private final Time time;
    private final ObjectMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "transfer-date"), this::validateTransferDate,
            callbackKey(ABOUT_TO_SUBMIT), this::addTakenOfflineDate
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateTransferDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validator.validate(caseData, CasemanTransferDateGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse addTakenOfflineDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder().takenOfflineByStaffDate(time.now()).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .build();
    }
}
