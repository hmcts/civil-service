package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class SdoGeneratorServiceTest {

    private static final String BEARER_TOKEN = "BEARER";

    @InjectMocks
    private SdoGeneratorService service;

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;

    @Test
    public void sdoSmall() {
        DocmosisTemplates expectedTemplate = DocmosisTemplates.SDO_SMALL;
        Class<? extends SdoDocumentForm> expectedFormClass = SdoDocumentFormSmall.class;

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .build();

        DocmosisDocument document = Mockito.mock(DocmosisDocument.class);
        Mockito.when(documentGeneratorService.generateDocmosisDocument(
            ArgumentMatchers.any(expectedFormClass),
            ArgumentMatchers.eq(expectedTemplate)
        )).thenReturn(document);
        byte[] content = "content".getBytes();
        Mockito.when(document.getBytes()).thenReturn(content);

        CaseDocument caseDocument = Mockito.mock(CaseDocument.class);
        Mockito.when(documentManagementService.uploadDocument(
            ArgumentMatchers.eq(BEARER_TOKEN),
            ArgumentMatchers.argThat(pdf ->
                                         String.format(
                                             expectedTemplate.getDocumentTitle(),
                                             caseData.getLegacyCaseReference()
                                         ).equals(pdf.getFileBaseName())
                                             && Arrays.equals(content, pdf.getBytes())
            )
        )).thenReturn(caseDocument);

        CaseDocument actual = service.generate(caseData, BEARER_TOKEN);
        Assertions.assertEquals(caseDocument, actual);
    }

    @Test
    public void sdoFast() {
        DocmosisTemplates expectedTemplate = DocmosisTemplates.SDO_FAST;
        Class<? extends SdoDocumentForm> expectedFormClass = SdoDocumentFormFast.class;

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .build();

        DocmosisDocument document = Mockito.mock(DocmosisDocument.class);
        Mockito.when(documentGeneratorService.generateDocmosisDocument(
            ArgumentMatchers.any(expectedFormClass),
            ArgumentMatchers.eq(expectedTemplate)
        )).thenReturn(document);
        byte[] content = "content".getBytes();
        Mockito.when(document.getBytes()).thenReturn(content);

        CaseDocument caseDocument = Mockito.mock(CaseDocument.class);
        Mockito.when(documentManagementService.uploadDocument(
            ArgumentMatchers.eq(BEARER_TOKEN),
            ArgumentMatchers.argThat(pdf ->
                                         String.format(
                                             expectedTemplate.getDocumentTitle(),
                                             caseData.getLegacyCaseReference()
                                         ).equals(pdf.getFileBaseName())
                                             && Arrays.equals(content, pdf.getBytes())
            )
        )).thenReturn(caseDocument);

        CaseDocument actual = service.generate(caseData, BEARER_TOKEN);
        Assertions.assertEquals(caseDocument, actual);
    }

    @Test
    public void sdoDisposal() {
        DocmosisTemplates expectedTemplate = DocmosisTemplates.DISPOSAL_DIRECTIONS;
        Class<? extends SdoDocumentForm> expectedFormClass = SdoDocumentFormDisposal.class;

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .hearingSelection("DISPOSAL_HEARING")
            .build();

        DocmosisDocument document = Mockito.mock(DocmosisDocument.class);
        Mockito.when(documentGeneratorService.generateDocmosisDocument(
            ArgumentMatchers.any(expectedFormClass),
            ArgumentMatchers.eq(expectedTemplate)
        )).thenReturn(document);
        byte[] content = "content".getBytes();
        Mockito.when(document.getBytes()).thenReturn(content);

        CaseDocument caseDocument = Mockito.mock(CaseDocument.class);
        Mockito.when(documentManagementService.uploadDocument(
            ArgumentMatchers.eq(BEARER_TOKEN),
            ArgumentMatchers.argThat(pdf ->
                                         String.format(
                                             expectedTemplate.getDocumentTitle(),
                                             caseData.getLegacyCaseReference()
                                         ).equals(pdf.getFileBaseName())
                                             && Arrays.equals(content, pdf.getBytes())
            )
        )).thenReturn(caseDocument);

        CaseDocument actual = service.generate(caseData, BEARER_TOKEN);
        Assertions.assertEquals(caseDocument, actual);
    }
}
