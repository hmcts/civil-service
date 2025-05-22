package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantResponseCuiRejectPayAllPartiesEmailGenerator extends AllPartiesEmailGenerator  {

    public ClaimantResponseCuiRejectPayAllPartiesEmailGenerator(ClaimantResponseCuiRejectPayClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                                ClaimantResponseCuiRejectPayRespLipEmailDTOGenerator defendantEmailDTOGenerator,
                                                                ClaimantResponseCuiRejectPayRespSol1EmailDTOGenerator appSolOneEmailDTOGenerator) {
        super(List.of(
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator,
            appSolOneEmailDTOGenerator
        ));
    }
}
