package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@Order(44)
@RequiredArgsConstructor
public class RespondentFullAdmissionStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && hasFullAdmissionState(caseData)
            && (defendant1ResponseExists.test(caseData) || defendant2ResponseExists.test(caseData)
            || defendant1v2SameSolicitorSameResponse.test(caseData));
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        if (defendant1ResponseExists.test(caseData)) {
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
            addReceiptOfAdmission(builder, respondent1ResponseDate, RESPONDENT_ID);
            addMiscellaneous(builder, caseData, caseData.getRespondent1(), true, respondent1ResponseDate);
            addLipVsLrMisc(builder, caseData);

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate() != null
                    ? caseData.getRespondent2ResponseDate()
                    : respondent1ResponseDate;
                addReceiptOfAdmission(builder, respondent2ResponseDate, RESPONDENT2_ID);
                addMiscellaneous(builder, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
            addReceiptOfAdmission(builder, respondent2ResponseDate, RESPONDENT2_ID);
            addMiscellaneous(builder, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
        }
    }

    private void addReceiptOfAdmission(EventHistory.EventHistoryBuilder builder,
                                       LocalDateTime responseDate,
                                       String partyId) {
        builder.receiptOfAdmission(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.RECEIPT_OF_ADMISSION.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .build());
    }

    private void addMiscellaneous(EventHistory.EventHistoryBuilder builder,
                                  CaseData caseData,
                                  Party respondent,
                                  boolean isRespondent1,
                                  LocalDateTime responseDate) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return;
        }

        String message = respondentResponseSupport.prepareRespondentResponseText(caseData, respondent, isRespondent1);
        builder.miscellaneous(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(responseDate)
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build());
    }

    private void addLipVsLrMisc(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (!caseData.isLipvLROneVOne()) {
            return;
        }

        String message = textFormatter.lipVsLrFullOrPartAdmissionReceived();
        builder.miscellaneous(Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(timelineHelper.now())
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build());
    }

    private boolean hasFullAdmissionState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.FULL_ADMISSION.fullName()::equals);
    }
}
