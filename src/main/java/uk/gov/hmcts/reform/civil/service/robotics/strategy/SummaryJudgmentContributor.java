package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

@Component
@Order(35)
@RequiredArgsConstructor
public class SummaryJudgmentContributor implements EventHistoryContributor {

    private static final String REQUESTED_MESSAGE = "Summary judgment requested and referred to judge.";
    private static final String GRANTED_MESSAGE = "Summary judgment granted and referred to judge.";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;

    @Override
    public boolean supports(CaseData caseData) {
        DynamicList defendantDetails = caseData != null ? caseData.getDefendantDetails() : null;
        DynamicListElement selected = defendantDetails != null ? defendantDetails.getValue() : null;
        return selected != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        String message = resolveMessage(caseData);
        builder.miscellaneous(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(timelineHelper.now())
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build());
    }

    private String resolveMessage(CaseData caseData) {
        boolean requested = caseData.getRespondent2() != null && !selectedLabelStartsWithBoth(caseData);
        String template = requested ? REQUESTED_MESSAGE : GRANTED_MESSAGE;
        return textFormatter.withRpaPrefix(template);
    }

    private boolean selectedLabelStartsWithBoth(CaseData caseData) {
        DynamicList list = caseData.getDefendantDetails();
        DynamicListElement selected = list != null ? list.getValue() : null;
        String label = selected != null ? selected.getLabel() : null;
        return label != null && label.startsWith("Both");
    }
}
