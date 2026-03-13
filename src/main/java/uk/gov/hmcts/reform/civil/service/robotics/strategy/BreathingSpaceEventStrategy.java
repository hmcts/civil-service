package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class BreathingSpaceEventStrategy implements EventHistoryStrategy {

    private static final String BS_REF = "Breathing space reference";
    private static final String BS_START_DT = "actual start date";
    private static final String BS_END_DATE = "actual end date";
    private static final String LITIGIOUS_PARTY_ID = "001";

    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && caseData.getBreathing() != null;
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building breathing space robotics events for caseId {}", caseData.getCcdCaseReference());

        if (caseData.getBreathing().getEnter() != null && caseData.getBreathing().getLift() == null) {
            if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(eventHistory, caseData, BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
            } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(eventHistory, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
            }
        } else if (caseData.getBreathing().getLift() != null) {
            if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(eventHistory, caseData, BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
                buildEvent(eventHistory, caseData, BREATHING_SPACE_LIFTED, BreathingStatus.LIFTED);
            } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(eventHistory, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
                buildEvent(eventHistory, caseData, MENTAL_HEALTH_BREATHING_SPACE_LIFTED, BreathingStatus.LIFTED);
            }
        }
    }

    private void buildEvent(EventHistory builder, CaseData caseData,
                             EventType eventType, BreathingStatus status) {
        LocalTime timeNow = LocalTime.now();
        String details = buildEventDetails(caseData, status);
        LocalDateTime dateReceived = resolveEventDate(caseData, eventType, timeNow);
        Event event = createEvent(
            sequenceGenerator.nextSequence(builder),
            eventType.getCode(),
            dateReceived,
            LITIGIOUS_PARTY_ID,
            details,
            new EventDetails().setMiscText(details)
        );

        switch (eventType) {
            case BREATHING_SPACE_ENTERED -> {
                List<Event> updatedBreathingSpaceEnteredEvents = builder.getBreathingSpaceEntered() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(builder.getBreathingSpaceEntered());
                updatedBreathingSpaceEnteredEvents.add(event);
                builder.setBreathingSpaceEntered(updatedBreathingSpaceEnteredEvents);
            }
            case BREATHING_SPACE_LIFTED -> {
                List<Event> updatedBreathingSpaceLiftedEvents = builder.getBreathingSpaceLifted() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(builder.getBreathingSpaceLifted());
                updatedBreathingSpaceLiftedEvents.add(event);
                builder.setBreathingSpaceLifted(updatedBreathingSpaceLiftedEvents);
            }
            case MENTAL_HEALTH_BREATHING_SPACE_ENTERED -> {
                List<Event> updatedMentalHealthBreathingSpaceEnteredEvents = builder.getBreathingSpaceMentalHealthEntered() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(builder.getBreathingSpaceMentalHealthEntered());
                updatedMentalHealthBreathingSpaceEnteredEvents.add(event);
                builder.setBreathingSpaceMentalHealthEntered(updatedMentalHealthBreathingSpaceEnteredEvents);
            }
            case MENTAL_HEALTH_BREATHING_SPACE_LIFTED -> {
                List<Event> updatedMentalHealthBreathingSpaceLiftedEvents = builder.getBreathingSpaceMentalHealthLifted() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(builder.getBreathingSpaceMentalHealthLifted());
                updatedMentalHealthBreathingSpaceLiftedEvents.add(event);
                builder.setBreathingSpaceMentalHealthLifted(updatedMentalHealthBreathingSpaceLiftedEvents);
            }
            default -> throw new IllegalStateException("Unsupported breathing-space event: " + eventType);
        }
    }

    private LocalDateTime resolveEventDate(CaseData caseData,
                                           EventType eventType,
                                           LocalTime timeNow) {
        return switch (eventType) {
            case BREATHING_SPACE_ENTERED, MENTAL_HEALTH_BREATHING_SPACE_ENTERED ->
                caseData.getBreathing().getEnter().getStart() != null
                    ? caseData.getBreathing().getEnter().getStart().atTime(timeNow)
                    : LocalDateTime.now();
            case BREATHING_SPACE_LIFTED, MENTAL_HEALTH_BREATHING_SPACE_LIFTED ->
                caseData.getBreathing().getLift().getExpectedEnd() != null
                    ? caseData.getBreathing().getLift().getExpectedEnd().atTime(timeNow)
                    : LocalDateTime.now();
            default -> LocalDateTime.now();
        };
    }

    private String buildEventDetails(CaseData caseData, BreathingStatus status) {
        String details = null;
        if (caseData.getBreathing().getEnter().getReference() != null) {
            details = BS_REF + " " + caseData.getBreathing().getEnter().getReference() + ", ";
        }

        if (status == BreathingStatus.ENTER) {
            if (caseData.getBreathing().getEnter().getStart() != null) {
                details = append(details, BS_START_DT,
                    caseData.getBreathing().getEnter().getStart().toString());
            } else {
                details = append(details, BS_START_DT, LocalDateTime.now().toString());
            }
        } else if (caseData.getBreathing().getLift() != null
            && caseData.getBreathing().getLift().getExpectedEnd() != null) {
            details = append(details, BS_END_DATE,
                caseData.getBreathing().getLift().getExpectedEnd().toString());
        }

        return details;
    }

    private String append(String current, String label, String value) {
        if (current == null) {
            return StringUtils.capitalize(label) + " " + value;
        }
        return current + label + " " + value;
    }

    private enum BreathingStatus {
        ENTER,
        LIFTED
    }
}
