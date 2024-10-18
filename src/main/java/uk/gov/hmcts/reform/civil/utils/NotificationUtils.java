package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class NotificationUtils {

    private NotificationUtils() {
        //NO-OP
    }

    public static String REFERENCE_NOT_PROVIDED = "Not provided";

    public static boolean isRespondent1(CallbackParams callbackParams, CaseEvent matchEvent) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(matchEvent);
    }

    @SuppressWarnings("java:S4144")
    public static boolean isEvent(CallbackParams callbackParams, CaseEvent matchEvent) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(matchEvent);
    }

    public static boolean is1v1Or2v1Case(CaseData caseData) {
        return getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE);
    }

    public static Map<String, String> caseOfflineNotificationAddProperties(
        CaseData caseData, OrganisationPolicy organisationPolicy, OrganisationService organisationService) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService)
            );
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            String responseTypeToApplicant2 = SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue()
                : caseData.getRespondent1ClaimResponseTypeToApplicant2().toString();
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                REASON, SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent1ClaimResponseType().getDisplayedValue()
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + responseTypeToApplicant2)
                    .concat(" against " + caseData.getApplicant2().getPartyName()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService)
            );
        } else {
            //1v2 template is used and expects different data
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                RESPONDENT_ONE_RESPONSE, SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? caseData.getRespondent1ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                RESPONDENT_TWO_RESPONSE, SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? caseData.getRespondent2ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent2ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService)
            );
        }
    }

    public static String getSolicitorClaimDismissedProperty(List<String> stateHistoryNameList,
                                                            NotificationsProperties notificationsProperties) {
        //scenerio 1: Claim notification does not happen within 4 months of issue
        if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName())) {
            return notificationsProperties.getSolicitorClaimDismissedWithin4Months();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())) {
            //scenerio 2: Claims details notification is not completed within 14 days of the claim notification step
            return notificationsProperties.getSolicitorClaimDismissedWithin14Days();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName())) {
            //scenerio 3 Claimant does not give their intention by the given deadline
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        } else {
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        }
    }

    public static boolean shouldSkipEventForRespondent1LiP(CaseData caseData) {
        return NO.equals(caseData.getRespondent1Represented())
            && caseData.getRespondentSolicitor1EmailAddress() == null;
    }

    public static boolean shouldSkipEventForRespondent2LiP(CaseData caseData) {
        return NO.equals(caseData.getRespondent2Represented())
            && caseData.getRespondentSolicitor2EmailAddress() == null;
    }

    public static String getFormattedHearingDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public static String getFormattedHearingTime(String hourMinute) {
        hourMinute = hourMinute.replace(":", "");
        int hours = Integer.parseInt(hourMinute.substring(0, 2));
        int minutes = Integer.parseInt(hourMinute.substring(2, 4));
        LocalTime time = LocalTime.of(hours, minutes, 0);
        return time.format(DateTimeFormatter.ofPattern("hh:mma")).replace("AM", "am").replace("PM", "pm");
    }

    public static boolean shouldSendMediationNotificationDefendant1LRCarm(CaseData caseData, boolean isCarmEnabled) {
        return isCarmEnabled
            && !caseData.isRespondent1NotRepresented()
            && (YES.equals(caseData.getApplicant1ProceedsWithClaimSpec())
            || (caseData.getCaseDataLiP() != null
            && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim())));
    }

    public static boolean shouldSendMediationNotificationDefendant2LRCarm(CaseData caseData, boolean isCarmEnabled) {
        return isCarmEnabled
            && !caseData.isRespondent2NotRepresented()
            && YES.equals(caseData.getApplicant1ProceedsWithClaimSpec());
    }

    public static String getRespondentLegalOrganizationName(OrganisationPolicy organisationPolicy, OrganisationService organisationService) {
        String id = organisationPolicy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);

        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }

    public static String getDefendantNameBasedOnCaseType(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return getPartyNameBasedOnType(caseData.getRespondent1());
        } else {
            return getPartyNameBasedOnType(caseData.getRespondent1())
                .concat(" and ")
                .concat(getPartyNameBasedOnType(caseData.getRespondent2()));
        }
    }

    public static String getApplicantLegalOrganizationName(CaseData caseData, OrganisationService organisationService) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    public static String getLegalOrganizationNameForRespondent(CaseData caseData, boolean isRespondent1, OrganisationService organisationService) {
        String legalOrganisationName = null;
        String id = isRespondent1 ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            legalOrganisationName = organisation.get().getName();
        }
        return legalOrganisationName;
    }

    public static String getApplicantEmail(CaseData caseData, boolean isApplicantLip) {
        if (isApplicantLip) {
            return caseData.getApplicant1().getPartyEmail() != null ? caseData.getApplicant1Email() : null;
        } else {
            return caseData.getApplicantSolicitor1UserDetails() != null ? caseData.getApplicantSolicitor1UserDetails().getEmail() : null;
        }
    }

    public static String buildPartiesReferencesEmailSubject(CaseData caseData) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();
        boolean addRespondent2Reference = ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData));

        stringBuilder.append(buildClaimantReferenceEmailSubject(caseData));
        if (stringBuilder.length() > 0) {
            stringBuilder.append(" - ");
        }

        Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor1Reference)
            .ifPresentOrElse(ref -> {
                stringBuilder.append(addRespondent2Reference ? "Defendant 1 reference: " : "Defendant reference: ");
                stringBuilder.append(solicitorReferences.getRespondentSolicitor1Reference());
            }, () -> {
                stringBuilder.append(addRespondent2Reference ? "Defendant 1 reference: " : "Defendant reference: ");
                stringBuilder.append(REFERENCE_NOT_PROVIDED);
            });

        if (addRespondent2Reference) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" - ");
            }
            stringBuilder.append("Defendant 2 reference: ");
            if (caseData.getRespondentSolicitor2Reference() != null) {
                stringBuilder.append(caseData.getRespondentSolicitor2Reference());
            } else {
                stringBuilder.append(REFERENCE_NOT_PROVIDED);
            }
        }
        return stringBuilder.toString();
    }

    public static String buildClaimantReferenceEmailSubject(CaseData caseData) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Claimant reference: ");
        Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getApplicantSolicitor1Reference)
            .ifPresentOrElse(ref -> {
                stringBuilder.append(solicitorReferences.getApplicantSolicitor1Reference());
            }, () -> stringBuilder.append(REFERENCE_NOT_PROVIDED));

        return stringBuilder.toString();
    }
}
