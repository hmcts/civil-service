package uk.gov.hmcts.reform.civil.jms.hmc.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.camunda.bpm.engine.exception.cmmn.CaseException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hearings.jms.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.hearings.jms.model.HmcMessage;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

import static uk.gov.hmcts.reform.hearings.jms.model.HmcStatus.EXCEPTION;

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
    public void onMessage(JmsBytesMessage message) throws JMSException, HmcEventProcessingException {

        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);

            if (isMessageRelevantForService(hmcMessage)) {
                if (EXCEPTION.equals(hmcMessage.getHearingUpdate().getHmcStatus())) {
                    String caseId = String.valueOf(hmcMessage.getCaseId());
                    String hearingId = hmcMessage.getHearingId();

                    log.info(
                        "Attempting to process message from HMC hearings topic for event {}, Case ID {}, and Hearing ID {}.",
                        hmcMessage.getHearingUpdate().getHmcStatus(), caseId, hearingId);

                    // trigger event for WA
                    StartEventResponse startEventResponse =
                        coreCaseDataService.startUpdate(caseId, CaseEvent.HEARING_MESSAGE_EXCEPTION);
                    CaseDataContent caseDataContent = CaseDataContent.builder()
                        .eventToken(startEventResponse.getToken())
                        .event(Event.builder().id(startEventResponse.getEventId()).build())
                        .data(startEventResponse.getCaseDetails().getData())
                        .build();
                    coreCaseDataService.submitUpdate(caseId, caseDataContent);
                    log.info(
                        "Triggered CCD event for HMC hearings topic for event {}, Case ID {}, and Hearing ID {}.",
                        hmcMessage.getHearingUpdate().getHmcStatus(), caseId, hearingId);

                }
            }
        }  catch (JsonProcessingException | CaseException ex) {
            throw new HmcEventProcessingException(String.format("Unable to successfully deliver HMC message: %s",
                                                                ""), ex);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }

}
