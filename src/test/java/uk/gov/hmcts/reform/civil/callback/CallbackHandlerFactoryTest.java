package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DASHBOARD_NOTIFICATION_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;

class CallbackHandlerFactoryTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    public static final CallbackResponse EVENT_HANDLED_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .data(Map.of("state", "created"))
        .build();

    public static final CallbackResponse ALREADY_HANDLED_EVENT_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .errors(List.of(format("Event %s is already processed", NOTIFY_EVENT.name())))
        .build();

    public static final CallbackResponse CIVIL_ONLY_EVENT_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .data(Map.of("caseType", "civil"))
        .build();

    public static final CallbackResponse GA_ONLY_EVENT_RESPONSE = AboutToStartOrSubmitCallbackResponse.builder()
        .data(Map.of("caseType", "ga"))
        .build();

    private CallbackHandlerFactory callbackHandlerFactory;

    @BeforeEach
    void setUp() {
        callbackHandlerFactory = buildFactory();
    }

    private CallbackHandlerFactory buildFactory() {
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(new ObjectMapper());
        CaseTypeHandlerKeyFactory keyFactory = new CaseTypeHandlerKeyFactory();
        return new CallbackHandlerFactory(
            caseDetailsConverter,
            keyFactory,
            new CreateCaseCallbackHandler(),
            new SendSealedClaimCallbackHandler(),
            new DefendantResponseHandlerWithoutCallbackVersion(),
            new MultiCaseDashboardNotificationHandler(),
            new CivilOnlyEventHandler(),
            new GaOnlyEventHandler()
        );
    }

    @Test
    void shouldThrowCallbackException_whenUnknownEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId("nope")
            .build();
        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(V_1);

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
        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(V_2);

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

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldNotProcessEventAgain_whenEventIsAlreadyProcessed() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(NOTIFY_EVENT.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of(
                "businessProcess",
                new BusinessProcess().setActivityId("CreateClaimPaymentSuccessfulNotifyRespondentSolicitor1")
            )).build())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(ALREADY_HANDLED_EVENT_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenEventIsNotAlreadyProcessed() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(NOTIFY_EVENT.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of(
                "businessProcess",
                new BusinessProcess().setActivityId("unProcessedTask")
            )).build())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

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
                new BusinessProcess().setActivityId("unProcessedTask")
            )).build())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenEventHasNoCaseDetailsBefore() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEvent_whenNoVersion() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE.name())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldDefaultToMethod_whenVersionSpecifiedInCallbackButNotInHandler() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE.name())
            .build();

        CallbackParams params = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackResponse);
    }

    @Test
    void shouldProcessEventForMultiCaseHandlerWhenCivilAndGaCaseTypes() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DASHBOARD_NOTIFICATION_EVENT.name())
            .build();

        CallbackParams civilParams = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackParams gaParams = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .isGeneralApplicationCaseType(true);

        assertEquals(EVENT_HANDLED_RESPONSE, callbackHandlerFactory.dispatch(civilParams));
        assertEquals(EVENT_HANDLED_RESPONSE, callbackHandlerFactory.dispatch(gaParams));
    }

    @Test
    void shouldDispatchToCivilOrGaHandlerForSameEventName() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(RESPOND_TO_APPLICATION.name())
            .build();

        CallbackParams civilParams = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN));

        CallbackParams gaParams = new CallbackParams()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .isGeneralApplicationCaseType(true);

        assertEquals(CIVIL_ONLY_EVENT_RESPONSE, callbackHandlerFactory.dispatch(civilParams));
        assertEquals(GA_ONLY_EVENT_RESPONSE, callbackHandlerFactory.dispatch(gaParams));
    }

    private static class CreateCaseCallbackHandler extends CallbackHandler {
        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
                callbackKey(ABOUT_TO_START), this::createCitizenClaim,
                callbackKey(V_1, ABOUT_TO_SUBMIT), this::createCitizenClaim,
                callbackKey(ABOUT_TO_SUBMIT, "start-claim"), this::createCitizenClaim,
                callbackKey(V_1, ABOUT_TO_SUBMIT, "start-claim"), this::createCitizenClaim
            );
        }

        private CallbackResponse createCitizenClaim(CallbackParams callbackParams) {
            return EVENT_HANDLED_RESPONSE;
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return Collections.singletonList(CREATE_CLAIM);
        }
    }

    private static class SendSealedClaimCallbackHandler extends CallbackHandler {
        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
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
            return Collections.singletonList(NOTIFY_EVENT);
        }
    }

    private static class DefendantResponseHandlerWithoutCallbackVersion extends CallbackHandler {
        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
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
    }

    private static class MultiCaseDashboardNotificationHandler extends CallbackHandler
        implements MultiCaseTypeCallbackHandler {

        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::handleDashboardNotification
            );
        }

        private CallbackResponse handleDashboardNotification(CallbackParams callbackParams) {
            return EVENT_HANDLED_RESPONSE;
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return Collections.singletonList(DASHBOARD_NOTIFICATION_EVENT);
        }
    }

    private static class CivilOnlyEventHandler extends CallbackHandler {
        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::handleEvent
            );
        }

        private CallbackResponse handleEvent(CallbackParams callbackParams) {
            return CIVIL_ONLY_EVENT_RESPONSE;
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return Collections.singletonList(RESPOND_TO_APPLICATION);
        }
    }

    private static class GaOnlyEventHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {
        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::handleEvent
            );
        }

        private CallbackResponse handleEvent(CallbackParams callbackParams) {
            return GA_ONLY_EVENT_RESPONSE;
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return Collections.singletonList(RESPOND_TO_APPLICATION);
        }
    }
}
