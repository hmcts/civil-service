package uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim;

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
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SealedClaimFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class SealedClaimFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000LR001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(SEALED_CLAIM)
        .build();

    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private SealedClaimFormGenerator sealedClaimFormGenerator;

    @Test
    void shouldGenerateSealedClaimForm_whenValidDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();

        when(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), eq(N1)))
            .thenReturn(new DocmosisDocument(N1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimForm.class), eq(N1));
    }
}
