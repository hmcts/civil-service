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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_INTERIM_JUDGMENT_CLAIMANT;

@Service
@RequiredArgsConstructor
public class InterimJudgmentClaimantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_INTERIM_JUDGMENT_CLAIMANT);
    private static final String REFERENCE_TEMPLATE_APPROVAL_CLAIMANT = "interim-judgment-approval-notification-%s";
    private static final String REFERENCE_TEMPLATE_REQUEST_CLAIMANT = "interim-judgment-requested-notification-%s";
    private static final String TASK_ID_CLAIMANT = "NotifyInterimJudgmentClaimant";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyAllPartiesInterimJudgmentApprovedClaimant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyAllPartiesInterimJudgmentApprovedClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YesOrNo.YES)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
                || checkIfBothDefendants(caseData)) {
                notificationService.sendMail(caseData.getApplicantSolicitor1UserDetails().getEmail(),
                                             checkIfBothDefendants(caseData)
                                                 ? notificationsProperties.getInterimJudgmentApprovalClaimant()
                                                 : notificationsProperties.getInterimJudgmentRequestedClaimant(),
                                             addProperties(caseData),
                                             String.format(checkIfBothDefendants(caseData)
                                                               ? REFERENCE_TEMPLATE_APPROVAL_CLAIMANT
                                                               : REFERENCE_TEMPLATE_REQUEST_CLAIMANT,
                                                           caseData.getLegacyCaseReference()));
            }
            if (checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())
                || checkIfBothDefendants(caseData)) {
                notificationService.sendMail(caseData.getApplicantSolicitor1UserDetails().getEmail(),
                                             checkIfBothDefendants(caseData)
                                                 ? notificationsProperties.getInterimJudgmentApprovalClaimant()
                                                 : notificationsProperties.getInterimJudgmentRequestedClaimant(),
                                             addPropertiesDefendant2(caseData),
                                             String.format(checkIfBothDefendants(caseData)
                                                               ? REFERENCE_TEMPLATE_APPROVAL_CLAIMANT
                                                               : REFERENCE_TEMPLATE_REQUEST_CLAIMANT,
                                                           caseData.getLegacyCaseReference())
                );
            }
        } else {
            notificationService.sendMail(caseData.getApplicantSolicitor1UserDetails().getEmail(),
                                         notificationsProperties.getInterimJudgmentApprovalClaimant(),
                                         addProperties(caseData),
                                         String.format(REFERENCE_TEMPLATE_APPROVAL_CLAIMANT,
                                                       caseData.getLegacyCaseReference()));
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
            LEGAL_REP_CLAIMANT, getLegalOrganizationName(caseData),
            CLAIM_NUMBER_INTERIM, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName()
        ));
    }

    public Map<String, String> addPropertiesDefendant2(final CaseData caseData) {
        return new HashMap<>(Map.of(
            LEGAL_REP_CLAIMANT, getLegalOrganizationName(caseData),
            CLAIM_NUMBER_INTERIM, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName()
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

    private boolean checkDefendantRequested(final CaseData caseData, String defendantName) {
        if (caseData.getDefendantDetails() != null) {
            return defendantName.equals(caseData.getDefendantDetails().getValue().getLabel());
        } else {
            return false;
        }
    }

    private Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }
}
