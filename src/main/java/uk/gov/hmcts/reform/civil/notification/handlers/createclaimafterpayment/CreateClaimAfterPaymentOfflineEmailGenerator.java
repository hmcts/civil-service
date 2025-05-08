package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class CreateClaimAfterPaymentOfflineEmailGenerator extends AllPartiesEmailGenerator {

    private final FeatureToggleService featureToggleService;

    public CreateClaimAfterPaymentOfflineEmailGenerator(
            CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator appSolOneGenerator,
            FeatureToggleService featureToggleService
    ) {
        super(appSolOneGenerator, null, null, null, null);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            return Collections.emptySet();
        }
        return super.getPartiesToNotify(caseData);
    }
}
