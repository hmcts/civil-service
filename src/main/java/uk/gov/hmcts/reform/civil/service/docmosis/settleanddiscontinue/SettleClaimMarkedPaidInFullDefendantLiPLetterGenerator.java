package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.SettleClaimMarkedPaidInFullDefendantLiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.isWelshHearingSelected;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final CivilStitchService civilStitchService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";
    private static final String SETTLE_CLAIM_PAID_IN_FULL_LETTER_TITLE = "settle-claim-paid-in-full-letter";

    public void generateAndPrintSettleClaimPaidInFullLetter(CaseData caseData, String auth) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating mailable document for caseId {}", caseId);

        CaseDocument englishDoc = generateLetter(
            caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER);
        CaseDocument settleClaimDoc = englishDoc;

        if (isBilingual(caseData)) {
            final CaseDocument welshDoc = generateLetter(
                caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH);

            List<DocumentMetaData> documentMetaDataList = appendCoverToDocument(englishDoc, welshDoc);

            log.info("Calling civil stitch service to combine bilingual settle claim marked paid in full lip defendant letter for caseId {}", caseId);
            CaseDocument stitchedCaseDocument = civilStitchService.generateStitchedCaseDocument(
                documentMetaDataList,
                welshDoc.getDocumentName(),
                caseId,
                SETTLE_CLAIM_PAID_IN_FULL_LETTER,
                auth
            );

            log.info("Bilingual settle claim marked paid in full generated {}  for caseId {}", stitchedCaseDocument, caseId);
            settleClaimDoc = stitchedCaseDocument;
        }

        byte[] letterContent = getLetterContent(settleClaimDoc, auth, caseId);

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), SETTLE_CLAIM_PAID_IN_FULL_LETTER_TITLE, recipients);
    }

    private CaseDocument generateLetter(CaseData caseData, String authorisation, DocmosisTemplates template) {
        DocmosisDocument settleClaimPaidInFullLetter = documentGeneratorService.generateDocmosisDocument(getTemplateData(caseData), template);

        CaseDocument document =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                settleClaimPaidInFullLetter.getBytes(),
                SETTLE_CLAIM_PAID_IN_FULL_LETTER
            )
        );
        return document;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    public SettleClaimMarkedPaidInFullDefendantLiPLetter getTemplateData(CaseData caseData) {
        return SettleClaimMarkedPaidInFullDefendantLiPLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .letterIssueDate(LocalDate.now())
            .letterIssueDateWelsh(formatDateInWelsh(LocalDate.now()))
            .defendantLipName(caseData.getRespondent1().getPartyName())
            .addressLine1(caseData.getRespondent1().getPrimaryAddress().getAddressLine1())
            .addressLine2(caseData.getRespondent1().getPrimaryAddress().getAddressLine2())
            .addressLine3(caseData.getRespondent1().getPrimaryAddress().getAddressLine3())
            .postCode(caseData.getRespondent1().getPrimaryAddress().getPostCode())
            .dateOfEvent(LocalDate.now())
            .dateOfEventWelsh(formatDateInWelsh(LocalDate.now()))
            .build();
    }

    private List<DocumentMetaData> appendCoverToDocument(CaseDocument coverLetter, CaseDocument... caseDocuments) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(coverLetter.getDocumentLink(),
                                                      "Welsh letter",
                                                      LocalDate.now().toString()));

        Arrays.stream(caseDocuments).forEach(caseDocument -> documentMetaDataList.add(new DocumentMetaData(
            caseDocument.getDocumentLink(),
            "Welsh letter to attach",
            LocalDate.now().toString()
        )));

        return documentMetaDataList;
    }

    private byte[] getLetterContent(CaseDocument mailableSdoDocument, String authorisation, Long caseId) {
        byte[] letterContent;
        try {
            String documentUrl = mailableSdoDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Failed getting letter content for document to mail for caseId {}", caseId, e);
            throw new DocumentDownloadException(mailableSdoDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }

    private boolean isBilingual(CaseData caseData) {
        return isWelshHearingSelected(caseData) || caseData.isRespondentResponseBilingual();
    }
}
