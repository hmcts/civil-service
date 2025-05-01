package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;

@ExtendWith(MockitoExtension.class)
class BundleCreatedNotificationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private BundleCreatedNotificationCallbackHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new BundleCreatedNotificationCallbackHandler(objectMapper, featureToggleService);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldStartCorrectBusinessProcess_WhenInvoked() {
            //Given: Casedata at hearingScheduled state and callback param with about-to-start event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: Handler is called
            when(featureToggleService.isAmendBundleEnabled()).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: It should start BUNDLE_CREATION_NOTIFICATION camunda business process
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(BUNDLE_CREATION_NOTIFICATION.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldReturnCorrectEvent_WhenInvoked() {
            //Given: Casedata at hearingScheduled state and callback param with about-to-start event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            handler.handle(params);

            //Then: Event should start BUNDLE_CREATION_NOTIFICATION
            assertTrue(handler.handledEvents().contains(BUNDLE_CREATION_NOTIFICATION));
        }

        @Test
        void shouldReturnCorrectResponse_WhenFeatureToggleIsEnabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("bundleEvent")
                .isEqualTo("BUNDLE_CREATED_NOTIFICATION");
        }

        @Test
        void shouldReturnCorrectResponse_WhenFeatureToggleIsDisabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isAmendBundleEnabled()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("bundleEvent")).isNull();;
        }
    }
}
