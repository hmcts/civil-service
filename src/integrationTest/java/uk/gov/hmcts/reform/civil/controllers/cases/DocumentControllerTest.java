package uk.gov.hmcts.reform.civil.controllers.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ResourceReader.readString;

@SpringBootTest(classes = {
    ClaimFormService.class,
    JacksonAutoConfiguration.class})
public class DocumentControllerTest extends BaseIntegrationTest {

    protected static final int DOC_UUID_LENGTH = 36;

    @Autowired
    private ClaimFormService claimFormService;

    @MockBean
    private DocumentUtil documentUtil;

    @Autowired
    private DocumentController documentController;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    private UserService userService;

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(SEALED_CLAIM)
        .build();

    @InjectMocks
    private SealedClaimFormGeneratorForSpec sealedClaimFormGenerator;

    @Mock
    private RepresentativeService representativeService;

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

    @Test
    void shouldDownloadDocumentFromDocumentManagement_FromCaseDocumentController() throws JsonProcessingException {

        CaseDocument caseDocument = mapper.readValue(
            readString("document-management/download.document.json"),
            CaseDocument.class
        );

        Document document = mapper.readValue(
            readString("document-management/download.success.json"),
            Document.class
        );
        String documentPath = URI.create(document.links.self.href).getPath();
        UUID documentId = getDocumentIdFromSelfHref(documentPath);

        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(caseDocumentClientApi.getDocumentBinary(
                 anyString(),
                 anyString(),
                 eq(documentId)
             )
        ).thenReturn(responseEntity);

        byte[] pdf = documentController.downloadSealedDocument(BEARER_TOKEN, caseDocument);

        assertNotNull(pdf);
        assertArrayEquals("test".getBytes(), pdf);

        verify(caseDocumentClientApi)
            .getDocumentBinary(anyString(), anyString(), eq(documentId));
    }

    @Test
    void shouldDownloadDocumentFromDocumentManagement_FromCaseDocumentClientApi() throws JsonProcessingException {

        CaseDocument caseDocument = mapper.readValue(
            readString("document-management/download.document.json"),
            CaseDocument.class
        );

        Document document = mapper.readValue(
            readString("document-management/download.success.json"),
            Document.class
        );
        String documentPath = URI.create(document.links.self.href).getPath();
        UUID documentId = getDocumentIdFromSelfHref(documentPath);

        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(caseDocumentClientApi.getDocumentBinary(
                 anyString(),
                 anyString(),
                 eq(documentId)
             )
        ).thenReturn(responseEntity);

        byte[] pdf = claimFormService.downloadSealedDocument(BEARER_TOKEN, caseDocument);

        assertNotNull(pdf);
        assertArrayEquals("test".getBytes(), pdf);

        verify(caseDocumentClientApi)
            .getDocumentBinary(anyString(), anyString(), eq(documentId));
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

    private CaseData.CaseDataBuilder getBaseCaseDataBuilder() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(850_00))
            .claimFee(Fee.builder()
                          .calculatedAmountInPence(BigDecimal.valueOf(70_00))
                          .build());
    }
}
