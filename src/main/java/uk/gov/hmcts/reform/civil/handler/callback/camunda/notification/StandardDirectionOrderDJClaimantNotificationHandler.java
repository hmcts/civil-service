package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJClaimantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private static final String CLAIM_NUMBER = "claimReferenceNumber";
    private static final String LEGAL_ORG_NAME = "legalOrgName";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT);
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-claimant-%s";
    private static final String TASK_ID_CLAIMANT = "StandardDirectionOrderDj";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantSDOrderDj
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyClaimantSDOrderDj(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(
            caseData.isApplicant1NotRepresented()
                ? caseData.getClaimantUserDetails().getEmail()
                : caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getStandardDirectionOrderDJTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE_SDO_DJ, caseData.getLegacyCaseReference()));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            LEGAL_ORG_NAME, getLegalOrganizationName(caseData),
            CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
        return properties;
    }

    private String getLegalOrganizationName(final CaseData caseData) {
        if (nonNull(caseData.getApplicant1OrganisationPolicy().getOrganisation())) {
            Optional<Organisation> organisation = organisationService
                .findOrganisationById(caseData.getApplicant1OrganisationPolicy()
                                          .getOrganisation().getOrganisationID());
            if (organisation.isPresent()) {
                return organisation.get().getName();
            }
        }
        return caseData.getApplicant1().getPartyName();
    }
}
