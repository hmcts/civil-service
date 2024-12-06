package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1AckExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2AckExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAcknowledgedBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(NOTIFICATION_ACKNOWLEDGED);
    }

    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildAcknowledgementOfServiceReceived(builder, caseData);
    }

    private void buildAcknowledgementOfServiceReceived(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP: {
                List<Event> events = new ArrayList<>();
                if (defendant1AckExists.test(caseData)) {
                    events.add(buildAcknowledgementOfServiceEvent(builder, caseData, true, format(
                        "Defendant: %s has acknowledged: %s",
                        caseData.getRespondent1().getPartyName(),
                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                    )));
                }
                if (defendant2AckExists.test(caseData)) {
                    events.add(buildAcknowledgementOfServiceEvent(builder, caseData, false, format(
                        "Defendant: %s has acknowledged: %s",
                        caseData.getRespondent2().getPartyName(),
                        caseData.getRespondent2ClaimResponseIntentionType().getLabel()
                    )));
                }

                builder.acknowledgementOfServiceReceived(events);
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                String currentTime = time.now().toLocalDate().toString();

                builder
                    .acknowledgementOfServiceReceived(
                        List.of(
                            buildAcknowledgementOfServiceEvent(
                                builder, caseData, true, format(
                                    "[1 of 2 - %s] Defendant: %s has acknowledged: %s",
                                    currentTime,
                                    caseData.getRespondent1().getPartyName(),
                                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                )
                            ),
                            buildAcknowledgementOfServiceEvent(
                                builder, caseData, false, format(
                                    "[2 of 2 - %s] Defendant: %s has acknowledged: %s",
                                    currentTime,
                                    caseData.getRespondent2().getPartyName(),
                                    evaluateRespondent2IntentionType(caseData)
                                )
                            )
                        ));
                break;
            }
            default: {
                if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                    buildAcknowledgementOfServiceSpec(builder, caseData.getRespondent1AcknowledgeNotificationDate());
                    return;
                }

                LocalDateTime dateAcknowledge = caseData.getRespondent1AcknowledgeNotificationDate();
                if (dateAcknowledge == null) {
                    return;
                }

                builder
                    .acknowledgementOfServiceReceived(
                        List.of(
                            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                ?
                                Event.builder()
                                    .eventSequence(prepareEventSequence(builder.build()))
                                    .eventCode("38")
                                    .dateReceived(dateAcknowledge)
                                    .litigiousPartyID("002")
                                    .eventDetails(EventDetails.builder()
                                        .acknowledgeService("Acknowledgement of Service")
                                        .build())
                                    .eventDetailsText("Defendant LR Acknowledgement of Service ")
                                    .build()
                                :
                                buildAcknowledgementOfServiceEvent(
                                    builder, caseData, true,
                                    format(
                                        "responseIntention: %s",
                                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                    )
                                )));
            }
        }
    }

    public String evaluateRespondent2IntentionType(CaseData caseData) {
        if (caseData.getRespondent2ClaimResponseIntentionType() != null) {
            return caseData.getRespondent2ClaimResponseIntentionType().getLabel();
        }
        //represented by same solicitor
        return caseData.getRespondent1ClaimResponseIntentionType().getLabel();
    }

    private Event buildAcknowledgementOfServiceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                     boolean isRespondent1, String eventDetailsText) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED.getCode())
            .dateReceived(isRespondent1
                ? caseData.getRespondent1AcknowledgeNotificationDate()
                : caseData.getRespondent2AcknowledgeNotificationDate())
            .litigiousPartyID(isRespondent1 ? "002" : "003")
            .eventDetails(EventDetails.builder()
                .responseIntention(
                    isRespondent1
                        ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                        : evaluateRespondent2IntentionType(caseData))
                .build())
            .eventDetailsText(eventDetailsText)
            .build();
    }

    private void buildAcknowledgementOfServiceSpec(EventHistory.EventHistoryBuilder builder,
                                                   LocalDateTime dateAcknowledge) {
        builder
            .acknowledgementOfServiceReceived(
                List.of(Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode("38")
                    .dateReceived(dateAcknowledge)
                    .litigiousPartyID("002")
                    .eventDetails(EventDetails.builder()
                        .acknowledgeService("Acknowledgement of Service")
                        .build())
                    .eventDetailsText("Defendant LR Acknowledgement of Service ")
                    .build()));
    }
}
