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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ChangeOfRepresentationNotifyParties;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DEFENDANT_NOC_ONLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
public class ChangeOfRepresentationNotifier extends Notifier {

    private static final String LITIGANT_IN_PERSON = "LiP";
    private static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private static final String NOTIFY_FORMER_SOLICITOR = "NOTIFY_FORMER_SOLICITOR";
    private static final String NOTIFY_OTHER_SOLICITOR_1 = "NOTIFY_OTHER_SOLICITOR_1";
    private static final String NOTIFY_OTHER_SOLICITOR_2 = "NOTIFY_OTHER_SOLICITOR_2";
    private static final String NOTIFY_CLAIMANT_LIP = "NOTIFY_CLAIMANT_LIP";
    private static final String NOTIFY_NEW_DEFENDANT_SOLICITOR = "NOTIFY_NEW_DEFENDANT_SOLICITOR";
    private static final String NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC = "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC";

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
                        getClaimantLipToNotify(caseData),
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
                CASEMAN_REF, caseData.getLegacyCaseReference(),
                LEGAL_REP_NAME_WITH_SPACE, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
                REFERENCE, caseData.getCcdCaseReference().toString()
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
        return organisationService.findOrganisationById(id)
                .map(Organisation::getName)
                .orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    private String getOrganisationName(String orgToName) {
        return Optional.ofNullable(orgToName)
                .map(id -> organisationService.findOrganisationById(id)
                        .orElseThrow(() -> new CallbackException("Organisation is not valid for: " + id))
                        .getName())
                .orElse(LITIGANT_IN_PERSON);
    }

    private String getOtherSolicitorOrganisationName(CaseData caseData, boolean isOtherSolicitor2Notification) {
        return getOrganisationName(
                isOtherSolicitor2Notification
                        ? NocNotificationUtils.getOtherSolicitor2Name(caseData)
                        : NocNotificationUtils.getOtherSolicitor1Name(caseData)
        );
    }

