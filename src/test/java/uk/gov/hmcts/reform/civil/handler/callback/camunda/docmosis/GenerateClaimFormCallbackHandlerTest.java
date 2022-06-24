package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateClaimFormCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GenerateClaimFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;
    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    @MockBean
    private SealedClaimFormGenerator sealedClaimFormGenerator;

    @Autowired
    private GenerateClaimFormCallbackHandler handler;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final CaseDocument CLAIM_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument LIP_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(LITIGANT_IN_PERSON_CLAIM_FORM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument STITCHED_DOC =
        CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private final List<DocumentMetaData> documents = Arrays.asList(
        new DocumentMetaData(
            CLAIM_FORM.getDocumentLink(),
            "Sealed Claim Form",
            LocalDate.now().toString()
        ),
        new DocumentMetaData(
            LIP_FORM.getDocumentLink(),
            "Litigant in person claim form",
            LocalDate.now().toString()
        ));

    private final LocalDate issueDate = now();

    @BeforeEach
    void setup() {
        when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGenerateDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);
        }

        @Test
        void shouldStitchClaimFormWithLipForm_andUploadToCaseDocuments_whenOneVsOne_withLitigantInPerson() {
            when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(),
                                                      any(CaseData.class))).thenReturn(STITCHED_DOC);

            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(documents), anyString(), anyString(), anyString(),
                                                         eq(caseData));
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateClaimForm");
        }
    }
}
