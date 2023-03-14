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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJDefendantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private static final String BOTH_DEFENDANTS = "Both Defendants";
    private static final String CLAIM_NUMBER = "claimReferenceNumber";
    private static final String LEGAL_ORG_NAME = "legalOrgName";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT);
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private static final String TASK_ID_DEFENDANT = "StandardDirectionOrderDj";

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
        if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
            || checkIfBothDefendants(caseData)) {
            notificationService.sendMail(caseData.getRespondentSolicitor1EmailAddress(),
                                         notificationsProperties.getStandardDirectionOrderDJTemplate(),
                                         addProperties(caseData),
                                         String.format(REFERENCE_TEMPLATE_SDO_DJ,
                                                       caseData.getLegacyCaseReference()));
        }
        if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YesOrNo.YES)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())
                || checkIfBothDefendants(caseData)) {
                notificationService.sendMail(caseData.getRespondentSolicitor2EmailAddress() != null
                                                 ? caseData.getRespondentSolicitor2EmailAddress() :
                                                 caseData.getRespondentSolicitor1EmailAddress(),
                                             notificationsProperties.getStandardDirectionOrderDJTemplate(),
                                             addPropertiesDef2(caseData),
                                             String.format(REFERENCE_TEMPLATE_SDO_DJ,
                                                           caseData.getLegacyCaseReference()));
            }
        }
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
        return new HashMap<>(Map.of(
            LEGAL_ORG_NAME, getLegalOrganizationName(caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesDef2(final CaseData caseData) {
        return new HashMap<>(Map.of(
            LEGAL_ORG_NAME, getLegalOrganizationDef2Name(caseData),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        ));
    }

    private String getLegalOrganizationName(final CaseData caseData) {
        Optional<Organisation> organisation = organisationService
            .findOrganisationById(caseData.getApplicant1OrganisationPolicy()
                                      .getOrganisation().getOrganisationID());
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicant1().getPartyName();
    }

    private String getLegalOrganizationDef2Name(final CaseData caseData) {
        Optional<Organisation> organisation = organisationService
            .findOrganisationById(caseData.getApplicant2OrganisationPolicy() != null
                                      ? caseData.getApplicant2OrganisationPolicy()
                .getOrganisation().getOrganisationID() : caseData.getApplicant1OrganisationPolicy()
                .getOrganisation().getOrganisationID());
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicant2().getPartyName();
    }

    private Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }

    private boolean checkDefendantRequested(final CaseData caseData, String defendantName) {
        if (caseData.getDefendantDetails() != null) {
            return defendantName.equals(caseData.getDefendantDetails().getValue().getLabel());
        } else {
            return false;
        }
    }
}
