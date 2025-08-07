package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.raisingclaimagainstlitigantinperson;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RaisingClaimAgainstLitigantInPersonForSpecAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RaisingClaimAgainstLitigantInPersonForSpecAllPartiesEmailGenerator(
            RaisingClaimAgainstLitigantInPersonForSpecAppSolOneEmailDTOGenerator appSolOne
    ) {
        super(List.of(appSolOne));
    }
}
