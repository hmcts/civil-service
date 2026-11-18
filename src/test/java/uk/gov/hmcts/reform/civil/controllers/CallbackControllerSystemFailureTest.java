package uk.gov.hmcts.reform.civil.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.callback.SystemCallbackFailureResponder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DTSCCI-7095: a failed system-user callback goes back to CCD as a labelled callback error,
 * while real users keep the existing exception handling.
 */
@ExtendWith(MockitoExtension.class)
class CallbackControllerSystemFailureTest {

    private static final String SYSTEM_ROLES = "caseworker,caseworker-civil,caseworker-civil-systemupdate";
    private static final String USER_ROLES = "caseworker,caseworker-civil,caseworker-civil-admin";

    @Mock
    private CallbackHandlerFactory callbackHandlerFactory;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private HttpResponseHeadersService responseHeadersService;

    private CallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new CallbackController(callbackHandlerFactory, caseDetailsConverter, responseHeadersService,
                                            new SystemCallbackFailureResponder(true));
    }

    @Test
    void systemUserAboutToSubmitFailureIsReturnedAsALabelledCallbackError() {
        when(callbackHandlerFactory.dispatch(any())).thenThrow(new NullPointerException("repayment plan is null"));

        CallbackResponse response = invoke("about-to-submit", SYSTEM_ROLES);

        assertThat(((AboutToStartOrSubmitCallbackResponse) response).getErrors())
            .containsExactly("NOT RETRYABLE: java.lang.NullPointerException: repayment plan is null");
    }

    @Test
    void realUserFailureIsStillThrown() {
        when(callbackHandlerFactory.dispatch(any())).thenThrow(new NullPointerException("repayment plan is null"));

        assertThatThrownBy(() -> invoke("about-to-submit", USER_ROLES)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void systemUserSubmittedCallbackFailureIsStillThrown() {
        when(callbackHandlerFactory.dispatch(any())).thenThrow(new IllegalStateException("notification failed"));

        assertThatThrownBy(() -> invoke("submitted", SYSTEM_ROLES)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void successfulCallbackIsUnchanged() {
        AboutToStartOrSubmitCallbackResponse ok = AboutToStartOrSubmitCallbackResponse.builder().data(Map.of("a", "b")).build();
        when(callbackHandlerFactory.dispatch(any())).thenReturn(ok);

        assertThat(invoke("about-to-submit", SYSTEM_ROLES)).isSameAs(ok);
    }

    private CallbackResponse invoke(String callbackType, String userRoles) {
        CaseDetails caseDetails = CaseDetails.builder().id(1792450652432573L).caseTypeId("CIVIL").data(Map.of()).build();
        CallbackRequest request = CallbackRequest.builder()
            .eventId("GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT")
            .caseDetails(caseDetails)
            .build();
        return controller.callback(mock(HttpServletResponse.class), "Bearer token", userRoles, callbackType,
                                   request, Optional.empty(), Optional.empty());
    }
}
