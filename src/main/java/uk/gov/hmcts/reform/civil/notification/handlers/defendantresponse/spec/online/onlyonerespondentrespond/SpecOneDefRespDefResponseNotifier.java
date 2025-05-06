package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.lipvlrfulladmitpartadmit.LipvLrSpecDefRespAllPartiesEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseSpecOneRespRespondedNotifyParties;

@Component
public class SpecOneDefRespDefResponseNotifier extends Notifier {

    public SpecOneDefRespDefResponseNotifier(NotificationService notificationService,
                                             CaseTaskTrackingService caseTaskTrackingService,
                                             LipvLrSpecDefRespAllPartiesEmailDTOGenerator allPartiesEmailDTOGenerator) {
        super(notificationService, caseTaskTrackingService, allPartiesEmailDTOGenerator);
    }

    @Override
    public String getTaskId() {
        return DefendantResponseSpecOneRespRespondedNotifyParties.toString();
    }
}
