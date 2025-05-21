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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class CaseProceedsInCasemanRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN
    );

    public static final String TASK_ID1 = "CaseProceedsInCasemanNotifyRespondentSolicitor1";
    public static final String TASK_ID2 = "CaseProceedsInCasemanNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final IStateFlowEngine stateFlowEngine;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseProceedsInCaseman
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN.name()
            .equals(callbackParams.getRequest().getEventId())) {
            return TASK_ID1;
        } else {
            return TASK_ID2;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForCaseProceedsInCaseman(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        var multiPartyScenario = getMultiPartyScenario(caseData);

        if (NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN.name().equals(callbackParams.getRequest().getEventId())
            && ONE_V_TWO_TWO_LEGAL_REP != multiPartyScenario) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        if (stateFlowEngine.hasTransitionedTo(callbackParams.getRequest().getCaseDetails(), CLAIM_NOTIFIED)
            || (stateFlowEngine.hasTransitionedTo(callbackParams.getRequest().getCaseDetails(), TAKEN_OFFLINE_BY_STAFF)
            && caseData.isLipvLROneVOne())) {

            String emailAddress;
            if (NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN.name()
                .equals(callbackParams.getRequest().getEventId())) {
                emailAddress = caseData.getRespondentSolicitor1EmailAddress();
            } else {
                emailAddress = caseData.getRespondentSolicitor2EmailAddress();
            }

            Map<String, String> notificationProperties = addProperties(caseData);
            notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentOrgName(callbackParams.getRequest().getEventId(), caseData));
            Optional.ofNullable(emailAddress).ifPresent(
                email -> notificationService.sendMail(
                    email,
                    caseData.isLipvLROneVOne() ? notificationsProperties.getSolicitorCaseTakenOfflineForSpec() :
                        notificationsProperties.getSolicitorCaseTakenOffline(),
                    notificationProperties,
                    String.format(
                        REFERENCE_TEMPLATE,
                        caseData.getLegacyCaseReference()
                    )
                ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getRespondentOrgName(String caseEvent, CaseData caseData) {
        return NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN.name()
            .equals(caseEvent) ? getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
            : getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }
}
