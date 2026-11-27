package uk.gov.hmcts.reform.civil.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Returns a failed system-user callback to CCD as a callback error instead of an HTTP error.
 *
 * <p>For events submitted by the civil system update user (Camunda-driven events, scheduler and
 * payment updates), CCD turns any non-2xx callback response into {@code 502 CallbackException}
 * and discards the body, so the Camunda incident never names the real exception. Returned as a
 * callback error, it reaches the caller as a 422 with {@code callbackErrors}, labelled
 * {@code RETRYABLE} or {@code NOT RETRYABLE} by {@link CallbackFailureClassifier}.
 *
 * <p>Only callbacks that can block the event are covered (about-to-start, mid-event and
 * about-to-submit). Real users keep the existing HTTP error handling, so exception text is never
 * shown to them. The exception is still logged at error level with its stack trace, so App Insights
 * is unchanged. DTSCCI-7095.
 */
@Slf4j
@Component
public class SystemCallbackFailureResponder {

    static final String SYSTEM_UPDATE_ROLE = "caseworker-civil-systemupdate";
    private static final Set<CallbackType> BLOCKING_CALLBACKS =
        EnumSet.of(CallbackType.ABOUT_TO_START, CallbackType.MID, CallbackType.ABOUT_TO_SUBMIT);

    private final boolean enabled;

    public SystemCallbackFailureResponder(
        @Value("${callbacks.system-user-failures-as-callback-errors:true}") boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * True when this callback is a blocking callback for an event submitted by the system update
     * user. {@code userRoles} is CCD's {@code user-roles} header: the caller's IDAM roles, comma separated.
     */
    public boolean appliesTo(CallbackType callbackType, String userRoles) {
        return enabled
            && callbackType != null
            && BLOCKING_CALLBACKS.contains(callbackType)
            && userRoles != null
            && Arrays.stream(userRoles.split(",")).map(String::trim).anyMatch(SYSTEM_UPDATE_ROLE::equals);
    }

    public CallbackResponse toCallbackError(RuntimeException exception, Long caseId, String eventId,
                                            CallbackType callbackType) {
        String error = CallbackFailureClassifier.describe(exception);
        log.error("System callback {} for event {} on case {} failed; returned to CCD as callback error: {}",
                  callbackType, eventId, caseId, error, exception);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(error))
            .build();
    }
}
