package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.ClaimSettledDefendantLiPLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_SETTLED_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIM_SETTLED_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClaimSettledDefendantLiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendClaimSettledLetterLipDef";
    private static final String CLAIM_SETTLED_LETTER_TITLE = "claim-settled-letter";

    public void generateAndPrintClaimSettledLetter(CaseData caseData, String auth) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating claim settled letter for caseId {}", caseId);

        CaseDocument claimSettledDoc = generateLetter(caseData, auth, CLAIM_SETTLED_LIP_DEFENDANT_LETTER);
        log.info("Generated claim settled letter document for caseId {}, documentUrl: {}, binaryUrl: {}",
                 caseId, claimSettledDoc.getDocumentLink().getDocumentUrl(),
                 claimSettledDoc.getDocumentLink().getDocumentBinaryUrl());

        String errorMessage = "Failed getting claim settled letter for caseId {}";
        byte[] letterContent = documentDownloadService.downloadDocument(claimSettledDoc, auth, caseId.toString(), errorMessage);

        List<String> recipients = getRecipientsList(caseData);
        List<String> bulkPrintFileNames = List.of(claimSettledDoc.getDocumentLink().getDocumentFileName());
        SendLetterResponse sendLetterResponse = bulkPrintService.printLetter(letterContent, String.valueOf(caseData.getCcdCaseReference()),
                                     caseData.getLegacyCaseReference(), CLAIM_SETTLED_LETTER_TITLE,
                                     recipients, bulkPrintFileNames);
        log.info("Claim settled letter sent to bulk print for caseId {}, send-letter-service letterId: {}",
                 caseId, sendLetterResponse.letterId);
    }

    private CaseDocument generateLetter(CaseData caseData, String authorisation, DocmosisTemplates template) {
        DocmosisDocument claimSettledLetter = documentGeneratorService.generateDocmosisDocument(getTemplateData(caseData), template);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                CLAIM_SETTLED_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                claimSettledLetter.getBytes(),
                CLAIM_SETTLED_LETTER
            )
        );
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    public ClaimSettledDefendantLiPLetter getTemplateData(CaseData caseData) {
        return new ClaimSettledDefendantLiPLetter()
            .setClaimReferenceNumber(caseData.getLegacyCaseReference())
            .setCcdCaseReference(String.valueOf(caseData.getCcdCaseReference()))
            .setLetterIssueDate(LocalDate.now())
            .setDefendantLipName(caseData.getRespondent1().getPartyName())
            .setAddressLine1(caseData.getRespondent1().getPrimaryAddress().getAddressLine1())
            .setAddressLine2(caseData.getRespondent1().getPrimaryAddress().getAddressLine2())
            .setAddressLine3(caseData.getRespondent1().getPrimaryAddress().getAddressLine3())
            .setPostCode(caseData.getRespondent1().getPrimaryAddress().getPostCode())
            .setDateOfEvent(LocalDate.now());
    }
}
