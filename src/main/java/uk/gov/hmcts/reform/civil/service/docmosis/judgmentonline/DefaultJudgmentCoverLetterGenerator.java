package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

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
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.DefaultJudgmentDefendantLrCoverLetter;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.SetAsideJudgmentInErrorLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultJudgmentCoverLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private static final String DEFAULT_JUDGMENT_COVER_LETTER = "default-judgment-cover-letter";

    public byte[] generateAndPrintSetAsideLetter(CaseData caseData, String authorisation) {
        DocmosisDocument setAsideLetter = generate(caseData);
        CaseDocument setAsideLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR.getDocumentTitle(),
                setAsideLetter.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_COVER_LETTER
            )
        );
        String documentUrl = setAsideLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Default Judgment Cover Letter ");
            throw new DocumentDownloadException(setAsideLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), DEFAULT_JUDGMENT_COVER_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR
        );
    }

    public DefaultJudgmentDefendantLrCoverLetter getTemplateData(CaseData caseData) {
        return DefaultJudgmentDefendantLrCoverLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .letterIssueDate(LocalDate.now())
            .issueDate(caseData.getJoIssuedDate())
            .build();
    }
}
