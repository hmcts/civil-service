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
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        events.forEach(event -> {
            EventType eventType = EventType.valueOfCode(event.getEventCode()).orElseThrow(IllegalStateException::new);
            switch (eventType) {
                case MISCELLANEOUS:
                    builder.miscellaneous(event);
                    break;
                case ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED:
                    builder.acknowledgementOfServiceReceived(event);
                    break;
                case CONSENT_EXTENSION_FILING_DEFENCE:
                    builder.consentExtensionFilingDefence(event);
                    break;
                case DEFENCE_FILED:
                    builder.defenceFiled(List.of(event));
                    break;
                case STATES_PAID:
                    builder.statesPaid(List.of(event));
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
                    builder.replyDefence(event);
                    break;
                case DIRECTIONS_QUESTIONNAIRE_FILED:
                    builder.directionsQuestionnaire(event);
                    break;
                case BREATHING_SPACE_ENTERED:
                    builder.breathingSpaceEntered(event);
                    break;
                case BREATHING_SPACE_LIFTED:
                    builder.breathingSpaceLifted(event);
                    break;
                case MENTAL_HEALTH_BREATHING_SPACE_ENTERED:
                    builder.breathingSpaceMentalHealthEntered(event);
                    break;
                case MENTAL_HEALTH_BREATHING_SPACE_LIFTED:
                    builder.breathingSpaceMentalHealthLifted(event);
                    break;
                case INTERLOCUTORY_JUDGMENT_GRANTED:
                    builder.interlocutoryJudgment(event);
                    break;
                case DEFAULT_JUDGMENT_GRANTED:
                    builder.defaultJudgment(event);
                    break;
                case JUDGEMENT_BY_ADMISSION:
                    builder.judgmentByAdmission(event);
                    break;
                case GENERAL_FORM_OF_APPLICATION:
                    builder.generalFormOfApplication(event);
                    break;
                case DEFENCE_STRUCK_OUT:
                    builder.defenceStruckOut(event);
                    break;
                default:
                    throw new IllegalStateException("Unexpected event type: " + eventType);
            }
        });
        if (isEmpty(builder.build().getDirectionsQuestionnaireFiled())) {
            builder.directionsQuestionnaireFiled(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getDefenceFiled())) {
            builder.defenceFiled(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getStatesPaid())) {
            builder.statesPaid(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getReceiptOfAdmission())) {
            builder.receiptOfAdmission(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getReceiptOfPartAdmission())) {
            builder.receiptOfPartAdmission(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getDefenceAndCounterClaim())) {
            builder.defenceAndCounterClaim(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getAcknowledgementOfServiceReceived())) {
            builder.acknowledgementOfServiceReceived(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getConsentExtensionFilingDefence())) {
            builder.consentExtensionFilingDefence(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getReplyToDefence())) {
            builder.replyToDefence(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getBreathingSpaceEntered())) {
            builder.breathingSpaceEntered(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getBreathingSpaceLifted())) {
            builder.breathingSpaceLifted(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getBreathingSpaceMentalHealthEntered())) {
            builder.breathingSpaceMentalHealthEntered(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getBreathingSpaceMentalHealthLifted())) {
            builder.breathingSpaceMentalHealthLifted(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getInterlocutoryJudgment())) {
            builder.interlocutoryJudgment(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getDefaultJudgment())) {
            builder.defaultJudgment(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getJudgmentByAdmission())) {
            builder.judgmentByAdmission(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getGeneralFormOfApplication())) {
            builder.generalFormOfApplication(List.of(Event.builder().build()));
        }
        if (isEmpty(builder.build().getDefenceStruckOut())) {
            builder.defenceStruckOut(List.of(Event.builder().build()));
        }
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
            eventHistory.getStatesPaid(),
            eventHistory.getDirectionsQuestionnaireFiled(),
            eventHistory.getBreathingSpaceEntered(),
            eventHistory.getBreathingSpaceLifted(),
            eventHistory.getBreathingSpaceMentalHealthEntered(),
            eventHistory.getBreathingSpaceMentalHealthLifted(),
            eventHistory.getInterlocutoryJudgment(),
            eventHistory.getDefaultJudgment(),
            eventHistory.getJudgmentByAdmission(),
            eventHistory.getGeneralFormOfApplication(),
            eventHistory.getDefenceStruckOut()
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(event -> event.getDateReceived() != null)
            .collect(Collectors.toList());
    }
}
