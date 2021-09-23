package uk.gov.hmcts.reform.civil.service.documentmanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.civil.config.DocumentManagementConfiguration;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.net.URI;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadException.MESSAGE_TEMPLATE;
import static uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService.FILES_NAME;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

@SpringBootTest(classes = {
    SecuredDocumentManagementService.class,
    JacksonAutoConfiguration.class,
    DocumentManagementConfiguration.class},
    properties = {"document_management.secured=true"})
class SecuredDocumentManagementServiceTest {

    private static final String USER_ROLES_JOINED = "caseworker-civil,caseworker-civil-solicitor";
    public static final String BEARER_TOKEN = "Bearer Token";

    @MockBean
    private CaseDocumentClient caseDocumentClient;
    @MockBean
    private DocumentDownloadClientApi documentDownloadClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SecuredDocumentManagementService documentManagementService;

    @Mock
    private ResponseEntity<Resource> responseEntity;
    private final UserInfo userInfo = UserInfo.builder()
        .roles(List.of("role"))
        .uid("id")
        .givenName("userFirstName")
        .familyName("userLastName")
        .sub("mail@mail.com")
        .build();

    @BeforeEach
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
    }

    @Nested
    class UploadDocument {

        @Test
        void shouldUploadToDocumentManagement() throws JsonProcessingException {
            PDF document = new PDF("0000-claim.pdf", "test".getBytes(), SEALED_CLAIM);

            List<MultipartFile> files = List.of(new InMemoryMultipartFile(
                FILES_NAME,
                document.getFileBaseName(),
                APPLICATION_PDF_VALUE,
                document.getBytes()
            ));

            uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse uploadResponse = mapper.readValue(
                readString("document-management/secured.response.success.json"),
                uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse.class
            );

            when(caseDocumentClient.uploadDocuments(
                     anyString(),
                     anyString(),
                     anyString(),
                     anyString(),
                     eq(files)
                 )
            ).thenReturn(uploadResponse);

            CaseDocument caseDocument = documentManagementService.uploadDocument(BEARER_TOKEN, document);
            assertNotNull(caseDocument.getDocumentLink());
            assertEquals(
                uploadResponse.getDocuments().get(0).links.self.href,
                caseDocument.getDocumentLink().getDocumentUrl()
            );

            verify(caseDocumentClient)
                .uploadDocuments(anyString(), anyString(), anyString(), anyString(), eq(files));
        }

        @Test
        void shouldThrow_whenUploadDocumentFails() throws JsonProcessingException {
            PDF document = new PDF("0000-failed-claim.pdf", "failed-test".getBytes(), SEALED_CLAIM);

            List<MultipartFile> files = List.of(new InMemoryMultipartFile(
                FILES_NAME,
                document.getFileBaseName(),
                APPLICATION_PDF_VALUE,
                document.getBytes()
            ));

            when(caseDocumentClient.uploadDocuments(
                     anyString(),
                     anyString(),
                     anyString(),
                     anyString(),
                     eq(files)
                 )
            ).thenReturn(mapper.readValue(
                readString("document-management/response.failure.json"),
                uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse.class
            ));

            DocumentUploadException documentManagementException = assertThrows(
                DocumentUploadException.class,
                () -> documentManagementService.uploadDocument(BEARER_TOKEN, document)
            );

            assertEquals(
                "Unable to upload document 0000-failed-claim.pdf to document management.",
                documentManagementException.getMessage()
            );

            verify(caseDocumentClient)
                .uploadDocuments(anyString(), anyString(), anyString(), anyString(), eq(files));
        }
    }

    @Nested
    class DownloadDocument {

        @Test
        void shouldDownloadDocumentFromDocumentManagement() throws JsonProcessingException {

            Document document = mapper.readValue(
                readString("document-management/download.success.json"),
                Document.class
            );
            String documentPath = URI.create(document.links.self.href).getPath();
            String documentBinary = URI.create(document.links.binary.href).getPath().replaceFirst("/", "");

            when(caseDocumentClient.getMetadataForDocument(
                     anyString(),
                     anyString(),
                     eq(documentPath)
                 )
            ).thenReturn(document);

            when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

            when(documentDownloadClient.downloadBinary(
                     anyString(),
                     anyString(),
                     eq(USER_ROLES_JOINED),
                     anyString(),
                     eq(documentBinary)
                 )
            ).thenReturn(responseEntity);

            byte[] pdf = documentManagementService.downloadDocument(BEARER_TOKEN, documentPath);

            assertNotNull(pdf);
            assertArrayEquals("test".getBytes(), pdf);

            verify(caseDocumentClient).getMetadataForDocument(anyString(), anyString(), eq(documentPath));

            verify(documentDownloadClient)
                .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), eq(documentBinary));
        }

        @Test
        void shouldThrow_whenDocumentDownloadFails() throws JsonProcessingException {
            Document document = mapper.readValue(
                readString("document-management/download.success.json"),
                Document.class
            );
            String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b7";
            String documentBinary = "documents/85d97996-22a5-40d7-882e-3a382c8ae1b7/binary";
            when(caseDocumentClient.getMetadataForDocument(
                     anyString(),
                     anyString(),
                     eq(documentPath)
                 )
            ).thenReturn(document);

            when(documentDownloadClient
                     .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), eq(documentBinary))
            ).thenReturn(null);

            DocumentDownloadException documentManagementException = assertThrows(
                DocumentDownloadException.class,
                () -> documentManagementService.downloadDocument(BEARER_TOKEN, documentPath)
            );

            assertEquals(format(MESSAGE_TEMPLATE, documentPath), documentManagementException.getMessage());

            verify(caseDocumentClient).getMetadataForDocument(anyString(), anyString(), eq(documentPath));
        }
    }

    @Nested
    class DocumentMetaData {
        @Test
        void getDocumentMetaData() throws JsonProcessingException {
            String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b3";

            when(caseDocumentClient.getMetadataForDocument(
                     anyString(),
                     anyString(),
                     eq(documentPath)
                 )
            ).thenReturn(mapper.readValue(
                readString("document-management/metadata.success.json"), Document.class)
            );

            when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

            uk.gov.hmcts.reform.ccd.document.am.model.Document documentMetaData
                = documentManagementService.getDocumentMetaData(BEARER_TOKEN, documentPath);

            assertEquals(72552L, documentMetaData.size);
            assertEquals("000DC002.pdf", documentMetaData.originalDocumentName);

            verify(caseDocumentClient)
                .getMetadataForDocument(anyString(), anyString(), eq(documentPath));
        }

        @Test
        void shouldThrow_whenMetadataDownloadFails() {
            String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b5";

            when(caseDocumentClient
                     .getMetadataForDocument(anyString(), anyString(), eq(documentPath))
            ).thenThrow(new RuntimeException("Failed to access document metadata"));

            DocumentDownloadException documentManagementException = assertThrows(
                DocumentDownloadException.class,
                () -> documentManagementService.getDocumentMetaData(BEARER_TOKEN, documentPath)
            );

            assertEquals(
                String.format(MESSAGE_TEMPLATE, documentPath),
                documentManagementException.getMessage()
            );

            verify(caseDocumentClient)
                .getMetadataForDocument(anyString(), anyString(), eq(documentPath));
        }
    }
}
