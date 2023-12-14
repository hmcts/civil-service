package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefendantResponseApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC
    );

    public static final String TASK_ID = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    public static final String TASK_ID_CC = "DefendantResponseFullDefenceNotifyRespondentSolicitor1CC";
    public static final String TASK_ID_CC_RESP1 = "DefendantResponseFullDefenceNotifyRespondentSolicitor1";
    public static final String TASK_ID_CC_RESP2 = "DefendantResponseFullDefenceNotifyRespondentSolicitor2CC";
    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifySolicitorsForDefendantResponse
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                return TASK_ID;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC_RESP1;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC_RESP2;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifySolicitorsForDefendantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient;
        //determine recipient for notification, based on Camunda Event ID
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
                break;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                recipient = caseData.getRespondentSolicitor1EmailAddress();
                break;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                if (caseData.getRespondent1DQ() != null
                    && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
                    var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor1EmailAddress());
                    recipient = emailAddress.orElse(null);
                } else {
                    var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor2EmailAddress());
                    recipient = emailAddress.orElse(null);
                }
                break;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                recipient = caseData.getRespondentSolicitor2EmailAddress();
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            sendNotificationToSolicitorSpec(caseData, recipient, caseEvent);
        } else {
            sendNotificationToSolicitor(caseData, recipient);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData, String recipient, CaseEvent caseEvent) {
        String emailTemplate;
        if (caseEvent.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE)) {

            if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
                && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
            ) {
                emailTemplate = notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec();
            } else {
                if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                    emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
                } else {
                    emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponseForSpec();
                }
            }

            notificationService.sendMail(
                recipient,
                emailTemplate,
                addPropertiesSpec(caseData, caseEvent),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else if (caseEvent.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC)) {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
            } else {
                emailTemplate = getTemplateForSpecOtherThan1v2DS(caseData);
            }
            if (caseData.getRespondent1ResponseDate() == null || !MultiPartyScenario.getMultiPartyScenario(caseData)
                .equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                notificationService.sendMail(
                    recipient,
                    emailTemplate,
                    addPropertiesSpec(caseData, caseEvent),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }

        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
            } else {
                emailTemplate = getTemplateForSpecOtherThan1v2DS(caseData);
            }
            notificationService.sendMail(
                recipient,
                emailTemplate,
                addPropertiesSpec(caseData, caseEvent),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }

    private String getTemplateForSpecOtherThan1v2DS(CaseData caseData) {

        String emailTemplate;
        if ((caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == BY_SET_DATE
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            &&
            (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction();
        } else {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }

        return emailTemplate;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE) || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        } else {
            //if there are 2 respondents on the case, concatenate the names together for the template subject line
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1())
                    .concat(" and ")
                    .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        }
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData, CaseEvent caseEvent) {
        if (caseEvent.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE)) {
            if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
                && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_ADMISSION.equals(
                caseData.getRespondent2ClaimResponseTypeForSpec()))
            ) {
                String shouldBePaidBy = caseData.getRespondToClaimAdmitPartLRspec()
                    .getWhenWillThisAmountBePaid().getDayOfMonth()
                    + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getMonth()
                    + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getYear();
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                    WHEN_WILL_BE_PAID_IMMEDIATELY, shouldBePaidBy
                );
            } else {
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
                );
            }
        } else if (caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC)) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
            );
        } else {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
                );
            } else {
                return Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
                );
            }
        }
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData,  CaseEvent caseEvent) {
        String organisationID;
        if (caseEvent.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE)) {
            organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else if (caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC)) {
            organisationID = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
                organisationID = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            } else {
                organisationID = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
            }
        }
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
