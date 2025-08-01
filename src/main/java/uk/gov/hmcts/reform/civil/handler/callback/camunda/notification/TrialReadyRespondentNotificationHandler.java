package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class TrialReadyRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY
    );

    public static final String TASK_ID_RESPONDENT_ONE = "TrialReadyNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT_TWO = "TrialReadyNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE = "trial-ready-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForTrialReady
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isForRespondentSolicitor1(callbackParams) ? TASK_ID_RESPONDENT_ONE : TASK_ID_RESPONDENT_TWO;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForTrialReady(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String respondentEmail = isForRespondentSolicitor1(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();

        if (isLipDefendant(callbackParams)) {
            if (caseData.getRespondent1().getPartyEmail() != null) {
                notificationService.sendMail(
                    caseData.getRespondent1().getPartyEmail(),
                    caseData.isRespondentResponseBilingual()
                        ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                        : notificationsProperties.getNotifyLipUpdateTemplate(),
                    addPropertiesLRvLip(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        if (null == respondentEmail && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            respondentEmail = caseData.getRespondentSolicitor1EmailAddress();
        }

        notificationService.sendMail(
            respondentEmail,
            notificationsProperties.getSolicitorTrialReady(),
            addPropertiesRep(caseData, isForRespondentSolicitor1(callbackParams) ? true : false),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private Map<String, String> addPropertiesRep(CaseData caseData, boolean isFirst) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, isFirst, organisationService),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private Map<String, String> addPropertiesLRvLip(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private boolean isForRespondentSolicitor1(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY.name());
    }

    private boolean isLipDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.isRespondent1NotRepresented();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
