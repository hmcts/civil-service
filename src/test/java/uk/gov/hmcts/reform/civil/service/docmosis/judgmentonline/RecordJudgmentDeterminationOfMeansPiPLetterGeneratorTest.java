package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

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
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.RecordJudgmentDeterminationOfMeansLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.RecordJudgmentDeterminationOfMeansPiPLetterGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;

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
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    RecordJudgmentDeterminationOfMeansPiPLetterGenerator.class,
    JacksonAutoConfiguration.class
})
class RecordJudgmentDeterminationOfMeansPiPLetterGeneratorTest {

    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Autowired
    private RecordJudgmentDeterminationOfMeansPiPLetterGenerator generator;
    @MockBean
    private GeneralAppFeesService generalAppFeesService;
    private static final String RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER = "record-judgment-determination-of-means-letter";
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final CaseDocument caseDocument =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(DocumentType.RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    @BeforeEach
    void setup() {
        given(defendantPinToPostLRspecService.buildDefendantPinToPost())
            .willReturn(DefendantPinToPostLRspec.builder()
                            .accessCode(
                                AccessCodeGenerator.generateAccessCode())
                            .respondentCaseRole(
                                CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                            .expiryDate(LocalDate.now().plusDays(
                                180))
                            .build());
        given(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER)
        ))
            .willReturn(new DocmosisDocument(
                RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                LETTER_CONTENT
            ));

        given(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                             LETTER_CONTENT,
                             DocumentType.RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER
                     )
                 ))
            .willReturn(caseDocument);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .addRespondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost())
            .buildJudgmentOnlineCaseDataWithDeterminationMeans();

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        when(generalAppFeesService.getFeeForJOWithApplicationType(any())).thenReturn(Fee.builder().calculatedAmountInPence(
            BigDecimal.valueOf(1000)).build());

        // when
        generator.generateAndPrintRecordJudgmentDeterminationOfMeansLetter(caseData, BEARER_TOKEN);
        // then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

    @Test
    void shouldGetTemplateFeesCorrectly() {
        //Given
        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .addRespondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost())
            .buildJudgmentOnlineCaseDataWithDeterminationMeans();

        when(generalAppFeesService.getFeeForJOWithApplicationType(VARY_ORDER))
            .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1500)).build());
        when(generalAppFeesService.getFeeForJOWithApplicationType(OTHER))
            .thenReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1400)).build());
        //When
        RecordJudgmentDeterminationOfMeansLiPDefendantLetter recordJudgmentDeterminationOfMeansLiPDefendantLetter
            = generator.getTemplateData(caseData);
        //Then
        assertThat(recordJudgmentDeterminationOfMeansLiPDefendantLetter.getVaryJudgmentFee()).isEqualTo("£15.00");
        assertThat(recordJudgmentDeterminationOfMeansLiPDefendantLetter.getCertifOfSatisfactionFee()).isEqualTo("£14.00");
    }
}
