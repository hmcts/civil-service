package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.DefaultJudgmentNonDivergentSpecLipDefendantLetter;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentNonDivergentSpecPiPLetterGenerator.class,
    JacksonAutoConfiguration.class
})
class DefaultJudgmentNonDivergentSpecPiPLetterGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String CLAIM_REFERENCE = "ABC";
    private static String fileNameTrial = null;
    private static final String fileName = String.format(DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER.getDocumentTitle(), CLAIM_REFERENCE);
    private static final String PIN = "1234789";
    private static final CaseDocument CASE_DOCUMENT_TRIAL = CaseDocumentBuilder.builder()
        .documentName(fileNameTrial)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
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
    private static final String DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER_REF = "default-judgment-non-divergent-spec-pin_in_letter";

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private DefaultJudgmentNonDivergentSpecPiPLetterGenerator generator;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private PinInPostConfiguration pipInPostConfiguration;

    @MockBean
    private GeneralAppFeesService generalAppFeesService;

    @BeforeEach
    void setUp() {
        fileNameTrial = LocalDate.now() + "_Judge Dredd" + ".pdf";
    }

    @Test
    void shouldDefaultJudgmentSpecPiPLetterGenerator_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER)))
            .thenReturn(new DocmosisDocument(DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        when(pipInPostConfiguration.getRespondToClaimUrl()).thenReturn("Response URL");
        when(generalAppFeesService.getFeeForJOWithApplicationType(any())).thenReturn(Fee.builder().calculatedAmountInPence(
            BigDecimal.valueOf(1000)).build());

        byte[] letterContentByteData = generator.generateAndPrintDefaultJudgementSpecLetter(CASE_DATA, BEARER_TOKEN);

        assertThat(letterContentByteData).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER));
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                CASE_DATA.getLegacyCaseReference(),
                CASE_DATA.getLegacyCaseReference(),
                DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER_REF,
                List.of(CASE_DATA.getRespondent1().getPartyName())
            );
    }

    @Test
    void shouldGetTemplateFeesCorrectly() {
        //Given
        when(generalAppFeesService.getFeeForJOWithApplicationType(VARY_ORDER))
            .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1500)).build());
        when(generalAppFeesService.getFeeForJOWithApplicationType(SET_ASIDE_JUDGEMENT))
            .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30300)).build());
        when(generalAppFeesService.getFeeForJOWithApplicationType(OTHER))
            .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1400)).build());
        //When
        DefaultJudgmentNonDivergentSpecLipDefendantLetter defaultJudgmentNonDivergentSpecLipDefendantLetter
            = generator.getTemplateData(CASE_DATA);
        //Then
        assertThat(defaultJudgmentNonDivergentSpecLipDefendantLetter.getVaryJudgmentFee()).isEqualTo("£15.00");
        assertThat(defaultJudgmentNonDivergentSpecLipDefendantLetter.getJudgmentSetAsideFee()).isEqualTo("£303.00");
        assertThat(defaultJudgmentNonDivergentSpecLipDefendantLetter.getCertifOfSatisfactionFee()).isEqualTo("£14.00");
    }
}
