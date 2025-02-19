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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.SetAsideJudgmentInErrorLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class SetAsideJudgmentInErrorLiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendSetAsideLiPLetterDef1";
    private static final String SET_ASIDE_JUDGMENT_LETTER = "set-aside-judgment-letter";

    public byte[] generateAndPrintSetAsideLetter(CaseData caseData, String authorisation) {
        DocmosisDocument setAsideLetter = generate(caseData);
        CaseDocument setAsideLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                setAsideLetter.getBytes(),
                DocumentType.SET_ASIDE_JUDGMENT_LETTER
            )
        );
        String documentUrl = setAsideLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Set Aside LiP Letter ");
            throw new DocumentDownloadException(setAsideLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), SET_ASIDE_JUDGMENT_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER
        );
    }

    public SetAsideJudgmentInErrorLiPDefendantLetter getTemplateData(CaseData caseData) {
        return SetAsideJudgmentInErrorLiPDefendantLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .letterIssueDate(LocalDate.now())
            .issueDate(getJudgmentIssueDate(caseData))
            .build();
    }

    private LocalDate getJudgmentIssueDate(CaseData caseData) {
        if (!caseData.getHistoricJudgment().isEmpty()) {
            List<Element<JudgmentDetails>> judgmentList = caseData.getHistoricJudgment();
            JudgmentDetails judgmentDetails = judgmentList.get(judgmentList.size() - 1).getValue();
            return judgmentDetails.getIssueDate();
        }
        return LocalDate.now();
    }
}
