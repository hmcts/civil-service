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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseNotAgreedRepaymentRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT);
    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";
    public static final String TASK_ID_CLAIMANT = "ClaimantDisAgreeRepaymentPlanNotifyApplicant";
    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantRejectRepayment
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyClaimantRejectRepayment(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getEmail(caseData),
            addTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(CaseData caseData) {
        return (caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled())
            ? getTemplateForLip(caseData)
            : notificationsProperties.getNotifyClaimantLrTemplate();

    }

    private String getTemplateForLip(CaseData caseData) {
        if (featureToggleService.isGaForWelshEnabled() && caseData.isClaimantBilingual()) {
            return notificationsProperties.getNotifyClaimantLipTemplateManualDeterminationForWelsh();
        }
        return notificationsProperties.getNotifyClaimantLipTemplateManualDetermination();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        if (caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled()) {
            HashMap<String, String> lipProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            ));
            addAllFooterItems(caseData, lipProperties, configuration,
                              featureToggleService.isPublicQueryManagementEnabled(caseData));
            return lipProperties;
        } else {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
            return properties;
        }
    }

    private String getEmail(CaseData caseData) {
        return (caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled())
            ? caseData.getApplicant1Email()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }
}
