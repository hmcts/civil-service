package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only reporting migration task.
 *
 * <p>Hearing notice party names are rendered from CCD CaseData at generation time
 * ({@code HearingNoticeHmcGenerator}), so with CCD as the source of truth a notice is
 * correct if its printed names still match the current case. This task downloads each
 * HMC hearing notice PDF from CDAM and checks that the current CCD claimant/defendant
 * names appear in the document text. A notice whose text no longer contains a current
 * name is a stale notice that should be reissued.</p>
 *
 * <p>The task makes no changes to CaseData; it returns the case unchanged and emits one
 * structured log line per notice document (prefix {@code VERIFY_HEARING_NOTICE}) so
 * results can be filtered from App Insights.</p>
 */
@Component
@Slf4j
public class VerifyHearingNoticeNamesTask extends MigrationTask<CaseReference> {

    private static final String LOG_PREFIX = "VERIFY_HEARING_NOTICE";
    private static final String DOWNLOAD_ERROR = "VERIFY_HEARING_NOTICE download failed for case {}";
    private static final String TASK_NAME = "VerifyHearingNoticeNamesTask";
    private static final String EVENT_SUMMARY = "Verify hearing notice attendee names against CCD";
    private static final String EVENT_DESCRIPTION =
        "Read-only check that generated hearing notices carry the current CCD party names";

    private final DocumentDownloadService documentDownloadService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    public VerifyHearingNoticeNamesTask(DocumentDownloadService documentDownloadService,
                                        UserService userService,
                                        SystemUpdateUserConfiguration userConfig) {
        super(CaseReference.class);
        this.documentDownloadService = documentDownloadService;
        this.userService = userService;
        this.userConfig = userConfig;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected String getEventSummary() {
        return EVENT_SUMMARY;
    }

    @Override
    protected String getEventDescription() {
        return EVENT_DESCRIPTION;
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        if (caseData == null || caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }

        String caseId = caseReference.getCaseReference();
        List<String> expectedNames = expectedNames(caseData);
        List<CaseDocument> notices = hearingNoticeDocuments(caseData);

        if (notices.isEmpty()) {
            log.info("{} case={} result=NO_NOTICE expectedNames={}", LOG_PREFIX, caseId, expectedNames);
            return caseData;
        }

        String authorisation = getSystemUserToken();

        for (CaseDocument notice : notices) {
            verifyNotice(caseData, caseId, notice, expectedNames, authorisation);
        }

        return caseData;
    }

    private void verifyNotice(CaseData caseData, String caseId, CaseDocument notice,
                              List<String> expectedNames, String authorisation) {
        String fileName = notice.getDocumentLink() != null ? notice.getDocumentLink().getDocumentFileName() : null;
        try {
            byte[] content = documentDownloadService.downloadDocument(
                notice, authorisation, caseId, DOWNLOAD_ERROR);
            String noticeText = normalise(extractText(content));

            List<String> missing = new ArrayList<>();
            for (String name : expectedNames) {
                if (!noticeText.contains(normalise(name))) {
                    missing.add(name);
                }
            }

            String result = missing.isEmpty() ? "MATCH" : "STALE";
            log.info("{} case={} document=\"{}\" result={} expectedNames={} missingNames={}",
                     LOG_PREFIX, caseId, fileName, result, expectedNames, missing);
        } catch (Exception e) {
            log.error("{} case={} document=\"{}\" result=ERROR message={}",
                      LOG_PREFIX, caseId, fileName, e.getMessage(), e);
        }
    }

    private List<CaseDocument> hearingNoticeDocuments(CaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        addAll(documents, caseData.getHearingDocuments());
        addAll(documents, caseData.getHearingDocumentsWelsh());
        return documents;
    }

    private void addAll(List<CaseDocument> target, List<Element<CaseDocument>> source) {
        if (source != null) {
            source.stream()
                .filter(nonNullElement -> nonNullElement != null && nonNullElement.getValue() != null)
                .forEach(element -> target.add(element.getValue()));
        }
    }

    /**
     * Builds the party names exactly as {@code HearingNoticeHmcGenerator} renders them onto the notice:
     * minors are shown with their litigation friend, everyone else by party name.
     */
    private List<String> expectedNames(CaseData caseData) {
        List<String> names = new ArrayList<>();
        addName(names, claimantName(caseData.getApplicant1(), caseData.getApplicant1LitigationFriend()));
        addName(names, claimantName(caseData.getApplicant2(), caseData.getApplicant2LitigationFriend()));
        addName(names, caseData.getRespondent1() != null ? caseData.getRespondent1().getPartyName() : null);
        addName(names, caseData.getRespondent2() != null ? caseData.getRespondent2().getPartyName() : null);
        return names;
    }

    private String claimantName(Party party, uk.gov.hmcts.reform.civil.model.LitigationFriend litigationFriend) {
        if (party == null) {
            return null;
        }
        return PartyUtils.isMinor(party)
            ? PartyUtils.getPartyNameWithLitigiousFriend(party, litigationFriend)
            : party.getPartyName();
    }

    private void addName(List<String> names, String name) {
        if (name != null && !name.isBlank()) {
            names.add(name);
        }
    }

    private String extractText(byte[] content) throws java.io.IOException {
        try (PDDocument document = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String normalise(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }
}
