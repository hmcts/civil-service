package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class CreateClaimAfterPaymentOfflineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CreateClaimAfterPaymentOfflineAllPartiesEmailGenerator(
            CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator appSolOneGenerator,
            CreateClaimAfterPaymentOfflineClaimantEmailDTOGenerator claimantEmailGenerator) {

        super(List.of(appSolOneGenerator, claimantEmailGenerator));
    }
}
