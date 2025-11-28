package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PartyRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ExtensionExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsentExtensionEventStrategy implements EventHistoryStrategy {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MM yyyy");

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        boolean hasExtensionData = defendant1ExtensionExists.test(caseData) || defendant2ExtensionExists.test(caseData);
        if (!hasExtensionData) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return true;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return hasTimeExtensionState(stateFlow);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info("Building consent extension robotics events for caseId {}", caseData.getCcdCaseReference());

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        List<Event> events = new ArrayList<>();

        if (defendant1ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionEvent(builder, PartyUtils.respondent1Data(caseData), scenario));
        }

        if (defendant2ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionEvent(builder, PartyUtils.respondent2Data(caseData), scenario));
        }

        if (!events.isEmpty()) {
            builder.consentExtensionFilingDefence(events);
        }
    }

    private Event buildConsentExtensionEvent(EventHistory.EventHistoryBuilder builder,
                                             PartyData party,
                                             MultiPartyScenario scenario) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.CONSENT_EXTENSION_FILING_DEFENCE.getCode())
            .dateReceived(party.getTimeExtensionDate())
            .litigiousPartyID(resolveLitigiousPartyId(party.getRole()))
            .eventDetailsText(getExtensionEventText(scenario, party))
            .eventDetails(EventDetails.builder()
                              .agreedExtensionDate(party.getSolicitorAgreedDeadlineExtension().format(ISO_DATE))
                              .build())
            .build();
    }

    private String resolveLitigiousPartyId(PartyRole role) {
        if (RESPONDENT_ONE.equals(role)) {
            return RESPONDENT_ID;
        }
        if (RESPONDENT_TWO.equals(role)) {
            return RESPONDENT2_ID;
        }
        return null;
    }

    private String getExtensionEventText(MultiPartyScenario scenario, PartyData party) {
        String extensionDate = party.getSolicitorAgreedDeadlineExtension().format(DISPLAY_DATE);
        return switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP -> format("Defendant(s) have agreed extension: %s", extensionDate);
            case ONE_V_TWO_TWO_LEGAL_REP ->
                format("Defendant: %s has agreed extension: %s", party.getDetails().getPartyName(), extensionDate);
            default -> format("agreed extension date: %s", extensionDate);
        };
    }

    private boolean hasTimeExtensionState(StateFlow stateFlow) {
        if (stateFlow == null) {
            return false;
        }
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name ->
                FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName().equals(name)
                    || FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName().equals(name));
    }
}
