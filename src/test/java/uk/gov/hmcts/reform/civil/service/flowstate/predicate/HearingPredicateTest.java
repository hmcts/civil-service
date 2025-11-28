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
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;

@ExtendWith(MockitoExtension.class)
class HearingPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class IsInReadiness {

        @Test
        void should_return_false_for_isInReadiness_when_no_hearing_reference() {
            when(caseData.getHearingReferenceNumber()).thenReturn(null);
            assertFalse(HearingPredicate.isInReadiness.test(caseData));
        }

        @Test
        void should_return_false_for_isInReadiness_when_hearing_not_listed() {
            when(caseData.getHearingReferenceNumber()).thenReturn("000HN001");
            assertFalse(HearingPredicate.isInReadiness.test(caseData));
        }

        @Test
        void should_return_true_for_isInReadiness_when_hearing_listed() {
            when(caseData.getHearingReferenceNumber()).thenReturn("000HN001");
            when(caseData.getListingOrRelisting()).thenReturn(LISTING);
            assertTrue(HearingPredicate.isInReadiness.test(caseData));
        }

        @Test
        void should_return_false_for_isInReadiness_when_case_dismissed() {
            when(caseData.getHearingReferenceNumber()).thenReturn("000HN001");
            when(caseData.getListingOrRelisting()).thenReturn(LISTING);
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(LocalDateTime.now().plusDays(5));
            assertFalse(HearingPredicate.isInReadiness.test(caseData));
        }

        @Test
        void should_return_false_for_isInReadiness_when_taken_offline() {
            when(caseData.getHearingReferenceNumber()).thenReturn("000HN001");
            when(caseData.getListingOrRelisting()).thenReturn(LISTING);
            when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
            assertFalse(HearingPredicate.isInReadiness.test(caseData));
        }

        @Test
        void should_return_false_for_isInReadiness_when_taken_offline_by_staff() {
            when(caseData.getHearingReferenceNumber()).thenReturn("000HN001");
            when(caseData.getListingOrRelisting()).thenReturn(LISTING);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
            assertFalse(HearingPredicate.isInReadiness.test(caseData));
        }
    }
}
