package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SystemCallbackFailureResponderTest {

    private static final String SYSTEM_ROLES = "caseworker,caseworker-civil,caseworker-civil-systemupdate";

    private final SystemCallbackFailureResponder responder = new SystemCallbackFailureResponder(true);

    @Test
    void appliesToBlockingCallbacksForTheSystemUpdateUser() {
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, SYSTEM_ROLES)).isTrue();
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_START, SYSTEM_ROLES)).isTrue();
        assertThat(responder.appliesTo(CallbackType.MID, SYSTEM_ROLES)).isTrue();
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, " caseworker , caseworker-civil-systemupdate ")).isTrue();
    }

    @Test
    void doesNotApplyToRealUsers() {
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, "caseworker,caseworker-civil,caseworker-civil-admin")).isFalse();
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, "citizen")).isFalse();
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, null)).isFalse();
        // a similarly named role must not match
        assertThat(responder.appliesTo(CallbackType.ABOUT_TO_SUBMIT, "caseworker-civil-systemupdate-readonly")).isFalse();
    }

    @Test
    void doesNotApplyToSubmittedCallbacks() {
        assertThat(responder.appliesTo(CallbackType.SUBMITTED, SYSTEM_ROLES)).isFalse();
    }

    @Test
    void doesNotApplyWhenSwitchedOff() {
        assertThat(new SystemCallbackFailureResponder(false).appliesTo(CallbackType.ABOUT_TO_SUBMIT, SYSTEM_ROLES)).isFalse();
    }

    @Test
    void returnsTheLabelledExceptionAsTheOnlyCallbackError() {
        CallbackResponse response = responder.toCallbackError(
            new NullPointerException("repayment plan is null"), 1792450652432573L,
            "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT", CallbackType.ABOUT_TO_SUBMIT);

        assertThat(response).isInstanceOf(AboutToStartOrSubmitCallbackResponse.class);
        AboutToStartOrSubmitCallbackResponse body = (AboutToStartOrSubmitCallbackResponse) response;
        assertThat(body.getErrors())
            .containsExactly("NOT RETRYABLE: java.lang.NullPointerException: repayment plan is null");
        assertThat(body.getData()).isNull();
    }
}
