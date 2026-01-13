package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent2DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDefenceOrStatesPaidEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentFullDefenceStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building respondent full defence robotics events for caseId {}", caseData.getCcdCaseReference());

        EventBuckets buckets = initialiseBuckets();

        processRespondent1(builder, caseData, buckets);
        processRespondent2(builder, caseData, buckets);

        builder.defenceFiled(buckets.defenceEvents);
        builder.statesPaid(buckets.statesPaidEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(buckets.directionsQuestionnaireEvents);
    }

    private EventBuckets initialiseBuckets() {
        List<Event> defenceEvents = new ArrayList<>();
        List<Event> statesPaidEvents = new ArrayList<>();
        List<Event> directionsQuestionnaireEvents = new ArrayList<>();
        return new EventBuckets(defenceEvents, statesPaidEvents, directionsQuestionnaireEvents);
    }

    private void processRespondent1(EventHistory.EventHistoryBuilder builder,
                                    CaseData caseData,
                                    EventBuckets buckets) {
        if (!defendant1ResponseExists.test(caseData)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent1ResponseDate();
        if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
            addLipVsLipFullDefenceEvent(builder, caseData, buckets, responseDate);
        } else {
            addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT_ID, caseData.getRespondToClaim());
        }

        Respondent1DQ respondent1DQ = getRespondent1DQOrDefault(caseData);
        buckets.directionsQuestionnaireEvents.add(
            createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                responseDate,
                RESPONDENT_ID,
                respondent1DQ,
                caseData.getRespondent1(),
                true
            )
        );

        if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
            handleSameSolicitorResponse(builder, caseData, buckets, responseDate, respondent1DQ);
        }
    }

    private void processRespondent2(EventHistory.EventHistoryBuilder builder,
                                    CaseData caseData,
                                    EventBuckets buckets) {
        if (!defendant2ResponseExists.test(caseData)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent2ResponseDate();
        if (responseDate == null) {
            return;
        }

        addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT2_ID, caseData.getRespondToClaim2());

        Respondent2DQ respondent2DQ = getRespondent2DQOrDefault(caseData);
        buckets.directionsQuestionnaireEvents.add(
            createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                responseDate,
                RESPONDENT2_ID,
                respondent2DQ,
                caseData.getRespondent2(),
                false
            )
        );
    }

    private void addDefenceOrStatesPaid(EventHistory.EventHistoryBuilder builder,
                                        CaseData caseData,
                                        EventBuckets buckets,
                                        LocalDateTime responseDate,
                                        String partyId,
                                        RespondToClaim respondToClaim) {
        if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, responseDate, partyId, true));
        } else {
            buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, responseDate, partyId, false));
        }
    }

    private void addLipVsLipFullDefenceEvent(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             EventBuckets buckets,
                                             LocalDateTime respondent1ResponseDate) {
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT_ID, true));
        } else {
            buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT_ID, false));
        }
    }

    private void handleSameSolicitorResponse(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             EventBuckets buckets,
                                             LocalDateTime respondent1ResponseDate,
                                             Respondent1DQ respondent1DQ) {
        LocalDateTime respondent2ResponseDate = Optional.ofNullable(caseData.getRespondent2ResponseDate())
            .orElse(respondent1ResponseDate);

        if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT2_ID, true));
        }
        buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent2ResponseDate, RESPONDENT2_ID, false));

        buckets.directionsQuestionnaireEvents.add(
            createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                respondent2ResponseDate,
                RESPONDENT2_ID,
                respondent1DQ,
                caseData.getRespondent2(),
                true
            )
        );
    }

    private Event createDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                     CaseData caseData,
                                                     LocalDateTime responseDate,
                                                     String partyId,
                                                     DQ respondentDQ,
                                                     Party respondent,
                                                     boolean isRespondent1) {
        return buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            responseDate,
            partyId,
            respondentDQ,
            getPreferredCourtCode(respondentDQ),
            respondentResponseSupport.prepareFullDefenceEventText(respondentDQ, caseData, isRespondent1, respondent)
        );
    }

    private boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim respondToClaim) {
        return totalClaimAmount != null
            && Optional.ofNullable(respondToClaim)
                .map(RespondToClaim::getHowMuchWasPaid)
                .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0)
                .orElse(false);
    }

    private record EventBuckets(List<Event> defenceEvents,
                                List<Event> statesPaidEvents,
                                List<Event> directionsQuestionnaireEvents) {
    }

}
