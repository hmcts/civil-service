package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.dq.Hearing;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.CLAIMANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    BusinessProcessService.class
})
class RespondToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateServiceAcknowledge().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventConfirmDetailsCallback {

        private static final String PAGE_ID = "confirm-details";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .respondent1(PartyBuilder.builder().individual()
                                 .soleTraderDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateUnavailableDates {

        private static final String PAGE_ID = "validate-unavailable-dates";

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            Hearing hearing = Hearing.builder().unavailableDates(wrapElements(
                UnavailableDate.builder()
                    .date(now().plusYears(5))
                    .build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            Hearing hearing = Hearing.builder()
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .date(now().minusYears(5))
                                                   .build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            Hearing hearing = Hearing.builder()
                .unavailableDates(wrapElements(UnavailableDate.builder()
                                                   .date(now().plusDays(5))
                                                   .build()))
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(hearing).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQ(Respondent1DQ.builder().respondent1DQHearing(Hearing.builder().build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetClaimantResponseDeadline_whenInvoked() {
            LocalDateTime claimantResponseDeadline = now().atTime(16, 0);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(
                    "applicantSolicitorResponseDeadlineToRespondentSolicitor1",
                    claimantResponseDeadline.format(ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                );
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(
                caseData,
                ABOUT_TO_SUBMIT
            ));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've submitted your response%n## Claim number: TBC"))
                    .confirmationBody(format(
                        "<br />The claimant has until %s to proceed. "
                            + "We will let you know when they respond.",
                        formatLocalDateTime(CLAIMANT_RESPONSE_DEADLINE, DATE)
                    ))
                    .build());
        }
    }
}
