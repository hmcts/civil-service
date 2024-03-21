package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR2_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class NotifyDefendantJudgmentVariedDeterminationOfMeansNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS,
                                                           NOTIFY_SOLICITOR2_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    public static final String TASK_ID = "NotifyDefendantJudgmentVariedDeterminationOfMeans";
    private static final String REFERENCE_TEMPLATE =
        "defendant-judgment-varied-determination-of-means-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyDefendantJudgmentVariedDeterminationOfMeans
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyDefendantJudgmentVariedDeterminationOfMeans(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = (callbackParams.getRequest().getEventId()
                .equals(NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS.name()))
            ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();

        if (nonNull(recipient)) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                getTemplate(),
                callbackParams.getRequest().getEventId()
                        .equals(NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS.name())
                    ? addProperties(caseData) : addRespondent2Properties(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME_SPEC, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
            );
    }

    public Map<String, String> addRespondent2Properties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME_SPEC, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService)
        );
    }

    private String getTemplate() {
        return notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

}
