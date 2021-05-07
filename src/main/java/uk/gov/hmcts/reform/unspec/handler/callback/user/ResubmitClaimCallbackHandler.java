package uk.gov.hmcts.reform.unspec.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.ExitSurveyContentService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RESUBMIT_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResubmitClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESUBMIT_CLAIM);

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(CREATE_CLAIM))
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Claim pending")
            .confirmationBody("## What happens next %n "
                                  + "You claim will be processed. Wait for us to contact you."
                                  + exitSurveyContentService.applicantSurvey()
            )
            .build();
    }
}
