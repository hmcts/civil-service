package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.unspec.model.dq.Hearing;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private RespondToDefenceCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateRespondedToClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventCallbackValidateUnavailableDates {

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder().date(
                                                                   LocalDate.now().plusYears(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder().date(
                                                                   LocalDate.now().minusYears(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder().date(
                                                                   LocalDate.now().plusDays(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder().applicant1DQHearing(Hearing.builder().build()).build()).build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceState(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've decided to proceed with the claim%n## Claim number: 000LR001"))
                    .confirmationBody(format(
                        "<br />We'll review the case. We'll contact you to tell you what to do next.%n%n"
                            + "[Download directions questionnaire](http://www.google.com)"))
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsNotProceedingWithClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You have chosen not to proceed with the claim%n## Claim number:"
                                                   + " 000LR001"))
                    .confirmationBody(format("<br />If you do want to proceed you need to do it within: %n%n"
                                                 + "- 14 days if the claim is allocated to a small claims track%n"
                                                 + "- 28 days if the claim is allocated to a fast or multi track%n%n"
                                                 + "The case will be stayed if you do not proceed within the allowed"
                                                 + " timescale."))
                    .build());
        }
    }
}
