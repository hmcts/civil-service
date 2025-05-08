package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class CreateClaimAfterPaymentContinuingOnlineEmailGenerator extends AllPartiesEmailGenerator {

    public CreateClaimAfterPaymentContinuingOnlineEmailGenerator(
            CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGenerator appSolOneGenerator
    ) {
        super(appSolOneGenerator, null, null, null, null);
    }
}