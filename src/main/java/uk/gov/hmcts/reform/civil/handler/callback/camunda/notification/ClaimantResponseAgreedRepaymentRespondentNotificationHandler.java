package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseAgreedRepaymentRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT);
    public static final String TASK_ID = "ClaimantAgreedRepaymentNotifyRespondent1";
    private static final String REFERENCE_TEMPLATE = "claimant-agree-repayment-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final PinInPostConfiguration pipInPostConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyRespondent1ForAgreedRepayment
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

    private CallbackResponse notifyRespondent1ForAgreedRepayment(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if ((caseData.getRespondent1OrgRegistered().equals(YesOrNo.YES)
            && caseData.getRespondentSolicitor1EmailAddress() == null)
            || (!caseData.getRespondent1OrgRegistered().equals(YesOrNo.YES)
            && caseData.getRespondent1().getPartyEmail() == null
            )) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            addEmail(caseData),
            addTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.getRespondent1OrgRegistered().equals(YesOrNo.YES)) {
            return Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                    caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        } else {
            return Map.of(
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl(),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
    }

    private String addEmail(CaseData caseData) {
        if (caseData.getRespondent1OrgRegistered().equals(YesOrNo.YES)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else {
            return caseData.getRespondent1().getPartyEmail();
        }
    }

    private String addTemplate(CaseData caseData) {
        if (caseData.getRespondent1OrgRegistered().equals(YesOrNo.YES)) {
            return notificationsProperties.getRespondentSolicitorCcjNotificationTemplate();
        } else {
            return notificationsProperties.getRespondentCcjNotificationTemplate();
        }
    }

    public String getRespondentLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }
}
