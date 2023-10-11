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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefendantResponseApplicantForCuiNotificationHandler
    extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI
    );

    public static final String TASK_ID = "DefendantResponseNotifyApplicantSolicitor1ForCui";
    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifySolicitorsForDefendantResponse
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

    private CallbackResponse notifySolicitorsForDefendantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isLiPClaimant = caseData.isApplicantNotRepresented();

        notificationService.sendMail(
            isLiPClaimant ? caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getEmailTemplate(caseData, isLiPClaimant),
            isLiPClaimant ? addPropertiesForLipClaimant(caseData) : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private Map<String, String> addPropertiesForLipClaimant(CaseData caseData) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData));
        if (caseData.getRespondent1ClaimResponseTypeForSpec().equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        }
        return properties;
    }

    public String getEmailTemplate(CaseData caseData, boolean isLiPClaimant) {
        String emailTemplate;
        if (isLiPClaimant) {
            emailTemplate = notificationsProperties.getNotifyLiPClaimantDefendantResponded();
        } else {
            if (caseData.getRespondent1ClaimResponseTypeForSpec().equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                if (caseData.getResponseClaimMediationSpecRequired().equals(YES)) {
                    emailTemplate = notificationsProperties.getRespondentLipFullDefenceWithMediationTemplate();
                } else {
                    emailTemplate = notificationsProperties.getRespondentLipFullDefenceNoMediationTemplate();
                }
            } else {
                emailTemplate = notificationsProperties.getRespondentLipFullAdmitOrPartAdmitTemplate();
            }
        }

        return emailTemplate;
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
