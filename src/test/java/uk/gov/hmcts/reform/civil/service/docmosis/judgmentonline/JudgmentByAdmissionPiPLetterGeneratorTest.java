package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER;

@SpringBootTest(classes = {
    JudgmentByAdmissionPiPLetterGenerator.class,
    JacksonAutoConfiguration.class
})
class JudgmentByAdmissionPiPLetterGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String CLAIM_REFERENCE = "ABC";
    private static String fileNameTrial = null;
    private static final String fileName = String.format(JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER.getDocumentTitle(), CLAIM_REFERENCE);
    private static final String PIN = "1234789";
    private static final String JUDGMENT_BY_ADMISSION_LETTER = "judgment-by-admission-letter";
    private static final CaseDocument CASE_DOCUMENT_TRIAL = CaseDocumentBuilder.builder()
        .documentName(fileNameTrial)
        .documentType(DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER)
        .build();

    private static final Address RESPONDENT_ADDRESS = Address.builder().addressLine1("123 road")
        .postTown("London")
        .postCode("EX12RT")
        .build();
    private static final Party DEFENDANT = Party.builder().primaryAddress(RESPONDENT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Smith")
        .individualLastName("John")
        .build();

    private static final CaseData CASE_DATA = CaseData.builder()
        .legacyCaseReference(CLAIM_REFERENCE)
        .applicant1(Party.builder()
                        .type(Party.Type.INDIVIDUAL)
                        .individualTitle("Mr.")
                        .individualFirstName("John")
                        .individualLastName("Smith").build())
        .respondent1(DEFENDANT)
        .respondent1Represented(YesOrNo.NO)
        .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
        .submittedDate(LocalDateTime.now())
        .build();
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private IdamClient idamClient;

    @Autowired
    private JudgmentByAdmissionPiPLetterGenerator generator;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private PinInPostConfiguration pipInPostConfiguration;

    @BeforeEach
    void setUp() {
        fileNameTrial = LocalDate.now() + "_Judge Dredd" + ".pdf";

        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .forename("Judge")
                                                                    .surname("Dredd")
                                                                    .roles(Collections.emptyList()).build());
    }

    @Test
    void shouldDefaultJudgmentSpecPiPLetterGenerator_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(
            JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        when(pipInPostConfiguration.getRespondToClaimUrl()).thenReturn("Response URL");

        byte[] letterContentByteData = generator.generateAndPrintJudgmentByAdmissionLetter(CASE_DATA, BEARER_TOKEN);

        assertThat(letterContentByteData).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER));
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                CASE_DATA.getLegacyCaseReference(),
                CASE_DATA.getLegacyCaseReference(),
                JUDGMENT_BY_ADMISSION_LETTER,
                List.of(CASE_DATA.getRespondent1().getPartyName())
            );
    }
}
