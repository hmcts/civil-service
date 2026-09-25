package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification.SettleClaimPaidInFullNotificationAllPartiesEmailGenerator;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;

@Component
public class UnspecClaimSettledAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    private final SettleClaimPaidInFullNotificationAllPartiesEmailGenerator settleClaimPaidInFullEmailGenerator;

    public UnspecClaimSettledAllPartiesEmailGenerator(
        UnspecClaimSettledRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SettleClaimPaidInFullNotificationAllPartiesEmailGenerator settleClaimPaidInFullEmailGenerator) {
        super(List.of(respSolOneEmailDTOGenerator));
        this.settleClaimPaidInFullEmailGenerator = settleClaimPaidInFullEmailGenerator;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        if (isOneVOne(caseData)) {
            return super.getPartiesToNotify(caseData, taskId);
        }
        return settleClaimPaidInFullEmailGenerator.getPartiesToNotify(caseData, taskId);
    }
}
