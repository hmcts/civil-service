package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DJRespondentReceivedNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED);
    public static final String TASK_ID = "NotifyRespondentSolicitorDJReceived";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private static final String REFERENCE_TEMPLATE = "default-judgment-respondent-received-notification-%s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorDefaultJudgmentReceived);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyRespondentSolicitorDefaultJudgmentReceived(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
//        Send email to respondent solicitor
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_SPECIFIED, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public String getLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        System.out.println(organisation.get().getName());
        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
