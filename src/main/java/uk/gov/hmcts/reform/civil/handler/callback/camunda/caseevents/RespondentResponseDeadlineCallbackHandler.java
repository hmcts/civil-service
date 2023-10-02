package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_LIP_RESPONDENT_RESPONSE_DEADLINE;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RespondentResponseDeadlineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(SET_LIP_RESPONDENT_RESPONSE_DEADLINE);
    public static final String TASK_ID = "SetRespondent1Deadline";

    private final ObjectMapper objectMapper;

    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateRespondentDeadlineDate);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateRespondentDeadlineDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder()
                .respondent1ResponseDeadline(deadlinesCalculator.plus28DaysAt4pmDeadline(
                    caseData.getRespondent1ResponseDeadline()));

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper))
            .build();
    }

}
