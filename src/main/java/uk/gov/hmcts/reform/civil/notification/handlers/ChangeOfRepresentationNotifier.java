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

    private static final String LIP = "LiP";
    private static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private static class NotificationType {
        static final String FORMER_SOLICITOR = "NOTIFY_FORMER_SOLICITOR";
        static final String OTHER_SOLICITOR_1 = "NOTIFY_OTHER_SOLICITOR_1";
        static final String OTHER_SOLICITOR_2 = "NOTIFY_OTHER_SOLICITOR_2";
        static final String CLAIMANT_LIP = "NOTIFY_CLAIMANT_LIP";
        static final String NEW_DEFENDANT_SOLICITOR = "NOTIFY_NEW_DEFENDANT_SOLICITOR";
        static final String CLAIMANT_SOLICITOR_UNPAID_HEARING = "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC";
    }

    public ChangeOfRepresentationNotifier(NotificationService notificationService,
                                          NotificationsProperties notificationsProperties,
                                          OrganisationService organisationService,
                                          SimpleStateFlowEngine stateFlowEngine,
                                          CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @FunctionalInterface
    private interface PropertyBuilder {
        Map<String, String> build(CaseData caseData);
    }

    @Override
    protected String getTaskId() {
        return ChangeOfRepresentationNotifyParties.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Stream.of(
                    //In case of LR v LR/LR
                    buildPartyEmail(caseData, NotificationType.FORMER_SOLICITOR, this::addProperties),
                    buildPartyEmail(caseData, NotificationType.OTHER_SOLICITOR_1, this::addProperties),
                    buildPartyEmail(caseData, NotificationType.OTHER_SOLICITOR_2, this::addOtherSolicitor2Properties),
                    //In case of Lip v LR
                    buildPartyEmail(caseData, NotificationType.CLAIMANT_LIP, this::addPropertiesClaimant),
                    buildPartyEmail(caseData, NotificationType.NEW_DEFENDANT_SOLICITOR, this::addProperties),
                    getUnpaidHearingFeeApplicantSolicitorNotification(caseData))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private EmailDTO buildPartyEmail(CaseData caseData, String type, PropertyBuilder propertyBuilder) {
        if (shouldSkipNotification(caseData, type)) {
            return null;
        }

        Map<String, String> props = propertyBuilder.build(caseData);

        return EmailDTO.builder()
            .targetEmail(getRecipientEmail(caseData, type))
            .emailTemplate(getTemplate(caseData, type))
            .parameters(props)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getUnpaidHearingFeeApplicantSolicitorNotification(CaseData caseData) {
        String state = stateFlowEngine.evaluate(caseData).getState().getName();
        if (IN_HEARING_READINESS.fullName().equals(state) && !isHearingFeePaid(caseData) && nonNull(caseData.getHearingFee())) {
            return buildPartyEmail(caseData, NotificationType.CLAIMANT_SOLICITOR_UNPAID_HEARING, this::addHearingFeeEmailProperties);
        }
        return null;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CASE_NAME, NocNotificationUtils.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            FORMER_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOtherSolicitorOrgName(caseData, false),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            LEGAL_REP_NAME_WITH_SPACE, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            REFERENCE, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addPropertiesClaimant(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addHearingFeeEmailProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getLegalOrganizationName(caseData),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, caseData.getHearingLocation().getValue().getLabel(),
            HEARING_TIME, caseData.getHearingTimeHourMinute(),
            HEARING_FEE, String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private Map<String, String> addOtherSolicitor2Properties(CaseData caseData) {
        Map<String, String> props = new HashMap<>(addProperties(caseData));
        props.put(OTHER_SOL_NAME, getOtherSolicitorOrgName(caseData, true));
        return props;
    }

    private String getLegalOrganizationName(CaseData caseData) {
        String orgId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        return organisationService.findOrganisationById(orgId)
            .map(Organisation::getName)
            .orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    private String getOrganisationName(String orgId) {
        return Optional.ofNullable(orgId)
            .map(id -> organisationService.findOrganisationById(id)
                .orElseThrow(() -> new CallbackException("Invalid organisation ID: " + id)).getName())
            .orElse(LIP);
    }

    private String getOtherSolicitorOrgName(CaseData caseData, boolean isOtherSolicitor2) {
        String orgId = isOtherSolicitor2
            ? NocNotificationUtils.getOtherSolicitor2Name(caseData)
            : NocNotificationUtils.getOtherSolicitor1Name(caseData);
        return getOrganisationName(orgId);
    }

    private boolean isHearingFeePaid(CaseData caseData) {
        PaymentDetails details = caseData.getHearingFeePaymentDetails();
        return details != null && PaymentStatus.SUCCESS.equals(details.getStatus());
    }

    private String getRecipientEmail(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.FORMER_SOLICITOR -> NocNotificationUtils.getPreviousSolicitorEmail(caseData);
            case NotificationType.OTHER_SOLICITOR_1 -> NocNotificationUtils.getOtherSolicitor1Email(caseData);
            case NotificationType.OTHER_SOLICITOR_2 -> NocNotificationUtils.getOtherSolicitor2Email(caseData);
            case NotificationType.CLAIMANT_LIP -> NocNotificationUtils.getClaimantLipEmail(caseData);
            case NotificationType.NEW_DEFENDANT_SOLICITOR -> caseData.getRespondentSolicitor1EmailAddress();
            case NotificationType.CLAIMANT_SOLICITOR_UNPAID_HEARING -> caseData.getApplicantSolicitor1UserDetailsEmail();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private String getTemplate(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.FORMER_SOLICITOR -> notificationsProperties.getNoticeOfChangeFormerSolicitor();
            case NotificationType.OTHER_SOLICITOR_1, NotificationType.OTHER_SOLICITOR_2 ->
                notificationsProperties.getNoticeOfChangeOtherParties();
            case NotificationType.CLAIMANT_LIP -> caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()
                : notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
            case NotificationType.NEW_DEFENDANT_SOLICITOR -> notificationsProperties.getNotifyNewDefendantSolicitorNOC();
            case NotificationType.CLAIMANT_SOLICITOR_UNPAID_HEARING -> notificationsProperties.getHearingFeeUnpaidNoc();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private boolean shouldSkipNotification(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.FORMER_SOLICITOR -> caseData.getChangeOfRepresentation().getOrganisationToRemoveID() == null;
            case NotificationType.OTHER_SOLICITOR_1 -> NocNotificationUtils.isOtherParty1Lip(caseData);
            case NotificationType.OTHER_SOLICITOR_2 -> !stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
                || NocNotificationUtils.isOtherParty2Lip(caseData);
            case NotificationType.CLAIMANT_LIP -> !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case NotificationType.NEW_DEFENDANT_SOLICITOR -> !stateFlowEngine.evaluate(caseData).isFlagSet(DEFENDANT_NOC_ONLINE)
                || !NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData);
            case NotificationType.CLAIMANT_SOLICITOR_UNPAID_HEARING ->
                isHearingFeePaid(caseData) || caseData.getHearingFee() == null;
            default -> throw new CallbackException("Unknown skip logic for notification type: " + type);
        };
    }
}
