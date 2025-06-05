package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;
import uk.gov.hmcts.reform.civil.utils.LanguageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeOfDiscontinuanceLiPLetterGenerator {

    private final BulkPrintService bulkPrintService;
    private static final String NOTICE_OF_DISCONTINUANCE_LETTER = "notice-of-discontinuance";
    private final CoverLetterAppendService coverLetterAppendService;
    private final FeatureToggleService featureToggleService;

    public void printNoticeOfDiscontinuanceLetter(CaseData caseData, String authorisation) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        if (Objects.nonNull(caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc())
            || (featureToggleService.isGaForWelshEnabled()
            && Objects.nonNull(caseData.getRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc()))) {
            Language language = LanguageUtils.determineLanguageForBulkPrint(caseData, false, featureToggleService.isGaForWelshEnabled());
            switch (language) {
                case ENGLISH -> caseDocuments.add(caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc());
                case WELSH -> caseDocuments.add(caseData.getRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc());
                case BOTH -> {
                    caseDocuments.add(caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc());
                    caseDocuments.add(caseData.getRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc());
                }
                default -> { }
            }

            List<String> recipient = getRecipientsList(caseData);
            byte[] letterContent;
            letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, caseData.getRespondent1(), NOTICE_OF_DISCONTINUANCE, caseDocuments.toArray(new CaseDocument[0]));
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                    caseData.getLegacyCaseReference(), NOTICE_OF_DISCONTINUANCE_LETTER, recipient);
        }
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }
}
