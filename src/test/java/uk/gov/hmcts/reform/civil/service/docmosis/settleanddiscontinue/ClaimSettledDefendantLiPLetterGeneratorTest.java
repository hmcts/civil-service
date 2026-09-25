package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
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
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.UNSPEC_CLAIM_SETTLED_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIM_SETTLED_LIP_DEFENDANT_LETTER;

@ExtendWith(MockitoExtension.class)
class ClaimSettledDefendantLiPLetterGeneratorTest {

    @InjectMocks
    private ClaimSettledDefendantLiPLetterGenerator claimSettledDefendantLiPLetterGenerator;

    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private BulkPrintService bulkPrintService;

    private static final String UNSPEC_CLAIM_SETTLED_LETTER_TITLE = "unspec-claim-settled-letter";
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final CaseDocument CLAIM_SETTLED_DOC = CaseDocumentBuilder.builder()
        .documentName("unspec_claim_settled_letter.pdf")
        .documentType(UNSPEC_CLAIM_SETTLED_LETTER)
        .build();

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        //Given
        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(CLAIM_SETTLED_LIP_DEFENDANT_LETTER)
        ))
            .thenReturn(new DocmosisDocument(
                CLAIM_SETTLED_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                LETTER_CONTENT
            ));
        when(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(CLAIM_SETTLED_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                             LETTER_CONTENT,
                             UNSPEC_CLAIM_SETTLED_LETTER
                     )
                 ))
            .thenReturn(CLAIM_SETTLED_DOC);

        byte[] bytes = new ByteArrayResource(LETTER_CONTENT).getByteArray();
        given(documentDownloadService.downloadDocument(
            any(), any(), anyString(), anyString()
        )).willReturn(bytes);

        when(bulkPrintService.printLetter(any(), anyString(), anyString(), anyString(), any(), any()))
            .thenReturn(new SendLetterResponse(UUID.randomUUID()));

        Party applicant = new PartyBuilder().soleTrader().build();
        Party defendant = new PartyBuilder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .legacyCaseReference("100DC001")
            .build();

        //When
        claimSettledDefendantLiPLetterGenerator.generateAndPrintClaimSettledLetter(caseData, BEARER_TOKEN);

        //Then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getCcdCaseReference().toString(),
                caseData.getLegacyCaseReference(),
                UNSPEC_CLAIM_SETTLED_LETTER_TITLE,
                List.of(caseData.getRespondent1().getPartyName()),
                List.of(CLAIM_SETTLED_DOC.getDocumentLink().getDocumentFileName())
            );
    }
}
