package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ExtensionExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDetailsNotifiedTimeExtensionBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
            CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildConsentExtensionFilingDefence(builder, caseData);
    }

    protected void buildConsentExtensionFilingDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        List<Event> events = new ArrayList<>();
        if (defendant1ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent1Data(caseData), scenario, prepareEventSequence(builder.build())
            ));
        }
        if (defendant2ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent2Data(caseData), scenario, prepareEventSequence(builder.build())
            ));
        }
        builder.consentExtensionFilingDefence(events);
    }
}
