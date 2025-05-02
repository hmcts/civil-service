package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.fulldefencefulladmitpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties;

@Component
public class LrvLrLrSpecDefResponseNotifier extends Notifier {

    public LrvLrLrSpecDefResponseNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            LrvLrLrSpecDefRespAllLegalRepsEmailDTOGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties.toString();
    }
}
