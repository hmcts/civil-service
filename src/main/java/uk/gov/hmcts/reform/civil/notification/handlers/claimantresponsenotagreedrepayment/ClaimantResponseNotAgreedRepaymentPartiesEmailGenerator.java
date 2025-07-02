package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import java.util.List;

@Component
public class ClaimantResponseNotAgreedRepaymentPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantResponseNotAgreedRepaymentPartiesEmailGenerator(ClaimantResponseNotAgreedRepaymentAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                                                   ClaimantResponseNotAgreedRepaymentClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                                   ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                                   ClaimantResponseNotAgreedRepaymentRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator, claimantEmailDTOGenerator, defendantEmailDTOGenerator, respSolOneEmailDTOGenerator));
    }

}
