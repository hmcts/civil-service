package uk.gov.hmcts.reform.civil.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.exception.cmmn.CaseException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.exception.HmcTopicEventProcessingException;
import uk.gov.hmcts.reform.hmc.model.jms.HmcMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class HmcHearingsEventTopicListener {

    private final ObjectMapper objectMapper;
    private final PaymentsConfiguration paymentsConfiguration;
    private final CoreCaseDataService coreCaseDataService;

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory")
    public void onMessage(Message message) throws JMSException, HmcTopicEventProcessingException {
        TextMessage msg = null;

        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                log.info("MESSAGE BEAN: Message received: " +
                                msg.getText());
            } else {
                log.info("Message of wrong type: " +
                                   message.getClass().getName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // byte[] messageBytes = new byte[(int) message.getBodyLength()];
        // message.readBytes(messageBytes);
        // String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);
        if (msg != null) {
            String convertedMessage = msg.getText();
            log.info("Message received {}", convertedMessage);

            try {
                log.info("try block");
                HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);
            }  catch (JsonProcessingException | CaseException ex) {
                throw new HmcTopicEventProcessingException(String.format("Unable to successfully deliver HMC message: %s",
                                                                         ""), ex);
            }
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }

}
