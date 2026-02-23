package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

import java.util.List;

@Component
public class RoboticsSequenceGenerator {

    public int nextSequence(EventHistory history) {
        int currentSequence = 0;
        currentSequence = maxSequence(history.getMiscellaneous(), currentSequence);
        currentSequence = maxSequence(history.getAcknowledgementOfServiceReceived(), currentSequence);
        currentSequence = maxSequence(history.getConsentExtensionFilingDefence(), currentSequence);
        currentSequence = maxSequence(history.getDefenceFiled(), currentSequence);
        currentSequence = maxSequence(history.getDefenceAndCounterClaim(), currentSequence);
        currentSequence = maxSequence(history.getReceiptOfPartAdmission(), currentSequence);
        currentSequence = maxSequence(history.getReceiptOfAdmission(), currentSequence);
        currentSequence = maxSequence(history.getReplyToDefence(), currentSequence);
        currentSequence = maxSequence(history.getBreathingSpaceEntered(), currentSequence);
        currentSequence = maxSequence(history.getBreathingSpaceLifted(), currentSequence);
        currentSequence = maxSequence(history.getBreathingSpaceMentalHealthEntered(), currentSequence);
        currentSequence = maxSequence(history.getBreathingSpaceMentalHealthLifted(), currentSequence);
        currentSequence = maxSequence(history.getStatesPaid(), currentSequence);
        currentSequence = maxSequence(history.getDirectionsQuestionnaireFiled(), currentSequence);
        currentSequence = maxSequence(history.getJudgmentByAdmission(), currentSequence);
        currentSequence = maxSequence(history.getGeneralFormOfApplication(), currentSequence);
        currentSequence = maxSequence(history.getDefenceStruckOut(), currentSequence);
        return currentSequence + 1;
    }

    private int maxSequence(List<Event> events, int currentSequence) {
        if (events == null) {
            return currentSequence;
        }
        for (Event event : events) {
            if (event.getEventSequence() != null && event.getEventSequence() > currentSequence) {
                currentSequence = event.getEventSequence();
            }
        }
        return currentSequence;
    }
}
