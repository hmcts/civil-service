package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class EventHistorySequencer {

    public EventHistory sortEvents(EventHistory eventHistory) {
        requireNonNull(eventHistory);
        List<Event> events = flatEvents(eventHistory);
        events.sort(getComparator());
        return prepareEventHistory(prepareSequenceId(events));
    }

    private Comparator<Event> getComparator() {
        return (event1, event2) -> {
            if (event1.getDateReceived().isAfter(event2.getDateReceived())) {
                return 1;
            } else if (event1.getDateReceived().isBefore(event2.getDateReceived())) {
                return -1;
            } else {
                if ("999".equals(event1.getEventCode()) || "999".equals(event2.getEventCode())) {
                    return event1.getEventCodeInt().compareTo(event2.getEventCodeInt());
                }
                return 0;
            }
        };
    }

    private EventHistory prepareEventHistory(List<Event> events) {
        EventHistory history = new EventHistory();
        events.forEach(event -> addEvent(history, event));
        ensureRequiredLists(history);
        return history;
    }

    private static void ensureRequiredLists(EventHistory history) {
        REQUIRED_EVENT_LISTS.forEach(accessor -> {
            if (isEmpty(accessor.getter().apply(history))) {
                accessor.setter().accept(history, List.of(new Event()));
            }
        });
    }

    private void addEvent(EventHistory history, Event event) {
        EventType eventType = EventType.valueOfCode(event.getEventCode()).orElseThrow(IllegalStateException::new);
        BiConsumer<EventHistory, Event> handler = EVENT_APPENDERS.get(eventType);
        if (handler == null) {
            throw new IllegalStateException("Unexpected event type: " + eventType);
        }
        handler.accept(history, event);
    }

    private static List<Event> appendEvent(List<Event> events, Event event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        return events;
    }

    private List<Event> prepareSequenceId(List<Event> events) {
        AtomicInteger sequence = new AtomicInteger(1);
        return events
            .stream()
            .map(event -> {
                event.setEventSequence(sequence.getAndIncrement());
                return event;
            })
            .collect(Collectors.toList());
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
            eventHistory.getStatesPaid(),
            eventHistory.getDirectionsQuestionnaireFiled(),
            eventHistory.getBreathingSpaceEntered(),
            eventHistory.getBreathingSpaceLifted(),
            eventHistory.getBreathingSpaceMentalHealthEntered(),
            eventHistory.getBreathingSpaceMentalHealthLifted(),
            eventHistory.getInterlocutoryJudgment(),
            eventHistory.getDefaultJudgment(),
            eventHistory.getSetAsideJudgment(),
            eventHistory.getJudgmentByAdmission(),
            eventHistory.getGeneralFormOfApplication(),
            eventHistory.getDefenceStruckOut(),
            eventHistory.getCertificateOfSatisfactionOrCancellation()
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(event -> event.getDateReceived() != null)
            .collect(Collectors.toList());
    }

    private static final Map<EventType, BiConsumer<EventHistory, Event>> EVENT_APPENDERS = Map.ofEntries(
        Map.entry(EventType.MISCELLANEOUS,
                  (history, event) -> history.setMiscellaneous(appendEvent(history.getMiscellaneous(), event))),
        Map.entry(EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED,
                  (history, event) -> history.setAcknowledgementOfServiceReceived(
                      appendEvent(history.getAcknowledgementOfServiceReceived(), event))),
        Map.entry(EventType.CONSENT_EXTENSION_FILING_DEFENCE,
                  (history, event) -> history.setConsentExtensionFilingDefence(
                      appendEvent(history.getConsentExtensionFilingDefence(), event))),
        Map.entry(EventType.DEFENCE_FILED,
                  (history, event) -> history.setDefenceFiled(appendEvent(history.getDefenceFiled(), event))),
        Map.entry(EventType.STATES_PAID,
                  (history, event) -> history.setStatesPaid(appendEvent(history.getStatesPaid(), event))),
        Map.entry(EventType.DEFENCE_AND_COUNTER_CLAIM,
                  (history, event) -> history.setDefenceAndCounterClaim(
                      appendEvent(history.getDefenceAndCounterClaim(), event))),
        Map.entry(EventType.RECEIPT_OF_PART_ADMISSION,
                  (history, event) -> history.setReceiptOfPartAdmission(
                      appendEvent(history.getReceiptOfPartAdmission(), event))),
        Map.entry(EventType.RECEIPT_OF_ADMISSION,
                  (history, event) -> history.setReceiptOfAdmission(
                      appendEvent(history.getReceiptOfAdmission(), event))),
        Map.entry(EventType.REPLY_TO_DEFENCE,
                  (history, event) -> history.setReplyToDefence(appendEvent(history.getReplyToDefence(), event))),
        Map.entry(EventType.DIRECTIONS_QUESTIONNAIRE_FILED,
                  (history, event) -> history.setDirectionsQuestionnaireFiled(
                      appendEvent(history.getDirectionsQuestionnaireFiled(), event))),
        Map.entry(EventType.BREATHING_SPACE_ENTERED,
                  (history, event) -> history.setBreathingSpaceEntered(
                      appendEvent(history.getBreathingSpaceEntered(), event))),
        Map.entry(EventType.BREATHING_SPACE_LIFTED,
                  (history, event) -> history.setBreathingSpaceLifted(
                      appendEvent(history.getBreathingSpaceLifted(), event))),
        Map.entry(EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED,
                  (history, event) -> history.setBreathingSpaceMentalHealthEntered(
                      appendEvent(history.getBreathingSpaceMentalHealthEntered(), event))),
        Map.entry(EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED,
                  (history, event) -> history.setBreathingSpaceMentalHealthLifted(
                      appendEvent(history.getBreathingSpaceMentalHealthLifted(), event))),
        Map.entry(EventType.INTERLOCUTORY_JUDGMENT_GRANTED,
                  (history, event) -> history.setInterlocutoryJudgment(
                      appendEvent(history.getInterlocutoryJudgment(), event))),
        Map.entry(EventType.DEFAULT_JUDGMENT_GRANTED,
                  (history, event) -> history.setDefaultJudgment(appendEvent(history.getDefaultJudgment(), event))),
        Map.entry(EventType.JUDGEMENT_BY_ADMISSION,
                  (history, event) -> history.setJudgmentByAdmission(
                      appendEvent(history.getJudgmentByAdmission(), event))),
        Map.entry(EventType.GENERAL_FORM_OF_APPLICATION,
                  (history, event) -> history.setGeneralFormOfApplication(
                      appendEvent(history.getGeneralFormOfApplication(), event))),
        Map.entry(EventType.DEFENCE_STRUCK_OUT,
                  (history, event) -> history.setDefenceStruckOut(
                      appendEvent(history.getDefenceStruckOut(), event))),
        Map.entry(EventType.SET_ASIDE_JUDGMENT,
                  (history, event) -> history.setSetAsideJudgment(
                      appendEvent(history.getSetAsideJudgment(), event))),
        Map.entry(EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION,
                  (history, event) -> history.setCertificateOfSatisfactionOrCancellation(
                      appendEvent(history.getCertificateOfSatisfactionOrCancellation(), event)))
    );

    private static final List<EventHistoryListAccessor> REQUIRED_EVENT_LISTS = List.of(
        new EventHistoryListAccessor(EventHistory::getDirectionsQuestionnaireFiled,
                                     EventHistory::setDirectionsQuestionnaireFiled),
        new EventHistoryListAccessor(EventHistory::getDefenceFiled, EventHistory::setDefenceFiled),
        new EventHistoryListAccessor(EventHistory::getStatesPaid, EventHistory::setStatesPaid),
        new EventHistoryListAccessor(EventHistory::getReceiptOfAdmission, EventHistory::setReceiptOfAdmission),
        new EventHistoryListAccessor(EventHistory::getReceiptOfPartAdmission, EventHistory::setReceiptOfPartAdmission),
        new EventHistoryListAccessor(EventHistory::getDefenceAndCounterClaim,
                                     EventHistory::setDefenceAndCounterClaim),
        new EventHistoryListAccessor(EventHistory::getAcknowledgementOfServiceReceived,
                                     EventHistory::setAcknowledgementOfServiceReceived),
        new EventHistoryListAccessor(EventHistory::getConsentExtensionFilingDefence,
                                     EventHistory::setConsentExtensionFilingDefence),
        new EventHistoryListAccessor(EventHistory::getReplyToDefence, EventHistory::setReplyToDefence),
        new EventHistoryListAccessor(EventHistory::getBreathingSpaceEntered, EventHistory::setBreathingSpaceEntered),
        new EventHistoryListAccessor(EventHistory::getBreathingSpaceLifted, EventHistory::setBreathingSpaceLifted),
        new EventHistoryListAccessor(EventHistory::getBreathingSpaceMentalHealthEntered,
                                     EventHistory::setBreathingSpaceMentalHealthEntered),
        new EventHistoryListAccessor(EventHistory::getBreathingSpaceMentalHealthLifted,
                                     EventHistory::setBreathingSpaceMentalHealthLifted),
        new EventHistoryListAccessor(EventHistory::getInterlocutoryJudgment, EventHistory::setInterlocutoryJudgment),
        new EventHistoryListAccessor(EventHistory::getDefaultJudgment, EventHistory::setDefaultJudgment),
        new EventHistoryListAccessor(EventHistory::getJudgmentByAdmission, EventHistory::setJudgmentByAdmission),
        new EventHistoryListAccessor(EventHistory::getGeneralFormOfApplication,
                                     EventHistory::setGeneralFormOfApplication),
        new EventHistoryListAccessor(EventHistory::getDefenceStruckOut, EventHistory::setDefenceStruckOut),
        new EventHistoryListAccessor(EventHistory::getSetAsideJudgment, EventHistory::setSetAsideJudgment),
        new EventHistoryListAccessor(EventHistory::getCertificateOfSatisfactionOrCancellation,
                                     EventHistory::setCertificateOfSatisfactionOrCancellation)
    );

    private record EventHistoryListAccessor(Function<EventHistory, List<Event>> getter,
                                            BiConsumer<EventHistory, List<Event>> setter) {
    }
}
