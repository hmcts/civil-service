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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getApplicantEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgementClaimantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_CLAIMANT);
    public static final String TASK_ID = "NotifyClaimantSetAsideJudgement";

    private static final String REFERENCE_TEMPLATE = "set-aside-judgement-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_LIP = "set-aside-judgement-applicant-notification-lip-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimSetAsideJudgementToClaimant
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

    private CallbackResponse notifyClaimSetAsideJudgementToClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isApplicantLip = caseData.isApplicantLiP();
        String recipientEmail = getApplicantEmail(caseData, isApplicantLip);
        if (recipientEmail != null) {
            notificationService.sendMail(
                recipientEmail,
                getTemplate(isApplicantLip),
                addProperties(caseData),
                getReferenceTemplate(caseData, isApplicantLip)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        String defendantName = "";
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1());
        } else {
            defendantName = getPartyNameBasedOnType(caseData.getRespondent1())
                .concat(" and ")
                .concat(getPartyNameBasedOnType(caseData.getRespondent2()));
        }

        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData),
            DEFENDANT_NAME_INTERIM,  defendantName,
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText()
        );

    }

    private String getTemplate(boolean isApplicantLip) {
        return isApplicantLip ? notificationsProperties.getNotifyUpdateTemplate()
            : notificationsProperties.getNotifySetAsideJudgementTemplate();
    }

    private String getReferenceTemplate(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
