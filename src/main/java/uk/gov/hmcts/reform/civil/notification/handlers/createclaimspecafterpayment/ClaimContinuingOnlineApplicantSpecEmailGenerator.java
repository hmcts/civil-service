package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineApplicantSpecEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineApplicantSpecEmailGenerator(
            ClaimContinuingOnlineApplicantSpecAppSolOneEmailDTOGenerator appGen,
            ClaimContinuingOnlineApplicantSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineApplicantSpecRespSolTwoEmailDTOGenerator respTwoGen
    ) {
        super(List.of(
                appGen,
                respOneGen,
                respTwoGen
        ));
    }
}
