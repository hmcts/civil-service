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

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgementClaimantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_CLAIMANT);
    public static final String TASK_ID = "NotifyClaimantSetAsideJudgement";

    //TODO: Need to check on reference template
    private static final String REFERENCE_TEMPLATE =
        "set-aside-judgement-applicant-notification-%s";

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
        if (caseData.getApplicantSolicitor1UserDetails().getEmail() != null) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                getTemplate(),
                addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            LEGAL_ORG, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                         .getOrganisation()
                                                         .getOrganisationID(), caseData)
            //REASON_FROM_CASEWORKER, caseData.getJoJudgmentRecordReason().name() //TODO: Confirm on Reason field
        );
    }

    //TODO: Change to notifySetAsideJudgementTemplate property in civil-commons
    private String getTemplate() {
        return notificationsProperties.getNotifySetAsideJudgementTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getLegalOrganizationName(String id, CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
