package uk.gov.hmcts.reform.unspec.handler.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.unspec.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.IssueDateCalculator;
import uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID_SECONDARY;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.ClaimType.PERSONAL_INJURY;
import static uk.gov.hmcts.reform.unspec.handler.callback.CreateClaimCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.unspec.utils.PartyNameUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    CreateClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimIssueConfiguration.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class},
    properties = {"reference.database.enabled=false"})
class CreateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000LR001";
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(format(N1.getDocumentTitle(), REFERENCE_NUMBER))
        .documentType(SEALED_CLAIM)
        .build();

    @MockBean
    private SealedClaimFormGenerator sealedClaimFormGenerator;
    @MockBean
    IssueDateCalculator issueDateCalculator;
    @MockBean
    DeadlinesCalculator deadlinesCalculator;

    @Autowired
    private CreateClaimCallbackHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${unspecified.response-pack-url}")
    private String responsePackLink;

    @BeforeEach
    public void setup() {
        when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CASE_DOCUMENT);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventCallback {

        @Test
        void shouldReturnExpectedErrorInMidEvent_whenValuesAreInvalid() {
            Map<String, Object> data = new HashMap<>();
            data.put("claimValue", ClaimValue.builder()
                .higherValue(BigDecimal.valueOf(1)).lowerValue(BigDecimal.valueOf(10)).build());

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsOnly("CONTENT TBC: Higher value must not be lower than the lower value.");
        }

        @Test
        void shouldReturnNoErrorInMidEvent_whenValuesAreValid() {
            Map<String, Object> data = new HashMap<>();
            data.put("claimValue", ClaimValue.builder()
                .higherValue(BigDecimal.valueOf(10)).lowerValue(BigDecimal.valueOf(1)).build());
            data.put("claimType", PERSONAL_INJURY);
            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData())
                .isEqualTo(
                    Map.of(
                        "claimValue", ClaimValue.builder()
                            .higherValue(BigDecimal.valueOf(10)).lowerValue(BigDecimal.valueOf(1)).build(),
                        "claimType", PERSONAL_INJURY,
                        "allocatedTrack", SMALL_CLAIM
                    ));
        }
    }

    @Nested
    class MidSecondaryEventCallback {

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnError_whenDateOfBirthIsInTheFuture(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("applicant1", Map.of(dateOfBirthField, now().plusDays(1)));

            CallbackParams params = callbackParamsOf(data, MID_SECONDARY);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnNoError_whenDateOfBirthIsInThePast(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("applicant1", Map.of(dateOfBirthField, now().minusDays(1)));

            CallbackParams params = callbackParamsOf(data, MID_SECONDARY);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;

        @BeforeEach
        void setup() {
            when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(now());
            when(deadlinesCalculator.calculateConfirmationOfServiceDeadline(any(LocalDate.class)))
                .thenReturn(now().atTime(23, 59, 59));
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(convertToMap(caseData), CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldAddClaimIssuedDateAndSubmittedAt_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry("claimIssuedDate", now());
            assertThat(response.getData()).containsEntry("legacyCaseReference", REFERENCE_NUMBER);
            assertThat(response.getData()).containsEntry(
                "confirmationOfServiceDeadline",
                now().atTime(23, 59, 59)
            );
            assertThat(response.getData()).containsKey("claimSubmittedDateTime");
        }

        @Test
        void shouldIssueClaimWithSystemGeneratedDocumentsAndDate_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseData.getSystemGeneratedCaseDocuments()).isNotEmpty()
                .contains(Element.<CaseDocument>builder().value(CASE_DOCUMENT).build());

            assertThat(responseData.getClaimIssuedDate()).isEqualTo(now());
        }

        @Test
        void shouldUpdateRespondentAndApplicantWithPartyNameAndPartyTypeDisplayValue_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("respondent1").extracting("partyName").isEqualTo(
                getPartyNameBasedOnType(caseData.getRespondent1()));
            assertThat(response.getData()).extracting("respondent1").extracting("partyTypeDisplayValue").isEqualTo(
                caseData.getRespondent1().getType().getDisplayValue());
            assertThat(response.getData()).extracting("applicant1").extracting("partyName").isEqualTo(
                getPartyNameBasedOnType(caseData.getApplicant1()));
            assertThat(response.getData()).extracting("applicant1").extracting("partyTypeDisplayValue").isEqualTo(
                caseData.getApplicant1().getType().getDisplayValue());
        }

        @Test
        void shouldSetClaimIssueBusinessProcessToReady_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //TODO: uncomment when CMC-794 is played
            //assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(READY);
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isEqualTo(
                "ClaimIssueHandling");
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isNull();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> convertToMap(CaseData caseData) {
            return (Map<String, Object>) objectMapper.convertValue(caseData, Map.class);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponseObject_whenDocumentIsGenerated() {
            Map<String, Object> data = new HashMap<>();
            int documentSize = 125952;
            Element<CaseDocument> documents = Element.<CaseDocument>builder()
                .value(CaseDocument.builder().documentSize(documentSize).documentType(SEALED_CLAIM).build())
                .build();
            data.put("systemGeneratedCaseDocuments", List.of(documents));
            data.put("legacyCaseReference", REFERENCE_NUMBER);
            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);
            String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

            String body = format(
                CONFIRMATION_SUMMARY,
                format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                documentSize / 1024,
                responsePackLink,
                formattedServiceDeadline
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Your claim has been issued%n## Claim number: %s", REFERENCE_NUMBER))
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponseObject_whenDocumentIsNotGenerated() {
            Map<String, Object> data = new HashMap<>();
            data.put("legacyCaseReference", REFERENCE_NUMBER);
            int documentSize = 0;
            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime serviceDeadline = now().plusDays(112).atTime(23, 59);
            String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

            String body = format(
                CONFIRMATION_SUMMARY,
                format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                documentSize / 1024,
                responsePackLink,
                formattedServiceDeadline
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Your claim has been issued%n## Claim number: %s", REFERENCE_NUMBER))
                    .confirmationBody(body)
                    .build());
        }
    }
}
