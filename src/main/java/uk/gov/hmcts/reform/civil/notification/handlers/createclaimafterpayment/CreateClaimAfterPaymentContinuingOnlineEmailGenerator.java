package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class CreateClaimAfterPaymentContinuingOnlineEmailGenerator extends AllPartiesEmailGenerator {

    public CreateClaimAfterPaymentContinuingOnlineEmailGenerator(
            CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGenerator appSolOneGenerator
    ) {
        super(List.of(appSolOneGenerator));
    }
}