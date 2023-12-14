package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@SpringBootTest(classes = {
    InformAgreedExtensionDateCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    DeadlineExtensionValidator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    DeadlinesCalculator.class,
    StateFlowEngine.class,
})
class InformAgreedExtensionDateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InformAgreedExtensionDateCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private Time time;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private UserService userService;

    @MockBean
    private FeatureToggleService toggleService;

    @Nested
    class AboutToStartCallback {
        public static final String ERROR_EXTENSION_DATE_SUBMITTED =
            "This action cannot currently be performed because it has already been completed";

        @BeforeEach
        void setup() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenOneRespondentRepresentative() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("Yes");
        }

        @Test
        void shouldSetDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            // When
            when(coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                "uid",
                RESPONDENTSOLICITORONE
            )).thenReturn(true);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("respondentSolicitor1AgreedDeadlineExtension")
                .isEqualTo(caseData.getClaimDetailsNotificationDate().plusDays(42).toLocalDate().toString());
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenTwoRespondentRepresentativesWithNoRespondent2CaseRole() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent2SameLegalRepresentative(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("Yes");
        }

        @Test
        void shouldSetRespondent1FlagToNo_whenTwoRespondentRepresentativesWithRespondent2CaseRole() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent2SameLegalRepresentative(NO)
                .build();
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, true);
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("No");
        }

        @Test
        void shouldReturnErrorWhenRespondentRespondsAgain1v1() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent1TimeExtensionDate(timeExtensionDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly(ERROR_EXTENSION_DATE_SUBMITTED);
        }

        @Test
        void shouldReturnErrorIfAfterDeadline() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .build().toBuilder()
                .nextDeadline(LocalDate.now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            String error =
                "You can no longer request an \"Inform agreed 28 day extension\" as the deadline has passed.";
            assertThat(response.getErrors()).containsOnly(error);
        }

        @Test
        void shouldReturnErrorWhenRespondentRespondsAgain2v1() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addApplicant2(YES)
                .respondent1TimeExtensionDate(timeExtensionDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly(ERROR_EXTENSION_DATE_SUBMITTED);
        }

        @Test
        void shouldReturnErrorWhenRespondentRespondsAgain1v2SameSolicitor() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .respondent1TimeExtensionDate(timeExtensionDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly(ERROR_EXTENSION_DATE_SUBMITTED);
        }

        @Test
        void shouldReturnErrorWhenRespondentRespondsAgain1v2Respondent1() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1TimeExtensionDate(timeExtensionDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly(ERROR_EXTENSION_DATE_SUBMITTED);
        }

        @Test
        void shouldReturnErrorWhenRespondentRespondsAgain1v2Respondent2() {
            // Given
            LocalDateTime timeExtensionDate = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2TimeExtensionDate(timeExtensionDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).containsOnly(ERROR_EXTENSION_DATE_SUBMITTED);
        }
    }

    @Nested
    class ExtensionValidation {

        private static final String PAGE_ID = "extension-date";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors())
                .containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(RESPONSE_DEADLINE.toLocalDate().plusDays(14))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrorLRSpec_whenValuesAreValid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(RESPONSE_DEADLINE.toLocalDate().plusDays(14))
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent(InformAgreedExtensionDateCallbackHandler
                                                       .SPEC_ACKNOWLEDGEMENT_OF_SERVICE)
                                     .build())
                .build();
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalidMultiparty() {
            // Given
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            LocalDate extensionDateRespondent2 = now().minusDays(2);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(now().minusDays(1))
                .respondentSolicitor2AgreedDeadlineExtension(extensionDateRespondent2)
                .respondent2SameLegalRepresentative(NO)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors())
                .containsOnly("The agreed extension date must be a date in the future");
        }

    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate extensionDateRespondent1;
        LocalDate extensionDateRespondent2;

        @BeforeEach
        void setup() {
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(timeExtensionDate);

            extensionDateRespondent1 = now().plusDays(14);
            extensionDateRespondent2 = now().plusDays(16);
            when(deadlinesCalculator.calculateFirstWorkingDay(extensionDateRespondent1))
                .thenReturn(extensionDateRespondent1);
            when(deadlinesCalculator.calculateFirstWorkingDay(extensionDateRespondent2))
                .thenReturn(extensionDateRespondent2);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldUpdateRespondent1ResponseDeadlineToExtensionDate_whenRepresentingRespondent1() {
            // Given
            LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();
            when(deadlinesCalculator.nextDeadline(any())).thenReturn(nextDeadline);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .addRespondent2(NO)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

            // Then
            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(INFORM_AGREED_EXTENSION_DATE.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .extracting("nextDeadline")
                .isEqualTo(nextDeadline.toLocalDate().toString());
        }

        @Test
        void shouldUpdateRespondent2ResponseDeadlineToExtensionDate_whenRepresentingRespondent2() {
            // Given
            LocalDateTime nextDeadline = LocalDateTime.now().plusDays(7);
            when(deadlinesCalculator.nextDeadline(any())).thenReturn(nextDeadline);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondentSolicitor2AgreedDeadlineExtension(extensionDateRespondent2)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent2.atTime(END_OF_BUSINESS_DAY);

            // Then
            assertThat(response.getData())
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("nextDeadline")
                .isEqualTo(nextDeadline.toLocalDate().toString());
        }

        @Test
        void shouldUpdateBothRespondentResponseDeadlinesToExtensionDate_whenSolicitorRepresentingBothRespondents() {
            // Given
            LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

            // Then
            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .extracting("nextDeadline")
                .isEqualTo(nextDeadline.toLocalDate().toString());
        }

        @Nested
        class NextDeadline {
            @Test
            void oneVOne_TwoVOne_shouldReturnCorrectNextDeadline() {
                // Given
                LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();
                when(deadlinesCalculator.nextDeadline(any())).thenReturn(nextDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                    .addRespondent2(NO)
                    .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("nextDeadline")
                    .isEqualTo(nextDeadline.toLocalDate().toString());
            }

            @Test
            void oneVTwoSameSolicitor_shouldReturnCorrectNextDeadline() {
                // Given
                LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();

                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(YES)
                    .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("nextDeadline")
                    .isEqualTo(nextDeadline.toLocalDate().toString());
            }

            @Test
            void oneVTwoDifferentSolicitor_shouldReturnCorrectNextDeadline() {
                // Given
                LocalDateTime nextDeadline = LocalDateTime.now().plusDays(7);
                when(deadlinesCalculator.nextDeadline(any())).thenReturn(nextDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2Represented(YES)
                    .respondent2OrgRegistered(YES)
                    .respondentSolicitor2AgreedDeadlineExtension(extensionDateRespondent2)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                LocalDateTime newDeadline = extensionDateRespondent2.atTime(END_OF_BUSINESS_DAY);

                // Then
                assertThat(response.getData())
                    .extracting("nextDeadline")
                    .isEqualTo(nextDeadline.toLocalDate().toString());
            }
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String BODY = "<br />You must respond to the claimant by %s";
        private static final String BODY_SPEC =
            "<h2 class=\"govuk-heading-m\">What happens next</h2>You need to respond before %s";

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            // Given
            LocalDateTime responseDeadline = now().atTime(END_OF_BUSINESS_DAY);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Extension deadline submitted")
                    .confirmationBody(format(BODY, formatLocalDateTime(responseDeadline, DATE_TIME_AT))
                                          + exitSurveyContentService.respondentSurvey())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponseLRSpec_whenInvoked() {
            // Given
            LocalDateTime responseDeadline = now().atTime(END_OF_BUSINESS_DAY);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ResponseDeadline(responseDeadline)
                .caseAccessCategory(SPEC_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Extension deadline submitted")
                    .confirmationBody(format(BODY_SPEC, formatLocalDateTime(responseDeadline, DATE_TIME_AT))
                                          + exitSurveyContentService.respondentSurvey())
                    .build());
        }
    }
}
