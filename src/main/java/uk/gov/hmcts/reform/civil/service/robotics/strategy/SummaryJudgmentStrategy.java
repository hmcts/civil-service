package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryJudgmentStrategy implements EventHistoryStrategy {

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
        log.info("Building summary judgment robotics event for caseId {}", caseData.getCcdCaseReference());

        String message = resolveMessage(caseData);
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, timelineHelper.now()));
    }

    private String resolveMessage(CaseData caseData) {
        boolean requested = caseData.getRespondent2() != null && !selectedLabelStartsWithBoth(caseData);
        return requested ? textFormatter.summaryJudgmentRequested() : textFormatter.summaryJudgmentGranted();
    }

    private boolean selectedLabelStartsWithBoth(CaseData caseData) {
        DynamicList list = caseData.getDefendantDetails();
        DynamicListElement selected = list != null ? list.getValue() : null;
        String label = selected != null ? selected.getLabel() : null;
        return label != null && label.startsWith("Both");
    }
}
