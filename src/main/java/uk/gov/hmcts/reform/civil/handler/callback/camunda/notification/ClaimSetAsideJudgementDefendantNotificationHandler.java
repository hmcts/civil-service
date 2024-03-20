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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgementDefendantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_DEFENDANT);
    public static final String TASK_ID = "NotifyDefendantSetAsideJudgement";
    private static final String REFERENCE_TEMPLATE =
        "set-aside-judgement-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimSetAsideJudgementToDefendant
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

    private CallbackResponse notifyClaimSetAsideJudgementToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getRespondent1() != null && !caseData.getRespondent1().getPartyName().isEmpty()) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                getTemplate(),
                addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        }
        if (caseData.getRespondent2() != null && !caseData.getRespondent2().getPartyName().isEmpty()) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor2EmailAddress() != null
                    ? caseData.getRespondentSolicitor2EmailAddress() :
                    caseData.getRespondentSolicitor1EmailAddress(),
                getTemplate(),
                addPropertiesDef2(caseData),
                getReferenceTemplate(caseData)
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText()
        );
    }

    public Map<String, String> addPropertiesDef2(final CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText(),
            DEFENDANT_NAME_INTERIM, getDefendantName(caseData)
        );
    }

    //TODO: Change to notifySetAsideJudgementTemplate property in civil-commons
    private String getTemplate() {
        return notificationsProperties.getNotifySetAsideJudgementTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getDefendantNameBasedOnCaseType(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return getPartyNameBasedOnType(caseData.getRespondent1());
        } else {
            return getPartyNameBasedOnType(caseData.getRespondent1())
                .concat(" and ")
                .concat(getPartyNameBasedOnType(caseData.getRespondent2()));
        }
    }

}
