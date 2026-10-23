package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.fulldefencefulladmitpartadmit;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties;

@Component
public class LrvLrLrSpecDefResponseNotifier extends Notifier<CaseData> {

    public LrvLrLrSpecDefResponseNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            LrvLrLrSpecDefRespAllPartiesEmailDTOGenerator allLegalRepsEmailGenerator) {
        super(notificationService, caseTaskTrackingService, allLegalRepsEmailGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties.toString();
    }
}
