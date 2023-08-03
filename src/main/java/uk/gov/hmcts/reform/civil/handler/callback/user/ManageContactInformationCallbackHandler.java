package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@Service
@RequiredArgsConstructor
public class ManageContactInformationCallbackHandler extends CallbackHandler {

    private static final String INVALID_CASE_STATE_ERROR = "You will be able run the manage contact information " +
        "event once the claimant has responded.";
    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin");
    private static final List<CaseEvent> EVENTS = List.of(
        MANAGE_CONTACT_INFORMATION
    );

    private final UserService userService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateUserCanTriggerEvent)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse)
            .put(callbackKey(SUBMITTED), this::emptyCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateUserCanTriggerEvent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = isAwaitingClaimantIntention(caseData)
            && !isAdmin(callbackParams.getParams().get(BEARER_TOKEN).toString())
            ? List.of(INVALID_CASE_STATE_ERROR) : null;

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean isAwaitingClaimantIntention(CaseData caseData) {
        return caseData.getCcdState().equals(AWAITING_APPLICANT_INTENTION);
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(role -> ADMIN_ROLES.contains(role));
    }

}
