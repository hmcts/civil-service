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
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class TranslatedDocumentUploadedClaimantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED);
    private static final String REFERENCE_TEMPLATE = "translated-document-uploaded-claimant-notification-%s";
    public static final String TASK_ID = "NotifyTranslatedDocumentUploadedToClaimant";
    private final FeatureToggleService featureToggleService;
    final  OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimant
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

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        if (caseData.isApplicantNotRepresented() && featureToggleService.isLipVLipEnabled()) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            ));
            addAllFooterItems(caseData, properties, configuration,
                              featureToggleService.isPublicQueryManagementEnabled(caseData));
            return properties;
        }
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private CallbackResponse notifyClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String email = getEmail(caseData);
        if (Objects.nonNull(email)) {
            notificationService.sendMail(
                email,
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(CaseData caseData) {
        if (caseData.isApplicantNotRepresented() && featureToggleService.isLipVLipEnabled()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getNotifyLiPClaimantDefendantRespondedWelshLip();
            }
            return notificationsProperties.getNotifyLiPClaimantDefendantResponded();
        }
        return notificationsProperties.getNotifyClaimantTranslatedDocumentUploaded();
    }

    private String getEmail(CaseData caseData) {
        return (caseData.isApplicantNotRepresented() && featureToggleService.isLipVLipEnabled())
            ? caseData.getApplicant1Email()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
