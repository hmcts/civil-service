package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.EMAIL;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.IS_LIP_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.IS_LIP_OTHER_PARTY_WELSH;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.LEGAL_ORG;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.LIP_NAME;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.getOtherPartyEmailDetails;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.getOtherPartyEmailDetailsPublicQuery;

@Service
@RequiredArgsConstructor
public class NotifyOtherPartyQueryRaisedNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY);

    public static final String TASK_ID = "NotifyOtherPartyQueryRaised";
    private static final String REFERENCE_TEMPLATE = "a-query-has-been-raised-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CoreCaseUserService coreCaseUserService;
    private final OrganisationService organisationService;
    private final QueryManagementCamundaService runtimeService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyOtherPartyQueryHasBeenRaised
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyOtherPartyQueryHasBeenRaised(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
            QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
            String queryId = processVariables.getQueryId();
            List<Map<String, String>> emailDetailsList = getOtherPartyEmailDetailsPublicQuery(caseData, organisationService, coreCaseUserService, queryId);
            boolean isLipOtherParty = emailDetailsList.stream().anyMatch(map -> "TRUE".equalsIgnoreCase(map.get(IS_LIP_OTHER_PARTY)));

            if (isLipOtherParty) {
                emailDetailsList.forEach(otherPartyEmailDetails -> {
                    boolean isWelsh = "WELSH".equalsIgnoreCase(otherPartyEmailDetails.get(IS_LIP_OTHER_PARTY_WELSH));
                    String templateId = isWelsh
                        ? notificationsProperties.getNotifyOtherLipPartyWelshPublicQueryRaised()
                        : notificationsProperties.getNotifyOtherLipPartyPublicQueryRaised();

                    notificationService.sendMail(
                        otherPartyEmailDetails.get(EMAIL),
                        templateId,
                        addProperties(caseData, otherPartyEmailDetails.get(LIP_NAME), isLipOtherParty),
                        String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                    );
                });
            } else {
                emailDetailsList.forEach(otherPartyEmailDetails -> {
                    notificationService.sendMail(
                        otherPartyEmailDetails.get(EMAIL),
                        notificationsProperties.getNotifyOtherPartyPublicQueryRaised(),
                        addProperties(caseData, otherPartyEmailDetails.get(LEGAL_ORG), false),
                        String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                    );
                });
            }
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        } else {
            String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
            QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
            String queryId = processVariables.getQueryId();
            List<Map<String, String>> emailDetailsList = getOtherPartyEmailDetails(caseData, organisationService, coreCaseUserService, queryId);

            emailDetailsList.forEach(otherPartyEmailDetails -> {
                notificationService.sendMail(
                    otherPartyEmailDetails.get(EMAIL),
                    notificationsProperties.getNotifyOtherPartyQueryRaised(),
                    addProperties(caseData, otherPartyEmailDetails.get(LEGAL_ORG), false),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            });
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
    }

    public Map<String, String> addProperties(CaseData caseData, String legalOrgNameOrPartyName, boolean isLipOtherParty) {
        HashMap<String, String> properties = new HashMap<>();

        if (isLipOtherParty) {
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_NAME, legalOrgNameOrPartyName);
        } else {
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgNameOrPartyName);
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        }
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
