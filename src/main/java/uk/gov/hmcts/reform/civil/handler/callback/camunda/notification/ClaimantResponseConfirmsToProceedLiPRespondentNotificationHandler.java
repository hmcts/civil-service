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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
    private final FeatureToggleService featureToggleService;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::notifyRespondentForClaimantConfirmsToProceed
    );

    private CallbackResponse notifyRespondentForClaimantConfirmsToProceed(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        boolean shouldSendEmailToDefendantLR = shouldSendMediationNotificationDefendant1LRCarm(caseData, carmEnabled);
        boolean proceedWithTheClaim = YES.equals(caseData.getApplicant1ProceedsWithClaimSpec());
        if (shouldSendNotification(caseData, callbackParams.getRequest().getEventId())) {
            notificationService.sendMail(
                shouldSendEmailToDefendantLR ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondent1().getPartyEmail(),
                shouldSendEmailToDefendantLR ? getRespondent1LREmailTemplate(proceedWithTheClaim)
                    : getRespondent1LipEmailTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean shouldSendNotification(CaseData caseData, String eventId) {
        return Objects.nonNull(caseData.getRespondent1().getPartyEmail())
            && (!caseData.isClaimantBilingual()
            || NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name().equals(eventId));
    }

    private String getRespondent1LipEmailTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
            : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
    }

    private String getRespondent1LREmailTemplate(boolean proceedWithTheClaim) {
        return proceedWithTheClaim ? notificationsProperties.getNotifyDefendantLRForMediation()
            : notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
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
        if (shouldSendMediationNotificationDefendant1LRCarm(
            caseData,
            featureToggleService.isCarmEnabledForCase(caseData)
        )) {
            return Map.of(
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_REFERENCE_NUMBER,
                caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
            );
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }
}
