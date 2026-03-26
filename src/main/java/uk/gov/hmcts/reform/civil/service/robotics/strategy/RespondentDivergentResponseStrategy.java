package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent2DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDefenceOrStatesPaidEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2DivergentResponseExists;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentDivergentResponseStrategy implements EventHistoryStrategy {

    private static final Set<String> SUPPORTED_STATES =
            Set.of(
                    FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                    FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName(),
                    FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName(),
                    FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
                    FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());

    private static final Set<String> OFFLINE_STATES =
            Set.of(
                    FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
                    FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(SUPPORTED_STATES::contains);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        FlowState.Main flowState = null;
        if (caseData != null) {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            flowState = (FlowState.Main) FlowState.fromFullName(stateFlow.getState().getName());
        }
        contribute(eventHistory, caseData, authToken, flowState);
    }

    @Override
    public void contribute(
            EventHistory eventHistory, CaseData caseData, String authToken, FlowState.Main flowState) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building respondent divergent response robotics events for caseId {}",
                caseData.getCcdCaseReference());

        boolean goingOffline =
                flowState != null
                        ? OFFLINE_STATES.contains(flowState.fullName())
                        : OFFLINE_STATES.contains(stateFlowEngine.evaluate(caseData).getState().getName());

        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate =
                ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                        ? caseData.getRespondent1ResponseDate()
                        : caseData.getRespondent2ResponseDate();

        if (defendant1ResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                RespondentResponseTypeSpec responseType = resolveRespondent1SpecResponse(caseData);
                addSpecResponseEvents(
                        eventHistory, caseData, responseType, respondent1ResponseDate, RESPONDENT_ID, true);
            } else {
                addUnspecResponseEvents(
                        eventHistory,
                        caseData,
                        caseData.getRespondent1ClaimResponseType(),
                        respondent1ResponseDate,
                        RESPONDENT_ID,
                        true);
            }

            if (shouldAddMiscEvent(caseData, goingOffline, true)) {
                addMiscellaneous(
                        eventHistory, caseData, caseData.getRespondent1(), true, respondent1ResponseDate);
            }
        }

        if (defendant2DivergentResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                addSpecResponseEvents(
                        eventHistory,
                        caseData,
                        caseData.getRespondent2ClaimResponseTypeForSpec(),
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        false);
            } else {
                addUnspecResponseEvents(
                        eventHistory,
                        caseData,
                        caseData.getRespondent2ClaimResponseType(),
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        false);
            }

            if (shouldAddMiscEvent(caseData, goingOffline, false)) {
                addMiscellaneous(
                        eventHistory, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
            }
        }
    }

    private RespondentResponseTypeSpec resolveRespondent1SpecResponse(CaseData caseData) {
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData))
                ? caseData.getClaimant1ClaimResponseTypeForSpec()
                : caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private void addSpecResponseEvents(
            EventHistory builder,
            CaseData caseData,
            RespondentResponseTypeSpec responseType,
            LocalDateTime responseDate,
            String respondentId,
            boolean isRespondent1) {
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

    private void addUnspecResponseEvents(
            EventHistory builder,
            CaseData caseData,
            RespondentResponseType responseType,
            LocalDateTime responseDate,
            String respondentId,
            boolean isRespondent1) {
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

    private void addFullDefenceEvent(
            EventHistory builder,
            CaseData caseData,
            LocalDateTime responseDate,
            String respondentId,
            boolean isRespondent1) {
        RespondToClaim respondToClaim = resolveRespondToClaim(caseData, respondentId);
        boolean allPaid = isAllPaid(caseData.getTotalClaimAmount(), respondToClaim);

        Event defenceEvent =
                buildDefenceOrStatesPaidEvent(
                        builder, sequenceGenerator, responseDate, respondentId, allPaid);

        if (allPaid) {
            List<Event> updatedStatesPaidEvents1 =
                    builder.getStatesPaid() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(builder.getStatesPaid());
            updatedStatesPaidEvents1.add(defenceEvent);
            builder.setStatesPaid(updatedStatesPaidEvents1);
        } else {
            List<Event> updatedDefenceFiledEvents2 =
                    builder.getDefenceFiled() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(builder.getDefenceFiled());
            updatedDefenceFiledEvents2.add(defenceEvent);
            builder.setDefenceFiled(updatedDefenceFiledEvents2);
        }

        DQ respondentDQ =
                isRespondent1 ? getRespondent1DQOrDefault(caseData) : getRespondent2DQOrDefault(caseData);
        Party respondent = isRespondent1 ? caseData.getRespondent1() : caseData.getRespondent2();
        List<Event> updatedDirectionsQuestionnaireFiledEvents3 =
                builder.getDirectionsQuestionnaireFiled() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getDirectionsQuestionnaireFiled());
        updatedDirectionsQuestionnaireFiledEvents3.add(
                createDirectionsQuestionnaireEvent(
                        builder,
                        caseData,
                        responseDate,
                        respondentId,
                        respondentDQ,
                        respondent,
                        isRespondent1));
        builder.setDirectionsQuestionnaireFiled(updatedDirectionsQuestionnaireFiledEvents3);
    }

    private RespondToClaim resolveRespondToClaim(CaseData caseData, String respondentId) {
        if (RESPONDENT_ID.equals(respondentId)) {
            return caseData.getRespondToClaim();
        }

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                && caseData.getSameSolicitorSameResponse() == YES) {
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

    private Event createDirectionsQuestionnaireEvent(
            EventHistory builder,
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
                respondentResponseSupport.prepareFullDefenceEventText(
                        respondentDQ, caseData, isRespondent1, respondent));
    }

    private void addReceiptOfPartAdmission(
            EventHistory builder, LocalDateTime responseDate, String respondentId) {
        List<Event> updatedReceiptOfPartAdmissionEvents4 =
                builder.getReceiptOfPartAdmission() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getReceiptOfPartAdmission());
        updatedReceiptOfPartAdmissionEvents4.add(
                createEvent(
                        sequenceGenerator.nextSequence(builder),
                        RECEIPT_OF_PART_ADMISSION.getCode(),
                        responseDate,
                        respondentId,
                        null,
                        null));
        builder.setReceiptOfPartAdmission(updatedReceiptOfPartAdmissionEvents4);
    }

    private void addReceiptOfAdmission(
            EventHistory builder, LocalDateTime responseDate, String respondentId) {
        List<Event> updatedReceiptOfAdmissionEvents5 =
                builder.getReceiptOfAdmission() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getReceiptOfAdmission());
        updatedReceiptOfAdmissionEvents5.add(
                createEvent(
                        sequenceGenerator.nextSequence(builder),
                        RECEIPT_OF_ADMISSION.getCode(),
                        responseDate,
                        respondentId,
                        null,
                        null));
        builder.setReceiptOfAdmission(updatedReceiptOfAdmissionEvents5);
    }

    private boolean shouldAddMiscEvent(
            CaseData caseData, boolean goingOffline, boolean isRespondent1) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseType =
                    isRespondent1
                            ? caseData.getRespondent1ClaimResponseTypeForSpec()
                            : caseData.getRespondent2ClaimResponseTypeForSpec();
            return goingOffline && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(responseType);
        }

        RespondentResponseType responseType =
                isRespondent1
                        ? caseData.getRespondent1ClaimResponseType()
                        : caseData.getRespondent2ClaimResponseType();
        return responseType != FULL_DEFENCE;
    }

    private void addMiscellaneous(
            EventHistory builder,
            CaseData caseData,
            Party respondent,
            boolean isRespondent1,
            LocalDateTime responseDate) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && (isSameSolicitorDivergent(caseData)
                        || ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)))) {
            respondentResponseSupport.addSpecDivergentRespondentMiscEvent(
                    builder, sequenceGenerator, caseData, respondent, isRespondent1, responseDate);
            return;
        }
        respondentResponseSupport.addRespondentMiscEvent(
                builder, sequenceGenerator, caseData, respondent, isRespondent1, responseDate);
    }

    private boolean isSameSolicitorDivergent(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                && NO.equals(caseData.getRespondentResponseIsSame());
    }
}
