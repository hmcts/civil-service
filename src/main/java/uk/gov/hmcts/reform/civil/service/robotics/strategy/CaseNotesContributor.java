package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

/**
 * Emits miscellaneous case-note events for any notes recorded against the case.
 */
@Component
@Order(85)
@RequiredArgsConstructor
public class CaseNotesContributor implements EventHistoryContributor {

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
            .map(this::buildEvent)
            .forEach(event -> builder.miscellaneous(event.toBuilder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .build()));
    }

    private Event buildEvent(CaseNote caseNote) {
        String note = caseNote != null ? normalise(caseNote.getNote()) : "";
        String eventText = left(textFormatter.format(CASE_NOTE_TEMPLATE, note), MAX_TEXT_LENGTH);
        return Event.builder()
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(caseNote != null ? caseNote.getCreatedOn() : null)
            .eventDetailsText(eventText)
            .eventDetails(EventDetails.builder()
                .miscText(eventText)
                .build())
            .build();
    }

    private String normalise(String note) {
        if (note == null) {
            return "";
        }
        return note.replaceAll("\\s+", " ");
    }
}
