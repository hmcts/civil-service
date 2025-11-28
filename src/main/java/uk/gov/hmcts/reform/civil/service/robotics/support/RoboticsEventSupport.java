package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import java.time.LocalDateTime;
import java.util.function.BinaryOperator;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_AND_COUNTER_CLAIM;

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

    public static Event buildCounterClaimEvent(EventHistory.EventHistoryBuilder builder,
                                               RoboticsSequenceGenerator sequenceGenerator,
                                               LocalDateTime dateReceived,
                                               String partyId) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
            .dateReceived(dateReceived)
            .litigiousPartyID(partyId)
            .build();
    }

    public static Event buildEnumeratedMiscEvent(EventHistory.EventHistoryBuilder builder,
                                                 RoboticsSequenceGenerator sequenceGenerator,
                                                 RoboticsTimelineHelper timelineHelper,
                                                 EnumeratedMiscParams params) {
        String prefix = params.total > 1
            ? String.format("[%d of %d - %s] ", params.index + 1, params.total, timelineHelper.now().toLocalDate())
            : "";
        String details = params.messageResolver.apply(prefix, params.subject);
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(params.dateReceived)
            .eventDetailsText(details)
            .eventDetails(EventDetails.builder().miscText(details).build())
            .build();
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

    public static Event buildLipVsLrMiscEvent(EventHistory.EventHistoryBuilder builder,
                                              RoboticsSequenceGenerator sequenceGenerator,
                                              RoboticsEventTextFormatter textFormatter,
                                              RoboticsTimelineHelper timelineHelper) {
        return buildMiscEvent(
            builder,
            sequenceGenerator,
            textFormatter.lipVsLrFullOrPartAdmissionReceived(),
            timelineHelper.now()
        );
    }

    public static void addRespondentMiscEvent(EventHistory.EventHistoryBuilder builder,
                                              RoboticsSequenceGenerator sequenceGenerator,
                                              String message,
                                              LocalDateTime dateReceived) {
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, dateReceived));
    }
}
