package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerLip extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP);
    public static final String TASK_ID_LIP = "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor1Lip";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimantConfirmsNotToProceedLip
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_LIP;
    }

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimantConfirmsNotToProceedLip(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = caseData.getRespondent1().getPartyEmail();
        String template = caseData.isPartAdmitPayImmediatelyAccepted()
            ? notificationsProperties.getNotifyRespondentLipPartAdmitPayImmediatelyAcceptedSpec()
            : notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpecLip();
        Map<String, String> properties = addProperties(caseData);
        if (StringUtils.isNotBlank(recipient)) {
            notificationService.sendMail(
                recipient,
                template,
                properties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));
        } else {
            log.info("Party " + caseData.getSolicitorReferences().getRespondentSolicitor1Reference() +
                         " has no email address for case " + caseData.getLegacyCaseReference());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isPartAdmitPayImmediatelyAccepted()) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
