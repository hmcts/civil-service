package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED;

@Service
@RequiredArgsConstructor
public class CreateSDOApplicantsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED);

    private static final String REFERENCE_TEMPLATE = "create-sdo-applicants-notification-%s";
    public static final String TASK_ID = "CreateSDONotifyApplicantsSolicitor";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantsSolicitorSDOTriggered
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantsSolicitorSDOTriggered(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipientEmail(caseData),
            getNotificationTemplate(caseData),
            getEmailProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantsLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                 .getOrganisation().getOrganisationID(), caseData)
        );
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public String getApplicantsLegalOrganizationName(String id, CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getNotificationTemplate(CaseData caseData) {

        String unspecTemplate = featureToggleService.isEarlyAdoptersEnabled()
            ? notificationsProperties.getSdoOrderedEA() : notificationsProperties.getSdoOrdered();

        String specTemplate = featureToggleService.isEarlyAdoptersEnabled()
            ? notificationsProperties.getSdoOrderedSpecEA() : notificationsProperties.getSdoOrderedSpec();

        if (caseData.isApplicantLiP()) {
            return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        } else {
            if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
                return specTemplate;
            } else {
                return unspecTemplate;
            }
        }
    }

    private String getRecipientEmail(CaseData caseData) {
        return caseData.isApplicantLiP() ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    private Map<String, String> getEmailProperties(CaseData caseData) {
        return caseData.isApplicantLiP() ? addPropertiesLip(caseData)
            : addProperties(caseData);
    }
}
