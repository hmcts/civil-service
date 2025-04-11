package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantLipHelpWithFeesNotifier;

@Component
public class NotifyClaimantLipHelpWithFeesNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "notify-claimant-lip-help-with-fees-notification-%s";

    public NotifyClaimantLipHelpWithFeesNotifier(NotificationService notificationService,
                                                 NotificationsProperties notificationsProperties,
                                                 OrganisationService organisationService,
                                                 SimpleStateFlowEngine stateFlowEngine,
                                                 CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    public String getTaskId() {
        return ClaimantLipHelpWithFeesNotifier.toString();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        ));
    }

    @Override
    @NotNull
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        EmailDTO emailDTO = EmailDTO.builder()
                .targetEmail(getRecipientEmail(caseData))
                .emailTemplate(getNotificationTemplate(caseData))
                .parameters(addProperties(caseData))
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
        return Set.of(emailDTO);
    }

    private String getRecipientEmail(CaseData caseData) {
        return caseData.getClaimantUserDetails().getEmail();
    }

    private String getNotificationTemplate(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh()
                : notificationsProperties.getNotifyClaimantLipHelpWithFees();
    }
}
