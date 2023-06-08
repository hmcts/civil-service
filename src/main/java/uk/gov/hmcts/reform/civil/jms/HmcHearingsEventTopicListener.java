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

import static uk.gov.hmcts.reform.hmc.model.jms.HmcStatus.EXCEPTION;

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
                if (msg != null) {
                    String convertedMessage = msg.getText();
                    log.info("Message received {}", convertedMessage);
                    HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);
                    if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
                        log.info("Hearing ID: {} for case {} in EXCEPTION status, triggering event for WA",
                                 hmcMessage.getHearingId(), hmcMessage.getCaseId());
                    }
                }
            } else {
                log.info("Message of wrong type: " +
                                   message.getClass().getName());
            }
        } catch (JsonProcessingException | CaseException e) {
            throw  new HmcTopicEventProcessingException(String.format("Unable to successfully deliver HMC message: %s",
                                                                      ""), e);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }

}
