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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseConfirmsToProceedRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC,
        NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK,
        NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK,
        NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK);

    public static final String TASK_ID = "ClaimantConfirmsToProceedNotifyRespondentSolicitor1";
    public static final String Task_ID_RESPONDENT_SOL2 = "ClaimantConfirmsToProceedNotifyRespondentSolicitor2";
    public static final String TASK_ID_CC = "ClaimantConfirmsToProceedNotifyApplicantSolicitor1CC";

    public static final String TASK_ID_MULTITRACK = "ClaimantConfirmsToProceedNotifyRespondentSolicitor1Multitrack";
    public static final String TASK_ID_RESPONDENT_SOL2_MULTITRACK = "ClaimantConfirmsToProceedNotifyRespondentSolicitor2Multitrack";
    public static final String TASK_ID_CC_MULTITRACK = "ClaimantConfirmsToProceedNotifyApplicantSolicitor1CCMultitrack";
    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";
    private static final String NP_PROCEED_REFERENCE_TEMPLATE
        = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimantConfirmsToProceed
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            if (isRespondentSolicitor2NotificationMultiTrack(callbackParams)) {
                return TASK_ID_RESPONDENT_SOL2_MULTITRACK;
            }
            return isCcNotificationMultiTrack(callbackParams) ? TASK_ID_CC_MULTITRACK : TASK_ID_MULTITRACK;
        }
        if (isRespondentSolicitor2Notification(callbackParams)) {
            return Task_ID_RESPONDENT_SOL2;
        }
        return isCcNotification(callbackParams) ? TASK_ID_CC : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimantConfirmsToProceed(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = isCcNotification(callbackParams)
            ? caseData.getApplicantSolicitor1UserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();

        if (MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            recipient = isCcNotificationMultiTrack(callbackParams)
                ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                : caseData.getRespondentSolicitor1EmailAddress();
        }

        if (isRespondentSolicitor2Notification(callbackParams)) {
            recipient = caseData.getRespondentSolicitor2EmailAddress();
        }

        if (isRespondentSolicitor2NotificationMultiTrack(callbackParams)) {
            recipient = caseData.getRespondentSolicitor2EmailAddress();
        }

        if (isLRvLipToDefendant(callbackParams)) {
            if (caseData.getRespondent1().getPartyEmail() != null) {
                notificationService.sendMail(
                    caseData.getRespondent1().getPartyEmail(),
                    notificationsProperties.getRespondent1LipClaimUpdatedTemplate(),
                    addPropertiesLRvLip(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        if ((isRespondentSolicitor2Notification(callbackParams)
            && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()))
            || (!isRespondentSolicitor2Notification(callbackParams)
            && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()))) {
            notificationService.sendMail(
                recipient,
                notificationsProperties.getClaimantSolicitorConfirmsNotToProceed(),
                addProperties(caseData),
                String.format(NP_PROCEED_REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        String template;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            template = getSpecTemplate(callbackParams, caseData);
        } else if (MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            template = notificationsProperties.getSolicitorCaseTakenOffline();
        } else {
            template = notificationsProperties.getClaimantSolicitorConfirmsToProceed();
        }

        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        notificationService.sendMail(
            recipient,
            template,
            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? addPropertiesSpec(caseData, caseEvent)
                : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getSpecTemplate(CallbackParams callbackParams, CaseData caseData) {
        String template;
        if (isCcNotification(callbackParams)) {
            if (rejectedAll(caseData) && mediationRejected(caseData)) {
                template = notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction();
            } else {
                template = notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec();
            }
        } else {
            if (rejectedAll(caseData) && mediationRejected(caseData)) {
                template = notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction();
            } else {
                template = notificationsProperties.getRespondentSolicitorNotifyToProceedSpec();
            }
        }
        return template;
    }

    /**
     * Consider that reject all is true if any respondent rejected all the claim.
     *
     * @param caseData the case data of a spec claim
     * @return true if and only if at least one respondent rejected all the claim
     */
    private boolean rejectedAll(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE;
    }

    /**
     * Mediation is applicable only when all parties are willing to try it.
     *
     * @param caseData a spec claim
     * @return true if and only if at least one party did not agree to mediation
     */
    private boolean mediationRejected(CaseData caseData) {
        return Stream.of(
            caseData.getResponseClaimMediationSpecRequired(),
            caseData.getResponseClaimMediationSpec2Required(),
            Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation).orElse(null)
        ).filter(Objects::nonNull).anyMatch(YesOrNo.NO::equals);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData, CaseEvent caseEvent) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
        );
    }

    public Map<String, String> addPropertiesLRvLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC.name());
    }

    private boolean isCcNotificationMultiTrack(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK.name());
    }

    private boolean isRespondentSolicitor2Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED.name());
    }

    private boolean isRespondentSolicitor2NotificationMultiTrack(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK.name());
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData, CaseEvent caseEvent) {
        String organisationID;
        if (caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC)) {
            organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            organisationID = caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED)
                ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
                : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private boolean isLRvLipToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && (caseData.isLRvLipOneVOne()
            || caseData.isLipvLipOneVOne())
            && !isCcNotification(callbackParams);
    }
}
