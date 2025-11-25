package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@ExtendWith(MockitoExtension.class)
class ClaimPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class IssuedPredicate {

        @Test
        void should_return_true_for_issued_when_claim_has_notification_deadline() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertTrue(ClaimPredicate.issued.test(caseData));
        }

        @Test
        void should_return_false_for_issued_when_claim_does_not_have_notification_deadline() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(null);
            assertFalse(ClaimPredicate.issued.test(caseData));
        }
    }

    @Nested
    class IsSpecPredicate {

        @Test
        void should_return_true_for_isSpec_when_claim_is_spec_claim() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            assertTrue(ClaimPredicate.isSpec.test(caseData));
        }

        @Test
        void should_return_false_for_isSpec_when_claim_is_not_spec_claim() {
            when(caseData.getCaseAccessCategory()).thenReturn(null);
            assertFalse(ClaimPredicate.isSpec.test(caseData));
        }
    }
}
