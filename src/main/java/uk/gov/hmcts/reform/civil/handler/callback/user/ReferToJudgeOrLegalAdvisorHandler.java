package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_LEGAL_ADVISOR;

@Service
@RequiredArgsConstructor
public class ReferToJudgeOrLegalAdvisorHandler extends CallbackHandler {

    public static final String COURT_ASSIGNE_ERROR_MESSAGE = "A Court has already been assigned";

    private static final List<CaseEvent> EVENTS = List.of(
        REFER_TO_JUDGE,
        REFER_TO_LEGAL_ADVISOR);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::courtValidation,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private AboutToStartOrSubmitCallbackResponse courtValidation(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(courtAssignedValidation(callbackParams))
            .build();
    }

    public List<String> courtAssignedValidation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        YesOrNo localCourtAssigned = caseData.getIsCcmccLocation();

        List<String> errors = new ArrayList<>();
        if (YesOrNo.NO.equals(localCourtAssigned)
            && callbackParams.getRequest().getEventId().equals("REFER_TO_LEGAL_ADVISOR")) {
            errors.add(COURT_ASSIGNE_ERROR_MESSAGE);
        }
        return errors;
    }
}
