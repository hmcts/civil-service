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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_JUDGEMENT_VARIED_DETERMINATION_OF_MEANS;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class NotifyClaimantJudgementVariedDeterminationOfMeansNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_JUDGEMENT_VARIED_DETERMINATION_OF_MEANS);
    public static final String TASK_ID = "NotifyClaimantJudgementVariedDeterminationOfMeans";
    private static final String REFERENCE_TEMPLATE =
        "claimant-judgement-varied-determination-of-means-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimantJudgementVariedDeterminationOfMeans
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

    private CallbackResponse notifyClaimantJudgementVariedDeterminationOfMeans(CallbackParams callbackParams) {
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
                LEGAL_ORG_NAME, getLegalOrganizationName(caseData), //ToDo: Check the legal organization name
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
    }

    private String getTemplate() {
        return notificationsProperties.getNotifyUpdateTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getLegalOrganizationName(final CaseData caseData) {
        Optional<Organisation> organisation = organisationService
            .findOrganisationById(caseData.getApplicant1OrganisationPolicy()
                                      .getOrganisation().getOrganisationID());
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicant1().getPartyName();
    }
}
