package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED;

@Component
@Order(10)
@RequiredArgsConstructor
public class BreathingSpaceEventContributor implements EventHistoryContributor {

    private static final String BS_REF = "Breathing space reference";
    private static final String BS_START_DT = "actual start date";
    private static final String BS_END_DATE = "actual end date";
    private static final String LITIGIOUS_PARTY_ID = "001";

    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && caseData.getBreathing() != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        if (caseData.getBreathing().getEnter() != null && caseData.getBreathing().getLift() == null) {
            if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(builder, caseData, BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
            } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
            }
        } else if (caseData.getBreathing().getLift() != null) {
            if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(builder, caseData, BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
                buildEvent(builder, caseData, BREATHING_SPACE_LIFTED, BreathingStatus.LIFTED);
            } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                buildEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, BreathingStatus.ENTER);
                buildEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_LIFTED, BreathingStatus.LIFTED);
            }
        }
    }

    private void buildEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                             EventType eventType, BreathingStatus status) {
        LocalDateTime now = timelineHelper.now();
        String details = buildEventDetails(caseData, status, now);
        LocalDateTime dateReceived = resolveEventDate(caseData, eventType, now);
        Event event = Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(eventType.getCode())
            .dateReceived(dateReceived)
            .litigiousPartyID(LITIGIOUS_PARTY_ID)
            .eventDetailsText(details)
            .eventDetails(EventDetails.builder().miscText(details).build())
            .build();

        switch (eventType) {
            case BREATHING_SPACE_ENTERED -> builder.breathingSpaceEntered(event);
            case BREATHING_SPACE_LIFTED -> builder.breathingSpaceLifted(event);
            case MENTAL_HEALTH_BREATHING_SPACE_ENTERED -> builder.breathingSpaceMentalHealthEntered(event);
            case MENTAL_HEALTH_BREATHING_SPACE_LIFTED -> builder.breathingSpaceMentalHealthLifted(event);
            default -> throw new IllegalStateException("Unsupported breathing-space event: " + eventType);
        }
    }

    private LocalDateTime resolveEventDate(CaseData caseData, EventType eventType, LocalDateTime fallback) {
        return switch (eventType) {
            case BREATHING_SPACE_ENTERED, MENTAL_HEALTH_BREATHING_SPACE_ENTERED ->
                caseData.getBreathing().getEnter().getStart() != null
                    ? caseData.getBreathing().getEnter().getStart().atTime(fallback.toLocalTime())
                    : fallback;
            case BREATHING_SPACE_LIFTED, MENTAL_HEALTH_BREATHING_SPACE_LIFTED ->
                caseData.getBreathing().getLift().getExpectedEnd() != null
                    ? caseData.getBreathing().getLift().getExpectedEnd().atTime(fallback.toLocalTime())
                    : fallback;
            default -> fallback;
        };
    }

    private String buildEventDetails(CaseData caseData, BreathingStatus status, LocalDateTime fallback) {
        String details = null;
        if (caseData.getBreathing().getEnter().getReference() != null) {
            details = BS_REF + " " + caseData.getBreathing().getEnter().getReference() + ", ";
        }

        if (status == BreathingStatus.ENTER) {
            if (caseData.getBreathing().getEnter().getStart() != null) {
                details = append(details, BS_START_DT,
                    caseData.getBreathing().getEnter().getStart().toString());
            } else {
                details = append(details, BS_START_DT, fallback.toString());
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
