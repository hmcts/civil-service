package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
@Slf4j
public class StandardDirectionOrderDJDefendantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final String BOTH_DEFENDANTS = "Both Defendants";
    private static final String CLAIM_NUMBER = "claimReferenceNumber";
    private static final String LEGAL_ORG_NAME = "legalOrgName";
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private static final String TASK_ID_DEFENDANT = "StandardDirectionOrderDj";
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT,
        NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2
    );

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantSDOrderDj
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_DEFENDANT;
    }

    private CallbackResponse notifyDefendantSDOrderDj(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String eventId = callbackParams.getRequest().getEventId();

        if (eventId.equals(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name())) {
            notifyDefendant1(callbackParams, caseData);
        } else if (eventId.equals(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name())) {
            notifyDefendant2(callbackParams, caseData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private void notifyDefendant1(CallbackParams callbackParams, CaseData caseData) {
        if (isTargetDefendantNotSelected(caseData, caseData.getRespondent1().getPartyName())) {
            return;
        }

        String recipientEmail = caseData.isRespondent1NotRepresented()
            ? caseData.getDefendantUserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();

        sendDefendantNotification(
            callbackParams,
            caseData,
            recipientEmail,
            addProperties(caseData),
            "respondent 1"
        );
    }

    private void notifyDefendant2(CallbackParams callbackParams, CaseData caseData) {
        if (!YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return;
        }
        if (isTargetDefendantNotSelected(caseData, caseData.getRespondent2().getPartyName())) {
            return;
        }

        // In 1v2 with a single legal rep, respondent 1's LR receives the defendant 2 notification.
        String recipientEmail = getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP
            ? caseData.getRespondent1EmailAddress()
            : getDefendant2RecipientEmail(caseData);

        sendDefendantNotification(
            callbackParams,
            caseData,
            recipientEmail,
            addPropertiesDef2(caseData),
            "respondent 2"
        );
    }

    private static String getDefendant2RecipientEmail(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getRespondent2Represented())
                ? caseData.getRespondentSolicitor2EmailAddress() : caseData.getRespondent2().getPartyEmail();
    }

    private void sendDefendantNotification(
        CallbackParams callbackParams,
        CaseData caseData,
        String recipientEmail,
        Map<String, String> properties,
        String recipientLabel
    ) {
        try {
            notificationService.sendMail(
                recipientEmail,
                notificationsProperties.getStandardDirectionOrderDJTemplate(),
                properties,
                String.format(REFERENCE_TEMPLATE_SDO_DJ, caseData.getLegacyCaseReference())
            );
        } catch (Exception e) {
            log.error(
                "Failed to send email to {} for case {} due to error {}",
                recipientLabel,
                callbackParams.getRequest().getCaseDetails().getId(),
                e.getMessage()
            );
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String organisationId = getRespondent1OrganisationId(caseData);
        String legalOrgName = findOrganisationName(organisationId)
            .orElse(caseData.getRespondent1().getPartyName());

        return addCommonProperties(caseData, legalOrgName);
    }

    public Map<String, String> addPropertiesDef2(final CaseData caseData) {
        String organisationId = getRespondent2OrganisationId(caseData);
        String legalOrgName = findOrganisationName(organisationId)
            .orElse(caseData.getRespondent2().getPartyName());

        return addCommonProperties(caseData, legalOrgName);
    }

    private Map<String, String> addCommonProperties(CaseData caseData, String legalOrgName) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            LEGAL_ORG_NAME, legalOrgName,
            CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    private boolean isTargetDefendantNotSelected(CaseData caseData, String defendantName) {
        // Defendant can be selected directly by name or implicitly through "Both Defendants".
        return !isRequestedDefendant(caseData, defendantName) && !isBothDefendantsSelected(caseData);
    }

    private Optional<String> findOrganisationName(String organisationId) {
        return Optional.ofNullable(organisationId)
            .flatMap(organisationService::findOrganisationById)
            .map(Organisation::getName);
    }

    private String getRespondent1OrganisationId(CaseData caseData) {
        if (nonNull(caseData.getRespondent1OrganisationPolicy().getOrganisation())) {
            return caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        return null;
    }

    private String getRespondent2OrganisationId(CaseData caseData) {
        if (nonNull(caseData.getRespondent2OrganisationPolicy())) {
            return caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        // For some 1v2 states only respondent 1 org policy exists; use it as a fallback for org lookup.
        return getRespondent1OrganisationId(caseData);
    }

    private boolean isBothDefendantsSelected(CaseData caseData) {
        return !caseData.isRespondent1NotRepresented() && BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }

    private boolean isRequestedDefendant(final CaseData caseData, String defendantName) {
        return caseData.getDefendantDetails() != null && defendantName.equals(caseData.getDefendantDetails().getValue().getLabel());
    }
}
