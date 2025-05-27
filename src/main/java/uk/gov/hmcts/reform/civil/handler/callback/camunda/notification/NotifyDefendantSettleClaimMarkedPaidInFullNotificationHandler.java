package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL,
                                                           NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL);

    public static final String TASK_ID_RESPONDENT1 = "NotifyDefendantSettleClaimMarkedPaidInFull1";
    public static final String TASK_ID_RESPONDENT2 = "NotifyDefendantSettleClaimMarkedPaidInFull2";
    private static final String REFERENCE_TEMPLATE =
        "defendant-settle-claim-marked-paid-in-full-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyDefendantSettleClaimMarkedPaidInFull
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.equals(caseEvent)) {
            return TASK_ID_RESPONDENT1;
        } else {
            return TASK_ID_RESPONDENT2;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyDefendantSettleClaimMarkedPaidInFull(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String recipient = callbackParams.getRequest().getEventId()
                .equals(NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();
        log.info("caseData.getRespondentSolicitor1EmailAddress(): {}" +
                     ", caseData.getRespondentSolicitor2EmailAddress(): {}", caseData.getRespondentSolicitor1EmailAddress(),
                 caseData.getRespondentSolicitor2EmailAddress());
        log.info("recipient: {}", recipient);

        if (nonNull(recipient)) {
            log.info("Sending notification..");
            notificationService.sendMail(
                recipient,
                getTemplate(),
                callbackParams.getRequest().getEventId()
                    .equals(NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                    ? addProperties(caseData) : addRespondent2Properties(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private Map<String, String> addRespondent2Properties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String getTemplate() {
        return notificationsProperties.getNotifySettleClaimMarkedPaidInFullDefendantTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

}
