package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class UnspecClaimSettledAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public UnspecClaimSettledAllPartiesEmailGenerator(
        UnspecClaimSettledRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(respSolOneEmailDTOGenerator));
    }
}
