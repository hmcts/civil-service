package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_INTERIM_JUDGMENT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class InterimJudgmentDefendantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_INTERIM_JUDGMENT_DEFENDANT);
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";
    private static final String REFERENCE_TEMPLATE_REQUEST_DEF = "interim-judgment-requested-notification-def-%s";
    private static final String TASK_ID_DEF = "NotifyInterimJudgmentDefendant";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyAllPartiesInterimJudgmentApprovedDefendant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_DEF;
    }

    private CallbackResponse notifyAllPartiesInterimJudgmentApprovedDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var state = "JUDICIAL_REFERRAL";

        if (caseData.isRespondent1LiP() && !YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(state)
                .data(caseData.toMap(objectMapper))
                .build();
        }

        if (caseData.isRespondent1LiP() && YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            if (caseData.isRespondent2LiP()) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .state(state)
                    .data(caseData.toMap(objectMapper))
                    .build();
            }

            notificationService.sendMail(
                caseData.getRespondentSolicitor2EmailAddress() != null
                    ? caseData.getRespondentSolicitor2EmailAddress() :
                    caseData.getRespondentSolicitor1EmailAddress(),
                checkIfBothDefendants(caseData)
                    ? notificationsProperties.getInterimJudgmentApprovalDefendant()
                    : notificationsProperties.getInterimJudgmentRequestedDefendant(),
                addPropertiesDefendant2(caseData),
                String.format(
                    checkIfBothDefendants(caseData)
                        ? REFERENCE_TEMPLATE_APPROVAL_DEF
                        : REFERENCE_TEMPLATE_REQUEST_DEF,
                    caseData.getLegacyCaseReference()
                )
            );

            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(state)
                .data(caseData.toMap(objectMapper))
                .build();
        }

        if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YesOrNo.YES)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
                || checkIfBothDefendants(caseData)) {
                notificationService.sendMail(
                    caseData.getRespondentSolicitor1EmailAddress(),
                    checkIfBothDefendants(caseData)
                        ? notificationsProperties.getInterimJudgmentApprovalDefendant()
                        : notificationsProperties.getInterimJudgmentRequestedDefendant(),
                    addProperties(caseData),
                    String.format(
                        checkIfBothDefendants(caseData)
                            ? REFERENCE_TEMPLATE_APPROVAL_DEF
                            : REFERENCE_TEMPLATE_REQUEST_DEF,
                        caseData.getLegacyCaseReference()
                    )
                );
            }
            if (checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())
                || checkIfBothDefendants(caseData)) {
                notificationService.sendMail(
                    caseData.getRespondentSolicitor2EmailAddress() != null
                        ? caseData.getRespondentSolicitor2EmailAddress() :
                        caseData.getRespondentSolicitor1EmailAddress(),
                    checkIfBothDefendants(caseData)
                        ? notificationsProperties.getInterimJudgmentApprovalDefendant()
                        : notificationsProperties.getInterimJudgmentRequestedDefendant(),
                    addPropertiesDefendant2(caseData),
                    String.format(
                        checkIfBothDefendants(caseData)
                            ? REFERENCE_TEMPLATE_APPROVAL_DEF
                            : REFERENCE_TEMPLATE_REQUEST_DEF,
                        caseData.getLegacyCaseReference()
                    )
                );
            }
        } else {
            notificationService.sendMail(caseData.getRespondentSolicitor1EmailAddress(),
                                         notificationsProperties.getInterimJudgmentApprovalDefendant(),
                                         addProperties(caseData),
                                         String.format(REFERENCE_TEMPLATE_APPROVAL_DEF,
                                                       caseData.getLegacyCaseReference()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(state)
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
            LEGAL_ORG_DEF, getLegalOrganizationName(caseData),
            CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    public Map<String, String> addPropertiesDefendant2(final CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            LEGAL_ORG_DEF, getLegalOrganizationNameDefendant2(caseData),
            CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    private boolean checkDefendantRequested(final CaseData caseData, String defendantName) {
        if (caseData.getDefendantDetails() != null) {
            return defendantName.equals(caseData.getDefendantDetails().getValue().getLabel());
        } else {
            return false;
        }
    }

    private String getLegalOrganizationName(final CaseData caseData) {
        Optional<OrganisationPolicy> respondent1OrganisationPolicy = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy());
        if (respondent1OrganisationPolicy.isPresent()) {
            OrganisationPolicy organisationPolicy = respondent1OrganisationPolicy.get();
            Optional<uk.gov.hmcts.reform.ccd.model.Organisation> policyOrganisation = Optional.ofNullable(organisationPolicy.getOrganisation());
            if (policyOrganisation.isPresent()) {
                Optional<Organisation> organisation = organisationService
                    .findOrganisationById(respondent1OrganisationPolicy.get()
                                              .getOrganisation().getOrganisationID());
                if (organisation.isPresent()) {
                    return organisation.get().getName();
                }
            }
        }
        return caseData.getRespondent1().getPartyName();
    }

    private String getLegalOrganizationNameDefendant2(final CaseData caseData) {
        Optional<Organisation> organisation = organisationService
            .findOrganisationById(getOrganisationIdRespondent2(caseData));
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getRespondent2().getPartyName();
    }

    private Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }

    private String getOrganisationIdRespondent2(final CaseData caseData) {
        if (isNull(caseData.getRespondent2OrganisationPolicy())
            || isNull(caseData.getRespondent2OrganisationPolicy().getOrganisation())) {
            return caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            return caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }
    }
}

