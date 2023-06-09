package uk.gov.hmcts.reform.civil.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("message received");
        TextMessage msg;
        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                String convertedMessage = msg.getText();
                log.info("Message received {}", convertedMessage);
                HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);
                if (isMessageRelevantForService(hmcMessage)) {
                    if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
                        triggerReviewHearingExceptionEvent(hmcMessage);
                    }
                }
            } else {
                log.info("Message of wrong type: {}", message.getClass().getName());
            }
        } catch (JsonProcessingException e) {
            throw  new HmcTopicEventProcessingException(String.format("Unable to successfully deliver HMC message: %s",
                                                                      ""), e);
        }
    }

    private void triggerReviewHearingExceptionEvent(HmcMessage hmcMessage) {
        Long caseId = hmcMessage.getCaseId();
        String hearingId = hmcMessage.getHearingId();
        log.info("Hearing ID: {} for case {} in EXCEPTION status, triggering REVIEW_HEARING_EXCEPTION event",
                 hearingId, caseId
        );

        // trigger event for WA
        // StartEventResponse startEventResponse =
        //    coreCaseDataService.startUpdate(String.valueOf(caseId), REVIEW_HEARING_EXCEPTION);
        // CaseDataContent caseDataContent = CaseDataContent.builder()
        //     .eventToken(startEventResponse.getToken())
        //     .event(Event.builder().id(startEventResponse.getEventId()).build())
        //     .data(startEventResponse.getCaseDetails().getData())
        // .build();
        // coreCaseDataService.submitUpdate(String.valueOf(caseId), caseDataContent);
        log.info(
            "Triggered REVIEW_HEARING_EXCEPTION event for Case ID {}, and Hearing ID {}.",
            caseId, hearingId);
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }

}
