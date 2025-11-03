package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;

public final class RoboticsEventSupport {

    private RoboticsEventSupport() {
        // utility class
    }

    public static Event buildMiscEvent(EventHistory.EventHistoryBuilder builder,
                                       RoboticsSequenceGenerator sequenceGenerator,
                                       String message,
                                       LocalDateTime dateReceived) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(dateReceived)
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build();
    }

    public static Event buildDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                          RoboticsSequenceGenerator sequenceGenerator,
                                                          LocalDateTime dateReceived,
                                                          String partyId,
                                                          DQ dq,
                                                          String preferredCourtCode,
                                                          String eventDetailsText) {
        String courtCode = preferredCourtCode != null
            ? preferredCourtCode
            : RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(dq);
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(dateReceived)
            .litigiousPartyID(partyId)
            .eventDetailsText(eventDetailsText)
            .eventDetails(EventDetails.builder()
                              .stayClaim(RoboticsDirectionsQuestionnaireSupport.isStayClaim(dq))
                              .preferredCourtCode(courtCode)
                              .preferredCourtName("")
                              .build())
            .build();
    }

    public static Event buildDefenceOrStatesPaidEvent(EventHistory.EventHistoryBuilder builder,
                                                      RoboticsSequenceGenerator sequenceGenerator,
                                                      LocalDateTime dateReceived,
                                                      String partyId,
                                                      boolean statesPaid) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(statesPaid ? STATES_PAID.getCode() : DEFENCE_FILED.getCode())
            .dateReceived(dateReceived)
            .litigiousPartyID(partyId)
            .build();
    }

    public static Event buildEnumeratedMiscEvent(EventHistory.EventHistoryBuilder builder,
                                                 RoboticsSequenceGenerator sequenceGenerator,
                                                 RoboticsTimelineHelper timelineHelper,
                                                 LocalDateTime dateReceived,
                                                 int index,
                                                 int total,
                                                 String subject,
                                                 BiFunction<String, String, String> messageResolver) {
        String prefix = total > 1
            ? String.format("[%d of %d - %s] ", index + 1, total, timelineHelper.now().toLocalDate())
            : "";
        String details = messageResolver.apply(prefix, subject);
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(dateReceived)
            .eventDetailsText(details)
            .eventDetails(EventDetails.builder().miscText(details).build())
            .build();
    }
}
