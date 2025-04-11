package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipRepresentedWithNoCNotifier;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
public class NoCLipClaimantRepresentedNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "notify-lip-after-noc-approval-%s";

    private static class NotificationType {
        static final String NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL = "NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL";
        static final String NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED = "NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED";
        static final String NOTIFY_APPLICANT_LIP_SOLICITOR = "NOTIFY_APPLICANT_LIP_SOLICITOR";
    }

    public NoCLipClaimantRepresentedNotifier(NotificationService notificationService,
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
        return ClaimantLipRepresentedWithNoCNotifier.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Stream.of(
                //In case of LR v LR/LR
                buildPartyEmail(caseData, NotificationType.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL, this::addProperties),
                buildPartyEmail(caseData, NotificationType.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED, this::addProperties),
                buildPartyEmail(caseData, NotificationType.NOTIFY_APPLICANT_LIP_SOLICITOR, this::addPropertiesApplicantSolicitor))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private EmailDTO buildPartyEmail(CaseData caseData, String type, PropertyBuilder propertyBuilder) {
        Map<String, String> props = propertyBuilder.build(caseData);

        return EmailDTO.builder()
            .targetEmail(getRecipientEmail(caseData, type))
            .emailTemplate(getTemplateID(caseData, type))
            .parameters(props)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getTemplateID(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL -> caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate()
                : notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate();
            case NotificationType.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED -> notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate();
            case NotificationType.NOTIFY_APPLICANT_LIP_SOLICITOR -> notificationsProperties.getNoticeOfChangeApplicantLipSolicitorTemplate();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private String getRecipientEmail(CaseData caseData, String type) {
        return switch (type) {
            case NotificationType.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL -> caseData.getApplicant1Email();
            case NotificationType.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED -> getRecipientEmailForRespondent(caseData);
            case NotificationType.NOTIFY_APPLICANT_LIP_SOLICITOR -> caseData.getApplicantSolicitor1UserDetailsEmail();
            default -> throw new CallbackException("Unknown notification type: " + type);
        };
    }

    private String getRecipientEmailForRespondent(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyEmail)
            .orElse("");
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> addPropertiesApplicantSolicitor(CaseData caseData) {
        return Map.of(
            CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            LEGAL_ORG_APPLICANT1, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID()),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public String getLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.map(Organisation::getName).orElse(null);
    }
}
