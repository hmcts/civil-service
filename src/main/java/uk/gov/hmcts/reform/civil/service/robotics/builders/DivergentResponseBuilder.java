package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2DivergentResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class DivergentResponseBuilder extends BaseEventBuilder {

    private final IStateFlowEngine stateFlowEngine;

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED,
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED,
            DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE,
            DIVERGENT_RESPOND_GO_OFFLINE);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        boolean isOffline = false;
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();

        List<State> stateHistory = stateFlowEngine.evaluate(caseData).getStateHistory();
        if (stateHistory.get(stateHistory.size() - 1).getName().contains("OFFLINE")) {
            isOffline = true;
        }
        buildRespondentDivergentResponse(builder, caseData, isOffline);
    }

    private void buildRespondentDivergentResponse(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                  boolean goingOffline) {
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = getRespondent2ResponseDate(caseData);

        if (defendant1ResponseExists.test(caseData)) {
            handleRespondentResponse(builder, caseData, RESPONDENT_ID, respondent1ResponseDate, true, goingOffline);
        }

        if (defendant2DivergentResponseExists.test(caseData)) {
            handleRespondentResponse(builder, caseData, RESPONDENT2_ID, respondent2ResponseDate, false, goingOffline);
        }
    }

    private LocalDateTime getRespondent2ResponseDate(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)
            ? caseData.getRespondent1ResponseDate()
            : caseData.getRespondent2ResponseDate();
    }

    private void handleRespondentResponse(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                          String respondentId, LocalDateTime responseDate,
                                          boolean isRespondent1, boolean goingOffline) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec respondentResponseTypeSpec = getRespondentResponseTypeForSpec(caseData, respondentId, isRespondent1);
            buildRespondentResponseEventForSpec(builder, caseData, respondentResponseTypeSpec, responseDate, respondentId);
        } else {
            buildRespondentResponseEvent(builder, caseData, getRespondentResponseType(caseData, respondentId), responseDate, respondentId);
        }

        if (shouldAddMiscEvent(caseData, isRespondent1, goingOffline)) {
            addMiscellaneousEvent(builder, caseData, responseDate, isRespondent1);
        }
    }

    private RespondentResponseType getRespondentResponseType(CaseData caseData, String respondentId) {
        return RESPONDENT_ID.equals(respondentId) ? caseData.getRespondent1ClaimResponseType() : caseData.getRespondent2ClaimResponseType();
    }

    private RespondentResponseTypeSpec getRespondentResponseTypeForSpec(CaseData caseData, String respondentId, boolean isRespondent1) {
        if (MultiPartyScenario.TWO_V_ONE.equals(getMultiPartyScenario(caseData)) && isRespondent1) {
            return caseData.getClaimant1ClaimResponseTypeForSpec();
        }
        return RESPONDENT_ID.equals(respondentId) ? caseData.getRespondent1ClaimResponseTypeForSpec() : caseData.getRespondent2ClaimResponseTypeForSpec();
    }

    private boolean shouldAddMiscEvent(CaseData caseData, boolean isRespondent1, boolean goingOffline) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseTypeSpec = isRespondent1 ? caseData.getRespondent1ClaimResponseTypeForSpec() : caseData.getRespondent2ClaimResponseTypeForSpec();
            return goingOffline && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(responseTypeSpec);
        } else {
            RespondentResponseType responseType = isRespondent1 ? caseData.getRespondent1ClaimResponseType() : caseData.getRespondent2ClaimResponseType();
            return !FULL_DEFENCE.equals(responseType);
        }
    }

    private void addMiscellaneousEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                       LocalDateTime responseDate, boolean isRespondent1) {
        String miscText = prepareRespondentResponseText(caseData, isRespondent1 ? caseData.getRespondent1() : caseData.getRespondent2(), isRespondent1);
        builder.miscellaneous(Event.builder()
            .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(responseDate)
            .eventDetailsText(miscText)
            .eventDetails(EventDetails.builder().miscText(miscText).build())
            .build());
    }
}


