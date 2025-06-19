package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCnbcContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED,
        NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC
    );
    public static final String TASK_ID = "NotifyLiPRespondentClaimantConfirmToProceed";
    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";
    private final NotificationService notificationService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::notifyRespondentForClaimantConfirmsToProceed
    );

    private CallbackResponse notifyRespondentForClaimantConfirmsToProceed(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        boolean shouldSendEmailToDefendantLR = shouldSendMediationNotificationDefendant1LRCarm(caseData, carmEnabled);
        if (shouldSendNotification(caseData, callbackParams.getRequest().getEventId())) {
            notificationService.sendMail(
                caseData.isRespondent1NotRepresented()
                    ? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress(),
                getEmailTemplate(caseData, shouldSendEmailToDefendantLR),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getEmailTemplate(CaseData caseData, boolean shouldSendEmailToDefendantLR) {
        if (isClaimProceedForLipVsLR(caseData)) {
            return caseData.isSmallClaim()
                ? notificationsProperties.getRespondentSolicitorNotifyToProceedInMediation()
                : notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction();
        }
        if (isClaimantNotProceedLipVsLRWithNoc(caseData)) {
            return notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
        }
        if (shouldSendEmailToDefendantLR) {
            return notificationsProperties.getNotifyDefendantLRForMediation();
        }
        return getRespondent1LipEmailTemplate(caseData);
    }

    private boolean shouldSendNotification(CaseData caseData, String eventId) {
        return Objects.nonNull(caseData.getRespondent1().getPartyEmail())
            && ((!caseData.isClaimantBilingual()
            || NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name().equals(eventId))
            || (featureToggleService.isDefendantNoCOnlineForCase(caseData)
            && NO.equals(caseData.getApplicant1ProceedWithClaim())));
    }

    private String getRespondent1LipEmailTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
            : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
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

        if (caseData.isLipvLROneVOne()) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_REFERENCE_NUMBER,
                caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getRespondentLegalOrganizationName(
                    caseData.getRespondent1OrganisationPolicy(), organisationService),
                APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
            addCnbcContact(caseData, properties, configuration,
                           featureToggleService.isQueryManagementLRsEnabled());
            return properties;
        }
        if (shouldSendMediationNotificationDefendant1LRCarm(
            caseData,
            featureToggleService.isCarmEnabledForCase(caseData)
        )) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(),
                                                                              organisationService),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference(),
                APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            ));
            addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
            addCnbcContact(caseData, properties, configuration,
                           featureToggleService.isQueryManagementLRsEnabled());
            return properties;
        }
        HashMap<String, String> lipProperties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        ));
        addAllFooterItems(caseData, lipProperties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
        return lipProperties;
    }

    private boolean isFullDefenceStatesPaid(CaseData caseData) {
        return HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec());
    }

    private boolean isClaimantNotProceedLipVsLRWithNoc(CaseData caseData) {
        return caseData.isLipvLROneVOne()
            && ((isFullDefenceStatesPaid(caseData) && YES.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
                || YesOrNo.NO.equals(caseData.getApplicant1ProceedWithClaim()))
            && featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    private boolean isClaimProceedForLipVsLR(CaseData caseData) {
        return caseData.isLipvLROneVOne()
            && ((isFullDefenceStatesPaid(caseData) && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
                || YES.equals(caseData.getApplicant1ProceedWithClaim()))
            && featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }
}
