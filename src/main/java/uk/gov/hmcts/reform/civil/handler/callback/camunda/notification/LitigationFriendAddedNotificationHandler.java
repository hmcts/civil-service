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
import uk.gov.hmcts.reform.civil.notification.EmailNotification1V2;
import uk.gov.hmcts.reform.civil.notification.EmailTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
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
public class LitigationFriendAddedNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    public static final String TASK_ID = "LitigationFriendAddedNotifier";
    private static final String REFERENCE_TEMPLATE_APPLICANT = "litigation-friend-added-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "litigation-friend-added-respondent-notification-%s";

    private final EmailNotification1V2 emailNotification1V2;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForLitigationFriendAdded
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

    private CallbackResponse notifyForLitigationFriendAdded(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info(
            "Entering notifyForLitigationFriendAdded. Case id: {}",
            callbackParams.getCaseData().getCcdCaseReference()
        );

        emailNotification1V2.notifyParties(getEmailTO(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected EmailTO getEmailTO(CaseData caseData) {

        Map<String, String> applicantSol1Props = new HashMap<>(addProperties(caseData));
        applicantSol1Props.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));

        Map<String, String> respondentSol1Props = new HashMap<>(addProperties(caseData));
        respondentSol1Props.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                true, organisationService));

        Map<String, String> respondentSol2Props = new HashMap<>(addProperties(caseData));
        respondentSol1Props.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                false, organisationService));

        return EmailTO.builder()
                .emailTemplate(notificationsProperties.getSolicitorLitigationFriendAdded())
                .applicantSol1Email(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .applicantSol1Params(applicantSol1Props)
                .applicantRef(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
                .respondentSol1Email(caseData.getRespondentSolicitor1EmailAddress())
                .respondentSol1Params(respondentSol1Props)
                .respondentRef(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
                .respondentSol2Email(caseData.getRespondentSolicitor2EmailAddress())
                .respondentSol2Params(respondentSol2Props)
                .canSendEmailToRespondentSol2(stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES))
                .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }
}
