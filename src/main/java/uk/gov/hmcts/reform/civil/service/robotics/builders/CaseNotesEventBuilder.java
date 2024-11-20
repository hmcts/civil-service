package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseNotesEventBuilder {

    public void buildCaseNotesEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (isNotEmpty(caseData.getCaseNotes())) {
            log.info("Building event: {} for case id: {} ", "CASE_NOTES", caseData.getCcdCaseReference());
            buildMiscellaneousCaseNotesEvent(builder, caseData);
        }
    }

    private void buildMiscellaneousCaseNotesEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = unwrapElements(caseData.getCaseNotes())
            .stream()
            .map(caseNote ->
                Event.builder()
                    .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseNote.getCreatedOn())
                    .eventDetailsText(left((format(
                        "case note added: %s",
                        caseNote.getNote() != null
                            ? caseNote.getNote().replaceAll("\\s+", " ") : ""
                    )), 250))
                    .eventDetails(EventDetails.builder()
                        .miscText(left((format(
                            "case note added: %s",
                            caseNote.getNote() != null
                                ? caseNote.getNote().replaceAll("\\s+", " ") : ""
                        )), 250))
                        .build())
                    .build())
            .toList();
        builder.miscellaneous(events);
    }

}
