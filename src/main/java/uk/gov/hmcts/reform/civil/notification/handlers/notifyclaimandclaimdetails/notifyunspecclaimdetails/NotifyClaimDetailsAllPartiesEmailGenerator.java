package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class NotifyClaimDetailsAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyClaimDetailsAllPartiesEmailGenerator(NotifyClaimDetailsAppSolEmailDTOGenerator notifyClaimDetailsAppSolEmailDTOGenerator,
                                                      NotifyClaimDetailsRespOneSolEmailDTOGenerator notifyClaimDetailsRespOneSolEmailDTOGenerator,
                                                      NotifyClaimDetailsRespSolTwoEmailDTOGenerator notifyClaimDetailsRespSolTwoEmailDTOGenerator) {
        super(List.of(notifyClaimDetailsAppSolEmailDTOGenerator,
                      notifyClaimDetailsRespOneSolEmailDTOGenerator,
                      notifyClaimDetailsRespSolTwoEmailDTOGenerator));
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        if (caseData.getRespondent1ResponseDeadline() == null) {
            log.info("Skipping claim-details notification for case ID: {} because there is no response deadline",
                     caseData.getCcdCaseReference());
            return Set.of();
        }
        return super.getPartiesToNotify(caseData, taskId);
    }

}
