package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseAgreedRepaymentRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT,
        NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION);
    public static final String TASK_ID = "ClaimantAgreedRepaymentNotifyRespondent1";
    public static final String TASK_ID_JUDGEMENT_ADMISSION = "RequestJudgementByAdmissionNotifyRespondent1";
    private static final String REFERENCE_TEMPLATE = "claimant-agree-repayment-respondent-notification-%s";
    private static final String REFERENCE_TEMPLATE_JUDGEMENT_ADMISSION = "request-judgement-by-admission-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final PinInPostConfiguration pipInPostConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyRespondent1ForAgreedRepayment
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT:
                return TASK_ID;
            case NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION:
                return TASK_ID_JUDGEMENT_ADMISSION;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondent1ForAgreedRepayment(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (isRespondentOrSolicitorHasNoEmail(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            addEmail(caseData),
            addTemplate(caseData),
            addProperties(caseData),
            String.format(getReferenceTemplate(callbackParams), caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (isRespondentSolicitorRegistered(caseData)) {
            return Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                    caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
        if (caseData.isRespondent1NotRepresented()) {
            return Map.of(
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
        return null;
    }

    private String addEmail(CaseData caseData) {
        if (isRespondentSolicitorRegistered(caseData)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.getRespondent1().getPartyEmail();
        }
        return null;
    }

    private String addTemplate(CaseData caseData) {
        if (isRespondentSolicitorRegistered(caseData)) {
            return notificationsProperties.getRespondentSolicitorCcjNotificationTemplate();
        }
        if (caseData.isRespondent1NotRepresented()) {
            return getCCJRespondentTemplate(caseData);
        }
        return null;
    }

    private String getCCJRespondentTemplate(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getRespondentCcjNotificationWelshTemplate();
        }
        return notificationsProperties.getRespondentCcjNotificationTemplate();
    }

    public String getRespondentLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.map(Organisation::getName).orElse(null);
    }

    public boolean isRespondentSolicitorRegistered(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getRespondent1OrgRegistered());
    }

    public boolean isRespondentOrSolicitorHasNoEmail(CaseData caseData) {
        return ((isRespondentSolicitorRegistered(caseData)
            && caseData.getRespondentSolicitor1EmailAddress() == null)
            || ((!isRespondentSolicitorRegistered(caseData)
            && caseData.getRespondent1().getPartyEmail() == null))
            );
    }

    private String getReferenceTemplate(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT:
                return REFERENCE_TEMPLATE;
            case NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION:
                return REFERENCE_TEMPLATE_JUDGEMENT_ADMISSION;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }
}
