package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@SpringBootTest(classes = {
    AcknowledgeOfServiceCallbackHandler.class,
    ObjectMapper.class,
    PostcodeValidator.class,
    ExitSurveyContentService.class
})
class AcknowledgeOfServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AcknowledgeOfServiceCallbackHandler handler;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private PostcodeValidator postcodeValidator;
    @MockBean
    private ExitSurveyContentService exitSurveyContentService;
    @MockBean
    private DateOfBirthValidator dateOfBirthValidator;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private Time time;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    public static final String REFERENCE_NUMBER = "000MC001";

    public static final String CONFIRMATION_SUMMARY = "<br />You need to respond to the claim before %s."
        + "%n%n[Download the Acknowledgement of Service form](%s)";

    @BeforeEach
    void prepare() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(ACKNOWLEDGEMENT_OF_SERVICE);
    }

    @Nested
    class AboutToStart {

        @Test
        void populateRespondentCopy1_checkIfDeadlineNotPassed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);
            CallbackRequest request = CallbackRequest.builder()
                .eventId("ACKNOWLEDGEMENT_OF_SERVICE")
                .build();
            params = params.toBuilder().request(request).build();

            CallbackResponse response = handler.handle(params);
            assertThat(((AboutToStartOrSubmitCallbackResponse) response).getErrors()).isEmpty();
        }

        @Test
        void populateRespondentCopy1_checkIfDeadlinePassed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePastResponseDeadline()
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);
            CallbackRequest request = CallbackRequest.builder()
                .eventId("ACKNOWLEDGEMENT_OF_SERVICE")
                .build();
            params = params.toBuilder().request(request).build();

            List<String> errors = Collections.singletonList("Deadline to file Acknowledgement of Service has passed, option is not available.");

            CallbackResponse response = handler.handle(params);
            Assertions.assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
        }

    }

    @Nested
    class Mid {
        @Test
        void midSpecCorrespondenceAddress_checkAddressIfWasIncorrect() {
            String postCode = "postCode";
            CaseData caseData = CaseData.builder()
                .specAoSApplicantCorrespondenceAddressRequired(YesOrNo.NO)
                .specAoSApplicantCorrespondenceAddressdetails(Address.builder()
                                                                  .postCode(postCode)
                                                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
            CallbackRequest request = CallbackRequest.builder()
                .eventId("ACKNOWLEDGEMENT_OF_SERVICE")
                .build();
            params = params.toBuilder().request(request).build();

            List<String> errors = Collections.singletonList("error 1");
            Mockito.when(postcodeValidator.validate(postCode)).thenReturn(errors);

            CallbackResponse response = handler.handle(params);
            Assertions.assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
        }

        @Test
        void midSpecCorrespondenceAddress_checkAddressIfWasIncorrectWithoutEventId() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
            CallbackRequest request = CallbackRequest.builder()
                .eventId("NOT_ACKNOWLEDGEMENT_OF_SERVICE")
                .build();
            params = params.toBuilder().request(request).build();

            CallbackResponse response = handler.handle(params);
            assertThat(((AboutToStartOrSubmitCallbackResponse) response).getErrors()).isNull();
        }

        @Test
        void midConfirmDetails_checkDateOfBirthValidRequiredAddress() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atSpecAoSRespondentCorrespondenceAddressRequired(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "confirm-details");
            CallbackRequest request = CallbackRequest.builder()
                .build();
            params = params.toBuilder().request(request).build();

            List<String> errors = Collections.singletonList("Error 1");
            Mockito.when(dateOfBirthValidator.validate(caseData.getRespondent1())).thenReturn(errors);

            CallbackResponse response = handler.handle(params);
            Assertions.assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
        }

        @Test
        void midConfirmDetails_checkDateOfBirthValidNoRequiredAddress() {
            String postCode = "postCode";
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atSpecAoSRespondentCorrespondenceAddressRequired(YesOrNo.NO)
                .atSpecAoSRespondentCorrespondenceAddressDetails(Address.builder()
                                                                   .postCode(postCode)
                                                                   .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "confirm-details");
            CallbackRequest request = CallbackRequest.builder()
                .build();
            params = params.toBuilder().request(request).build();

            List<String> errors = Collections.singletonList("Error 1");
            Mockito.when(dateOfBirthValidator.validate(caseData.getRespondent1())).thenReturn(errors);

            List<String> errors2 = Collections.singletonList("Error 2");
            Mockito.when(postcodeValidator.validate(postCode)).thenReturn(errors2);

            CallbackResponse response = handler.handle(params);
            Assertions.assertEquals(errors2, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
        }

    }

    @Nested
    class AboutToSubmit {

        @Test
        void aboutToSubmit_NewResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1Copy(Party.builder().partyName("Party 2").primaryAddress(
                                                                        Address
                                                                            .builder()
                                                                            .addressLine1("Triple street")
                                                                            .postCode("Postcode")
                                                                            .build())
                                     .build())
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(10))
                .build();

            Address address = Address.builder()
                .postCode("E11 5BB")
                .build();

            CaseData oldCaseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .build();

            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(oldCaseData);

            String newDeadline = LocalDateTime.now().plusDays(28).withHour(16).withMinute(0).withSecond(1).truncatedTo(
                ChronoUnit.SECONDS).toString();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class)))
                .thenReturn(LocalDateTime.parse(newDeadline));

            CallbackResponse response = handler.handle(params);

            assertThat(((AboutToStartOrSubmitCallbackResponse) response)
                           .getData().get("respondent1ResponseDeadline"))
                .hasToString(newDeadline);
        }

        @Test
        void aboutToSubmit_NewResponseDeadline1v2() {
            // Given: Create current case data with two respondent copies and an initial response deadline
            LocalDateTime initialDeadline = LocalDateTime.now().plusDays(10);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1Copy(Party.builder().partyName("Party 2").primaryAddress(
                        Address
                            .builder()
                            .addressLine1("Triple street")
                            .postCode("Postcode")
                            .build())
                                     .build())
                .respondent2Copy(Party.builder().partyName("Respondent 2").primaryAddress(
                        Address
                            .builder()
                            .addressLine1("Triple street")
                            .postCode("Postcode")
                            .build())
                                     .build())
                .respondent1ResponseDeadline(initialDeadline)
                .build();

            Address address = Address.builder()
                .postCode("E11 5BB")
                .build();
            CaseData oldCaseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .build();
            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(oldCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            Mockito.when(deadlinesCalculator.plus14DaysAt4pmDeadline(caseData.getRespondent1ResponseDeadline()))
                .thenReturn(caseData.getRespondent1ResponseDeadline()
                                .plusDays(14)
                                .withHour(16)
                                .withMinute(0)
                                .withSecond(1));

            String newDeadline = LocalDateTime.now().plusDays(28).withHour(16).withMinute(0).withSecond(1).truncatedTo(ChronoUnit.SECONDS).toString();

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class)))
                .thenReturn(LocalDateTime.parse(newDeadline));

            CallbackResponse response = handler.handle(params);

            assertThat(((AboutToStartOrSubmitCallbackResponse) response)
                           .getData().get("respondent1ResponseDeadline"))
                .hasToString(newDeadline);
        }

    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .legacyCaseReference("000MC001")
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String body = format(
                CONFIRMATION_SUMMARY,
                formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT),
                format("/cases/case-details/%s#CaseDocuments", CASE_ID)
            ) + exitSurveyContentService.respondentSurvey();

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# You have acknowledged the claim%n## Claim number: %s",
                        REFERENCE_NUMBER
                    ))
                    .confirmationBody(body)
                    .build());
        }

    }

}
