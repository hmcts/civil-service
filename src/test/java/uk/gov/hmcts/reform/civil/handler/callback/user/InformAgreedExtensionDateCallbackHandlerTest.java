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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
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
    CaseDetailsConverter.class,
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
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_whenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToStartV1Callback {

        @BeforeEach
        void setup() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenOneRespondentRepresentative() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("Yes");
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenTwoRespondentRepresentativesWithNoRespondent2CaseRole() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent2SameLegalRepresentative(NO)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("Yes");
        }

        @Test
        void shouldSetRespondent1FlagToNo_whenTwoRespondentRepresentativesWithRespondent2CaseRole() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .build();
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("No");
        }
    }

    @Nested
    class ExtensionValidation {

        private static final String PAGE_ID = "extension-date";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(RESPONSE_DEADLINE.toLocalDate().plusDays(14))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate extensionDate;

        @BeforeEach
        void setup() {
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(timeExtensionDate);

            extensionDate = now().plusDays(14);
            when(deadlinesCalculator.calculateFirstWorkingDay(extensionDate)).thenReturn(extensionDate);
        }

        @Test
        void shouldUpdateResponseDeadlineToExtensionDate_whenInvoked() {
            LocalDateTime responseDeadline = now().atTime(MIDNIGHT);

            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
                .respondent1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDate.atTime(END_OF_BUSINESS_DAY);

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
        }
    }

    @Nested
    class AboutToSubmitV1Callback {
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
        }

        @Test
        void shouldUpdateRespondent1ResponseDeadlineToExtensionDate_whenRepresentingRespondent1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .addRespondent2(NO)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

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
        }

        @Test
        void shouldUpdateRespondent2ResponseDeadlineToExtensionDate_whenRepresentingRespondent2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondentSolicitor2AgreedDeadlineExtension(extensionDateRespondent2)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent2.atTime(END_OF_BUSINESS_DAY);

            assertThat(response.getData())
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(INFORM_AGREED_EXTENSION_DATE.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String BODY = "<br />You must respond to the claimant by %s";

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            LocalDateTime responseDeadline = now().atTime(END_OF_BUSINESS_DAY);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Extension deadline submitted")
                    .confirmationBody(format(BODY, formatLocalDateTime(responseDeadline, DATE_TIME_AT))
                                          + exitSurveyContentService.respondentSurvey())
                    .build());
        }
    }
}
