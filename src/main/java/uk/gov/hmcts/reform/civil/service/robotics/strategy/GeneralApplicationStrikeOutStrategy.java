package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_STRUCK_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.GENERAL_FORM_OF_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralApplicationStrikeOutStrategy implements EventHistoryStrategy {

    private static final String STRIKE_OUT_TEXT = "APPLICATION TO Strike Out";

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || !hasTakenOfflineByStaffState(caseData)) {
            return false;
        }
        return !getStrikeOutApplications(caseData).isEmpty();
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        List<Element<GeneralApplication>> applications = getStrikeOutApplications(caseData);
        if (applications.isEmpty()) {
            return;
        }
        log.info(
                "Building general application strike out robotics events for caseId {}",
                caseData.getCcdCaseReference());

        List<Event> eventsToAdd1 =
                applications.stream()
                        .map(
                                app ->
                                        createEvent(
                                                sequenceGenerator.nextSequence(eventHistory),
                                                GENERAL_FORM_OF_APPLICATION.getCode(),
                                                app.getValue().getGeneralAppSubmittedDateGAspec(),
                                                app.getValue().getLitigiousPartyID(),
                                                STRIKE_OUT_TEXT,
                                                new EventDetails().setMiscText(STRIKE_OUT_TEXT)))
                        .toList();
        List<Event> updatedGeneralFormOfApplicationEvents1 =
                eventHistory.getGeneralFormOfApplication() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getGeneralFormOfApplication());
        updatedGeneralFormOfApplicationEvents1.addAll(eventsToAdd1);
        eventHistory.setGeneralFormOfApplication(updatedGeneralFormOfApplicationEvents1);

        List<Event> eventsToAdd2 =
                applications.stream()
                        .map(
                                app ->
                                        createEvent(
                                                sequenceGenerator.nextSequence(eventHistory),
                                                DEFENCE_STRUCK_OUT.getCode(),
                                                app.getValue().getGeneralAppSubmittedDateGAspec(),
                                                app.getValue().getLitigiousPartyID(),
                                                null,
                                                null))
                        .toList();
        List<Event> updatedDefenceStruckOutEvents2 =
                eventHistory.getDefenceStruckOut() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getDefenceStruckOut());
        updatedDefenceStruckOutEvents2.addAll(eventsToAdd2);
        eventHistory.setDefenceStruckOut(updatedDefenceStruckOutEvents2);
    }

    private List<Element<GeneralApplication>> getStrikeOutApplications(CaseData caseData) {
        if (caseData.getGeneralApplications() == null) {
            return emptyList();
        }

        return caseData.getGeneralApplications().stream()
                .filter(element -> element != null && element.getValue() != null)
                .filter(element -> element.getValue().getCaseLink() != null)
                .filter(element -> isStrikeOutApplication(element.getValue()))
                .filter(element -> hasJudgeDecision(caseData, element.getValue()))
                .toList();
    }

    private boolean isStrikeOutApplication(GeneralApplication application) {
        if (application == null || application.getGeneralAppType() == null) {
            return false;
        }
        List<GeneralApplicationTypes> types = application.getGeneralAppType().getTypes();
        return types != null && types.contains(STRIKE_OUT);
    }

    private boolean hasJudgeDecision(CaseData caseData, GeneralApplication application) {
        if (caseData.getGaDetailsMasterCollection() == null
                || application == null
                || application.getCaseLink() == null) {
            return false;
        }

        return caseData.getGaDetailsMasterCollection().stream()
                .map(Element::getValue)
                .filter(Objects::nonNull)
                .anyMatch(details -> matchesStrikeOutDecision(details, application));
    }

    private boolean matchesStrikeOutDecision(
            GeneralApplicationsDetails details, GeneralApplication application) {
        return Objects.equals(
                        details.getCaseLink().getCaseReference(), application.getCaseLink().getCaseReference())
                && PROCEEDS_IN_HERITAGE.getDisplayedValue().equals(details.getCaseState());
    }

    private boolean hasTakenOfflineByStaffState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName()::equals);
    }
}
