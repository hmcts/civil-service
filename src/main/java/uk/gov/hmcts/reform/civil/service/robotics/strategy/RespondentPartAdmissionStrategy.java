package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@Order(43)
@RequiredArgsConstructor
public class RespondentPartAdmissionStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean hasState = stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.PART_ADMISSION.fullName()::equals);

        if (!hasState) {
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

        List<Event> existingDirections = Optional.ofNullable(builder.build().getDirectionsQuestionnaireFiled())
            .orElse(List.of());
        builder.clearDirectionsQuestionnaireFiled();
        existingDirections
            .stream()
            .filter(event -> event.getEventCode() != null)
            .forEach(builder::directionsQuestionnaire);

        if (defendant1ResponseExists.test(caseData)) {
            addLipVsLrMisc(builder, caseData);

            if (useStatesPaid(caseData)) {
                builder.statesPaid(addStatesPaidEvent(builder, caseData.getRespondent1ResponseDate()));
            } else {
                builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(builder, caseData.getRespondent1ResponseDate(), RESPONDENT_ID));
            }

            addRespondentMisc(builder, caseData, caseData.getRespondent1(), true, caseData.getRespondent1ResponseDate());

            builder.directionsQuestionnaire(addDirectionsQuestionnaireFiledEvent(
                builder,
                caseData,
                caseData.getRespondent1ResponseDate(),
                RESPONDENT_ID,
                caseData.getRespondent1DQ(),
                caseData.getRespondent1(),
                true
            ));

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(
                    builder,
                    resolveRespondent2ResponseDate(caseData),
                    RESPONDENT2_ID
                ));
                addRespondentMisc(
                    builder,
                    caseData,
                    caseData.getRespondent2(),
                    false,
                    resolveRespondent2ResponseDate(caseData)
                );
                builder.directionsQuestionnaire(addDirectionsQuestionnaireFiledEvent(
                    builder,
                    caseData,
                    resolveRespondent2ResponseDate(caseData),
                    RESPONDENT2_ID,
                    caseData.getRespondent1DQ(),
                    caseData.getRespondent2(),
                    true
                ));
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(builder, caseData.getRespondent2ResponseDate(), RESPONDENT2_ID));
            addRespondentMisc(builder, caseData, caseData.getRespondent2(), false, caseData.getRespondent2ResponseDate());

            builder.directionsQuestionnaire(addDirectionsQuestionnaireFiledEvent(
                builder,
                caseData,
                caseData.getRespondent2ResponseDate(),
                RESPONDENT2_ID,
                caseData.getRespondent2DQ(),
                caseData.getRespondent2(),
                false
            ));
        }
    }

    private boolean useStatesPaid(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getSpecDefenceAdmittedRequired() == YES;
    }

    private Event addReceiptOfPartAdmissionEvent(EventHistory.EventHistoryBuilder builder,
                                                 LocalDateTime responseDate,
                                                 String partyId) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.RECEIPT_OF_PART_ADMISSION.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .build();
    }

    private Event addStatesPaidEvent(EventHistory.EventHistoryBuilder builder,
                                     LocalDateTime responseDate) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.STATES_PAID.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID)
            .build();
    }

    private void addRespondentMisc(EventHistory.EventHistoryBuilder builder,
                                   CaseData caseData,
                                   Party respondent,
                                   boolean isRespondent1,
                                   LocalDateTime responseDate) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return;
        }

        builder.miscellaneous(createRespondentMiscEvent(builder, caseData, respondent, isRespondent1, responseDate));
    }

    private void addLipVsLrMisc(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (!caseData.isLipvLROneVOne()) {
            return;
        }

        builder.miscellaneous(createLipVsLrMiscEvent(builder));
    }

    private Event addDirectionsQuestionnaireFiledEvent(EventHistory.EventHistoryBuilder builder,
                                                       CaseData caseData,
                                                       LocalDateTime responseDate,
                                                       String partyId,
                                                       DQ dq,
                                                       Party respondent,
                                                       boolean isRespondent1) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .eventDetailsText(prepareEventDetailsText(caseData, respondent, dq, isRespondent1))
            .eventDetails(EventDetails.builder()
                .stayClaim(isStayClaim(dq))
                .preferredCourtCode(getPreferredCourtCode(dq))
                .preferredCourtName("")
                .build())
            .build();
    }

    private String prepareEventDetailsText(CaseData caseData,
                                           Party respondent,
                                           DQ dq,
                                           boolean isRespondent1) {
        String paginatedMessage = "";
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            paginatedMessage = respondentResponseSupport.getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
        }

        return format(
            "%sDefendant: %s has responded: %s; preferredCourtCode: %s; stayClaim: %s",
            paginatedMessage,
            respondent.getPartyName(),
            getResponseTypeForRespondent(caseData, respondent),
            getPreferredCourtCode(dq),
            isStayClaim(dq)
        );
    }

    private LocalDateTime resolveRespondent2ResponseDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent2ResponseDate())
            .orElse(caseData.getRespondent1ResponseDate());
    }

    private Event createRespondentMiscEvent(EventHistory.EventHistoryBuilder builder,
                                            CaseData caseData,
                                            Party respondent,
                                            boolean isRespondent1,
                                            LocalDateTime responseDate) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(responseDate)
            .eventDetailsText(respondentResponseSupport.prepareRespondentResponseText(caseData, respondent, isRespondent1))
            .eventDetails(EventDetails.builder()
                .miscText(respondentResponseSupport.prepareRespondentResponseText(caseData, respondent, isRespondent1))
                .build())
            .build();
    }

    private Event createLipVsLrMiscEvent(EventHistory.EventHistoryBuilder builder) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(timelineHelper.now())
            .eventDetailsText(textFormatter.lipVsLrFullOrPartAdmissionReceived())
            .eventDetails(EventDetails.builder()
                .miscText(textFormatter.lipVsLrFullOrPartAdmissionReceived())
                .build())
            .build();
    }
}
