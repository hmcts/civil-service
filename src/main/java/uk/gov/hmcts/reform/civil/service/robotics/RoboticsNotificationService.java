package uk.gov.hmcts.reform.civil.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.robotics.exception.RoboticsDataException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import static java.util.List.of;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment.json;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "sendgrid", value = "api-key")
public class RoboticsNotificationService {

    private final SendGridClient sendGridClient;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;
    private final RoboticsDataMapper roboticsDataMapper;
    private final RoboticsDataMapperForSpec roboticsDataMapperForSpec;

    public void notifyRobotics(@NotNull CaseData caseData, boolean multiPartyScenario) {
        requireNonNull(caseData);
        EmailData emailData = !multiPartyScenario
            ? prepareEmailData(caseData) : prepareEmailDataMultiParty(caseData);
        sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(CaseData caseData) {
        RoboticsCaseData roboticsCaseData;
        RoboticsCaseDataSpec roboticsCaseDataSpec;
        byte[] roboticsJsonData;
        try {
            if (null != caseData.getSuperClaimType() && caseData.getSuperClaimType().equals(SPEC_CLAIM)) {
                roboticsCaseDataSpec = roboticsDataMapperForSpec.toRoboticsCaseData(caseData);
                roboticsJsonData = roboticsCaseDataSpec.toJsonString().getBytes();
            } else {
                roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
                roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
            }

            String fileName = String.format("CaseData_%s.json", caseData.getLegacyCaseReference());

            return EmailData.builder()
                .message(String.format("Robotics case data JSON is attached for %s", caseData.getLegacyCaseReference()))
                .subject(String.format("Robotics case data for %s", caseData.getLegacyCaseReference()))
                .to(roboticsEmailConfiguration.getRecipient())
                .attachments(of(json(roboticsJsonData, fileName)))
                .build();
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    private EmailData prepareEmailDataMultiParty(CaseData caseData) {

        byte[] roboticsJsonData;
        try {
            String fileName = String.format("CaseData_%s.json", caseData.getLegacyCaseReference());
            String triggerEvent;

            if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
                RoboticsCaseDataSpec roboticsCaseData = roboticsDataMapperForSpec.toRoboticsCaseData(caseData);
                triggerEvent = findLatestEventTriggerReason(roboticsCaseData.getEvents());
                roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
            } else {
                RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
                triggerEvent = findLatestEventTriggerReason(roboticsCaseData.getEvents());
                roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
            }

            return EmailData.builder()
                .message(String.format(
                    "Multiparty claim data for %s",
                    caseData.getLegacyCaseReference() + " - " + caseData.getCcdState()))
                .subject(String.format("Multiparty claim data for %s", caseData.getLegacyCaseReference()
                    + " - " + caseData.getCcdState() + " - " + triggerEvent))
                .to(roboticsEmailConfiguration.getMultipartyrecipient())
                .attachments(of(json(roboticsJsonData, fileName)))
                .build();
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    public static String findLatestEventTriggerReason(EventHistory eventHistory) {
        List<Event> events = flatEvents(eventHistory);
        events.sort(Comparator.comparing(Event::getDateReceived));
        return events.get(events.size() - 1).getEventDetailsText();
    }

    private static List<Event> flatEvents(EventHistory eventHistory) {
        List<List<Event>> eventsList = Lists.newArrayList(
            eventHistory.getMiscellaneous(),
            eventHistory.getAcknowledgementOfServiceReceived(),
            eventHistory.getConsentExtensionFilingDefence(),
            eventHistory.getDefenceFiled(),
            eventHistory.getDefenceAndCounterClaim(),
            eventHistory.getReceiptOfPartAdmission(),
            eventHistory.getReceiptOfAdmission(),
            eventHistory.getReplyToDefence(),
            eventHistory.getDirectionsQuestionnaireFiled()
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(event -> event.getDateReceived() != null)
            .collect(Collectors.toList());
    }
}
