package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;

@ExtendWith(MockitoExtension.class)
class NotSuitableSDOCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private Time time;

    @Mock
    private IdamClient idamClient;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private NotSuitableSDOCallbackHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new NotSuitableSDOCallbackHandler(objectMapper, time, toggleService);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Nested
    class AboutToStartCallback {

        private CallbackParams params;

        private static final String EMAIL = "example@email.com";

        @BeforeEach
        void setup() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_START);
            String userId = UUID.randomUUID().toString();

            given(time.now()).willReturn(LocalDateTime.now());

        }

        @Test
        void checkUnsuitableSDODate() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Object unsuitableSDODateObj = response.getData().get("unsuitableSDODate");

            assertThat(unsuitableSDODateObj).isInstanceOf(java.util.List.class);
            java.util.List<?> unsuitableSDODateList = (java.util.List<?>) unsuitableSDODateObj;

            java.util.List<Integer> dateComponents = unsuitableSDODateList.stream()
                .map(o -> Integer.parseInt(o.toString())).toList();

            LocalDateTime unsuitableSDODate = LocalDateTime.of(
                dateComponents.get(0), dateComponents.get(1), dateComponents.get(2),
                dateComponents.get(3), dateComponents.get(4), dateComponents.get(5),
                dateComponents.get(6)
            );

            String formattedUnsuitableSDODate = unsuitableSDODate.format(JacksonConfiguration.DATE_TIME_FORMATTER);
            String expectedTimeString = time.now().format(JacksonConfiguration.DATE_TIME_FORMATTER);

            assertThat(formattedUnsuitableSDODate).isEqualTo(expectedTimeString);
        }
    }

    @Nested
    class MidCallback {

        private CallbackParams params;
        private CaseData caseData;

        @Mock
        private CallbackParams callbackParams;

        @Test
        void shouldValidateReasonLessThan150_whenInvoked() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(false);
            final String PAGE_ID = "not-suitable-reason";

            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();

        }

        @Test
        void shouldValidateReasonMoreThan150_whenInvokedA() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(false);
            final String PAGE_ID = "not-suitable-reason";
            final int lengthALlowed = 4000;

            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawnOverLimit().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0)).isEqualTo("Character Limit Reached: "
                                                   + "Reason for not drawing Standard Directions order cannot exceed "
                                                   + lengthALlowed + " characters.");

        }

        @Test
        void shouldValidateReasonLessThan150_whenInvokedAndTOCEnabledOtherReasons() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            final String PAGE_ID = "not-suitable-reason";

            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();

        }

        @Test
        void shouldValidateReasonMoreThan150_whenInvokedAndTOCEnabledOtherReasons() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            final String PAGE_ID = "not-suitable-reason";
            final int lengthALlowed = 4000;

            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawnOverLimit().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0)).isEqualTo("Character Limit Reached: "
                                                                  + "Reason for not drawing Standard Directions order cannot exceed "
                                                                  + lengthALlowed + " characters.");

        }

        @Test
        void shouldValidateTOCReasonLessThan150_whenInvokedAndTOCEnabledTransferCase() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            final String PAGE_ID = "not-suitable-reason";

            caseData = CaseDataBuilder.builder().atStateBeforeTransferCaseSDONotDrawn().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();

        }

        @Test
        void shouldValidateTOCReasonMoreThan150_whenInvokedAndTOCEnabledTransferCase() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            final String PAGE_ID = "not-suitable-reason";
            final int lengthALlowed = 4000;

            caseData = CaseDataBuilder.builder().atStateBeforeTransferCaseSDONotDrawnOverLimit().build();
            params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0)).isEqualTo("Character Limit Reached: "
                                                                  + "Reason for not drawing Standard Directions order cannot exceed "
                                                                  + lengthALlowed + " characters.");

        }
    }

    @Nested
    class AboutToSubmitCallback {
        private CaseData caseData;
        private CallbackParams params;

        private static final String EMAIL = "example@email.com";

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(false);
            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(NotSuitable_SDO.name(), "READY");

        }

        @Test
        void checkOtherDetailsUpdated() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(false);
            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("otherDetails").extracting("notSuitableForSDO").isEqualTo("Yes");
            assertThat(response.getData()).extracting("otherDetails").extracting("reasonNotSuitableForSDO").isEqualTo("unforeseen complexities");

        }

        @Test
        void shouldUpdateBusinessProcess_whenInvokedAndTOCEnabledOtherReasons() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(NotSuitable_SDO.name(), "READY");

        }

        @Test
        void checkOtherDetailsUpdated_whenTOCEnabledOtherReasons() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("notSuitableSdoOptions").isEqualTo("OTHER_REASONS");
            assertThat(response.getData()).extracting("otherDetails").extracting("notSuitableForSDO").isEqualTo("Yes");
            assertThat(response.getData()).extracting("otherDetails").extracting("reasonNotSuitableForSDO").isEqualTo("unforeseen complexities");

        }

        @Test
        void shouldUpdateBusinessProcess_whenInvokedAndTOCEnabledTransferCase() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            caseData = CaseDataBuilder.builder().atStateBeforeTransferCaseSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess").isNull();
        }

        @Test
        void checkOtherDetailsUpdated_whenTOCEnabledTransferCase() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            caseData = CaseDataBuilder.builder().atStateBeforeTransferCaseSDONotDrawn().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("notSuitableSdoOptions").isEqualTo("CHANGE_LOCATION");
            assertThat(response.getData()).extracting("otherDetails").extracting("notSuitableForSDO").isEqualTo("Yes");
            assertThat(response.getData()).extracting("transferCaseDetails").extracting("reasonForTransferCaseTxt").isEqualTo("unforeseen complexities");
        }
    }

    @Nested
    class SubmittedCallback {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnExpectedSubmittedCallbackResponse(Boolean toggleState) {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(toggleState);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format("# Your request was accepted%n## Case has now moved offline");
            String body = format("<br />If a Judge has submitted this information, "
                + "a notification will be sent to the listing officer to look at this case offline."
                + "%n%nIf a legal adviser has submitted this information a notification will be sent "
                                     + "to a judge for review.");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenTOCEnabledTransferCase() {
            when(toggleService.isTransferOnlineCaseEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateBeforeTransferCaseSDONotDrawn().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format("# Your request was successful%n## This claim will be transferred to a different location");
            String body = format("<br />A notification will be sent to the listing officer to look at this case and " +
                                     "process the transfer of case.");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleEventsReturnsTheExpectedCallbackEvent(Boolean toggleState) {
        assertThat(handler.handledEvents()).contains(NotSuitable_SDO);
    }
}
