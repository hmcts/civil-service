package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BreathingSpaceEnterNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER
    );

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::notifyRespondentSolicitor
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitor(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if ("".equals(callbackParams.getRequest().getEventId())) {
            // choose recipients and mail body
        }

//        notificationService.sendMail(
//            "TODO recipient",
//            notificationsProperties.getClaimantSolicitorConfirmsNotToProceed(), // email template
//            addProperties(caseData),
//            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
//        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, PartyUtils.buildPartiesReferences(caseData)
        );
    }
}
