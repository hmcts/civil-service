package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.utils.LanguageUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeOfDiscontinuanceLiPLetterGenerator {

    private final BulkPrintService bulkPrintService;
    private static final String NOTICE_OF_DISCONTINUANCE_LETTER = "notice-of-discontinuance";
    private final DocumentDownloadService documentDownloadService;
    private final FeatureToggleService featureToggleService;

    public void printNoticeOfDiscontinuanceLetter(CaseData caseData, String authorisation) {
        CaseDocument discontinuanceCaseDocument = caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc();
        if (Objects.nonNull(discontinuanceCaseDocument)) {
            Language language = LanguageUtils.determineLanguageForBulkPrint(caseData, false, featureToggleService.isWelshEnabledForMainCase());
            if (language.equals(Language.WELSH) || language.equals(Language.BOTH)) {
                if (Objects.nonNull(caseData.getRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc())) {
                    discontinuanceCaseDocument = caseData.getRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc();
                }
            }

            List<String> recipient = getRecipientsList(caseData);
            byte[] letterContent;
            try {
                String documentUrl = discontinuanceCaseDocument.getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
            } catch (IOException e) {
                log.error("Failed getting letter content for Notice of Discontinuance LiP Letter ");
                throw new DocumentDownloadException(discontinuanceCaseDocument.getDocumentLink().getDocumentFileName(), e);
            }
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                    caseData.getLegacyCaseReference(), NOTICE_OF_DISCONTINUANCE_LETTER, recipient);
        }
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }
}
