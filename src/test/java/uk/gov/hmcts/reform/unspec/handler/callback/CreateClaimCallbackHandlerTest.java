package uk.gov.hmcts.reform.unspec.handler.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.unspec.model.documents.Document;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.IssueDateCalculator;
import uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.unspec.utils.ResourceReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.ClaimType.PERSONAL_INJURY_WORK;
import static uk.gov.hmcts.reform.unspec.handler.callback.CreateClaimCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService.UNSPEC;

@SpringBootTest(classes = {
    CreateClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimIssueConfiguration.class,
    MockDatabaseConfiguration.class},
    properties = {"reference.database.enabled=false"})
class CreateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000LR001";
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
        when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(getCaseDocument());
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
            data.put("claimType", PERSONAL_INJURY_WORK);
            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData())
                .isEqualTo(
                    Map.of(
                        "claimValue", ClaimValue.builder()
                            .higherValue(BigDecimal.valueOf(10)).lowerValue(BigDecimal.valueOf(1)).build(),
                        "claimType", PERSONAL_INJURY_WORK,
                        "allocatedTrack", SMALL_CLAIM
                    ));
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldAddClaimIssuedDateAndSubmittedAt_whenInvoked() throws JsonProcessingException {
            when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(LocalDate.now());
            when(deadlinesCalculator.calculateConfirmationOfServiceDeadline(any(LocalDate.class)))
                .thenReturn(LocalDate.now().atTime(23, 59, 59));
            CallbackParams params = callbackParamsOf(getCaseData(), CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsEntry("claimIssuedDate", LocalDate.now());
            assertThat(response.getData()).containsEntry("legacyCaseReference", REFERENCE_NUMBER);
            assertThat(response.getData()).containsEntry(
                "confirmationOfServiceDeadline",
                LocalDate.now().atTime(23, 59, 59)
            );
            assertThat(response.getData()).containsKey("claimSubmittedDateTime");
        }

        @Test
        void shouldIssueClaimWithSystemGeneratedDocumentsAndDate_whenInvoked() throws JsonProcessingException {
            when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(LocalDate.now());
            when(deadlinesCalculator.calculateConfirmationOfServiceDeadline(any(LocalDate.class)))
                .thenReturn(LocalDate.now().atTime(23, 59, 59));

            CallbackParams params = callbackParamsOf(getCaseData(), CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData caseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(caseData.getSystemGeneratedCaseDocuments()).isNotEmpty()
                .contains(Element.<CaseDocument>builder().value(getCaseDocument()).build());

            assertThat(caseData.getClaimIssuedDate()).isEqualTo(LocalDate.now());
        }

        Map<String, Object> getCaseData() throws JsonProcessingException {
            Map<String, Object> caseData = objectMapper.readValue(
                ResourceReader.readString("case_data.json"),
                new TypeReference<>() {
                }
            );
            caseData.remove("systemGeneratedCaseDocuments");

            return caseData;
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

            LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
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

            LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
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

    private CaseDocument getCaseDocument() {
        String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);

        return CaseDocument.builder()
            .documentLink(Document.builder()
                              .documentFileName(fileName)
                              .documentBinaryUrl(
                                  "http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f/binary")
                              .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f")
                              .build())
            .documentSize(56975)
            .createdDatetime(LocalDateTime.of(2020, 7, 16, 14, 5, 15, 550439))
            .documentType(SEALED_CLAIM)
            .createdBy(UNSPEC)
            .documentName(fileName)
            .build();
    }
}
