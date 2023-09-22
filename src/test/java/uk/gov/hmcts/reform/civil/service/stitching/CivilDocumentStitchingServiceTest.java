package uk.gov.hmcts.reform.civil.service.stitching;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
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

    CaseData caseData;

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

    private CaseData buildCaseData(Bundle bundle) {
        List<IdValue<Bundle>> caseBundles = List.of(new IdValue<>("1", bundle));
        return CaseDataBuilder.builder().caseBundles(caseBundles).build();
    }

    @BeforeEach
    void sharedSetup() {
        caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        given(stitchingConfiguration.getStitchingUrl()).willReturn("dummy_url");
    }

    @Test
     void whenCaseDataIsNullWithPayloadAndNoCaseBundlesNoStitchingDocument() {
        //Given: Payload details but case data not provided
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        //When: The document stitching service is called
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                  "Title", "FileName", caseData);
        //Then: the case documents is null
        Assertions.assertNull(caseDocument);
    }

    @Test
    void whenCaseDataIsNullWithNoCaseBundles() {
        //Given: Payload details and stitching URL provided
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        //When: bundle Request post is called
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        //Then: the case documents is null
        Assertions.assertNull(caseDocument);
    }

    @Test
     void whenCaseDataIsNotNullWithCaseBundlesAndNoStitchingDocumentThenCaseDocumentIsNull() {
        //Given: Payload and case data with case bundle details
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        CaseData caseData1 = buildCaseData(mock(Bundle.class));
        given(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).willReturn(caseData1);
        //when: bundle Request post is called and case data is retrieved
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        //Then: the case documents is null
        Assertions.assertNull(caseDocument);
    }

    @Test
    void whenStitchingDocumentIsPresentAndRespondent1ResponseDateNotNullInCaseDataThenCaseDocumentNotNull() {
        //Given: Payload,case data with case bundle details and stitching document
        Optional<Document> stitchedDocument = Optional.of(mock(Document.class));
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        Bundle bundle = Bundle.builder().stitchedDocument(stitchedDocument).build();
        CaseData caseData1 = buildCaseData(bundle);
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").respondent1ResponseDate(LocalDateTime.now()).build();
        given(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).willReturn(caseData1);
        //when: bundle Request post is called and case data is retrieved with stitching document
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        //Then: Case Document is retrieved
        Assertions.assertNotNull(caseDocument);
    }

    @Test
    void whenStitchingDocumentIsPresentAndRespondent2ResponseDateNotNullInCaseDataThenCaseDocumentNotNull() {
        //Given: Payload,case data with case bundle details and stitching document
        Optional<Document> stitchedDocument = Optional.of(mock(Document.class));
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        Bundle bundle = Bundle.builder().stitchedDocument(stitchedDocument).build();
        CaseData caseData1 = buildCaseData(bundle);
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").respondent2ResponseDate(LocalDateTime.now()).build();
        given(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).willReturn(caseData1);
        //when: bundle Request post is called and case data is retrieved with stitching document
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        //Then: Case Document is retrieved
        Assertions.assertNotNull(caseDocument);
    }
}
