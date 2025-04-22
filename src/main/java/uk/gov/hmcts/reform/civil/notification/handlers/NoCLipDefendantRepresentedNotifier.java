package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantLipRepresentedWithNoCNotifier;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

public class NoCLipDefendantRepresentedNotifier extends Notifier {

    private static class NotificationType {
        static final String NOTIFY_DEFENDANT_LIP_AFTER_NOC_APPROVAL = "NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL";
        static final String NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED = "NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED";
        static final String NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED = "NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED";
        static final String NOTIFY_DEFENDANT_LIP_SOLICITOR = "NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL";
    }
    private static final String REFERENCE_TEMPLATE_DEFENDANT_LIP =
        "notify-lip-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_DEFENDANT_LR =
        "notify-lr-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_CLAIMANT_LIP =
        "notify-claimant-lip-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_CLAIMANT_LR =
        "notify-claimant-lr-after-defendant-noc-approval-%s";

    public NoCLipDefendantRepresentedNotifier(NotificationService notificationService,
                                                NotificationsProperties notificationsProperties,
                                                OrganisationService organisationService,
                                                SimpleStateFlowEngine stateFlowEngine,
                                                CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    protected String getTaskId() {
        return DefendantLipRepresentedWithNoCNotifier.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Stream.of(
                    //NoC - LR v Lip to LR v LR OR Lip v Lip to Lip v LR
                    caseData.getApplicant1Represented() == YesOrNo.NO
                        ? buildPartyEmail(caseData, NotificationType.NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED, this::addPropertiesClaimant)
                        : buildPartyEmail(caseData, NotificationType.NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED, this::addPropertiesClaimantLr),
                    buildPartyEmail(caseData, NotificationType.NOTIFY_DEFENDANT_LIP_AFTER_NOC_APPROVAL, this::addProperties),
                    buildPartyEmail(caseData, NotificationType.NOTIFY_DEFENDANT_LIP_SOLICITOR, this::addPropertiesDefendantLr)
                ).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private EmailDTO buildPartyEmail(CaseData caseData, String type, PropertyBuilder propertyBuilder) {
        Map<String, String> props = propertyBuilder.build(caseData);

        return EmailDTO.builder()
            .targetEmail(getRecipientEmail(caseData, type))
            .emailTemplate(getTemplateID(caseData, type))
            .parameters(props)
            .reference(String.format(getReferenceTemplate(type), caseData.getLegacyCaseReference()))
            .build();
    }

    private String getReferenceTemplate(String type) {
        return switch (type) {
            case NotificationType.NOTIFY_DEFENDANT_LIP_AFTER_NOC_APPROVAL -> REFERENCE_TEMPLATE_DEFENDANT_LIP;
            case NotificationType.NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED -> REFERENCE_TEMPLATE_CLAIMANT_LIP;
            case NotificationType.NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED -> REFERENCE_TEMPLATE_CLAIMANT_LR;
            case NotificationType.NOTIFY_DEFENDANT_LIP_SOLICITOR -> REFERENCE_TEMPLATE_DEFENDANT_LR;
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private String getTemplateID(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.NOTIFY_DEFENDANT_LIP_AFTER_NOC_APPROVAL -> caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC()
                : notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate();
            case NotificationType.NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED ->
                caseData.isClaimantBilingual()
                    ? notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()
                    : notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
            case NotificationType.NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED -> notificationsProperties.getNoticeOfChangeOtherParties();
            case NotificationType.NOTIFY_DEFENDANT_LIP_SOLICITOR -> notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private String getRecipientEmail(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.NOTIFY_CLAIMANT_LIP_DEFENDANT_REPRESENTED -> caseData.getApplicant1Email();
            case NotificationType.NOTIFY_CLAIMANT_LR_DEFENDANT_REPRESENTED ->
                caseData.getApplicantSolicitor1UserDetailsEmail();
            case NotificationType.NOTIFY_DEFENDANT_LIP_AFTER_NOC_APPROVAL -> caseData.getRespondent1().getPartyEmail();
            case NotificationType.NOTIFY_DEFENDANT_LIP_SOLICITOR -> caseData.getRespondentSolicitor1EmailAddress();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesDefendantLr(CaseData caseData) {
        return Map.of(
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_REP_NAME, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
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

    public Map<String, String> addPropertiesClaimantLr(CaseData caseData) {
        return Map.of(
            CASE_NAME, NocNotificationUtils.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOrganisationName(NocNotificationUtils.getOtherSolicitor1Name(caseData)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private String getOrganisationName(String orgToName) {
        if (orgToName != null) {
            return organisationService.findOrganisationById(orgToName)
                .orElseThrow(() -> new CallbackException("Organisation is not valid for: " + orgToName))
                .getName();
        }
        return LITIGANT_IN_PERSON;
    }
}
