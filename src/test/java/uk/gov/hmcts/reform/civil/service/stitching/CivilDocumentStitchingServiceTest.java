package uk.gov.hmcts.reform.civil.service.stitching;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CivilDocumentStitchingService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class CivilDocumentStitchingServiceTest {
    @Autowired
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private BundleRequestExecutor bundleRequestExecutor;
    @MockBean
    private StitchingConfiguration stitchingConfiguration;
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
    private List<DocumentMetaData> buildDocumentMetaDataList() {
        return List.of(
            new DocumentMetaData(
                CLAIM_FORM.getDocumentLink(),
                "Sealed Claim Form",
                LocalDate.now().toString()
            )
        );
    }

    private CaseData buildCaseData(Bundle bundle){
        List<IdValue<Bundle>> caseBundles = List.of(new IdValue<>("1", bundle));
        return CaseDataBuilder.builder().caseBundles(caseBundles).build();
    }
    @Test
     void whenCaseDataIsNullWithPayloadAndNoCaseBundlesNoStitchingDocument() {
        //Given: Payload details but case data not provided
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        //Then: the case documents is null
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                  "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
     void whenCaseDataIsNullWithNoCaseBundles() {
        //Given: Payload details and stitching URL provided
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        //when: bundle Request post is called
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        //Then: the case documents is null
         CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
     void whenCaseDataIsNotNullWithCaseBundlesAndNoStitchingDocumentThenCaseDocumentIsNull() {
        //Given: Payload and case data with case bundle details
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        CaseData caseData1 = buildCaseData(mock(Bundle.class));
        //when: bundle Request post is called and case data is retrieved
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        when(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).thenReturn(caseData1);
        //Then: the case documents is null
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
    void whenStitchingDocumentIsPresentAndCaseDataNotNullThenCaseDocumentNotNull() {
        //Given: Payload,case data with case bundle details and stitching document
        Optional<Document> stitchedDocument = Optional.of(mock(Document.class));
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        Bundle bundle = Bundle.builder().stitchedDocument(stitchedDocument).build();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        CaseData caseData1 = buildCaseData(bundle);
        //when: bundle Request post is called and case data is retrieved with stitching document
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        when(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).thenReturn(caseData1);
        //Then: Case Document is retrieved
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNotNull(caseDocument);
    }
}
