package uk.gov.hmcts.reform.civil.controllers.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

public class DocumentControllerTest extends BaseIntegrationTest {

    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String BASE_URL = "/case/document";
    private static final String GENERATE_DOC_URL = BASE_URL + "/generateSealedDoc";
    private static final String DOWNLOAD_FILE_URL = BASE_URL + "/downloadSealedDoc";

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ClaimFormService claimFormService;

    @Test
    void shouldDownloadDocumentFromDocumentManagement() throws Exception {
        // given
        CaseDocument caseDocument = getCaseDocument();
        byte[] expectedByteArray = new ByteArrayResource("test".getBytes()).getByteArray();
        when(claimFormService.downloadSealedDocument(eq(caseDocument))).thenReturn(expectedByteArray);

        //then
        doPost(BEARER_TOKEN, caseDocument, DOWNLOAD_FILE_URL)
            .andExpect(content().bytes(expectedByteArray))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnExpectedGeneratedDocument() throws Exception {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference(REFERENCE_NUMBER)
            .build();

        CaseDocument caseDocument = getCaseDocument();

        when(claimFormService.uploadSealedDocument(any(), any()))
            .thenReturn(caseDocument);

        doPost(BEARER_TOKEN, caseData, GENERATE_DOC_URL)
            .andExpect(content().json(toJson(caseDocument)))
            .andExpect(status().isOk());
    }

    private CaseDocument getCaseDocument() throws JsonProcessingException {
        return mapper.readValue(
            readString("document-management/download.document.json"),
            CaseDocument.class
        );
    }

}
