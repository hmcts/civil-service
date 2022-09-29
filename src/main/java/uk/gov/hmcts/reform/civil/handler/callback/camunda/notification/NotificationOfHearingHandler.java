package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.hearing.HearingNotificationEmailConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING;

@Service
@RequiredArgsConstructor
public class NotificationOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final HearingNotificationEmailConfiguration hearingNotificationEmailConfiguration;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_HEARING);
    private static final String REFERENCE_TEMPLATE_CASEWORKER = "default-judgment-caseworker-received-notification-%s";
    public static final String TASK_ID = "NotifyClaimantHearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDJApprovedCaseworker
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyDJApprovedCaseworker(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(hearingNotificationEmailConfiguration.getReceiver(),
                                     notificationsProperties.getNotificationOfHearing(),
                                     addProperties(caseData),
                                     String.format(REFERENCE_TEMPLATE_CASEWORKER,
                                                   caseData.getLegacyCaseReference()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        ));
    }

}

