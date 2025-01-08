package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeOfDiscontinuanceLiPLetterGenerator {

    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    private static final String NOTICE_OF_DISCONTINUANCE_LETTER = "notice-of-discontinuance";

    public void printNoticeOfDiscontinuanceLetter(CaseData caseData, String authorisation) {
        CaseDocument discontinuanceCaseDocument = caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc();
        if (nonNull(discontinuanceCaseDocument)) {
            String documentUrl = discontinuanceCaseDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            byte[] letterContent;
            try {
                letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
            } catch (IOException e) {
                log.error("Failed getting letter content for Notice of Discontinuance LiP Letter ");
                throw new DocumentDownloadException(discontinuanceCaseDocument.getDocumentLink().getDocumentFileName(), e);
            }
            List<String> recipients = getRecipientsList(caseData);
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                    caseData.getLegacyCaseReference(), NOTICE_OF_DISCONTINUANCE_LETTER, recipients);
        }
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }
}
