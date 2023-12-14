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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantDefendantAgreedMediationRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_RESPONDENT_MEDIATION_AGREEMENT);
    private static final String REFERENCE_TEMPLATE = "mediation-agreement-respondent-notification-%s";
    public static final String TASK_ID_LIP = "ClaimantDefendantAgreedMediationNotifyRespondent";
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantMediationAgreement
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_LIP;
    }

    private CallbackResponse notifyDefendantMediationAgreement(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(
            addEmail(caseData),
            addTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));

        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            );
        } else {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            );
        }
    }

    private String addEmail(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    private String addTemplate(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return getNotifyRespondentLiPMediationAgreementTemplate(caseData);
        } else {
            return notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate();
        }
    }

    private String getNotifyRespondentLiPMediationAgreementTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplateWelsh() :
            notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplate();
    }

    public String getRespondentLegalOrganizationName(CaseData caseData) {
        String id = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);

        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }
}
