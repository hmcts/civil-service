package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED;

@Service
public class NotifyDefendantStayUpdateRequestedHandler extends AbstractNotifyManageStayDefendantHandler {

    private static final String TASK_ID = "NotifyDefendantStayUpdateRequested";
    private static final String TASK_ID_DEFENDANT_2 = "NotifyDefendant2StayUpdateRequested";
    private static final String REFERENCE_TEMPLATE = "stay-update-requested-defendant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED,
                                                          NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED);

    public NotifyDefendantStayUpdateRequestedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties,
                                                     NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            return isBilingual(caseData)
                ? notificationsProperties.getNotifyLipBilingualStayUpdateRequested()
                : notificationsProperties.getNotifyLipStayUpdateRequested();
        } else {
            return notificationsProperties.getNotifyLRStayUpdateRequested();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isRespondentSolicitor2(callbackParams)) {
            return TASK_ID_DEFENDANT_2;
        } else {
            return TASK_ID;
        }
    }

    @Override
    protected boolean isRespondentSolicitor2(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED.equals(caseEvent);
    }
}
