package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;

class CaseTypeHandlerKeyFactoryTest {

    private final CaseTypeHandlerKeyFactory factory = new CaseTypeHandlerKeyFactory();

    @Test
    void shouldCreateCivilCaseHandlerKeyGivenCallParamsWithGeneralApplicationCaseFalse() {
        final CallbackParams callbackParams = new CallbackParams()
            .isGeneralApplicationCaseType(false)
            .request(CallbackRequest.builder().eventId(CREATE_CLAIM.name()).build());

        final String handlerKey = factory.createDispatchKey(callbackParams);
        assertEquals(CREATE_CLAIM.name(), handlerKey);
    }

    @Test
    void shouldCreateGeneralApplicationCaseHandlerKeyGivenCallParamsWithGeneralApplicationCaseTrue() {
        final String expectedHandlerKey = GENERALAPPLICATION_CASE_TYPE + "-" + RESPOND_TO_APPLICATION.name();
        final CallbackParams callbackParams = new CallbackParams()
            .isGeneralApplicationCaseType(true)
            .request(CallbackRequest.builder().eventId(RESPOND_TO_APPLICATION.name()).build());

        final String handlerKey = factory.createDispatchKey(callbackParams);

        assertEquals(expectedHandlerKey, handlerKey);
    }

    @Test
    void shouldCreateCivilCaseHandlerKeyGivenCallbackHandlerAndCaseEventForCivil() {
        final List<String> handlerKeys = factory.createRegistrationKeys(new CivilCallbackHandler(), CREATE_CLAIM);
        assertEquals(List.of(CREATE_CLAIM.name()), handlerKeys);
    }

    @Test
    void shouldCreateGeneralApplicationCaseHandlerKeyGivenCallbackHandlerAndCaseEventForGeneralApplication() {
        final String expectedHandlerKey = GENERALAPPLICATION_CASE_TYPE + "-" + RESPOND_TO_APPLICATION.name();
        final List<String> handlerKeys = factory.createRegistrationKeys(
            new GaCallbackHandler(),
            RESPOND_TO_APPLICATION
        );
        assertEquals(List.of(expectedHandlerKey), handlerKeys);
    }

    @Test
    void shouldCreateBothCaseHandlerKeysForMultiCaseHandler() {
        final List<String> handlerKeys = factory.createRegistrationKeys(
            new MultiCaseCallbackHandler(),
            CREATE_CLAIM
        );

        assertEquals(
            List.of(
                CREATE_CLAIM.name(),
                GENERALAPPLICATION_CASE_TYPE + "-" + CREATE_CLAIM.name()
            ),
            handlerKeys
        );
    }

    private static class CivilCallbackHandler extends CallbackHandler {

        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of();
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return List.of();
        }
    }

    private static class GaCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of();
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return List.of();
        }
    }

    private static class MultiCaseCallbackHandler extends CallbackHandler implements MultiCaseTypeCallbackHandler {

        @Override
        protected Map<String, Callback> callbacks() {
            return Map.of();
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return List.of();
        }
    }
}