    private EmailDTO getFormerSolicitorToNotify(CaseData caseData) {
        if (shouldSkipThisNotification(caseData, NOTIFY_FORMER_SOLICITOR)) {
            return null;
        }
        Map<String, String> properties = addProperties(caseData);
        return EmailDTO.builder()
            .targetEmail(getRecipientEmail(caseData, NOTIFY_FORMER_SOLICITOR))
            .emailTemplate(getTemplateId(caseData, NOTIFY_FORMER_SOLICITOR))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getOtherSolicitor1ToNotify(CaseData caseData) {
        if (shouldSkipThisNotification(caseData, NOTIFY_OTHER_SOLICITOR_1)) {
            return null;
        }
        Map<String, String> properties = addProperties(caseData);
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, NOTIFY_OTHER_SOLICITOR_1))
                .emailTemplate(getTemplateId(caseData,NOTIFY_OTHER_SOLICITOR_1))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getClaimantLipToNotify(CaseData caseData) {
        if (shouldSkipThisNotification(caseData, NOTIFY_CLAIMANT_LIP)) {
            return null;
        }
        Map<String, String> properties = addPropertiesClaimant(caseData);
        return EmailDTO.builder()
            .targetEmail(getRecipientEmail(caseData, NOTIFY_CLAIMANT_LIP))
            .emailTemplate(getTemplateId(caseData, NOTIFY_CLAIMANT_LIP))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getOtherSolicitor2ToNotify(CaseData caseData) {
        if (!stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES) ||
                shouldSkipThisNotification(caseData, NOTIFY_OTHER_SOLICITOR_2)) {
            return null;
        }
        Map<String, String> properties = new HashMap<>(addProperties(caseData));
        properties.put(OTHER_SOL_NAME, getOtherSolicitorOrganisationName(caseData, true));
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, NOTIFY_OTHER_SOLICITOR_2))
                .emailTemplate(getTemplateId(caseData,NOTIFY_OTHER_SOLICITOR_2))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    //In case of Lip v LR to Lip vs new LR
    private EmailDTO getNewDefendantSolicitor1ToNotify(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        if (!stateFlowEngine.evaluate(caseData).isFlagSet(DEFENDANT_NOC_ONLINE) ||
                shouldSkipThisNotification(caseData, NOTIFY_NEW_DEFENDANT_SOLICITOR)) {
            return null;
        }
        return EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData, NOTIFY_NEW_DEFENDANT_SOLICITOR))
                .emailTemplate(getTemplateId(caseData,NOTIFY_NEW_DEFENDANT_SOLICITOR))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getClaimantSolicitorToNotifyUnpaidHearingFee(CaseData caseData) {
        String stateName = stateFlowEngine.evaluate(caseData).getState().getName();

        if (IN_HEARING_READINESS.fullName().equals(stateName) &&
                !checkIfHearingAlreadyPaid(caseData) && nonNull(caseData.getHearingFee())) {
            return EmailDTO.builder()
                    .targetEmail(getRecipientEmail(caseData, NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC))
                    .emailTemplate(getTemplateId(caseData,NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC))
                    .parameters(addHearingFeeEmailProperties(caseData))
                    .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                    .build();
        }
        return null;
    }

    private boolean checkIfHearingAlreadyPaid(CaseData caseData) {
        PaymentDetails paymentDetails = caseData.getHearingFeePaymentDetails();
        return paymentDetails != null && PaymentStatus.SUCCESS.equals(paymentDetails.getStatus());
    }

    String getRecipientEmail(CaseData caseData, String notificationType) {
        return switch (notificationType) {
            case NOTIFY_FORMER_SOLICITOR -> NocNotificationUtils.getPreviousSolicitorEmail(caseData);
            case NOTIFY_OTHER_SOLICITOR_1 -> NocNotificationUtils.getOtherSolicitor1Email(caseData);
            case NOTIFY_OTHER_SOLICITOR_2 -> NocNotificationUtils.getOtherSolicitor2Email(caseData);
            case NOTIFY_CLAIMANT_LIP -> NocNotificationUtils.getClaimantLipEmail(caseData);
            case NOTIFY_NEW_DEFENDANT_SOLICITOR -> caseData.getRespondentSolicitor1EmailAddress();
            case NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC ->
                    caseData.getApplicantSolicitor1UserDetailsEmail();
            default ->
                    throw new CallbackException(String.format("Cannot find the recipient email for %s", notificationType));
        };
    }

    String getTemplateId(CaseData caseData, String notificationType) {
        return switch (notificationType) {
            case NOTIFY_FORMER_SOLICITOR -> notificationsProperties.getNoticeOfChangeFormerSolicitor();
            case NOTIFY_OTHER_SOLICITOR_1, NOTIFY_OTHER_SOLICITOR_2 -> notificationsProperties.getNoticeOfChangeOtherParties();
            case NOTIFY_CLAIMANT_LIP -> {
                if (caseData.isClaimantBilingual()) {
                    yield notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC();
                }
                yield notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
            }
            case NOTIFY_NEW_DEFENDANT_SOLICITOR -> notificationsProperties.getNotifyNewDefendantSolicitorNOC();
            case NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC ->
                    notificationsProperties.getHearingFeeUnpaidNoc();
            default ->
                    throw new CallbackException(String.format("Cannot find the template id for %s", notificationType));
        };
    }

    boolean shouldSkipThisNotification(CaseData caseData, String notificationType) {
        return switch (notificationType) {
            case NOTIFY_FORMER_SOLICITOR -> caseData.getChangeOfRepresentation().getOrganisationToRemoveID() == null;
            case NOTIFY_OTHER_SOLICITOR_1 -> NocNotificationUtils.isOtherParty1Lip(caseData);
            case NOTIFY_OTHER_SOLICITOR_2 -> NocNotificationUtils.isOtherParty2Lip(caseData);
            case NOTIFY_CLAIMANT_LIP, NOTIFY_NEW_DEFENDANT_SOLICITOR ->
                !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC ->
                    checkIfHearingAlreadyPaid(caseData) || nonNull(caseData.getHearingFee());
            default -> throw new CallbackException(String.format("Cannot find the event to skip %s", notificationType));
        };
    }
}
