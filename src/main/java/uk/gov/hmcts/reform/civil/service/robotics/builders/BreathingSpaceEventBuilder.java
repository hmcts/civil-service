package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.BS_END_DATE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.BS_REF;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.BS_START_DT;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.ENTER;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.LIFTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class BreathingSpaceEventBuilder {

    public void buildBreathingSpaceEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (caseData.getBreathing() != null) {
            BreathingSpaceInfo breathing = caseData.getBreathing();
            BreathingSpaceType type = breathing.getEnter() != null ? breathing.getEnter().getType() : null;
            boolean isLifted = breathing.getLift() != null;

            if (type != null) {
                if (isLifted) {
                    buildBreathingSpaceLiftedEvents(builder, caseData, type);
                } else {
                    buildBreathingSpaceEnteredEvent(builder, caseData, type);
                }
            }
        }
    }

    private void buildBreathingSpaceEnteredEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, BreathingSpaceType type) {
        if (type == BreathingSpaceType.STANDARD) {
            buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_ENTERED, ENTER);
        } else if (type == BreathingSpaceType.MENTAL_HEALTH) {
            buildBreathingSpaceEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, ENTER);
        }
    }

    private void buildBreathingSpaceLiftedEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData, BreathingSpaceType type) {
        if (type == BreathingSpaceType.STANDARD) {
            buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_ENTERED, ENTER);
            buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_LIFTED, LIFTED);
        } else if (type == BreathingSpaceType.MENTAL_HEALTH) {
            buildBreathingSpaceEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_ENTERED, ENTER);
            buildBreathingSpaceEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_LIFTED, LIFTED);
        }
    }

    private void buildBreathingSpaceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                          EventType eventType, String bsStatus) {
        log.info("Building event: {} for case id: {} ", eventType.toString(), caseData.getCcdCaseReference());
        String eventDetails = buildEventDetails(caseData, bsStatus);

        LocalDateTime dateReceived = determineDateReceived(caseData, eventType);

        Event event = buildMiscEvent(eventType, eventDetails, dateReceived, builder);

        switch (eventType) {
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
            default:
                break;
        }
    }

    private String buildEventDetails(CaseData caseData, String bsStatus) {
        String eventDetails = null;

        if (caseData.getBreathing().getEnter().getReference() != null) {
            eventDetails = BS_REF + " " + caseData.getBreathing().getEnter().getReference() + ", ";
        }

        if (ENTER.equals(bsStatus)) {
            eventDetails = appendDateDetails(
                eventDetails,
                BS_START_DT,
                caseData.getBreathing().getEnter().getStart()
            );
        } else if (LIFTED.equals(bsStatus) && caseData.getBreathing().getLift().getExpectedEnd() != null) {
            eventDetails = appendDateDetails(
                eventDetails,
                BS_END_DATE,
                caseData.getBreathing().getLift().getExpectedEnd()
            );
        }
        return eventDetails;
    }

    private String appendDateDetails(String eventDetails, String label, LocalDate date) {
        String dateText = (date != null) ? date.toString() : LocalDateTime.now().toString();
        if (eventDetails == null) {
            return StringUtils.capitalize(label) + " " + dateText;
        }
        return eventDetails + label + " " + dateText;
    }

    private LocalDateTime determineDateReceived(CaseData caseData, EventType eventType) {
        if (eventType == BREATHING_SPACE_ENTERED || eventType == EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED) {
            return caseData.getBreathing().getEnter().getStart() != null
                ? caseData.getBreathing().getEnter().getStart().atTime(LocalTime.now())
                : LocalDateTime.now();
        } else {
            return caseData.getBreathing().getLift().getExpectedEnd() != null
                ? caseData.getBreathing().getLift().getExpectedEnd().atTime(LocalTime.now())
                : LocalDateTime.now();
        }
    }

    private Event buildMiscEvent(EventType eventType, String eventDetails, LocalDateTime dateReceived,
                                 EventHistory.EventHistoryBuilder builder) {
        return Event.builder()
            .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
            .eventCode(eventType.getCode())
            .dateReceived(dateReceived)
            .eventDetailsText(eventDetails)
            .litigiousPartyID("001")
            .eventDetails(EventDetails.builder().miscText(eventDetails).build())
            .build();
    }
}
