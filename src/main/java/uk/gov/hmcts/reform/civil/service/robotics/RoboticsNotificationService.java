package uk.gov.hmcts.reform.civil.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
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
import java.util.Optional;
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
    private final FeatureToggleService featureToggleService;

    public void notifyRobotics(@NotNull CaseData caseData, boolean isMultiParty) {
        requireNonNull(caseData);
        EmailData emailData = prepareEmailData(caseData, isMultiParty);
        sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(CaseData caseData, boolean isMultiParty) {

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
                .message(getMessage(caseData, isMultiParty))
                .subject(getSubject(caseData, triggerEvent, isMultiParty))
                .to(getRoboticsEmailRecipient(isMultiParty))
                .attachments(of(json(roboticsJsonData, fileName)))
                .build();
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    private String getMessage(CaseData caseData, boolean isMultiParty) {
        return isMultiParty ? String.format("Multiparty claim data for %s - %s", caseData.getLegacyCaseReference(),
            caseData.getCcdState()) : String.format("Robotics case data JSON is attached for %s",
                caseData.getLegacyCaseReference());
    }

    private String getSubject(CaseData caseData, String triggerEvent, boolean isMultiParty) {
        return isMultiParty ? String.format("Multiparty claim data for %s - %s - %s", caseData.getLegacyCaseReference(),
            caseData.getCcdState(), triggerEvent) : String.format("Robotics case data for %s",
                caseData.getLegacyCaseReference());
    }

    private String getRoboticsEmailRecipient(boolean isMultiParty) {
        return isMultiParty && !featureToggleService.isRpaContinuousFeedEnabled() ? roboticsEmailConfiguration
            .getMultipartyrecipient() : roboticsEmailConfiguration.getRecipient();
    }

    public static String findLatestEventTriggerReason(EventHistory eventHistory) {

        List<Event> event = eventHistory.getMiscellaneous();
        String triggerReason = getLastDetailsText(event).orElse(null);

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

    private static Optional<String> getLastDetailsText(List<Event> events) {
        if (events.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(events.get(events.size() - 1).getEventDetailsText());
        }
    }

    private static String updateTriggerReason(List<Event> event, String triggerReason) {
        return getLastDetailsText(event).orElse(triggerReason);
    }
}
