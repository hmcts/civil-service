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
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Read-only reporting migration task.
 *
 * <p>A hearing notice is built from two sources: the claimant/defendant header comes from CCD
 * CaseData, but the "Attending in person / by telephone / by video" section is built from the HMC
 * hearing party details ({@code HmcDataUtils.getInPersonAttendeeNames}). When an HMC hearing is
 * linked to the wrong party, the notice header can be correct while the attendee section names
 * someone from a different case.</p>
 *
 * <p>With CCD as the source of truth, this task downloads each hearing notice PDF from CDAM, parses
 * the attendee section, and flags any attendee who is not a current CCD case participant (party,
 * litigation friend, expert or witness). It makes no changes to CaseData and emits one structured
 * log line per notice document (prefix {@code VERIFY_HEARING_NOTICE}) for filtering in App Insights.</p>
 */
@Component
@Slf4j
public class VerifyHearingNoticeNamesTask extends MigrationTask<CaseReference> {

    private static final String LOG_PREFIX = "VERIFY_HEARING_NOTICE";
    private static final String DOWNLOAD_ERROR = "VERIFY_HEARING_NOTICE download failed for case {}";
    private static final String TASK_NAME = "VerifyHearingNoticeNamesTask";
    private static final String EVENT_SUMMARY = "Verify hearing notice attendees against CCD parties";
    private static final String EVENT_DESCRIPTION =
        "Read-only check that hearing notice attendee names belong to the current CCD case participants";

    private static final List<String> ATTENDEE_LABELS = List.of(
        "attending in person", "attending by telephone", "attending by video");
    private static final List<String> ATTENDEE_TERMINATORS = List.of(
        "attending in person", "attending by telephone", "attending by video",
        // "the time allocated for the {hearing|trial}" ends the attendee block on both
        // Notice of Hearing and Notice of Trial templates
        "the time allocated for the", "hearing fees", "trial fees");
    private static final Pattern TITLE_PREFIX = Pattern.compile("^(mr|mrs|ms|miss|mx|dr|prof)\\.?\\s+");
    // Notice header/venue lines that PDFBox interleaves into the attendee run when the list
    // spans a page break; skipped so they are not mistaken for foreign attendees.
    private static final List<String> BOILERPLATE_PREFIXES = List.of(
        "in the ", "claim reference", "claim number", "reference number",
        "date ", "at ", "notice of hearing", "the hearing");

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
        Set<String> participants = participantNames(caseData);
        List<CaseDocument> notices = hearingNoticeDocuments(caseData);

        if (notices.isEmpty()) {
            log.info("{} case={} result=NO_NOTICE participants={}", LOG_PREFIX, caseId, participants);
            return caseData;
        }

        String authorisation = getSystemUserToken();

        for (CaseDocument notice : notices) {
            verifyNotice(caseId, notice, participants, authorisation);
        }

