package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediationEventStrategy implements EventHistoryStrategy {

    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.hasDefendantAgreedToFreeMediation()
            && caseData.hasClaimantAgreedToFreeMediation()
            && hasMediationState(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building mediation robotics events for caseId {}", caseData.getCcdCaseReference());

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
            List<Event> dqEvents = applicantDetails.stream().map(applicantDetail -> buildApplicantDirectionsQuestionnaireEvent(builder, caseData, applicantDetail))
                .toList();
            builder.directionsQuestionnaireFiled(dqEvents);
        }

        builder.miscellaneous(buildMiscEvent(
            builder,
            sequenceGenerator,
            textFormatter.inMediation(),
            resolveApplicantResponseDate(caseData)
        ));
    }

    private Event buildApplicantDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                             CaseData caseData,
                                                             ClaimantResponseDetails claimantDetails) {
        String preferredCourtCode = RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(caseData.getApplicant1DQ());
        return buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            claimantDetails.getResponseDate(),
            claimantDetails.getLitigiousPartyID(),
            claimantDetails.getDq(),
            preferredCourtCode,
            RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText(
                claimantDetails.getDq(),
                preferredCourtCode
            )
        );
    }

    private LocalDateTime resolveApplicantResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        LocalDateTime now = LocalDateTime.now();
        if (applicant1ResponseDate == null || applicant1ResponseDate.isBefore(now)) {
            return now;
        }
        return applicant1ResponseDate;
    }

    private boolean hasMediationState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.IN_MEDIATION.fullName()::equals);
    }
}
