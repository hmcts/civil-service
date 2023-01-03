package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

abstract class EvidenceUploadHandlerBase extends CallbackHandler {

    private final List<CaseEvent> events;
    private final String pageId;
    //private final String createShowCondition;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    protected EvidenceUploadHandlerBase(UserService userService, CoreCaseUserService coreCaseUserService,
                                        ObjectMapper objectMapper, Time time, List<CaseEvent> events, String pageId) {
        this.objectMapper = objectMapper;
        this.time = time;
        //this.createShowCondition = createShowCondition;
        this.events = events;
        this.pageId = pageId;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse)
            .put(callbackKey(SUBMITTED), this::emptyCallbackResponse)
            .build();
    }

}
