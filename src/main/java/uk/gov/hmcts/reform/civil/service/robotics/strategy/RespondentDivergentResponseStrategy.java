package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDefenceOrStatesPaidEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2DivergentResponseExists;

@Component
@Order(45)
@RequiredArgsConstructor
public class RespondentDivergentResponseStrategy implements EventHistoryStrategy {

    private static final Set<String> SUPPORTED_STATES = Set.of(
        FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
        FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName(),
        FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName(),
        FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
        FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()
    );

    private static final Set<String> OFFLINE_STATES = Set.of(
        FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
        FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()
    );

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean hasState = stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(SUPPORTED_STATES::contains);

        if (!hasState) {
            return false;
        }

        return defendant1ResponseExists.test(caseData) || defendant2DivergentResponseExists.test(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean goingOffline = OFFLINE_STATES.contains(stateFlow.getState().getName());

        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = respondentResponseSupport.resolveRespondent2ResponseDate(caseData);

        if (defendant1ResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                RespondentResponseTypeSpec responseType = resolveRespondent1SpecResponse(caseData);
                addSpecResponseEvents(builder, caseData, responseType, respondent1ResponseDate, RESPONDENT_ID, true);
            } else {
                addUnspecResponseEvents(
                    builder,
                    caseData,
                    caseData.getRespondent1ClaimResponseType(),
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    true
                );
            }

            if (shouldAddMiscEvent(caseData, goingOffline, true)) {
                addMiscellaneous(builder, caseData, caseData.getRespondent1(), true, respondent1ResponseDate);
            }
        }

        if (defendant2DivergentResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                addSpecResponseEvents(
                    builder,
                    caseData,
                    caseData.getRespondent2ClaimResponseTypeForSpec(),
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    false
                );
            } else {
                addUnspecResponseEvents(
                    builder,
                    caseData,
                    caseData.getRespondent2ClaimResponseType(),
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    false
                );
            }

            if (shouldAddMiscEvent(caseData, goingOffline, false)) {
                addMiscellaneous(builder, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
            }
        }
    }

    private RespondentResponseTypeSpec resolveRespondent1SpecResponse(CaseData caseData) {
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            ? caseData.getClaimant1ClaimResponseTypeForSpec()
            : caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private void addSpecResponseEvents(EventHistory.EventHistoryBuilder builder,
                                       CaseData caseData,
                                       RespondentResponseTypeSpec responseType,
                                       LocalDateTime responseDate,
                                       String respondentId,
                                       boolean isRespondent1) {
        if (responseType == null) {
            return;
        }

        switch (responseType) {
            case FULL_DEFENCE:
                addFullDefenceEvent(builder, caseData, responseDate, respondentId, isRespondent1);
                break;
            case PART_ADMISSION:
                addReceiptOfPartAdmission(builder, responseDate, respondentId);
                break;
            case FULL_ADMISSION:
                addReceiptOfAdmission(builder, responseDate, respondentId);
                break;
            default:
                break;
        }
    }

    private void addUnspecResponseEvents(EventHistory.EventHistoryBuilder builder,
                                         CaseData caseData,
                                         RespondentResponseType responseType,
                                         LocalDateTime responseDate,
                                         String respondentId,
                                         boolean isRespondent1) {
        if (responseType == null) {
            return;
        }

        switch (responseType) {
            case FULL_DEFENCE:
                addFullDefenceEvent(builder, caseData, responseDate, respondentId, isRespondent1);
                break;
            case PART_ADMISSION:
                addReceiptOfPartAdmission(builder, responseDate, respondentId);
                break;
            case FULL_ADMISSION:
                addReceiptOfAdmission(builder, responseDate, respondentId);
                break;
            default:
                break;
        }
    }

    private void addFullDefenceEvent(EventHistory.EventHistoryBuilder builder,
                                     CaseData caseData,
                                     LocalDateTime responseDate,
                                     String respondentId,
                                     boolean isRespondent1) {
        RespondToClaim respondToClaim = resolveRespondToClaim(caseData, respondentId);
        boolean allPaid = isAllPaid(caseData.getTotalClaimAmount(), respondToClaim);

        Event defenceEvent = buildDefenceOrStatesPaidEvent(
            builder,
            sequenceGenerator,
            responseDate,
            respondentId,
            allPaid
        );

        if (allPaid) {
            builder.statesPaid(defenceEvent);
        } else {
            builder.defenceFiled(defenceEvent);
        }

        DQ respondentDQ = isRespondent1 ? caseData.getRespondent1DQ() : caseData.getRespondent2DQ();
        Party respondent = isRespondent1 ? caseData.getRespondent1() : caseData.getRespondent2();
        if (respondent != null) {
            builder.directionsQuestionnaire(createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                responseDate,
                respondentId,
                respondentDQ,
                respondent,
                isRespondent1
            ));
        }
    }

    private RespondToClaim resolveRespondToClaim(CaseData caseData, String respondentId) {
        if (RESPONDENT_ID.equals(respondentId)) {
            return caseData.getRespondToClaim();
        }

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData)) && caseData.getSameSolicitorSameResponse() == YES) {
            return caseData.getRespondToClaim();
        }

        return caseData.getRespondToClaim2();
    }

    private boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim respondToClaim) {
        return totalClaimAmount != null
            && Optional.ofNullable(respondToClaim)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0)
            .orElse(false);
    }

    private Event createDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                     CaseData caseData,
                                                     LocalDateTime responseDate,
                                                     String respondentId,
                                                     DQ respondentDQ,
                                                     Party respondent,
                                                     boolean isRespondent1) {
        return buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            responseDate,
            respondentId,
            respondentDQ,
            getPreferredCourtCode(respondentDQ),
            respondentResponseSupport.prepareFullDefenceEventText(respondentDQ, caseData, isRespondent1, respondent)
        );
    }

    private void addReceiptOfPartAdmission(EventHistory.EventHistoryBuilder builder,
                                           LocalDateTime responseDate,
                                           String respondentId) {
        builder.receiptOfPartAdmission(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(respondentId)
            .build());
    }

    private void addReceiptOfAdmission(EventHistory.EventHistoryBuilder builder,
                                       LocalDateTime responseDate,
                                       String respondentId) {
        builder.receiptOfAdmission(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(RECEIPT_OF_ADMISSION.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(respondentId)
            .build());
    }

    private boolean shouldAddMiscEvent(CaseData caseData, boolean goingOffline, boolean isRespondent1) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseType = isRespondent1
                ? caseData.getRespondent1ClaimResponseTypeForSpec()
                : caseData.getRespondent2ClaimResponseTypeForSpec();
            return goingOffline && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(responseType);
        }

        RespondentResponseType responseType = isRespondent1
            ? caseData.getRespondent1ClaimResponseType()
            : caseData.getRespondent2ClaimResponseType();
        return responseType != FULL_DEFENCE;
    }

    private void addMiscellaneous(EventHistory.EventHistoryBuilder builder,
                                  CaseData caseData,
                                  Party respondent,
                                  boolean isRespondent1,
                                  LocalDateTime responseDate) {
        respondentResponseSupport.addRespondentMiscEvent(builder, sequenceGenerator, caseData, respondent, isRespondent1, responseDate);
    }
}
