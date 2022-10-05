package uk.gov.hmcts.reform.civil.callback;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;

@SpringBootTest(classes = {
    CallbackHandlerFactory.class,
    CaseDetailsConverter.class,
    JacksonAutoConfiguration.class},
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Import(CallbackHandlerFactoryTest.OverrideBean.class)
class CallbackHandlerFactoryTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    public static final CallbackResponse EVENT_HANDLED_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .data(Map.of("state", "created"))
        .build();

    public static final CallbackResponse ALREADY_HANDLED_EVENT_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .errors(List.of(format("Event %s is already processed", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name())))
        .build();

    @TestConfiguration
    public static class OverrideBean {
        @Bean
        public CallbackHandler createCaseCallbackHandler() {

            return new CallbackHandler() {
                @Override
                protected Map<String, Callback> callbacks() {
                    return ImmutableMap.of(
                        callbackKey(V_1, ABOUT_TO_SUBMIT), this::createCitizenClaim
                    );
                }

                private CallbackResponse createCitizenClaim(CallbackParams callbackParams) {
                    return EVENT_HANDLED_RESPONSE;
                }

                @Override
                public List<CaseEvent> handledEvents() {
                    return Collections.singletonList(CREATE_CLAIM);
                }
            };
        }

        @Bean
        public CallbackHandler sendSealedClaimCallbackHandler() {

            return new CallbackHandler() {
                @Override
                protected Map<String, Callback> callbacks() {
                    return ImmutableMap.of(
                        callbackKey(V_1, ABOUT_TO_SUBMIT), this::sendSealedClaim
                    );
                }

                private CallbackResponse sendSealedClaim(CallbackParams callbackParams) {
                    return EVENT_HANDLED_RESPONSE;
                }

                @Override
                public String camundaActivityId(CallbackParams callbackParams) {
                    return "CreateClaimPaymentSuccessfulNotifyRespondentSolicitor1";
                }

                @Override
                public List<CaseEvent> handledEvents() {
                    return Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE);
                }
            };
        }

        @Bean
        public CallbackHandler defendantResponseHandlerWithoutCallbackVersion() {

            return new CallbackHandler() {
                @Override
                protected Map<String, Callback> callbacks() {
                    return ImmutableMap.of(
                        callbackKey(ABOUT_TO_SUBMIT), this::doMethod
                    );
                }

                private CallbackResponse doMethod(CallbackParams callbackParams) {
                    return EVENT_HANDLED_RESPONSE;
                }

                @Override
                public List<CaseEvent> handledEvents() {
                    return Collections.singletonList(DEFENDANT_RESPONSE);
                }
            };
        }
    }

    @Autowired
    private CallbackHandlerFactory callbackHandlerFactory;

    @Test
    void shouldThrowCallbackException_whenUnknownEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId("nope")
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(V_1)
            .build();

        assertThatThrownBy(() -> callbackHandlerFactory.dispatch(params))
            .isInstanceOf(CallbackException.class)
            .hasMessage("Could not handle callback for event nope");
    }

    @Test
    void shouldThrowCallbackException_whenUnknownVersion() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(V_2)
            .build();

        assertThatThrownBy(() -> callbackHandlerFactory.dispatch(params))
            .isInstanceOf(CallbackException.class)
            .hasMessage(
                "Callback for event CREATE_CLAIM, version V_2, type ABOUT_TO_SUBMIT and page id null not implemented");
    }

    @Test
    void shouldProcessEvent_whenValidCaseEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of("state", "created")).build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldNotProcessEventAgain_whenEventIsAlreadyProcessed() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of(
                "businessProcess",
                BusinessProcess.builder().activityId("CreateClaimPaymentSuccessfulNotifyRespondentSolicitor1").build()
            )).build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(ALREADY_HANDLED_EVENT_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenEventIsNotAlreadyProcessed() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of(
                "businessProcess",
                BusinessProcess.builder().activityId("unProcessedTask").build()
            )).build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenEventHasNoCamundaTask() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of(
                "businessProcess",
                BusinessProcess.builder().activityId("unProcessedTask").build()
            )).build())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenEventHasNoCaseDetailsBefore() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenNoVersion() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE.name())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldDefaultToMethod_whenVersionSpecifiedInCallbackButNotInHandler() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE.name())
            .build();

        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }
}
