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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_NEW_DEFENDANT_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangeOfRepresentationNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_FORMER_SOLICITOR,
        NOTIFY_OTHER_SOLICITOR_1,
        NOTIFY_OTHER_SOLICITOR_2,
        NOTIFY_NEW_DEFENDANT_SOLICITOR
    );

    public static final String TASK_ID_NOTIFY_FORMER_SOLICITOR = "NotifyFormerSolicitor";
    public static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_1 = "NotifyOtherSolicitor1";
    public static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_2 = "NotifyOtherSolicitor2";
    public static final String TASK_ID_NOTIFY_NEW_DEFENDANT_SOLICITOR1 = "NotifyNewDefendantSolicitor1";
    private static final String LITIGANT_IN_PERSON = "LiP";

    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";

    private static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final ObjectMapper objectMapper;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    private CaseEvent event;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::notifyChangeOfRepresentation);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        var caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        switch (caseEvent) {
            case NOTIFY_FORMER_SOLICITOR:
                return TASK_ID_NOTIFY_FORMER_SOLICITOR;
            case NOTIFY_OTHER_SOLICITOR_1:
                return TASK_ID_NOTIFY_OTHER_SOLICITOR_1;
            case NOTIFY_OTHER_SOLICITOR_2:
                return TASK_ID_NOTIFY_OTHER_SOLICITOR_2;
            case NOTIFY_NEW_DEFENDANT_SOLICITOR:
                return TASK_ID_NOTIFY_NEW_DEFENDANT_SOLICITOR1;
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyChangeOfRepresentation(CallbackParams callbackParams) {
        event = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();

        //skip the event if the party is LIP as there would be no one to notify
        if (shouldSkipEvent(event, caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        log.info("Sending NoC notification email");
        notificationService.sendMail(
            getRecipientEmail(caseData),
            getTemplateId(caseData),
            getPropertiesForEmail(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));
        log.info("NoC email sent successfully");

        if (NOTIFY_FORMER_SOLICITOR.equals(event)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(NocNotificationUtils.getCaseDataWithoutFormerSolicitorEmail(caseData).toMap(objectMapper))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getRecipientEmail(CaseData caseData) {
        switch (event) {
            case NOTIFY_FORMER_SOLICITOR:
                return NocNotificationUtils.getPreviousSolicitorEmail(caseData);
            case NOTIFY_OTHER_SOLICITOR_1:
                return NocNotificationUtils.getOtherSolicitor1Email(caseData);
            case NOTIFY_OTHER_SOLICITOR_2:
                return NocNotificationUtils.getOtherSolicitor2Email(caseData);
            case NOTIFY_NEW_DEFENDANT_SOLICITOR:
                return caseData.getRespondentSolicitor1EmailAddress();
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, event));
        }
    }

    private String getTemplateId(CaseData caseData) {
        switch (event) {
            case NOTIFY_FORMER_SOLICITOR:
                return notificationsProperties.getNoticeOfChangeFormerSolicitor();
            case NOTIFY_OTHER_SOLICITOR_1, NOTIFY_OTHER_SOLICITOR_2:
                if (NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData)) {
                    if (caseData.isClaimantBilingual()) {
                        return notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC();
                    }
                    return notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
                }
                return notificationsProperties.getNoticeOfChangeOtherParties();
            case NOTIFY_NEW_DEFENDANT_SOLICITOR:
                return notificationsProperties.getNotifyNewDefendantSolicitorNOC();
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, event));
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CASE_NAME, NocNotificationUtils.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            FORMER_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOtherSolicitorOrganisationName(caseData, event),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF,
            caseData.getLegacyCaseReference(),
            LEGAL_REP_NAME_WITH_SPACE,
            getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            REFERENCE,
            caseData.getCcdCaseReference().toString()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    public Map<String, String> addPropertiesClaimant(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    public Map<String, String> getPropertiesForEmail(CaseData caseData) {
        if (NOTIFY_OTHER_SOLICITOR_1.equals(event)
            && NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData)) {
            return addPropertiesClaimant(caseData);
        }
        return addProperties(caseData);
    }

    private String getOrganisationName(String orgToName) {
        if (orgToName != null) {
            return organisationService.findOrganisationById(orgToName).orElseThrow(() -> {
                throw new CallbackException("Organisation is not valid for: " + orgToName);
            }).getName();
        }
        return LITIGANT_IN_PERSON;
    }

    private String getOtherSolicitorOrganisationName(CaseData caseData, CaseEvent event) {
        if (event.equals(NOTIFY_OTHER_SOLICITOR_2)) {
            return getOrganisationName(NocNotificationUtils.getOtherSolicitor2Name(caseData));
        } else {
            return getOrganisationName(NocNotificationUtils.getOtherSolicitor1Name(caseData));
        }
    }

    private boolean shouldSkipEvent(CaseEvent event, CaseData caseData) {
        switch (event) {
            case NOTIFY_FORMER_SOLICITOR:
                return caseData.getChangeOfRepresentation().getOrganisationToRemoveID() == null;
            case NOTIFY_NEW_DEFENDANT_SOLICITOR:
                return !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case NOTIFY_OTHER_SOLICITOR_1:
                return NocNotificationUtils.isOtherParty1Lip(caseData)
                    && !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case NOTIFY_OTHER_SOLICITOR_2:
                return NocNotificationUtils.isOtherParty2Lip(caseData);
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, event));
        }
    }
}
