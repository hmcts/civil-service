package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DJRespondentReceivedNotificationHandler extends CallbackHandler implements NotificationData {

    public static final String TASK_ID = "NotifyRespondentSolicitorDJReceived";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED);
    private static final String REFERENCE_TEMPLATE_RECEIVED = "default-judgment-respondent-received-notification-%s";
    private static final String REFERENCE_TEMPLATE_REQUESTED = "default-judgment-respondent-requested-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorDefaultJudgmentReceived,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorDefaultJudgmentReceived
        );
    }

    private class EmailTemplateReference {
        String template;
        String templateReference;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private EmailTemplateReference identifyTemplate(CaseData caseData) {
        EmailTemplateReference emailTemplate = new EmailTemplateReference();
        if (caseData.isLRvLipOneVOne() || (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled())) {
            emailTemplate.template = notificationsProperties.getRespondent1DefaultJudgmentRequestedTemplate();
            emailTemplate.templateReference = REFERENCE_TEMPLATE_REQUESTED;
            return emailTemplate;
        }
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            emailTemplate.template = notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived();
            emailTemplate.templateReference = REFERENCE_TEMPLATE_RECEIVED;
        }
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && !caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            emailTemplate.template = notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested();
            emailTemplate.templateReference = REFERENCE_TEMPLATE_REQUESTED;
        }
        if (ofNullable(caseData.getRespondent2()).isEmpty()) {
            emailTemplate.template = notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived();
            emailTemplate.templateReference = REFERENCE_TEMPLATE_RECEIVED;
        }
        return emailTemplate;
    }

    private CallbackResponse notifyRespondentSolicitorDefaultJudgmentReceived(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        EmailTemplateReference emailTemplate = identifyTemplate(caseData);
        if ((caseData.isLRvLipOneVOne() || (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()))
            && toggleService.isPinInPostEnabled()
            && V_1.equals(callbackParams.getVersion())) {
            if (caseData.getRespondent1().getPartyEmail() != null) {
                notificationService.sendMail(
                    caseData.getRespondent1().getPartyEmail(),
                    emailTemplate.template,
                    addProperties1v1LRvLip(caseData),
                    String.format(emailTemplate.templateReference, caseData.getLegacyCaseReference()));
            }
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                emailTemplate.template,
                addProperties1v2FirstDefendant(caseData),
                String.format(emailTemplate.templateReference, caseData.getLegacyCaseReference())
            );
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                emailTemplate.template,
                addProperties1v2SecondDefendant(caseData),
                String.format(emailTemplate.templateReference, caseData.getLegacyCaseReference())
            );
        }
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && !caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                emailTemplate.template,
                addProperties2(caseData),
                String.format(emailTemplate.templateReference, caseData.getLegacyCaseReference())
            );
        }
        if (ofNullable(caseData.getRespondent2()).isEmpty()) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                emailTemplate.template,
                addProperties(caseData),
                String.format(emailTemplate.templateReference, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> addProperties2(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getDefendantDetailsSpec().getValue().getLabel(),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                         .getOrganisation()
                                                         .getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addProperties1v2FirstDefendant(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addProperties1v2SecondDefendant(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                              .getOrganisation()
                                                              .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addProperties1v1LRvLip(CaseData caseData) {
        return Map.of(
            CLAIM_NUMBER_INTERIM, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME_INTERIM, getPartyNameBasedOnType(caseData.getRespondent1()),
            APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
        );
    }

    public String getLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }

        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
