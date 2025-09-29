package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantResponseAgreedRepaymentPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantResponseAgreedRepaymentPartiesEmailGenerator(ClaimantResponseAgreedRepaymentRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                                ClaimantResponseAgreedRepaymentDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(respSolOneEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
