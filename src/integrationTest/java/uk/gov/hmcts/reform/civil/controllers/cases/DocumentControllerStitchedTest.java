package uk.gov.hmcts.reform.civil.controllers.cases;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.GenerateClaimFormForSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentUtil;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = {
    ClaimFormService.class,
    JacksonAutoConfiguration.class})
public class DocumentControllerStitchedTest extends BaseIntegrationTest {

    @MockBean
    private SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @MockBean
    private GenerateClaimFormForSpecCallbackHandler generateClaimFormForSpecCallbackHandler;

    @MockBean
    private DocumentUtil documentUtil;

    @Autowired
    private ClaimFormService claimFormService;

    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;

    @Test
    void shouldReturn_uploadedSealedClaimForm() {
        CaseData caseData = CaseData.builder()
            .build();
        CaseDocument sealClaimForm = Mockito.mock(CaseDocument.class);
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        Mockito.when(sealedClaimFormGeneratorForSpec.generate(caseData, BEARER_TOKEN)).thenReturn(sealClaimForm);
        Mockito.when(documentUtil.fetchDocumentsFromCaseData(caseData, sealClaimForm))
            .thenReturn(documentMetaDataList);

        Assertions.assertEquals(sealClaimForm, claimFormService.uploadSealedDocument(BEARER_TOKEN, caseData));
    }

    @Test
    void shouldReturnFirstScenerio_uploadedSealedClaimForm() {
        CaseData caseData = CaseData.builder().build();
        CaseDocument sealClaimForm = Mockito.mock(CaseDocument.class);
        CaseDocument stitchedDocument = Mockito.mock(CaseDocument.class);

        List<DocumentMetaData> documentMetaDataList = List.of(Mockito.mock(DocumentMetaData.class),
                                                              Mockito.mock(DocumentMetaData.class));
        Mockito.when(sealedClaimFormGeneratorForSpec.generate(caseData, BEARER_TOKEN)).thenReturn(sealClaimForm);
        Mockito.when(documentUtil.fetchDocumentsFromCaseData(caseData, sealClaimForm))
            .thenReturn(documentMetaDataList);

        stitchedDocument.setError(List.of("Error"));
        Mockito.when(civilDocumentStitchingService.bundle(documentMetaDataList, BEARER_TOKEN, null,
                                                          null, caseData)).thenReturn(sealClaimForm);

        Assertions.assertEquals(sealClaimForm, claimFormService.uploadSealedDocument(BEARER_TOKEN, caseData));
    }

    @Test
    void shouldReturnSecondScenerio_uploadedSealedClaimForm() {
        CaseData caseData = CaseData.builder().build();
        CaseDocument sealClaimForm = Mockito.mock(CaseDocument.class);
        CaseDocument stitchedDocument = Mockito.mock(CaseDocument.class);

        List<DocumentMetaData> documentMetaDataList = List.of(Mockito.mock(DocumentMetaData.class),
                                                              Mockito.mock(DocumentMetaData.class));
        Mockito.when(sealedClaimFormGeneratorForSpec.generate(caseData, BEARER_TOKEN)).thenReturn(sealClaimForm);
        Mockito.when(documentUtil.fetchDocumentsFromCaseData(caseData, sealClaimForm))
            .thenReturn(documentMetaDataList);
        stitchedDocument.setError(List.of(""));
        Mockito.when(civilDocumentStitchingService.bundle(documentMetaDataList, BEARER_TOKEN, null,
                                                          null, caseData)).thenReturn(stitchedDocument);

        Assertions.assertEquals(sealClaimForm, claimFormService.uploadSealedDocument(BEARER_TOKEN, caseData));
    }
}
