package uk.gov.hmcts.reform.civil.controllers.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.ResourceReader;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

@SpringBootTest(classes = {
    ClaimFormService.class,
    JacksonAutoConfiguration.class})
public class DocumentControllerTest extends BaseIntegrationTest {

    protected static final int DOC_UUID_LENGTH = 36;

    @Autowired
    private ClaimFormService claimFormService;

    @Autowired
    private DocumentController documentController;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private DocumentUploadClientApi documentUploadClientApi;
    @MockBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    private UserService userService;

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;

    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String FILE_NAME = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(FILE_NAME)
        .documentType(SEALED_CLAIM)
        .build();
    private static final String BASE_URL = "/case/document";
    private static final String GENERATE_SEALED_DOC_URL = BASE_URL + "/generateSealedDoc";

    private static final String GENERATE_ANY_DOC_URL = BASE_URL + "/generateAnyDoc";
    private static final LocalDate DATE = LocalDate.of(2023, 5, 1);
    private static final String DOWNLOAD_FILE_URL = BASE_URL + "/downloadDocument/{documentId}";
    public static final String DOCUMENT_ID = "documentId";

    private Document document;

    @InjectMocks
    private SealedClaimFormGeneratorForSpec sealedClaimFormGenerator;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private RestTemplate restTemplate;

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
    public void setUp() throws JsonProcessingException {
        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(BEARER_TOKEN);
        when(representativeService.getApplicantRepresentative(any())).thenReturn(Representative.builder().build());

        document = mapper.readValue(
            readString("document-management/download.success.json"),
            Document.class
        );
    }

    @Test
    void shouldDownloadDocumentById() throws Exception {
        Document document = mapper.readValue(
            ResourceReader.readString("document-management/download.success.json"),
            Document.class
        );
        byte[] file = "test".getBytes();
        String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b7";
        UUID documentId = getDocumentIdFromSelfHref(documentPath);

        when(caseDocumentClientApi.getMetadataForDocument(
                 anyString(),
                 anyString(),
                 eq(documentId)
             )
        ).thenReturn(document);

        when(caseDocumentClientApi.getDocumentBinary(
                 anyString(),
                 anyString(),
                 eq(documentId)
             )
        ).thenReturn(responseEntity);

        when(responseEntity.getBody()).thenReturn(new ByteArrayResource(file));

        //then
        doGet(BEARER_TOKEN, DOWNLOAD_FILE_URL, documentId)
            .andExpect(content().bytes(file))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnExpectedGeneratedSealedDocument() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .legacyCaseReference(REFERENCE_NUMBER)
            .totalClaimAmount(BigDecimal.ONE)
            .issueDate(DATE)
            .build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(ResponseEntity.of(Optional.of(bytes)));
        when(caseDocumentClientApi.uploadDocuments(anyString(), anyString(), any()))
            .thenReturn(new UploadResponse(List.of(document)));

        MvcResult result = doPost(BEARER_TOKEN, caseData, GENERATE_SEALED_DOC_URL)
            .andExpect(status().isCreated()).andReturn();

        JSONObject jsonReturnedCaseDocument = new JSONObject(result.getResponse().getContentAsString());
        assertEquals(FILE_NAME, jsonReturnedCaseDocument.get("documentName"),
                     "Document file names should match"
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionSealedDocument() throws Exception {

        //given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .legacyCaseReference(REFERENCE_NUMBER)
            .totalClaimAmount(BigDecimal.ONE)
            .issueDate(DATE)
            .build();

        //when
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(ResponseEntity.of(Optional.of(bytes)));
        when(caseDocumentClientApi.uploadDocuments(anyString(), anyString(), any()))
            .thenThrow(DocumentUploadException.class);

        MvcResult result = doPost(BEARER_TOKEN, caseData, GENERATE_SEALED_DOC_URL)
            .andExpect(status().isBadRequest()).andReturn();

        assertEquals("Document upload unsuccessful", result.getResponse().getContentAsString());
        //then
        assertThrows(
            DocumentUploadException.class,
            () -> claimFormService.uploadSealedDocument(BEARER_TOKEN, caseData)
        );
    }

    @Test
    void shouldReturnExpectedGeneratedAnyDocument() throws Exception {

        //given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "TestFile.png",
            "image/png",
            "This is a dummy file content".getBytes()
        );

        //when
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(ResponseEntity.of(Optional.of(bytes)));
        when(caseDocumentClientApi.uploadDocuments(anyString(), anyString(), any()))
            .thenReturn(new UploadResponse(List.of(document)));

        //then
        MvcResult result = doFilePost(BEARER_TOKEN, file, GENERATE_ANY_DOC_URL)
            .andExpect(status().isCreated()).andReturn();

        JSONObject jsonReturnedCaseDocument = new JSONObject(result.getResponse().getContentAsString());
        assertEquals("TestFile.png", jsonReturnedCaseDocument.get("documentName"),
                     "Document file names should match"
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionAnyDocument() throws Exception {

        //given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "TestFile.png",
            "image/png",
            "This is a dummy file content".getBytes()
        );

        //when
        when(documentManagementService.uploadDocument(anyString(), any(UploadedDocument.class)))
            .thenThrow(DocumentUploadException.class);

        MvcResult result = doFilePost(BEARER_TOKEN, file, GENERATE_ANY_DOC_URL)
            .andExpect(status().isBadRequest()).andReturn();

        assertEquals("Document upload unsuccessful", result.getResponse().getContentAsString());
        //then
        assertThrows(
            DocumentUploadException.class,
            () -> documentController.uploadAnyDocument(BEARER_TOKEN, file)
        );
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

}
