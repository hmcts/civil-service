package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyEventHandler extends NotificationHandler {
    //TODO: Rename NotifyEventHandler to NotifyLitigationFriendAddedHandler

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    public static final String TASK_ID = "LitigationFriendAddedNotifier";
    private static final String REFERENCE_TEMPLATE_APPLICANT = "litigation-friend-added-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "litigation-friend-added-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyParties
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
    protected CallbackResponse notifyParties(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info(
            "Entering notifyForLitigationFriendAdded. Case id: {}",
            callbackParams.getCaseData().getCcdCaseReference()
        );

        notifyApplicants(caseData);
        notifyRespondents(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected void notifyApplicants(final CaseData caseData) {
        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getSolicitorLitigationFriendAdded(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference())
        );
    }

    protected void notifyRespondents(final CaseData caseData) {

        Map<String, String> properties = addProperties(caseData);
        properties.put(
            CLAIM_LEGAL_ORG_NAME_SPEC,
            getLegalOrganizationNameForRespondent(caseData, true, organisationService)
        );

        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getSolicitorLitigationFriendAdded(),
            properties,
            String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference())
        );

        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            properties.put(
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getLegalOrganizationNameForRespondent(caseData, false, organisationService)
            );
            notificationService.sendMail(
                caseData.getRespondentSolicitor2EmailAddress(),
                notificationsProperties.getSolicitorLitigationFriendAdded(),
                properties,
                String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference())
            );
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService)
        ));
    }
}
