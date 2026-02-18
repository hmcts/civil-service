package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_AND_COUNTER_CLAIM;

public final class RoboticsEventSupport {

    private RoboticsEventSupport() {
        // utility class
    }

    public static Event buildMiscEvent(EventHistory builder,
                                       RoboticsSequenceGenerator sequenceGenerator,
                                       String message,
                                       LocalDateTime dateReceived) {
        return createEvent(
            sequenceGenerator.nextSequence(builder),
            MISCELLANEOUS.getCode(),
            dateReceived,
            null,
            message,
            new EventDetails().setMiscText(message)
        );
    }

    public static Event buildDirectionsQuestionnaireEvent(EventHistory builder,
                                                          RoboticsSequenceGenerator sequenceGenerator,
                                                          LocalDateTime dateReceived,
                                                          String partyId,
                                                          DQ dq,
                                                          String preferredCourtCode,
                                                          String eventDetailsText) {
        return createEvent(
            sequenceGenerator.nextSequence(builder),
            EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode(),
            dateReceived,
            partyId,
            eventDetailsText,
            new EventDetails()
                .setStayClaim(RoboticsDirectionsQuestionnaireSupport.isStayClaim(dq))
                .setPreferredCourtCode(preferredCourtCode)
                .setPreferredCourtName("")
        );
    }

    public static Event buildDefenceOrStatesPaidEvent(EventHistory builder,
                                                      RoboticsSequenceGenerator sequenceGenerator,
                                                      LocalDateTime dateReceived,
                                                      String partyId,
                                                      boolean statesPaid) {
        return createEvent(
            sequenceGenerator.nextSequence(builder),
            statesPaid ? STATES_PAID.getCode() : DEFENCE_FILED.getCode(),
            dateReceived,
            partyId,
            null,
            null
        );
    }

    public static Event buildCounterClaimEvent(EventHistory builder,
                                               RoboticsSequenceGenerator sequenceGenerator,
                                               LocalDateTime dateReceived,
                                               String partyId) {
        return createEvent(
            sequenceGenerator.nextSequence(builder),
            DEFENCE_AND_COUNTER_CLAIM.getCode(),
            dateReceived,
            partyId,
            null,
            null
        );
    }

    public static Event buildEnumeratedMiscEvent(EventHistory builder,
                                                 RoboticsSequenceGenerator sequenceGenerator,
                                                 RoboticsTimelineHelper timelineHelper,
                                                 EnumeratedMiscParams params) {
        String prefix = params.total > 1
            ? String.format("[%d of %d - %s] ", params.index + 1, params.total, timelineHelper.now().toLocalDate())
            : "";
        String details = params.messageResolver.apply(prefix, params.subject);
        return createEvent(
            sequenceGenerator.nextSequence(builder),
            MISCELLANEOUS.getCode(),
            params.dateReceived,
            null,
            details,
            new EventDetails().setMiscText(details)
        );
    }

    public static final class EnumeratedMiscParams {

        private final LocalDateTime dateReceived;
        private final int index;
        private final int total;
        private final String subject;
        private final BinaryOperator<String> messageResolver;

        public EnumeratedMiscParams(LocalDateTime dateReceived,
                                    int index,
                                    int total,
                                    String subject,
                                    BinaryOperator<String> messageResolver) {
            this.dateReceived = dateReceived;
            this.index = index;
            this.total = total;
            this.subject = subject;
            this.messageResolver = messageResolver;
        }
    }

    public static Event buildLipVsLrMiscEvent(EventHistory builder,
                                              RoboticsSequenceGenerator sequenceGenerator,
                                              RoboticsEventTextFormatter textFormatter) {
        return buildMiscEvent(
            builder,
            sequenceGenerator,
            textFormatter.lipVsLrFullOrPartAdmissionReceived(),
            LocalDateTime.now()
        );
    }

    public static void addRespondentMiscEvent(EventHistory builder,
                                              RoboticsSequenceGenerator sequenceGenerator,
                                              String message,
                                              LocalDateTime dateReceived) {
        List<Event> events = builder.getMiscellaneous() == null
            ? new ArrayList<>()
            : new ArrayList<>(builder.getMiscellaneous());
        events.add(buildMiscEvent(builder, sequenceGenerator, message, dateReceived));
        builder.setMiscellaneous(events);
    }

    public static Event createEvent(Integer eventSequence,
                                    String eventCode,
                                    LocalDateTime dateReceived,
                                    String litigiousPartyId,
                                    String eventDetailsText,
                                    EventDetails eventDetails) {
        Event event = new Event();
        event.setEventSequence(eventSequence);
        event.setEventCode(eventCode);
        event.setDateReceived(dateReceived);
        event.setLitigiousPartyID(litigiousPartyId);
        event.setEventDetailsText(eventDetailsText);
        event.setEventDetails(eventDetails);
        return event;
    }
}
