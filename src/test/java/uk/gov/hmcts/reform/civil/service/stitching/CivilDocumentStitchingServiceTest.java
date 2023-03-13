package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
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
public class CivilDocumentStitchingServiceTest {
    @Autowired
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private BundleRequestExecutor bundleRequestExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();
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
    private final Optional<String> stitchStatus = Optional.of(randomAlphabetic(8));
    private final Optional<Document> stitchedDocument = Optional.of(mock(Document.class));

    private List<DocumentMetaData> buildDocumentMetaDataList() {
        return List.of(
            new DocumentMetaData(
                CLAIM_FORM.getDocumentLink(),
                "Sealed Claim Form",
                LocalDate.now().toString()
            )
        );
    }
    @Test
    public void whenCaseDataIsNull() {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                  "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
    public void whenCaseDataIsNullAndPayloadNotNull() {
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
    public void whenCaseDataIsNullAndStitchingDocumentNotPresent() {
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        List<IdValue<Bundle>> caseBundles = List.of(new IdValue<>("1", mock(Bundle.class)));
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        CaseData caseData1 = CaseDataBuilder.builder().caseBundles(caseBundles).build();
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        when(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).thenReturn(caseData1);
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNull(caseDocument);
    }
    @Test
    public void whenStitchingDocumentIsPresentThenCaseDataIsNull() {
        List<DocumentMetaData> documentMetaDataList = buildDocumentMetaDataList();
        Bundle bundle = new Bundle(
            "Id1",
            "BundleTitle",
            "BundleDescription",
            "Yes",
            new ArrayList<>(),
            stitchStatus,
            stitchedDocument,
            YesOrNo.YES,
            YesOrNo.NO,
            "BundleFilename");
        List<IdValue<Bundle>> caseBundles = List.of(new IdValue<>("1", bundle));
        CaseData caseData = CaseDataBuilder.builder().legacyCaseReference("ClaimNumber").build();
        CaseData caseData1 = CaseDataBuilder.builder().caseBundles(caseBundles).build();
        when(stitchingConfiguration.getStitchingUrl()).thenReturn("dummy_url");
        when(bundleRequestExecutor.post(any(BundleRequest.class), anyString(), anyString())).thenReturn(caseData1);
        CaseDocument caseDocument = civilDocumentStitchingService.bundle(documentMetaDataList, "Auth",
                                                                         "Title", "FileName", caseData);
        Assertions.assertNotNull(caseDocument);
    }
}
