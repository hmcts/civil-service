package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ContinuingOnlineSpecClaimNotifier;

@Component
public class ClaimContinuingOnlineSpecRespondentPartyNotifier extends Notifier {

    private final CoreCaseDataService caseDataService;
    private final FeatureToggleService featureToggleService;
    private final PiPLetterGenerator pipLetterGenerator;
    private final BulkPrintService bulkPrintService;
    private final Time time;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    public ClaimContinuingOnlineSpecRespondentPartyNotifier(
            NotificationService notificationService,
            CaseTaskTrackingService caseTaskTrackingService,
            ClaimContinuingOnlineSpecRespondentPartyEmailGenerator partiesGenerator,
            CoreCaseDataService caseDataService,
            FeatureToggleService featureToggleService,
            PiPLetterGenerator pipLetterGenerator,
            BulkPrintService bulkPrintService,
            Time time,
            UserService userService,
            SystemUpdateUserConfiguration userConfig
    ) {
        super(notificationService, caseTaskTrackingService, partiesGenerator);
        this.caseDataService = caseDataService;
        this.featureToggleService = featureToggleService;
        this.pipLetterGenerator = pipLetterGenerator;
        this.bulkPrintService = bulkPrintService;
        this.time = time;
        this.userService = userService;
        this.userConfig = userConfig;
    }

    @Override
    protected String getTaskId() {
        return ContinuingOnlineSpecClaimNotifier.toString();
    }

    @Override
    public void notifyParties(CaseData caseData, String eventId, String taskId) {
        boolean bilingualLipvLip = caseData.isLipvLipOneVOne()
                && featureToggleService.isLipVLipEnabled()
                && caseData.isClaimantBilingual();

        Map<String, Object> updates = new HashMap<>();
        updates.put("claimNotificationDate", time.now());
        if (!bilingualLipvLip) {
            updates.put("state", AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
        }
        caseDataService.triggerEvent(
                caseData.getCcdCaseReference(),
                CaseEvent.NOTIFY_EVENT,
                updates
        );

        super.notifyParties(caseData, eventId, taskId);

        String bearerToken = userService.getAccessToken(
                userConfig.getUserName(),
                userConfig.getPassword()
        );

        byte[] letter = pipLetterGenerator.downloadLetter(caseData, bearerToken);
        List<String> recipients = List.of(caseData.getRespondent1().getPartyName());
        bulkPrintService.printLetter(
                letter,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                "first-contact-pack",
                recipients
        );
    }
}
