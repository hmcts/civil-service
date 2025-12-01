package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class NotifiedTimeExtension {

        @Test
        void should_return_true_for_notifiedTimeExtension_when_time_extension_granted_and_not_acknowledged() {
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            assertTrue(NotificationPredicate.notifiedTimeExtension.test(caseData));
        }

        @Test
        void should_return_false_for_notifiedTimeExtension_when_no_time_extension_and_not_acknowledged() {
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            assertFalse(NotificationPredicate.notifiedTimeExtension.test(caseData));
        }

        @Test
        void should_return_false_for_notifiedTimeExtension_when_time_extension_granted_and_acknowledged() {
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            assertFalse(NotificationPredicate.notifiedTimeExtension.test(caseData));
        }
    }

    @Nested
    class HasNotifyOptionsBoth {

        @Test
        void should_return_true_for_notifiedOptionsToBoth_when_notify_options_is_both() {
            DynamicList dynamicList = DynamicList.builder()
                .value(DynamicListElement.builder().label("Both").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
            assertTrue(NotificationPredicate.hasNotifyOptionsBoth.test(caseData));
        }

        @Test
        void should_return_false_for_notifiedOptionsToBoth_when_notify_options_is_not_both() {
            DynamicList dynamicList = DynamicList.builder()
                .value(DynamicListElement.builder().label("Solicitor 1").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
            assertFalse(NotificationPredicate.hasNotifyOptionsBoth.test(caseData));
        }

        @Test
        void should_return_false_for_notifiedOptionsToBoth_when_notify_options_is_null() {
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
            assertFalse(NotificationPredicate.hasNotifyOptionsBoth.test(caseData));
        }

        @Test
        void should_return_true_for_hasClaimNotifiedToBoth_when_date_present_and_no_notify_options() {
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(null);
            assertTrue(NotificationPredicate.hasClaimNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_true_for_hasClaimNotifiedToBoth_when_date_present_and_notify_options_is_both() {
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
            DynamicList claimNotifyOptions = DynamicList.builder()
                .value(DynamicListElement.builder().label("Both").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(claimNotifyOptions);
            assertTrue(NotificationPredicate.hasClaimNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasClaimNotifiedToBoth_when_date_present_and_notify_options_not_both() {
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
            DynamicList claimNotifyOptions = DynamicList.builder()
                .value(DynamicListElement.builder().label("Solicitor 1").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(claimNotifyOptions);
            assertFalse(NotificationPredicate.hasClaimNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasClaimNotifiedToBoth_when_date_missing_even_if_notify_options_both() {
            when(caseData.getClaimNotificationDate()).thenReturn(null);
            assertFalse(NotificationPredicate.hasClaimNotifiedToBoth.test(caseData));
        }
    }

    @Nested
    class HasClaimDetailsNotifiedToBoth {

        @Test
        void should_return_true_for_hasClaimDetailsNotifiedToBoth_when_date_present_and_no_notify_options() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
            assertTrue(NotificationPredicate.hasClaimDetailsNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_true_for_hasClaimDetailsNotifiedToBoth_when_date_present_and_notify_options_is_both() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
            DynamicList detailsNotifyOptions = DynamicList.builder()
                .value(DynamicListElement.builder().label("Both").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(detailsNotifyOptions);
            assertTrue(NotificationPredicate.hasClaimDetailsNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasClaimDetailsNotifiedToBoth_when_date_present_and_notify_options_not_both() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
            DynamicList detailsNotifyOptions = DynamicList.builder()
                .value(DynamicListElement.builder().label("Solicitor 1").build())
                .build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(detailsNotifyOptions);
            assertFalse(NotificationPredicate.hasClaimDetailsNotifiedToBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasClaimDetailsNotifiedToBoth_when_date_missing_even_if_notify_options_both() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
            assertFalse(NotificationPredicate.hasClaimDetailsNotifiedToBoth.test(caseData));
        }
    }

}
