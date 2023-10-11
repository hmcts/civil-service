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
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseAgreedSettledPartAdmitDefendantLipNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED);
    public static final String TASK_ID = "ClaimantAgreedSettledPartAdmitNotifyLip";
    private static final String REFERENCE_TEMPLATE = "claimant-part-admit-settle-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationDetailsService organisationDetailsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantForPartAdmitClaimSettled
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
        if (caseData.isRespondent1NotRepresented()) {
            return Map.of(
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondentLegalOrganizationName(caseData)
        );
    }

    private CallbackResponse notifyDefendantForPartAdmitClaimSettled(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (Objects.nonNull(caseData.getRespondent1Email())) {
            notificationService.sendMail(
                caseData.getRespondent1Email(),
                setUpEmailTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private String setUpEmailTemplate(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getRespondentLipPartAdmitSettleClaimBilingualTemplate()
                : notificationsProperties.getRespondentLipPartAdmitSettleClaimTemplate();
        }
        return notificationsProperties.getNotifyLiPClaimantDefendantResponded();
    }
}
