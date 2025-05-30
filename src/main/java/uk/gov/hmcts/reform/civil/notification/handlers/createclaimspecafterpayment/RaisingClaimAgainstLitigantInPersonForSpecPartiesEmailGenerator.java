package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RaisingClaimAgainstLitigantInPersonForSpecPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RaisingClaimAgainstLitigantInPersonForSpecPartiesEmailGenerator(
            RaisingClaimAgainstLitigantInPersonForSpecAppSolOneEmailDTOGenerator appSolOne
    ) {
        super(List.of(appSolOne));
    }
}
