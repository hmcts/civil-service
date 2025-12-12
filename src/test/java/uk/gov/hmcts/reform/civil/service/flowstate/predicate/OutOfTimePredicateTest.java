package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

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

@ExtendWith(MockitoExtension.class)
class OutOfTimePredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class NotBeingTakenOffline {

        @Test
        void should_return_true_for_notBeingTakenOffline_when_deadline_passed_and_no_response_and_not_taken_offline() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertTrue(OutOfTimePredicate.notBeingTakenOffline.test(caseData));
        }

        @Test
        void should_return_false_for_notBeingTakenOffline_when_deadline_not_passed() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(OutOfTimePredicate.notBeingTakenOffline.test(caseData));
        }

        @Test
        void should_return_false_for_notBeingTakenOffline_when_response_exists() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getApplicant1ResponseDate()).thenReturn(LocalDateTime.now());
            assertFalse(OutOfTimePredicate.notBeingTakenOffline.test(caseData));
        }

        @Test
        void should_return_false_for_notBeingTakenOffline_when_taken_offline() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
            assertFalse(OutOfTimePredicate.notBeingTakenOffline.test(caseData));
        }
    }

    @Nested
    class ProcessedByCamunda {

        @Test
        void should_return_true_for_processedByCamunda_when_taken_offline_date_exists() {
            when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
            assertTrue(OutOfTimePredicate.processedByCamunda.test(caseData));
        }

        @Test
        void should_return_false_for_processedByCamunda_when_taken_offline_date_does_not_exist() {
            when(caseData.getTakenOfflineDate()).thenReturn(null);
            assertFalse(OutOfTimePredicate.processedByCamunda.test(caseData));
        }
    }
}
