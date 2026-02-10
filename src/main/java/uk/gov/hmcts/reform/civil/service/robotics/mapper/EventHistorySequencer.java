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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
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
        events.forEach(event -> {
            EventType eventType = EventType.valueOfCode(event.getEventCode()).orElseThrow(IllegalStateException::new);
            switch (eventType) {
                case MISCELLANEOUS:
                    history.setMiscellaneous(appendEvent(history.getMiscellaneous(), event));
                    break;
                case ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED:
                    history.setAcknowledgementOfServiceReceived(appendEvent(history.getAcknowledgementOfServiceReceived(), event));
                    break;
                case CONSENT_EXTENSION_FILING_DEFENCE:
                    history.setConsentExtensionFilingDefence(appendEvent(history.getConsentExtensionFilingDefence(), event));
                    break;
                case DEFENCE_FILED:
                    history.setDefenceFiled(appendEvent(history.getDefenceFiled(), event));
                    break;
                case STATES_PAID:
                    history.setStatesPaid(appendEvent(history.getStatesPaid(), event));
                    break;
                case DEFENCE_AND_COUNTER_CLAIM:
                    history.setDefenceAndCounterClaim(appendEvent(history.getDefenceAndCounterClaim(), event));
                    break;
                case RECEIPT_OF_PART_ADMISSION:
                    history.setReceiptOfPartAdmission(appendEvent(history.getReceiptOfPartAdmission(), event));
                    break;
                case RECEIPT_OF_ADMISSION:
                    history.setReceiptOfAdmission(appendEvent(history.getReceiptOfAdmission(), event));
                    break;
                case REPLY_TO_DEFENCE:
                    history.setReplyToDefence(appendEvent(history.getReplyToDefence(), event));
                    break;
                case DIRECTIONS_QUESTIONNAIRE_FILED:
                    history.setDirectionsQuestionnaireFiled(appendEvent(history.getDirectionsQuestionnaireFiled(), event));
                    break;
                case BREATHING_SPACE_ENTERED:
                    history.setBreathingSpaceEntered(appendEvent(history.getBreathingSpaceEntered(), event));
                    break;
                case BREATHING_SPACE_LIFTED:
                    history.setBreathingSpaceLifted(appendEvent(history.getBreathingSpaceLifted(), event));
                    break;
                case MENTAL_HEALTH_BREATHING_SPACE_ENTERED:
                    history.setBreathingSpaceMentalHealthEntered(appendEvent(history.getBreathingSpaceMentalHealthEntered(), event));
                    break;
                case MENTAL_HEALTH_BREATHING_SPACE_LIFTED:
                    history.setBreathingSpaceMentalHealthLifted(appendEvent(history.getBreathingSpaceMentalHealthLifted(), event));
                    break;
                case INTERLOCUTORY_JUDGMENT_GRANTED:
                    history.setInterlocutoryJudgment(appendEvent(history.getInterlocutoryJudgment(), event));
                    break;
                case DEFAULT_JUDGMENT_GRANTED:
                    history.setDefaultJudgment(appendEvent(history.getDefaultJudgment(), event));
                    break;
                case JUDGEMENT_BY_ADMISSION:
                    history.setJudgmentByAdmission(appendEvent(history.getJudgmentByAdmission(), event));
                    break;
                case GENERAL_FORM_OF_APPLICATION:
                    history.setGeneralFormOfApplication(appendEvent(history.getGeneralFormOfApplication(), event));
                    break;
                case DEFENCE_STRUCK_OUT:
                    history.setDefenceStruckOut(appendEvent(history.getDefenceStruckOut(), event));
                    break;
                case SET_ASIDE_JUDGMENT:
                    history.setSetAsideJudgment(appendEvent(history.getSetAsideJudgment(), event));
                    break;
                case CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION:
                    history.setCertificateOfSatisfactionOrCancellation(appendEvent(history.getCertificateOfSatisfactionOrCancellation(), event));
                    break;
                default:
                    throw new IllegalStateException("Unexpected event type: " + eventType);
            }
        });

        if (isEmpty(history.getDirectionsQuestionnaireFiled())) {
            history.setDirectionsQuestionnaireFiled(List.of(new Event()));
        }
        if (isEmpty(history.getDefenceFiled())) {
            history.setDefenceFiled(List.of(new Event()));
        }
        if (isEmpty(history.getStatesPaid())) {
            history.setStatesPaid(List.of(new Event()));
        }
        if (isEmpty(history.getReceiptOfAdmission())) {
            history.setReceiptOfAdmission(List.of(new Event()));
        }
        if (isEmpty(history.getReceiptOfPartAdmission())) {
            history.setReceiptOfPartAdmission(List.of(new Event()));
        }
        if (isEmpty(history.getDefenceAndCounterClaim())) {
            history.setDefenceAndCounterClaim(List.of(new Event()));
        }
        if (isEmpty(history.getAcknowledgementOfServiceReceived())) {
            history.setAcknowledgementOfServiceReceived(List.of(new Event()));
        }
        if (isEmpty(history.getConsentExtensionFilingDefence())) {
            history.setConsentExtensionFilingDefence(List.of(new Event()));
        }
        if (isEmpty(history.getReplyToDefence())) {
            history.setReplyToDefence(List.of(new Event()));
        }
        if (isEmpty(history.getBreathingSpaceEntered())) {
            history.setBreathingSpaceEntered(List.of(new Event()));
        }
        if (isEmpty(history.getBreathingSpaceLifted())) {
            history.setBreathingSpaceLifted(List.of(new Event()));
        }
        if (isEmpty(history.getBreathingSpaceMentalHealthEntered())) {
            history.setBreathingSpaceMentalHealthEntered(List.of(new Event()));
        }
        if (isEmpty(history.getBreathingSpaceMentalHealthLifted())) {
            history.setBreathingSpaceMentalHealthLifted(List.of(new Event()));
        }
        if (isEmpty(history.getInterlocutoryJudgment())) {
            history.setInterlocutoryJudgment(List.of(new Event()));
        }
        if (isEmpty(history.getDefaultJudgment())) {
            history.setDefaultJudgment(List.of(new Event()));
        }
        if (isEmpty(history.getJudgmentByAdmission())) {
            history.setJudgmentByAdmission(List.of(new Event()));
        }
        if (isEmpty(history.getGeneralFormOfApplication())) {
            history.setGeneralFormOfApplication(List.of(new Event()));
        }
        if (isEmpty(history.getDefenceStruckOut())) {
            history.setDefenceStruckOut(List.of(new Event()));
        }
        if (isEmpty(history.getSetAsideJudgment())) {
            history.setSetAsideJudgment(List.of(new Event()));
        }
        if (isEmpty(history.getCertificateOfSatisfactionOrCancellation())) {
            history.setCertificateOfSatisfactionOrCancellation(List.of(new Event()));
        }

        return history;
    }

    private List<Event> appendEvent(List<Event> events, Event event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        return events;
    }

    private List<Event> appendEvents(List<Event> events, List<Event> eventsToAdd) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.addAll(eventsToAdd);
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
}
