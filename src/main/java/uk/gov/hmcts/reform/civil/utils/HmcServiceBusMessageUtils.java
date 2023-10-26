package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

public class HmcServiceBusMessageUtils {

    private static final List<HmcStatus> PROCESS_MESSAGE_STATUSES = List.of(LISTED, COMPLETED, ADJOURNED, CANCELLED);

    private HmcServiceBusMessageUtils() {
        //NO-OP
    }

    public static boolean isMessageRelevantForService(HmcMessage hmcMessage, PaymentsConfiguration paymentsConfiguration) {
        return paymentsConfiguration.getSpecSiteId().equals(hmcMessage.getHmctsServiceCode())
            || paymentsConfiguration.getSiteId().equals(hmcMessage.getHmctsServiceCode());
    }

    public static boolean statusShouldTriggerCamundaMessage(HmcMessage message) {
        return PROCESS_MESSAGE_STATUSES.contains(message.getHearingUpdate().getHmcStatus());
    }

    public static boolean statusShouldReviewHearingException(HmcMessage message) {
        return EXCEPTION.equals(message.getHearingUpdate().getHmcStatus());
    }
}
