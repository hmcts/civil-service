package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
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

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_STRUCK_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.GENERAL_FORM_OF_APPLICATION;

@Component
@Order(35)
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
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        List<Element<GeneralApplication>> applications = getStrikeOutApplications(caseData);
        if (applications.isEmpty()) {
            return;
        }

        builder.generalFormOfApplication(applications.stream()
            .map(app -> Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(GENERAL_FORM_OF_APPLICATION.getCode())
                .dateReceived(app.getValue().getGeneralAppSubmittedDateGAspec())
                .litigiousPartyID(app.getValue().getLitigiousPartyID())
                .eventDetailsText(STRIKE_OUT_TEXT)
                .eventDetails(EventDetails.builder().miscText(STRIKE_OUT_TEXT).build())
                .build())
            .toList());

        builder.defenceStruckOut(applications.stream()
            .map(app -> Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(DEFENCE_STRUCK_OUT.getCode())
                .dateReceived(app.getValue().getGeneralAppSubmittedDateGAspec())
                .litigiousPartyID(app.getValue().getLitigiousPartyID())
                .build())
            .toList());
    }

    private List<Element<GeneralApplication>> getStrikeOutApplications(CaseData caseData) {
        if (caseData.getGeneralApplications() == null) {
            return emptyList();
        }

        return caseData.getGeneralApplications().stream()
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
        if (caseData.getGaDetailsMasterCollection() == null || application == null
            || application.getCaseLink() == null) {
            return false;
        }

        return caseData.getGaDetailsMasterCollection().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .anyMatch(details -> matchesStrikeOutDecision(details, application));
    }

    private boolean matchesStrikeOutDecision(GeneralApplicationsDetails details, GeneralApplication application) {
        return STRIKE_OUT.getDisplayedValue().equals(details.getGeneralApplicationType())
            && details.getCaseLink() != null
            && application.getCaseLink() != null
            && Objects.equals(details.getCaseLink().getCaseReference(),
            application.getCaseLink().getCaseReference())
            && PROCEEDS_IN_HERITAGE.getDisplayedValue().equals(details.getCaseState());
    }

    private boolean hasTakenOfflineByStaffState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName()::equals);
    }
}
