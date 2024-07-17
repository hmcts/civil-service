package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgmentInErrorLiPLetterGeneratorTest {

    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private BulkPrintService bulkPrintService;
    @InjectMocks
    private SetAsideJudgmentInErrorLiPLetterGenerator setAsideJudgmentInErrorLiPLetterGenerator;
    private static final String SET_ASIDE_JUDGMENT_LETTER = "set-aside-judgment-letter";

    static final byte[] LETTER_CONTENT = new byte[] {37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final CaseDocument SET_ASIDE = CaseDocumentBuilder.builder()
        .documentName(null)
        .documentType(DocumentType.SET_ASIDE_JUDGMENT_LETTER)
        .build();

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();

        caseData.setJoIssuedDate(LocalDate.now());
        caseData.setJoSetAsideJudgmentErrorText("Some text");

        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER)
        ))
            .thenReturn(new DocmosisDocument(
                SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                LETTER_CONTENT
            ));
        when(documentManagementService
            .uploadDocument(
                BEARER_TOKEN,
                new PDF(SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                    LETTER_CONTENT,
                    DocumentType.SET_ASIDE_JUDGMENT_LETTER
                )
            ))
            .thenReturn(SET_ASIDE);

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        setAsideJudgmentInErrorLiPLetterGenerator.generateAndPrintSetAsideLetter(caseData, BEARER_TOKEN);
        // then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                SET_ASIDE_JUDGMENT_LETTER,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

}
