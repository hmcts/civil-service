package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.SetAsideApplicantTypeForRPA;
import uk.gov.hmcts.reform.civil.model.judgmentonline.SetAsideResultTypeForRPA;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetAsideJudgmentStrategy implements EventHistoryStrategy {

    private final FeatureToggleService featureToggleService;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
                && featureToggleService.isJOLiveFeedActive()
                && caseData.getJoSetAsideReason() != null;
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building set aside judgment robotics events for caseId {}",
                caseData.getCcdCaseReference());

        List<Event> events = new ArrayList<>();
        events.add(buildEvent(eventHistory, caseData, RESPONDENT_ID));
        if (caseData.getRespondent2() != null) {
            events.add(buildEvent(eventHistory, caseData, RESPONDENT2_ID));
        }
        List<Event> updatedSetAsideJudgmentEvents1 =
                eventHistory.getSetAsideJudgment() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getSetAsideJudgment());
        updatedSetAsideJudgmentEvents1.addAll(events);
        eventHistory.setSetAsideJudgment(updatedSetAsideJudgmentEvents1);
    }

    private Event buildEvent(EventHistory builder, CaseData caseData, String litigiousPartyId) {
        return createEvent(
                sequenceGenerator.nextSequence(builder),
                EventType.SET_ASIDE_JUDGMENT.getCode(),
                caseData.getJoSetAsideCreatedDate(),
                litigiousPartyId,
                "",
                buildEventDetails(caseData));
    }

    private EventDetails buildEventDetails(CaseData caseData) {
        String applicant = null;
        LocalDate applicationDate = null;
        LocalDate resultDate = null;

        if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGE_ORDER) {
            if (caseData.getJoSetAsideOrderType() == JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION) {
                applicationDate = caseData.getJoSetAsideApplicationDate();
            } else if (caseData.getJoSetAsideOrderType()
                    == JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE) {
                applicationDate = caseData.getJoSetAsideDefenceReceivedDate();
            }
            applicant = SetAsideApplicantTypeForRPA.PARTY_AGAINST.getValue();
            resultDate = caseData.getJoSetAsideOrderDate();
        } else if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGMENT_ERROR) {
            applicant = SetAsideApplicantTypeForRPA.PROPER_OFFICER.getValue();
        }

        return new EventDetails()
                .setResult(SetAsideResultTypeForRPA.GRANTED.name())
                .setApplicant(applicant)
                .setApplicationDate(applicationDate)
                .setResultDate(resultDate);
    }
}
