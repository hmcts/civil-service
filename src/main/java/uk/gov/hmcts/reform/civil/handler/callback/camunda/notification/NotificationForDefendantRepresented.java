package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
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
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class NotificationForDefendantRepresented extends CallbackHandler implements NotificationData {

    private final OrganisationService organisationService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private static final String REFERENCE_TEMPLATE_LIP =
        "notify-lip-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_LR =
        "notify-lr-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_CLAIMANT_LIP =
        "notify-claimant-lip-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_CLAIMANT_LR =
        "notify-claimant-lr-after-defendant-noc-approval-%s";
    public static final String TASK_ID_DEFENDANT = "NotifyDefendantLipAfterNocApproval";
    public static final String TASK_ID_DEFENDANT_LR = "NotifyDefendantLrAfterNocApproval";
    public static final String TASK_ID_CLAIMANT = "NotifyClaimantLipDefendantRepresented";
    public static final String TASK_ID_CLAIMANT_LR = "NotifyClaimantLrDefendantRepresented";
    private static final String TEMPLATE_MAP_ID = "templateId";
    private static final String EMAIL_MAP_ID = "emailId";
    private static final String REFERENCE_MAP_ID = "referenceId";
    private static final String LITIGANT_IN_PERSON = "LiP";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyLipAfterNocApproval
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        var caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();
        return setNotificationCamundaActivity(caseData, caseEvent);
    }

    private CallbackResponse notifyLipAfterNocApproval(CallbackParams callbackParams) {
        var caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();
        Map<String, Object> notificationTemplateMapping = setNotificationMapping(caseData, caseEvent);
        if (!notificationTemplateMapping.isEmpty() && notificationTemplateMapping.get(EMAIL_MAP_ID) != null
            && isNotEmpty(notificationTemplateMapping.get(EMAIL_MAP_ID).toString())) {
            notificationService.sendMail(
                notificationTemplateMapping.get(EMAIL_MAP_ID).toString(),
                notificationTemplateMapping.get(TEMPLATE_MAP_ID).toString(),
                setNotificationProperties(caseData, caseEvent),
                String.format(notificationTemplateMapping.get(REFERENCE_MAP_ID).toString(), caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(
            NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL,
            NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL,
            NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesDefendantLr(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_REP_NAME, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    public Map<String, String> addPropertiesClaimant(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesClaimantLr(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CASE_NAME, NocNotificationUtils.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOrganisationName(NocNotificationUtils.getOtherSolicitor1Name(caseData)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String getOrganisationName(String orgToName) {
        if (orgToName != null) {
            return organisationService.findOrganisationById(orgToName).orElseThrow(() -> {
                throw new CallbackException("Organisation is not valid for: " + orgToName);
            }).getName();
        }
        return LITIGANT_IN_PERSON;
    }

    private Map<String, Object> setNotificationMapping(CaseData caseData, CaseEvent caseEvent) {
        Map<String, Object> mapping = new HashMap<>();

        switch (caseEvent) {
            case NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL:
                mapping.put(TEMPLATE_MAP_ID, templateIDForDefendant(caseData));
                mapping.put(EMAIL_MAP_ID, caseData.getRespondent1().getPartyEmail());
                mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LIP);
                return mapping;
            case NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL:
                mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate());
                mapping.put(EMAIL_MAP_ID, caseData.getRespondentSolicitor1EmailAddress());
                mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LR);
                return mapping;
            case NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED:
                if (caseData.getApplicant1Represented() == YesOrNo.NO) {
                    mapping.put(TEMPLATE_MAP_ID, templateIDForClaimant(caseData));
                    mapping.put(EMAIL_MAP_ID, caseData.getApplicant1Email());
                    mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_CLAIMANT_LIP);
                } else {
                    mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNoticeOfChangeOtherParties());
                    mapping.put(EMAIL_MAP_ID, caseData.getApplicantSolicitor1UserDetails().getEmail());
                    mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_CLAIMANT_LR);
                }
                return mapping;
            default:
                return mapping;
        }
    }

    private Map<String, String> setNotificationProperties(CaseData caseData, CaseEvent caseEvent) {
        switch (caseEvent) {
            case NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL:
                return addProperties(caseData);
            case NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL:
                return addPropertiesDefendantLr(caseData);
            case NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED:
                if (caseData.getApplicant1Represented() == YesOrNo.NO) {
                    return addPropertiesClaimant(caseData);
                } else {
                    return addPropertiesClaimantLr(caseData);
                }
            default:
                return new HashMap<>();
        }
    }

    private String templateIDForClaimant(CaseData caseData) {
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC();
        }
        return notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
    }

    private String templateIDForDefendant(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC();
        }
        return notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate();
    }

    private String setNotificationCamundaActivity(CaseData caseData, CaseEvent caseEvent) {
        return switch (caseEvent) {
            case NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL -> TASK_ID_DEFENDANT;
            case NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL -> TASK_ID_DEFENDANT_LR;
            case NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED -> {
                if (caseData.getApplicant1Represented() == YesOrNo.NO) {
                    yield TASK_ID_CLAIMANT;
                }
                yield TASK_ID_CLAIMANT_LR;
            }
            default -> null;
        };
    }
}
