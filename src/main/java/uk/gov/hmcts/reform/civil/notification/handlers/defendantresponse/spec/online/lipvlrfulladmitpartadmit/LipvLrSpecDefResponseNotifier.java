package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.lipvlrfulladmitpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecLipvLRFullOrPartAdmit;

@Component
public class LipvLrSpecDefResponseNotifier extends Notifier {

    public LipvLrSpecDefResponseNotifier(NotificationService notificationService,
                                            CaseTaskTrackingService caseTaskTrackingService,
                                            LipvLrSpecDefRespAllPartiesEmailDTOGenerator allPartiesEmailDTOGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailDTOGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseSpecLipvLRFullOrPartAdmit.toString();
    }
}
