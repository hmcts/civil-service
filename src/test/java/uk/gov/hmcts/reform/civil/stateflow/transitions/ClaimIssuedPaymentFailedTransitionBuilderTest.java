package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class ClaimIssuedPaymentFailedTransitionBuilderTest {

    @Mock
    FeatureToggleService featureToggleService;
    ClaimIssuedPaymentFailedTransitionBuilder claimIssuedPaymentFailedTransitionBuilder;

    @BeforeEach
    public void setUpTest() {
        claimIssuedPaymentFailedTransitionBuilder = new ClaimIssuedPaymentFailedTransitionBuilder(featureToggleService);
    }

    @Test
    void testBuildTransitions() {
        List<Transition> result = claimIssuedPaymentFailedTransitionBuilder.buildTransitions();
        assertNotNull(result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSourceState()).isEqualTo("MAIN.CLAIM_ISSUED_PAYMENT_FAILED");
        assertThat(result.get(0).getTargetState()).isEqualTo("MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL");
    }


}
