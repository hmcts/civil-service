package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.function.Consumer;

@Slf4j
@Component
@AllArgsConstructor
public class HearingFeePublisherProvider {

    private final MultiOrIntermediateTrackProvider multiOrIntermediateTrackProvider;
    private final PreMultiIntermediateClaimProvider preMultiIntermediateClaimProvider;
    private final FeatureToggleService featureToggleService;

    public Consumer<Long> provide(CaseData caseData) {
        boolean multiOrIntermediateTrackEnabled = featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);

        Consumer<Long> publisher;

        if (multiOrIntermediateTrackEnabled) {
            publisher = multiOrIntermediateTrackProvider.getPublisher(caseData);
        } else {
            publisher = preMultiIntermediateClaimProvider.getPublisher(caseData);
        }

        return publisher;
    }
}
