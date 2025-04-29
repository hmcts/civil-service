package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyDefendantsClaimantSettleTheClaim extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private static final String REFERENCE_TEMPLATE = "notify-defendant-claimant-settle-the-claim-notification-%s";
    private static final String REFERENCE_TEMPLATE_LR = "notify-defendant-lr-claimant-settle-the-claim-notification-%s";
    public static final String TASK_ID = "NotifyDefendantClaimantSettleTheClaim";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendants
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyDefendants(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (StringUtils.isNotEmpty(caseData.getRespondentSolicitor1EmailAddress())) {
            log.info("Sending settle-claim email to defendant LR");
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate(),
                addPropertiesLR(caseData),
                String.format(REFERENCE_TEMPLATE_LR, caseData.getLegacyCaseReference())
            );
        } else if (StringUtils.isNotEmpty(caseData.getRespondent1().getPartyEmail())) {
            log.info("Sending settle-claim email to defendant LiP");
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                notificationsProperties.getNotifyDefendantLIPClaimantSettleTheClaimTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> addPropertiesLR(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_REFERENCE_NUMBER, getDefRefNumber(caseData),
            LEGAL_REP_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String getDefRefNumber(CaseData caseData) {
        if (nonNull(caseData.getSolicitorReferences())
            && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        } else {
            return "Not provided";
        }
    }

}
