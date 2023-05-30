package uk.gov.hmcts.reform.civil.controllers.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

public class DocumentControllerTest extends BaseIntegrationTest {

    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String BASE_URL = "/case/document";
    private static final String GENERATE_DOC_URL = BASE_URL + "/generateSealedDoc";
    private static final String DOWNLOAD_FILE_URL = BASE_URL + "/downloadDocument/{documentId}";
    public static final String DOCUMENT_ID = "documentId";

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ClaimFormService claimFormService;

    @Test
    void shouldDownloadDocumentById() throws Exception {
        // given
        String documentId = DOCUMENT_ID;
        byte[] data = new ByteArrayResource("test".getBytes()).getByteArray();
        Resource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.asMediaType(MimeTypeUtils.APPLICATION_JSON));
        // Create the ResponseEntity
        ResponseEntity<Resource> responseEntityExpected = new ResponseEntity<>(resource, headers, HttpStatus.OK);
        when(claimFormService.downloadDocumentById(eq(documentId))).thenReturn(responseEntityExpected);

        //then
        doGet(BEARER_TOKEN, DOWNLOAD_FILE_URL, documentId)
            .andExpect(content().bytes(data))
            .andExpect(status().isOk());
    }

    @Test
    void shouldNotFoundException() throws Exception {
        // given
        String documentId = DOCUMENT_ID;
        doThrow(FeignException.NotFound.class).when(claimFormService).downloadDocumentById(documentId);
        //then
        doGet(BEARER_TOKEN, DOWNLOAD_FILE_URL, documentId)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUnauthorizedException() throws Exception {
        // given
        String documentId = DOCUMENT_ID;
        doThrow(FeignException.Unauthorized.class).when(claimFormService).downloadDocumentById(documentId);
        //then
        doGet(BEARER_TOKEN, DOWNLOAD_FILE_URL, documentId)
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnExpectedGeneratedDocument() throws Exception {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference(REFERENCE_NUMBER)
            .build();

        CaseDocument caseDocument = getCaseDocument();

        when(claimFormService.uploadSealedDocument(any(), any()))
            .thenReturn(caseDocument);

        //then
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
