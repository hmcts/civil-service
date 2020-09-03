package uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.Document;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.ResourceReader;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService.UNSPEC;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SealedClaimFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class SealedClaimFormGeneratorTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    public static final String REFERENCE_NUMBER = "000LR001";
    private final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private final String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private SealedClaimFormGenerator sealedClaimFormGenerator;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateSealedClaimForm_whenValidDataIsProvided() throws JsonProcessingException {
        CaseData caseData = getCaseData().toBuilder().claimSubmittedDateTime(LocalDateTime.now()).build();

        when(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), eq(N1)))
            .thenReturn(new DocmosisDocument(N1.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(eq(BEARER_TOKEN), eq(new PDF(fileName, bytes, SEALED_CLAIM))))
            .thenReturn(getCaseDocument());

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(getCaseDocument());

        verify(documentManagementService).uploadDocument(eq(BEARER_TOKEN), eq(new PDF(fileName, bytes, SEALED_CLAIM)));
        verify(documentGeneratorService).generateDocmosisDocument(any(DocmosisData.class), eq(N1));
    }

    private CaseData getCaseData() throws JsonProcessingException {
        return objectMapper.readValue(ResourceReader.readString("case_data.json"), CaseData.class);
    }

    private CaseDocument getCaseDocument() {

        return CaseDocument.builder()
            .documentLink(Document.builder()
                              .documentFileName(fileName)
                              .documentBinaryUrl(
                                  "http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f/binary")
                              .documentUrl("http://dm-store:4506/documents/73526424-8434-4b1f-acca-bd33a3f8338f")
                              .build())
            .documentSize(56975)
            .createdDatetime(LocalDateTime.of(2020, 07, 16, 14, 05, 15, 550439))
            .documentType(SEALED_CLAIM)
            .createdBy(UNSPEC)
            .documentName(fileName)
            .build();
    }
}
