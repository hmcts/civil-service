package uk.gov.hmcts.reform.unspec.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.sendgrid.EmailData;
import uk.gov.hmcts.reform.unspec.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.RoboticsDataException;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper;

import javax.validation.constraints.NotNull;

import static java.util.List.of;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.unspec.sendgrid.EmailAttachment.json;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "sendgrid", value = "api-key")
public class RoboticsNotificationService {

    private final SendGridClient sendGridClient;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;
    private final RoboticsDataMapper roboticsDataMapper;
    private final ObjectMapper objectMapper;

    public void notifyRobotics(@NotNull CaseData caseData) {
        requireNonNull(caseData);
        EmailData emailData = prepareEmailData(caseData);
        sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(CaseData caseData) {
        RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
        byte[] roboticsJsonData = toJson(roboticsCaseData);
        String fileName = String.format("CaseData_%s.json", caseData.getLegacyCaseReference());

        return EmailData.builder()
            .message(String.format("Robotics case data JSON is attached for %s", caseData.getLegacyCaseReference()))
            .subject(String.format("Robotics case data for %s", caseData.getLegacyCaseReference()))
            .to(roboticsEmailConfiguration.getRecipient())
            .attachments(of(json(roboticsJsonData, fileName)))
            .build();
    }

    private byte[] toJson(RoboticsCaseData roboticsCaseData) {
        try {
            return objectMapper.writeValueAsBytes(roboticsCaseData);
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
    }
}
