package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class ChangeOfRepresentationNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_FORMER_SOLICITOR,
        NOTIFY_OTHER_SOLICITOR_1,
        NOTIFY_OTHER_SOLICITOR_2
    );

    public static final String TASK_ID_NOTIFY_FORMER_SOLICITOR = "NotifyFormerSolicitor";
    public static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_1 = "NotifyOtherSolicitor1";
    public static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_2 = "NotifyOtherSolicitor2";

    private static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private CaseEvent event;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::notifyChangeOfRepresentation);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        var caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        switch (caseEvent) {
            case NOTIFY_FORMER_SOLICITOR:
                return TASK_ID_NOTIFY_FORMER_SOLICITOR;
            case NOTIFY_OTHER_SOLICITOR_1:
                return TASK_ID_NOTIFY_OTHER_SOLICITOR_1;
            case NOTIFY_OTHER_SOLICITOR_2:
                return TASK_ID_NOTIFY_OTHER_SOLICITOR_2;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyChangeOfRepresentation(CallbackParams callbackParams) {
        event = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();

        //skip the former email notification if LiP as there isn't a sol to notify
        if (event.equals(NOTIFY_FORMER_SOLICITOR)
            && caseData.getChangeOfRepresentation().getOrganisationToRemoveID() == null) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            getRecipientEmail(caseData),
            getTemplateId(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getRecipientEmail(CaseData caseData) {
        switch (event) {
            case NOTIFY_FORMER_SOLICITOR:
                return NocNotificationUtils.getPreviousSolicitorEmail(caseData);
            case NOTIFY_OTHER_SOLICITOR_1:
                return NocNotificationUtils.getOtherSolicitor1Email(caseData);
            case NOTIFY_OTHER_SOLICITOR_2:
                return NocNotificationUtils.getOtherSolicitor2Email(caseData);
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", event));
        }
    }

    private String getTemplateId() {
        switch (event) {
            case NOTIFY_FORMER_SOLICITOR:
                return notificationsProperties.getNoticeOfChangeFormerSolicitor();
            case NOTIFY_OTHER_SOLICITOR_1:
            case NOTIFY_OTHER_SOLICITOR_2:
                return notificationsProperties.getNoticeOfChangeOtherParties();
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", event));
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CASE_NAME, NocNotificationUtils.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            FORMER_SOL,
            caseData.getChangeOfRepresentation().getOrganisationToRemoveID() != null
                ? getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()) : "LiP",
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, event.equals(NOTIFY_OTHER_SOLICITOR_2)
                ? getOrganisationName(NocNotificationUtils.getOtherSolicitor2Name(caseData)) :
                getOrganisationName(NocNotificationUtils.getOtherSolicitor1Name(caseData)));
    }

    private String getOrganisationName(String orgToName) {
        return organisationService.findOrganisationById(orgToName).orElseThrow(() -> {
            throw new CallbackException("Organisation is not valid for: " + orgToName);
        }).getName();
    }
}
