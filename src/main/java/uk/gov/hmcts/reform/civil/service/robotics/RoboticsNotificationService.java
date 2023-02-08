package uk.gov.hmcts.reform.civil.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import static java.util.List.of;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
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
    private final FeatureToggleService toggleService;

    public void notifyRobotics(@NotNull CaseData caseData, boolean isMultiParty, String authToken) {
        requireNonNull(caseData);
        log.info(String.format("Start notifyRobotics and case data is not null %s", caseData.getLegacyCaseReference()));
        Optional<EmailData> emailData = prepareEmailData(caseData, isMultiParty, authToken);
        emailData.ifPresent(data -> sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), data));
    }

    private boolean canSendEmailSpec() {
        try {
            return toggleService.isSpecRpaContinuousFeedEnabled();
        } catch (Throwable e) {
            log.error("Exception on launchdarkly check", e);
            return false;
        }
    }

    private Optional<EmailData> prepareEmailData(CaseData caseData, boolean isMultiParty, String authToken) {

        log.info(String.format("Start prepareEmailData %s", caseData.getLegacyCaseReference()));
        byte[] roboticsJsonData;
        try {
            String fileName = String.format("CaseData_%s.json", caseData.getLegacyCaseReference());
            String triggerEvent;

            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                if (canSendEmailSpec()) {
                    RoboticsCaseDataSpec roboticsCaseData = roboticsDataMapperForSpec.toRoboticsCaseData(caseData);
                    triggerEvent = findLatestEventTriggerReasonSpec(roboticsCaseData.getEvents());
                    roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
                } else {
                    return Optional.empty();
                }
            } else {
                RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData, authToken);
                triggerEvent = findLatestEventTriggerReason(roboticsCaseData.getEvents());
                roboticsJsonData = roboticsCaseData.toJsonString().getBytes();
                log.info(String.format("triggerEvent %s", triggerEvent));
            }
            return Optional.of(EmailData.builder()
                .message(getMessage(caseData, isMultiParty))
                .subject(getSubject(caseData, triggerEvent, isMultiParty))
                .to(getRoboticsEmailRecipient(
                    isMultiParty,
                    SPEC_CLAIM.equals(caseData.getCaseAccessCategory())))
                .attachments(of(json(roboticsJsonData, fileName)))
                .build());
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }

    private String getMessage(CaseData caseData, boolean isMultiParty) {
        return isMultiParty ? String.format("Multiparty claim data for %s - %s", caseData.getLegacyCaseReference(),
                                            caseData.getCcdState()
        ) : String.format(
            "Robotics case data JSON is attached for %s",
            caseData.getLegacyCaseReference()
        );
    }

    private String getSubject(CaseData caseData, String triggerEvent, boolean isMultiParty) {
        String subject = null;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            subject = isMultiParty ? String.format("Multiparty LR v LR Case Data for %s - %s - %s",
                                                   caseData.getLegacyCaseReference(),
                                                   caseData.getCcdState(), triggerEvent
            ) : String.format(
                "LR v LR Case Data for %s",
                caseData.getLegacyCaseReference()
            );
        } else {
            subject = isMultiParty ? String.format("Multiparty claim data for %s - %s - %s",
                                                   caseData.getLegacyCaseReference(),
                                                   caseData.getCcdState(), triggerEvent
            ) : String.format(
                "Robotics case data for %s",
                caseData.getLegacyCaseReference()
            );
        }
        log.info(String.format("Subject-------- %s", subject));
        return subject;
    }

    private String getRoboticsEmailRecipient(boolean isMultiParty, boolean isSpecClaim) {
        if (isSpecClaim) {
            log.info(String.format("EMAIl:--------- %s", roboticsEmailConfiguration.getSpecRecipient()));
            return roboticsEmailConfiguration.getSpecRecipient();
        }
        String recipient = isMultiParty ? roboticsEmailConfiguration
            .getMultipartyrecipient() : roboticsEmailConfiguration.getRecipient();

        log.info(String.format("EMAIl:--------- %s", recipient));
        return recipient;
    }

    public static String findLatestEventTriggerReason(EventHistory eventHistory) {
        List<Event> events = flatEvents(eventHistory);
        events.sort(Comparator.comparing(Event::getDateReceived));

        List<Event> lastMiscellaneousEvent = events.stream()
            .filter(event ->
                        event.getDateReceived().equals(events.get(events.size() - 1).getDateReceived())
                            && event.getEventCode().equals(MISCELLANEOUS.getCode()))
            .collect(Collectors.toList());

        return lastMiscellaneousEvent.size() == 1 ? lastMiscellaneousEvent.get(0).getEventDetailsText()
            : events.get(events.size() - 1).getEventDetailsText();
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
            eventHistory.getDirectionsQuestionnaireFiled(),
            eventHistory.getBreathingSpaceEntered(),
            eventHistory.getBreathingSpaceLifted(),
            eventHistory.getBreathingSpaceMentalHealthEntered(),
            eventHistory.getBreathingSpaceMentalHealthLifted()
        );
        return eventsList.stream()
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(event -> event.getDateReceived() != null)
            .collect(Collectors.toList());
    }

    public static String findLatestEventTriggerReasonSpec(EventHistory eventHistory) {

        List<Event> event = eventHistory.getMiscellaneous();
        String triggerReason = getLastDetailsText(event).orElse(null);

        triggerReason = updateTriggerReason(eventHistory.getAcknowledgementOfServiceReceived(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getConsentExtensionFilingDefence(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getDefenceFiled(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getDefenceAndCounterClaim(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReceiptOfPartAdmission(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReceiptOfAdmission(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getReplyToDefence(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getBreathingSpaceEntered(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getBreathingSpaceLifted(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getBreathingSpaceMentalHealthEntered(), triggerReason);
        triggerReason = updateTriggerReason(eventHistory.getBreathingSpaceMentalHealthLifted(), triggerReason);
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
