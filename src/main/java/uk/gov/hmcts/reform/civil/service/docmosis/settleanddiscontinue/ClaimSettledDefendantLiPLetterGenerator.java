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

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_SETTLED_LETTER;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.isWelshHearingSelected;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIM_SETTLED_LIP_DEFENDANT_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIM_SETTLED_LIP_DEFENDANT_LETTER_WELSH;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClaimSettledDefendantLiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final CivilStitchService civilStitchService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendClaimSettledLetterLipDef";
    private static final String CLAIM_SETTLED_LETTER_TITLE = "claim-settled-letter";

    public void generateAndPrintClaimSettledLetter(CaseData caseData, String auth) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating claim settled letter for caseId {}", caseId);

        CaseDocument englishDoc = generateLetter(caseData, auth, CLAIM_SETTLED_LIP_DEFENDANT_LETTER);
        CaseDocument claimSettledDoc = englishDoc;
        List<String> bulkPrintFileNames = new ArrayList<>();
        if (isBilingual(caseData)) {
            final CaseDocument welshDoc = generateLetter(caseData, auth, CLAIM_SETTLED_LIP_DEFENDANT_LETTER_WELSH);

            List<DocumentMetaData> documentMetaDataList = appendCoverToDocument(bulkPrintFileNames, englishDoc, welshDoc);

            log.info("Calling civil stitch service to combine bilingual claim settled lip defendant letter for caseId {}", caseId);
            CaseDocument stitchedCaseDocument = civilStitchService.generateStitchedCaseDocument(
                documentMetaDataList,
                welshDoc.getDocumentName(),
                caseId,
                CLAIM_SETTLED_LETTER,
                auth
            );

            log.info("Bilingual claim settled letter generated {} for caseId {}", stitchedCaseDocument, caseId);
            claimSettledDoc = stitchedCaseDocument;
        } else {
            bulkPrintFileNames.add(claimSettledDoc.getDocumentLink().getDocumentFileName());
        }

        String errorMessage = "Failed getting welsh claim settled letter for caseId {}";
        byte[] letterContent = documentDownloadService.downloadDocument(claimSettledDoc, auth, caseId.toString(), errorMessage);

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, String.valueOf(caseData.getCcdCaseReference()),
                                     caseData.getLegacyCaseReference(), CLAIM_SETTLED_LETTER_TITLE,
                                     recipients, bulkPrintFileNames);
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
            .setLetterIssueDate(LocalDate.now())
            .setLetterIssueDateWelsh(formatDateInWelsh(LocalDate.now(), false))
            .setDefendantLipName(caseData.getRespondent1().getPartyName())
            .setAddressLine1(caseData.getRespondent1().getPrimaryAddress().getAddressLine1())
            .setAddressLine2(caseData.getRespondent1().getPrimaryAddress().getAddressLine2())
            .setAddressLine3(caseData.getRespondent1().getPrimaryAddress().getAddressLine3())
            .setPostCode(caseData.getRespondent1().getPrimaryAddress().getPostCode())
            .setDateOfEvent(LocalDate.now())
            .setDateOfEventWelsh(formatDateInWelsh(LocalDate.now(), false));
    }

    private List<DocumentMetaData> appendCoverToDocument(List<String> bulkPrintFileNames,
                                                         CaseDocument coverLetter,
                                                         CaseDocument... caseDocuments) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        documentMetaDataList.add(new DocumentMetaData(
            coverLetter.getDocumentLink(),
            "Welsh letter",
            LocalDate.now().toString()
        ));
        bulkPrintFileNames.add(coverLetter.getDocumentLink().getDocumentFileName());
        Arrays.stream(caseDocuments).forEach(caseDocument -> {
            documentMetaDataList.add(new DocumentMetaData(
                caseDocument.getDocumentLink(),
                "Welsh letter to attach",
                LocalDate.now().toString()
            ));
            bulkPrintFileNames.add(caseDocument.getDocumentLink().getDocumentFileName());
        });
        return documentMetaDataList;
    }

    private boolean isBilingual(CaseData caseData) {
        return isWelshHearingSelected(caseData) || caseData.isRespondentResponseBilingual();
    }
}
