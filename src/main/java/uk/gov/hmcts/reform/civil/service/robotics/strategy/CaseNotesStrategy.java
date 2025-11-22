package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class CaseNotesStrategy implements EventHistoryStrategy {

    private static final String CASE_NOTE_TEMPLATE = "case note added: %s";
    private static final int MAX_TEXT_LENGTH = 250;

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && !unwrapElements(caseData.getCaseNotes()).isEmpty();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        List<CaseNote> notes = unwrapElements(caseData.getCaseNotes());
        notes.stream()
            .map(this::buildPayload)
            .forEach(payload -> builder.miscellaneous(buildMiscEvent(
                builder,
                sequenceGenerator,
                payload.message(),
                payload.createdOn()
            )));
    }

    private NotePayload buildPayload(CaseNote caseNote) {
        String note = caseNote != null ? normalise(caseNote.getNote()) : "";
        String eventText = left(textFormatter.format(CASE_NOTE_TEMPLATE, note), MAX_TEXT_LENGTH);
        return new NotePayload(eventText, caseNote != null ? caseNote.getCreatedOn() : null);
    }

    private String normalise(String note) {
        if (note == null) {
            return "";
        }
        return note.replaceAll("\\s+", " ");
    }

    private record NotePayload(String message, LocalDateTime createdOn) { }
}
