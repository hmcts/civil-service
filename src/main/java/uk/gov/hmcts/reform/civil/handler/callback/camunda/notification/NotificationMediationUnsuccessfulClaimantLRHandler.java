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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_TWO;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class NotificationMediationUnsuccessfulClaimantLRHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR);
    private static final String LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR = "notification-mediation-unsuccessful-claimant-LR-%s";
    private static final String LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LIP = "notification-mediation-unsuccessful-claimant-LIP-%s";
    public static final String TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR = "SendMediationUnsuccessfulClaimantLR";
    private static final String CLAIMANT_TEXT = "your claim against ";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantLRForMediationUnsuccessful
    );
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse notifyClaimantLRForMediationUnsuccessful(CallbackParams callbackParams) {
        // Notify for LR claimant or Lip claimant
        CaseData caseData = callbackParams.getCaseData();
        sendEmail(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesLip(final CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesNoAttendanceCARM(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesCARMforLR(final CaseData caseData) {
        String partyName = CLAIMANT_TEXT + caseData.getRespondent1().getPartyName();
        if (null != caseData.getRespondent2()) {
            partyName = String.format("%s and %s", partyName, caseData.getRespondent2().getPartyName());
        }
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            PARTY_NAME, partyName,
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesCARMforLIP(CaseData caseData) {
        return Map.of(PARTY_NAME, caseData.getApplicant1().getPartyName(),
                      CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    private void sendEmail(final CaseData caseData) {
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            if (NO.equals(caseData.getApplicant1Represented())) {
                sendMailUnrepresentedClaimant(caseData);
            } else {
                sendMailAccordingToReason(caseData);
            }
        } else {
            if (Objects.nonNull(caseData.getApplicant1Represented())
                && caseData.isApplicant1NotRepresented()) {
                notificationService.sendMail(
                    caseData.getApplicant1().getPartyEmail(),
                    addTemplate(caseData),
                    addPropertiesLip(caseData),
                    String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LIP, caseData.getLegacyCaseReference())
                );
            } else {
                notificationService.sendMail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate(),
                    addProperties(caseData),
                    String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR, caseData.getLegacyCaseReference())
                );
            }
        }
    }

    private String addTemplate(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getMediationUnsuccessfulClaimantLIPWelshTemplate() :
            notificationsProperties.getMediationUnsuccessfulClaimantLIPTemplate();
    }

    private void sendMailAccordingToReason(CaseData caseData) {
        if (findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_CLAIMANT_TWO))) {
            sendMailRepresentedClaimantNoAttendance(caseData);
        } else {
            sendMailRepresentedClaimant(caseData);
        }
    }

    private void sendMailRepresentedClaimant(CaseData caseData) {
        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getMediationUnsuccessfulLRTemplate(),
            addPropertiesCARMforLR(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR, caseData.getLegacyCaseReference()));
    }

    private void sendMailRepresentedClaimantNoAttendance(CaseData caseData) {
        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate(),
            addPropertiesNoAttendanceCARM(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR, caseData.getLegacyCaseReference()));
    }

    private void sendMailUnrepresentedClaimant(CaseData caseData) {
        notificationService.sendMail(
            caseData.getApplicant1().getPartyEmail(),
            caseData.isClaimantBilingual()
                ? notificationsProperties.getMediationUnsuccessfulLIPTemplateWelsh()
                : notificationsProperties.getMediationUnsuccessfulLIPTemplate(),
            addPropertiesCARMforLIP(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LIP, caseData.getLegacyCaseReference()));
    }
}
