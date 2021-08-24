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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class AgreedExtensionDateApplicantForSpecNotificationHandler
    extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC
    );

    public static final String TASK_ID = "AgreedExtensionDateNotifyApplicantSolicitor1ForSpec";
    public static final String TASK_ID_CC = "AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec";
    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-spec-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForAgreedExtensionDateForSpec
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isCcNotification(callbackParams) ? TASK_ID_CC : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForAgreedExtensionDateForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (isCcNotification(callbackParams)) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec(),
                addPropertiesForRespondent(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getApplicant1OrganisationPolicy()
                    .getOrganisation().getOrganisationID(),
                caseData
            ),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            AGREED_EXTENSION_DATE, formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE)
        );
    }

    public Map<String, String> addPropertiesForRespondent(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID(), caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            AGREED_EXTENSION_DATE, formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE)
        );
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC.name());
    }

    public String getApplicantLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
