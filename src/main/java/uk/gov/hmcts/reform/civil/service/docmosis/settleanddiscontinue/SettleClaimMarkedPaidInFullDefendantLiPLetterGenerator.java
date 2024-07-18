package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.SettleClaimMarkedPaidInFullDefendantLiPLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";
    private static final String SETTLE_CLAIM_PAID_IN_FULL_LETTER = "settle-claim-paid-in-full-letter";

    public byte[] generateAndPrintSettleClaimPaidInFullLetter(CaseData caseData, String authorisation) {
        DocmosisDocument settleClaimPaidInFullLetter = generate(caseData);
        CaseDocument settleClaimPaidInFullLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                settleClaimPaidInFullLetter.getBytes(),
                DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER
            )
        );
        String documentUrl = settleClaimPaidInFullLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Settle Claim Paid In Full LiP Letter ");
            throw new DocumentDownloadException(settleClaimPaidInFullLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), SETTLE_CLAIM_PAID_IN_FULL_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER
        );
    }

    public SettleClaimMarkedPaidInFullDefendantLiPLetter getTemplateData(CaseData caseData) {
        return SettleClaimMarkedPaidInFullDefendantLiPLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .letterIssueDate(LocalDate.now())
            .defendantLipName(caseData.getRespondent1().getPartyName())
            .addressLine1(caseData.getRespondent1().getPrimaryAddress().getAddressLine1())
            .addressLine2(caseData.getRespondent1().getPrimaryAddress().getAddressLine2())
            .addressLine3(caseData.getRespondent1().getPrimaryAddress().getAddressLine3())
            .postCode(caseData.getRespondent1().getPrimaryAddress().getPostCode())
            .dateOfEvent(LocalDate.now())
            .build();
    }
}
