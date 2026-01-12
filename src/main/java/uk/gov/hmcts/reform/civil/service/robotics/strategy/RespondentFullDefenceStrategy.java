package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
        if (caseData == null) {
            return false;
        }

        boolean hasFullDefenceResponse = hasFullDefenceResponse(caseData);

        if (!hasFullDefenceResponse) {
            return false;
        }

        return defendant1ResponseExists.test(caseData)
            || defendant2ResponseExists.test(caseData)
            || defendant1v2SameSolicitorSameResponse.test(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building respondent full defence robotics events for caseId {}", caseData.getCcdCaseReference());

        EventBuckets buckets = initialiseBuckets(builder.build());

        processRespondent1(builder, caseData, buckets);
        processRespondent2(builder, caseData, buckets);

        builder.defenceFiled(buckets.defenceEvents);
        builder.statesPaid(buckets.statesPaidEvents);
        builder.defenceAndCounterClaim(buckets.defenceAndCounterClaimEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(buckets.directionsQuestionnaireEvents);
    }

    private EventBuckets initialiseBuckets(EventHistory history) {
        List<Event> defenceEvents = sanitise(history.getDefenceFiled());
        List<Event> statesPaidEvents = sanitise(history.getStatesPaid());
        List<Event> directionsQuestionnaireEvents = new ArrayList<>();
        List<Event> counterClaimEvents = sanitise(history.getDefenceAndCounterClaim());
        return new EventBuckets(defenceEvents, statesPaidEvents, directionsQuestionnaireEvents, counterClaimEvents);
    }

    private List<Event> sanitise(List<Event> events) {
        List<Event> result = new ArrayList<>(Optional.ofNullable(events).orElse(List.of()));
        result.removeIf(event -> event.getEventCode() == null);
        return result;
    }

    private void processRespondent1(EventHistory.EventHistoryBuilder builder,
                                    CaseData caseData,
                                    EventBuckets buckets) {
        if (!defendant1ResponseExists.test(caseData)) {
            return;
        }

        if (respondentLacksFullDefenceResponse(caseData, true)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent1ResponseDate();
        if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
            addLipVsLipFullDefenceEvent(builder, caseData, buckets, responseDate);
        } else {
            addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT_ID, caseData.getRespondToClaim());
        }
        addCounterClaimEventIfNeeded(builder, caseData, buckets, responseDate, RESPONDENT_ID, true);

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
        boolean sameSolicitorDifferentResponse = hasSameSolicitorDifferentResponse(caseData);
        if (!defendant2ResponseExists.test(caseData) && !sameSolicitorDifferentResponse) {
            return;
        }

        if (respondentLacksFullDefenceResponse(caseData, false)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent2ResponseDate();
        if (responseDate == null) {
            return;
        }
        RespondToClaim respondToClaim = shouldUseRespondent1Response(caseData)
            ? caseData.getRespondToClaim()
            : caseData.getRespondToClaim2();

        addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT2_ID, respondToClaim);
        addCounterClaimEventIfNeeded(builder, caseData, buckets, responseDate, RESPONDENT2_ID, false);

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
        addCounterClaimEventIfNeeded(builder, caseData, buckets, respondent2ResponseDate, RESPONDENT2_ID, false);

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

    private boolean shouldUseRespondent1Response(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getSameSolicitorSameResponse() == YES;
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

    private boolean hasFullDefenceResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return hasSpecResponseOfType(caseData);
        }
        return RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
    }

    private boolean respondentLacksFullDefenceResponse(CaseData caseData, boolean isRespondent1) {
        if (caseData == null) {
            return true;
        }
        if (!CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return !RespondentResponseType.FULL_DEFENCE.equals(
                isRespondent1 ? caseData.getRespondent1ClaimResponseType() : caseData.getRespondent2ClaimResponseType()
            );
        }
        RespondentResponseTypeSpec responseType = getSpecResponseType(caseData, isRespondent1);
        return !(RespondentResponseTypeSpec.FULL_DEFENCE.equals(responseType));
    }

    private void addCounterClaimEventIfNeeded(EventHistory.EventHistoryBuilder builder,
                                              CaseData caseData,
                                              EventBuckets buckets,
                                              LocalDateTime responseDate,
                                              String partyId,
                                              boolean isRespondent1) {
        if (!isCounterClaimResponse(caseData, isRespondent1) || responseDate == null) {
            return;
        }
        buckets.defenceAndCounterClaimEvents.add(
            RoboticsEventSupport.buildCounterClaimEvent(builder, sequenceGenerator, responseDate, partyId)
        );
        Party respondent = isRespondent1 ? caseData.getRespondent1() : caseData.getRespondent2();
        if (respondent != null) {
            respondentResponseSupport.addRespondentMiscEvent(
                builder,
                sequenceGenerator,
                caseData,
                respondent,
                isRespondent1,
                responseDate
            );
        }
    }

    private boolean isCounterClaimResponse(CaseData caseData, boolean isRespondent1) {
        if (caseData == null) {
            return false;
        }
        if (CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseType = getSpecResponseType(caseData, isRespondent1);
            return RespondentResponseTypeSpec.COUNTER_CLAIM.equals(responseType);
        }
        RespondentResponseType responseType = isRespondent1
            ? caseData.getRespondent1ClaimResponseType()
            : caseData.getRespondent2ClaimResponseType();
        return RespondentResponseType.COUNTER_CLAIM.equals(responseType);
    }

    private record EventBuckets(List<Event> defenceEvents,
                                List<Event> statesPaidEvents,
                                List<Event> directionsQuestionnaireEvents,
                                List<Event> defenceAndCounterClaimEvents) {
    }

    private RespondentResponseTypeSpec getSpecResponseType(CaseData caseData, boolean isRespondent1) {
        if (caseData == null || !CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return null;
        }
        RespondentResponseTypeSpec responseType = isRespondent1
            ? caseData.getRespondent1ClaimResponseTypeForSpec()
            : caseData.getRespondent2ClaimResponseTypeForSpec();
        if (responseType != null) {
            return responseType;
        }
        if (getMultiPartyScenario(caseData).equals(MultiPartyScenario.TWO_V_ONE)) {
            return Optional.ofNullable(caseData.getClaimant1ClaimResponseTypeForSpec())
                .orElse(caseData.getClaimant2ClaimResponseTypeForSpec());
        }
        return null;
    }

    private boolean hasSpecResponseOfType(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
    }

    private boolean hasSameSolicitorDifferentResponse(CaseData caseData) {
        return caseData != null
            && YesOrNo.YES.equals(caseData.getRespondent2SameLegalRepresentative())
            && caseData.getRespondent1ResponseDate() != null
            && YesOrNo.NO.equals(caseData.getRespondentResponseIsSame());
    }

}
