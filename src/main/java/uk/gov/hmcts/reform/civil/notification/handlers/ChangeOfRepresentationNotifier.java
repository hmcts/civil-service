package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ChangeOfRepresentationNotifyParties;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
public class ChangeOfRepresentationNotifier extends Notifier {

    private static final String LITIGANT_IN_PERSON = "LiP";
    private static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    public ChangeOfRepresentationNotifier(NotificationService notificationService,
                                          NotificationsProperties notificationsProperties,
                                          OrganisationService organisationService,
                                          SimpleStateFlowEngine stateFlowEngine,
                                          CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    protected String getTaskId() {
        return ChangeOfRepresentationNotifyParties.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Stream.of(
                        getFormerSolicitorToNotify(caseData),
                        getOtherSolicitor1ToNotify(caseData),
                        getOtherSolicitor2ToNotify(caseData),
                        getNewDefendantSolicitor1ToNotify(caseData),
                        getClaimantSolicitorToNotifyUnpaidHearingFee(caseData)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Map<String, String> addPropertiesClaimant(CaseData caseData) {
        return Map.of(
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
                DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
                CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                CASE_NAME, NocNotificationUtils.getCaseName(caseData),
                ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                CCD_REF, caseData.getCcdCaseReference().toString(),
                FORMER_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()),
                NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
                OTHER_SOL_NAME, getOtherSolicitorOrganisationName(caseData, false),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF,
                caseData.getLegacyCaseReference(),
                LEGAL_REP_NAME_WITH_SPACE,
                getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
                REFERENCE,
                caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addHearingFeeEmailProperties(CaseData caseData) {
        return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                LEGAL_ORG_NAME, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                        .getOrganisation()
                        .getOrganisationID(), caseData),
                HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
                COURT_LOCATION, caseData.getHearingLocation().getValue().getLabel(),
                HEARING_TIME, caseData.getHearingTimeHourMinute(),
                HEARING_FEE, String.valueOf(caseData.getHearingFee().formData()),
                HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public String getLegalOrganizationName(String id, CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getOrganisationName(String orgToName) {
        if (orgToName != null) {
            return organisationService.findOrganisationById(orgToName).
                    orElseThrow(() -> new CallbackException("Organisation is not valid for: " + orgToName)).getName();
        }
        return LITIGANT_IN_PERSON;
    }

    private String getOtherSolicitorOrganisationName(CaseData caseData, boolean isOtherSolicitor2Notification) {
        if (isOtherSolicitor2Notification) {
            return getOrganisationName(NocNotificationUtils.getOtherSolicitor2Name(caseData));
        } else {
            return getOrganisationName(NocNotificationUtils.getOtherSolicitor1Name(caseData));
        }
    }

    private EmailDTO getFormerSolicitorToNotify(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        if (shouldSkipThisNotification(caseData, "NOTIFY_FORMER_SOLICITOR")) {
            return null;
        }
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, "NOTIFY_FORMER_SOLICITOR"))
                .emailTemplate(getTemplateId(caseData,"NOTIFY_FORMER_SOLICITOR"))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getOtherSolicitor1ToNotify(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        if (NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData)) {
            properties = addPropertiesClaimant(caseData);
        }
        if (shouldSkipThisNotification(caseData, "NOTIFY_OTHER_SOLICITOR_1")) {
            return null;
        }
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, "NOTIFY_OTHER_SOLICITOR_1"))
                .emailTemplate(getTemplateId(caseData,"NOTIFY_OTHER_SOLICITOR_1"))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getOtherSolicitor2ToNotify(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(OTHER_SOL_NAME, getOtherSolicitorOrganisationName(caseData, true));
        if (shouldSkipThisNotification(caseData, "NOTIFY_OTHER_SOLICITOR_2")) {
            return null;
        }
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, "NOTIFY_OTHER_SOLICITOR_2"))
                .emailTemplate(getTemplateId(caseData,"NOTIFY_OTHER_SOLICITOR_2"))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    //In case of Lip v LR to Lip vs new LR
    private EmailDTO getNewDefendantSolicitor1ToNotify(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        if (shouldSkipThisNotification(caseData, "NOTIFY_NEW_DEFENDANT_SOLICITOR")) {
            return null;
        }
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, "NOTIFY_NEW_DEFENDANT_SOLICITOR"))
                .emailTemplate(getTemplateId(caseData,"NOTIFY_NEW_DEFENDANT_SOLICITOR"))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getClaimantSolicitorToNotifyUnpaidHearingFee(CaseData caseData) {
        Map<String, String> properties = addHearingFeeEmailProperties(caseData);
        if (!checkIfHearingAlreadyPaid(caseData) && nonNull(caseData.getHearingFee())) {
            return EmailDTO.builder()
                    .targetEmail(getRecipientEmail(caseData, "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC"))
                    .emailTemplate(getTemplateId(caseData,"NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC"))
                    .parameters(properties)
                    .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                    .build();
        }
        return null;
    }

