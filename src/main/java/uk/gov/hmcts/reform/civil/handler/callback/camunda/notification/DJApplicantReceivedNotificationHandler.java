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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DJApplicantReceivedNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED);
    public static final String TASK_ID = "NotifyApplicantSolicitorDJReceived";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private static final String REFERENCE_TEMPLATE_RECEIVED = "default-judgment-applicant-received-notification-%s";
    private static final String REFERENCE_TEMPLATE_REQUESTED = "default-judgment-applicant-requested-notification-%s";
    private String templateReference;

    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorDefaultJudgmentReceived);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private String identifyTemplate(CaseData caseData) {
        String template = null;
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            template = notificationsProperties.getApplicantSolicitor1DefaultJudgmentReceived();
            templateReference = REFERENCE_TEMPLATE_RECEIVED;
        }
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && !caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            template = notificationsProperties.getApplicantSolicitor1DefaultJudgmentRequested();
            templateReference = REFERENCE_TEMPLATE_REQUESTED;
        }
        if (ofNullable(caseData.getRespondent2()).isEmpty() && !caseData.isLipvLipOneVOne()) {
            template = notificationsProperties.getApplicantSolicitor1DefaultJudgmentReceived();
            templateReference = REFERENCE_TEMPLATE_RECEIVED;
        }
        if (caseData.isLipvLipOneVOne()
            && toggleService.isLipVLipEnabled()) {
            template = notificationsProperties.getApplicantLiPDefaultJudgmentRequested();
            templateReference = REFERENCE_TEMPLATE_RECEIVED;
        }
        return template;
    }

    private CallbackResponse notifyApplicantSolicitorDefaultJudgmentReceived(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                identifyTemplate(caseData),
                addProperties1v2FirstDefendant(caseData),
                String.format(templateReference, caseData.getLegacyCaseReference())
            );
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                identifyTemplate(caseData),
                addProperties1v2SecondDefendant(caseData),
                String.format(templateReference, caseData.getLegacyCaseReference())
            );
        }
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ((ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && !caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")))) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                identifyTemplate(caseData),
                addProperties2(caseData),
                String.format(templateReference, caseData.getLegacyCaseReference())
            );
        }
        if (ofNullable(caseData.getRespondent2()).isEmpty() && !caseData.isLipvLipOneVOne()) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                identifyTemplate(caseData),
                addProperties(caseData),
                String.format(templateReference, caseData.getLegacyCaseReference())
            );
        }

        if (caseData.isLipvLipOneVOne()
            && toggleService.isLipVLipEnabled()) {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                identifyTemplate(caseData),
                addLipvsLiPProperties(caseData),
                String.format(templateReference, caseData.getLegacyCaseReference())
            );

        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_SPECIFIED, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                               .getOrganisation()
                                                               .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> addLipvsLiPProperties(CaseData caseData) {
        return Map.of(
            APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> addProperties2(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_APPLICANT1, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                              .getOrganisation()
                                                              .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getDefendantDetailsSpec().getValue().getLabel()
        );
    }

    public Map<String, String> addProperties1v2FirstDefendant(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_SPECIFIED, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                              .getOrganisation()
                                                              .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> addProperties1v2SecondDefendant(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_SPECIFIED, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                              .getOrganisation()
                                                              .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
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
