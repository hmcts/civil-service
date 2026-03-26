package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
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
        if (isTargetDefendantNotSelected(caseData, Optional.ofNullable(caseData.getRespondent1()).map(Party::getPartyName).orElse(null))) {
            return;
        }

        try {
            Optional<String> recipientEmail = getDefendant1RecipientEmail(caseData);
            if (recipientEmail.isEmpty()) {
                log.warn("Skipping defendant 1 notification due to missing recipient email for case {}",
                         getCaseId(callbackParams));
                return;
            }

            sendDefendantNotification(
                callbackParams,
                caseData,
                recipientEmail.get(),
                addProperties(caseData),
                "respondent 1"
            );
        } catch (Exception e) {
            log.error("Failed to send email to respondent 1 for case {} due to error {}",
                      getCaseId(callbackParams), e.getMessage());
        }
    }

    private void notifyDefendant2(CallbackParams callbackParams, CaseData caseData) {
        if (!YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return;
        }
        if (isTargetDefendantNotSelected(
            caseData,
            Optional.ofNullable(caseData.getRespondent2())
                .map(Party::getPartyName)
                .orElse(null))) {
            return;
        }

        try {
            Optional<String> recipientEmail = getDefendant2RecipientEmail(caseData);
            if (recipientEmail.isEmpty()) {
                log.warn("Skipping defendant 2 notification due to missing recipient email for case {}",
                         getCaseId(callbackParams));
                return;
            }

            sendDefendantNotification(
                callbackParams,
                caseData,
                recipientEmail.get(),
                addPropertiesDef2(caseData),
                "respondent 2"
            );
        } catch (Exception e) {
            log.error("Failed to send email to respondent 2 for case {} due to error {}",
                      getCaseId(callbackParams), e.getMessage());
        }
    }

    private Optional<String> getDefendant1RecipientEmail(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return Optional.ofNullable(caseData.getDefendantUserDetails())
                .map(IdamUserDetails::getEmail);
        }
        return Optional.ofNullable(caseData.getRespondentSolicitor1EmailAddress());
    }

    private Optional<String> getDefendant2RecipientEmail(CaseData caseData) {
        // In 1v2 with a single legal rep, respondent 1's LR receives the defendant 2 notification.
        if (getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP) {
            return Optional.ofNullable(caseData.getRespondent1EmailAddress());
        }
        return YesOrNo.YES.equals(caseData.getRespondent2Represented())
            ? Optional.ofNullable(caseData.getRespondentSolicitor2EmailAddress())
            : Optional.ofNullable(caseData.getRespondent2()).map(Party::getPartyEmail);
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
            .orElse(Optional.ofNullable(caseData.getRespondent2()).map(Party::getPartyName).orElse(""));

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
        if (nonNull(caseData.getRespondent1OrganisationPolicy())
            && nonNull(caseData.getRespondent1OrganisationPolicy().getOrganisation())) {
            return caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        return null;
    }

    private String getRespondent2OrganisationId(CaseData caseData) {
        if (nonNull(caseData.getRespondent2OrganisationPolicy())
            && nonNull(caseData.getRespondent2OrganisationPolicy().getOrganisation())) {
            return caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        // For some 1v2 states only respondent 1 org policy exists; use it as a fallback for org lookup.
        return getRespondent1OrganisationId(caseData);
    }

    private boolean isBothDefendantsSelected(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return false;
        }
        return getDefendantDetailsLabel(caseData)
            .map(BOTH_DEFENDANTS::equals)
            .orElse(false);
    }

    private boolean isRequestedDefendant(final CaseData caseData, String defendantName) {
        return defendantName != null && getDefendantDetailsLabel(caseData)
            .map(defendantName::equals)
            .orElse(false);
    }

    private Optional<String> getDefendantDetailsLabel(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantDetails())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel);
    }

    private Long getCaseId(CallbackParams callbackParams) {
        return Optional.ofNullable(callbackParams.getRequest())
            .map(CallbackRequest::getCaseDetails)
            .map(CaseDetails::getId)
            .orElse(null);
    }
}