    private boolean checkIfHearingAlreadyPaid(CaseData caseData) {
        PaymentDetails paymentDetails = caseData.getHearingFeePaymentDetails();
        return paymentDetails != null && PaymentStatus.SUCCESS.equals(paymentDetails.getStatus());
    }

    private String getRecipientEmail(CaseData caseData, String notificationType) {
        switch (notificationType) {
            case "NOTIFY_FORMER_SOLICITOR":
                return NocNotificationUtils.getPreviousSolicitorEmail(caseData);
            case "NOTIFY_OTHER_SOLICITOR_1":
                return NocNotificationUtils.getOtherSolicitor1Email(caseData);
            case "NOTIFY_OTHER_SOLICITOR_2":
                return NocNotificationUtils.getOtherSolicitor2Email(caseData);
            case "NOTIFY_NEW_DEFENDANT_SOLICITOR":
                return caseData.getRespondentSolicitor1EmailAddress();
            case "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC":
                return caseData.getApplicantSolicitor1UserDetails().getEmail();
            default:
                throw new CallbackException(String.format("Cannot find the recipient email for %s", notificationType));
        }
    }

    private String getTemplateId(CaseData caseData, String notificationType) {
        switch (notificationType) {
            case "NOTIFY_FORMER_SOLICITOR":
                return notificationsProperties.getNoticeOfChangeFormerSolicitor();
            case "NOTIFY_OTHER_SOLICITOR_1", "NOTIFY_OTHER_SOLICITOR_2":
                if (NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData)) {
                    if (caseData.isClaimantBilingual()) {
                        return notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC();
                    }
                    return notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
                }
                return notificationsProperties.getNoticeOfChangeOtherParties();
            case "NOTIFY_NEW_DEFENDANT_SOLICITOR":
                return notificationsProperties.getNotifyNewDefendantSolicitorNOC();
            case "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC":
                return notificationsProperties.getHearingFeeUnpaidNoc();
            default:
                throw new CallbackException(String.format("Cannot find the template id for %s", notificationType));
        }
    }

    private boolean shouldSkipThisNotification(CaseData caseData,  String notificationType) {
        switch (notificationType) {
            case "NOTIFY_FORMER_SOLICITOR":
                return caseData.getChangeOfRepresentation().getOrganisationToRemoveID() == null;
            case "NOTIFY_NEW_DEFENDANT_SOLICITOR":
                return !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case "NOTIFY_OTHER_SOLICITOR_1":
                return NocNotificationUtils.isOtherParty1Lip(caseData)
                        && !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case "NOTIFY_OTHER_SOLICITOR_2":
                return NocNotificationUtils.isOtherParty2Lip(caseData);
            case "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC":
                return checkIfHearingAlreadyPaid(caseData) || nonNull(caseData.getHearingFee());
            default:
                throw new CallbackException(String.format("Cannot find the event to skip %s", notificationType));
        }
    }
}
