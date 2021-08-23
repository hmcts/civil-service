package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class EventHistorySequencer {

    public EventHistory sortEvents(EventHistory eventHistory) {
        List<Event> events = flatEvents(eventHistory);
        events.sort(Comparator.comparing(Event::getDateReceived));
        return prepareEventHistory(prepareSequenceId(events));
    }

    private EventHistory prepareEventHistory(List<Event> events) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        events.forEach(event -> {
            switch (EventType.valueOfCode(event.getEventCode())) {
                case MISCELLANEOUS:
                    builder.miscellaneous(event);
                    break;
                case ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED:
                    builder.acknowledgementOfServiceReceived(List.of(event));
                    break;
                case CONSENT_EXTENSION_FILING_DEFENCE:
                    builder.consentExtensionFilingDefence(List.of(event));
                    break;
                case DEFENCE_FILED:
                    builder.defenceFiled(List.of(event));
                    break;
                case DEFENCE_AND_COUNTER_CLAIM:
                    builder.defenceAndCounterClaim(List.of(event));
                    break;
                case RECEIPT_OF_PART_ADMISSION:
                    builder.receiptOfPartAdmission(List.of(event));
                    break;
                case RECEIPT_OF_ADMISSION:
                    builder.receiptOfAdmission(List.of(event));
                    break;
                case REPLY_TO_DEFENCE:
                    builder.replyToDefence(List.of(event));
                    break;
                case DIRECTIONS_QUESTIONNAIRE_FILED:
                    builder.directionsQuestionnaire(event);
                    break;
            }
        });
        return builder
            .build();
    }

    private List<Event> prepareSequenceId(List<Event> events) {
        AtomicInteger sequence = new AtomicInteger(1);
        return events
            .stream()
            .map(event ->
                     event.toBuilder()
                         .eventSequence(sequence.getAndIncrement())
                         .build()
            ).collect(Collectors.toList());
    }

    private List<Event> flatEvents(EventHistory eventHistory) {
        List<List<Event>> eventsList = Lists.newArrayList(
            eventHistory.getMiscellaneous(),
            eventHistory.getAcknowledgementOfServiceReceived(),
            eventHistory.getConsentExtensionFilingDefence(),
            eventHistory.getDefenceFiled(),
            eventHistory.getDefenceAndCounterClaim(),
            eventHistory.getReceiptOfPartAdmission(),
            eventHistory.getReceiptOfAdmission(),
            eventHistory.getReplyToDefence(),
            eventHistory.getDirectionsQuestionnaireFiled()
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
