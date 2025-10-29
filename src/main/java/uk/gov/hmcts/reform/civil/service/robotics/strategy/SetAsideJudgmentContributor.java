package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

/**
 * Emits set-aside judgment events when JO live feed is active and the case records
 * a set-aside decision.
 */
@Component
@Order(95)
@RequiredArgsConstructor
public class SetAsideJudgmentContributor implements EventHistoryContributor {

    private final FeatureToggleService featureToggleService;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && featureToggleService.isJOLiveFeedActive()
            && caseData.getJoSetAsideReason() != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        List<Event> events = new ArrayList<>();
        events.add(buildEvent(builder, caseData, RESPONDENT_ID));
        if (caseData.getRespondent2() != null) {
            events.add(buildEvent(builder, caseData, RESPONDENT2_ID));
        }
        builder.setAsideJudgment(events);
    }

    private Event buildEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, String litigiousPartyId) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .litigiousPartyID(litigiousPartyId)
            .eventCode(EventType.SET_ASIDE_JUDGMENT.getCode())
            .dateReceived(caseData.getJoSetAsideCreatedDate())
            .eventDetails(buildEventDetails(caseData))
            .eventDetailsText("")
            .build();
    }

    private EventDetails buildEventDetails(CaseData caseData) {
        String applicant = null;
        LocalDate applicationDate = null;
        LocalDate resultDate = null;

        if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGE_ORDER) {
            if (caseData.getJoSetAsideOrderType() == JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION) {
                applicationDate = caseData.getJoSetAsideApplicationDate();
            } else if (caseData.getJoSetAsideOrderType() == JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE) {
                applicationDate = caseData.getJoSetAsideDefenceReceivedDate();
            }
            applicant = SetAsideApplicantTypeForRPA.PARTY_AGAINST.getValue();
            resultDate = caseData.getJoSetAsideOrderDate();
        } else if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGMENT_ERROR) {
            applicant = SetAsideApplicantTypeForRPA.PROPER_OFFICER.getValue();
        }

        return EventDetails.builder()
            .result(SetAsideResultTypeForRPA.GRANTED.name())
            .applicant(applicant)
            .applicationDate(applicationDate)
            .resultDate(resultDate)
            .build();
    }
}
