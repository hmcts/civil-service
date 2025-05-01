package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CreateClaimAfterPaymentOfflineEmailGenerator implements PartiesEmailGenerator {

    private final CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator appSolOneGenerator;
    private final FeatureToggleService featureToggleService;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            return Collections.emptySet();
        }
        return Set.of(appSolOneGenerator.buildEmailDTO(caseData));
    }
}
