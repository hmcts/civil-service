package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefendantClaimDetailsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC
    );

    public static final String TASK_ID_EMAIL_FIRST_SOL = "NotifyClaimDetailsRespondentSolicitor1";
    public static final String TASK_ID_EMAIL_APP_SOL_CC = "NotifyClaimDetailsApplicantSolicitor1CC";
    public static final String TASK_ID_EMAIL_SECOND_SOL = "NotifyClaimDetailsRespondentSolicitor2";

    private static final String REFERENCE_TEMPLATE = "claim-details-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final ToggleConfiguration toggleConfiguration;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimDetails
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_FIRST_SOL;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return TASK_ID_EMAIL_APP_SOL_CC;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_SECOND_SOL;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setFeatureToggleWA(toggleConfiguration.getFeatureToggle());
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        String recipient = getRecipientEmail(caseData, caseEvent);
        String emailTemplate = notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate();
        String orgName = getOrgName(caseData, caseEvent);

        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, orgName);

        if (recipient != null) {
            notificationService.sendMail(
                recipient,
                emailTemplate,
                notificationProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else {
            log.info(String.format(
                "Email address is null for caseEvent: %s for: %s",
                caseEvent,
                caseData.getLegacyCaseReference()
            ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private String getOrgName(CaseData caseData, CaseEvent caseEvent) {
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService);
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return getApplicantLegalOrganizationName(caseData, organisationService);
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService);
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    private String getRecipientEmail(CaseData caseData, CaseEvent caseEvent) {

        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return shouldEmailRespondent2Solicitor(caseData)
                    ? caseData.getRespondentSolicitor2EmailAddress()
                    : caseData.getRespondentSolicitor1EmailAddress();
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return caseData.getApplicantSolicitor1UserDetails().getEmail();
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return caseData.getRespondentSolicitor2EmailAddress();
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    private boolean shouldEmailRespondent2Solicitor(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("")
            .startsWith("Defendant Two:");
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>();
        properties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONSE_DEADLINE, formatLocalDate(caseData
                                                                     .getRespondent1ResponseDeadline()
                                                                     .toLocalDate(), DATE),
            RESPONSE_DEADLINE_PLUS_28,
            formatLocalDate(deadlinesCalculator.plus14DaysDeadline(caseData.getRespondent1ResponseDeadline())
                                .toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

}
