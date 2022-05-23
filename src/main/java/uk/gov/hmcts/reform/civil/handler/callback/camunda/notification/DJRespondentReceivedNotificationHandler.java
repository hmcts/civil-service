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

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DJRespondentReceivedNotificationHandler extends CallbackHandler implements NotificationData {

    public static final String TASK_ID = "NotifyRespondentSolicitorDJReceived";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED);
    private static final String REFERENCE_TEMPLATE = "default-judgment-respondent-received-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

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

    private String identifyTemplate(CaseData caseData){
        String template;
        if(ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")){
            template = notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived();
        } else{
            template = notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested();
        }
        return template;
    }

    private CallbackResponse notifyRespondentSolicitorDefaultJudgmentReceived(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        //Send email to applicant solicitor
        if(ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith(
            "Both")) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                identifyTemplate(caseData),
                addProperties1v2FirstDefendant(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                identifyTemplate(caseData),
                addProperties1v2SecondDefendant(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                identifyTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getDefendantDetailsSpec().getValue().getLabel(),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                         .getOrganisation()
                                                         .getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addProperties1v2FirstDefendant(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addProperties1v2SecondDefendant(CaseData caseData) {
        return Map.of(
            DEFENDANT_EMAIL, getLegalOrganizationName(caseData.getRespondent1OrganisationPolicy()
                                                              .getOrganisation()
                                                              .getOrganisationID(), caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
            CLAIMANT_EMAIL, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                          .getOrganisation()
                                                          .getOrganisationID(), caseData)
        );
    }

    public String getLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }

        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
