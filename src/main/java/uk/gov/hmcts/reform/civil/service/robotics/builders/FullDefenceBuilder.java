package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullDefenceBuilder extends BaseEventBuilder {

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildRespondentFullDefence(builder, caseData);
    }

    private void buildRespondentFullDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> defenceFiledEvents = new ArrayList<>();
        List<Event> statesPaidEvents = new ArrayList<>();
        List<Event> directionsQuestionnaireFiledEvents = new ArrayList<>();
        if (defendant1ResponseExists.test(caseData)) {
            handleDefendant1Response(
                builder,
                caseData,
                defenceFiledEvents,
                statesPaidEvents,
                directionsQuestionnaireFiledEvents
            );
        }
        if (defendant2ResponseExists.test(caseData)) {
            handleDefendant2Response(
                builder,
                caseData,
                statesPaidEvents,
                defenceFiledEvents,
                directionsQuestionnaireFiledEvents
            );
        }

        builder.defenceFiled(defenceFiledEvents);
        builder.statesPaid(statesPaidEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }

    private void handleDefendant1Response(EventHistory.EventHistoryBuilder builder, CaseData caseData, List<Event> defenceFiledEvents,
                                          List<Event> statesPaidEvents, List<Event> directionsQuestionnaireFiledEvents) {
        boolean isRespondent1 = true;
        Party respondent1 = caseData.getRespondent1();
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

        if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
            buildLrVLipFullDefenceEvent(builder, caseData, defenceFiledEvents, statesPaidEvents);
        } else {
            if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
                statesPaidEvents.add(buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    true
                ));
            } else {
                defenceFiledEvents.add(
                    buildDefenceFiledEvent(
                        builder,
                        respondent1ResponseDate,
                        RESPONDENT_ID,
                        false
                    ));
            }
        }
        directionsQuestionnaireFiledEvents.add(
            buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                respondent1ResponseDate,
                RESPONDENT_ID,
                respondent1DQ,
                respondent1,
                isRespondent1
            ));
        if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
            handleDefendant1v2SameSolicitorSameResponse(
                builder,
                caseData,
                statesPaidEvents,
                respondent1ResponseDate,
                defenceFiledEvents,
                directionsQuestionnaireFiledEvents,
                isRespondent1
            );
        }
    }

    private void handleDefendant1v2SameSolicitorSameResponse(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                             List<Event> statesPaidEvents, LocalDateTime respondent1ResponseDate,
                                                             List<Event> defenceFiledEvents, List<Event> directionsQuestionnaireFiledEvents, boolean isRespondent1) {
        Party respondent2 = caseData.getRespondent2();
        Respondent1DQ respondent2DQ = caseData.getRespondent1DQ();
        LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
            ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();

        if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
            statesPaidEvents.add(buildDefenceFiledEvent(
                builder,
                respondent1ResponseDate,
                RESPONDENT2_ID,
                true
            ));
        }
        defenceFiledEvents.add(
            buildDefenceFiledEvent(
                builder,
                respondent2ResponseDate,
                RESPONDENT2_ID,
                false
            ));
        directionsQuestionnaireFiledEvents.add(
            buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                respondent2ResponseDate,
                RESPONDENT2_ID,
                respondent2DQ,
                respondent2,
                isRespondent1
            ));
    }

    private void handleDefendant2Response(EventHistory.EventHistoryBuilder builder, CaseData caseData, List<Event> statesPaidEvents,
                                          List<Event> defenceFiledEvents, List<Event> directionsQuestionnaireFiledEvents) {
        boolean isRespondent1 = false;
        Party respondent2 = caseData.getRespondent2();
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();

        if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim2())) {
            statesPaidEvents.add(
                buildDefenceFiledEvent(
                    builder,
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    true
                ));
        } else {
            defenceFiledEvents.add(
                buildDefenceFiledEvent(
                    builder,
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    false
                ));
        }
        directionsQuestionnaireFiledEvents.add(
            buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                respondent2ResponseDate,
                RESPONDENT2_ID,
                respondent2DQ,
                respondent2,
                isRespondent1
            ));
    }

    private void buildLrVLipFullDefenceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                             List<Event> defenceFiledEvents, List<Event> statesPaidEvents) {
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            statesPaidEvents.add(buildDefenceFiledEvent(
                builder,
                respondent1ResponseDate,
                RESPONDENT_ID,
                true
            ));
        } else {
            defenceFiledEvents.add(
                buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    false
                ));
        }
    }
}