        return caseData;
    }

    private void verifyNotice(String caseId, CaseDocument notice, Set<String> participants, String authorisation) {
        String fileName = notice.getDocumentLink() != null ? notice.getDocumentLink().getDocumentFileName() : null;
        try {
            byte[] content = documentDownloadService.downloadDocument(notice, authorisation, caseId, DOWNLOAD_ERROR);
            List<String> attendees = extractAttendees(extractText(content));

            if (attendees.isEmpty()) {
                log.info("{} case={} document=\"{}\" result=NO_ATTENDEES", LOG_PREFIX, caseId, fileName);
                return;
            }

            List<String> foreign = new ArrayList<>();
            for (String attendee : attendees) {
                if (!participants.contains(normalise(attendee))) {
                    foreign.add(attendee);
                }
            }

            String result = foreign.isEmpty() ? "OK" : "INCORRECT_ATTENDEE";
            log.info("{} case={} document=\"{}\" result={} attendees={} foreignAttendees={} participants={}",
                     LOG_PREFIX, caseId, fileName, result, attendees, foreign, participants);
        } catch (Exception e) {
            log.error("{} case={} document=\"{}\" result=ERROR message={}",
                      LOG_PREFIX, caseId, fileName, e.getMessage(), e);
        }
    }

    private List<CaseDocument> hearingNoticeDocuments(CaseData caseData) {
        List<CaseDocument> documents = new ArrayList<>();
        addDocuments(documents, caseData.getHearingDocuments());
        addDocuments(documents, caseData.getHearingDocumentsWelsh());
        return documents;
    }

    private void addDocuments(List<CaseDocument> target, List<Element<CaseDocument>> source) {
        if (source != null) {
            source.stream()
                .filter(element -> element != null && element.getValue() != null)
                .forEach(element -> target.add(element.getValue()));
        }
    }

    /**
     * Extracts the names listed under each "Attending ..." heading, up to the next heading or the
     * "time allocated" / "hearing fees" line that follows the attendee block.
     */
    List<String> extractAttendees(String text) {
        List<String> names = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        for (String raw : text.split("\\R")) {
            String line = raw.trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        int i = 0;
        while (i < lines.size()) {
            if (startsWithAny(lines.get(i), ATTENDEE_LABELS)) {
                i++;
                while (i < lines.size() && !startsWithAny(lines.get(i), ATTENDEE_TERMINATORS)) {
                    if (!startsWithAny(lines.get(i), BOILERPLATE_PREFIXES)) {
                        names.add(lines.get(i));
                    }
                    i++;
                }
            } else {
                i++;
            }
        }
        return names;
    }

    private boolean startsWithAny(String line, List<String> prefixes) {
        String lower = line.toLowerCase();
        return prefixes.stream().anyMatch(lower::startsWith);
    }

    /**
     * Everyone CCD would put on the HMC hearing payload (see {@code HearingsPartyMapper}): parties,
     * their litigation friends, experts and witnesses. An attendee outside this set is foreign.
     */
    Set<String> participantNames(CaseData caseData) {
        Set<String> names = new HashSet<>();
        addParty(names, caseData.getApplicant1());
        addParty(names, caseData.getApplicant2());
        addParty(names, caseData.getRespondent1());
        addParty(names, caseData.getRespondent2());
        addLitigationFriend(names, caseData.getApplicant1LitigationFriend());
        addLitigationFriend(names, caseData.getApplicant2LitigationFriend());
        addLitigationFriend(names, caseData.getRespondent1LitigationFriend());
        addLitigationFriend(names, caseData.getRespondent2LitigationFriend());
        addFlagStructures(names, caseData.getApplicantExperts());
        addFlagStructures(names, caseData.getApplicantWitnesses());
        addFlagStructures(names, caseData.getRespondent1Experts());
        addFlagStructures(names, caseData.getRespondent1Witnesses());
        addFlagStructures(names, caseData.getRespondent2Experts());
        addFlagStructures(names, caseData.getRespondent2Witnesses());
        return names;
    }

    private void addParty(Set<String> names, Party party) {
        if (party != null) {
            add(names, party.getPartyName());
            add(names, joinName(party.getIndividualFirstName(), party.getIndividualLastName()));
        }
    }

    private void addLitigationFriend(Set<String> names, LitigationFriend litigationFriend) {
        if (litigationFriend != null) {
            add(names, joinName(litigationFriend.getFirstName(), litigationFriend.getLastName()));
        }
    }

    private void addFlagStructures(Set<String> names, List<Element<PartyFlagStructure>> people) {
        if (people != null) {
            people.stream()
                .filter(element -> element != null && element.getValue() != null)
                .map(Element::getValue)
                .forEach(person -> add(names, joinName(person.getFirstName(), person.getLastName())));
        }
    }

    private void add(Set<String> names, String name) {
        String normalised = normalise(name);
        if (!normalised.isEmpty()) {
            names.add(normalised);
        }
    }

    private String joinName(String first, String last) {
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
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
        String lower = value.toLowerCase().replaceAll("\\s+", " ").trim();
        return TITLE_PREFIX.matcher(lower).replaceFirst("");
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }
}
