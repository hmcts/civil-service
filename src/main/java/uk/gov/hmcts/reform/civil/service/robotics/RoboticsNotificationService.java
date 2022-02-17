package uk.gov.hmcts.reform.civil.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;
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
        RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
        try {
            roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
            String triggerEvent = findLatestEventTriggerReason(roboticsCaseData.getEvents());
            String fileName = String.format("CaseData_%s.json", caseData.getLegacyCaseReference());

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

        List<Event> event = eventHistory.getMiscellaneous();
        String triggerReason = event.get(event.size() - 1).getEventDetailsText();

        triggerReason = updateTriggerReason(eventHistory.getAcknowledgementOfServiceReceived(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getConsentExtensionFilingDefence(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getDefenceFiled(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getDefenceAndCounterClaim(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReceiptOfPartAdmission(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReceiptOfAdmission(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReplyToDefence(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getDirectionsQuestionnaireFiled(), triggerReason);

        return triggerReason;
    }

    public static String updateTriggerReason(List<Event> event, String triggerReason) {
        if (event.get(event.size() - 1).getEventDetailsText() != null) {
            triggerReason = event.get(event.size() - 1).getEventDetailsText();
        }
        return triggerReason;
    }
}
