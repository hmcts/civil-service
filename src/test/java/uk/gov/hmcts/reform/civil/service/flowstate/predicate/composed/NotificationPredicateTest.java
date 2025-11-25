package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

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

    @Test
    void should_return_true_for_notifiedOptionsToBoth_when_notify_options_is_both() {
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertTrue(NotificationPredicate.notifiedOptionsToBoth.test(caseData));
    }

    @Test
    void should_return_false_for_notifiedOptionsToBoth_when_notify_options_is_not_both() {
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Solicitor 1").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertFalse(NotificationPredicate.notifiedOptionsToBoth.test(caseData));
    }

    @Test
    void should_return_false_for_notifiedOptionsToBoth_when_notify_options_is_null() {
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
        assertFalse(NotificationPredicate.notifiedOptionsToBoth.test(caseData));
    }

    @Test
    void should_return_true_for_afterNotifiedOptions_when_notified_and_options_not_both() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Solicitor 1").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertTrue(NotificationPredicate.afterNotifiedOptions.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_not_notified() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);

        assertFalse(NotificationPredicate.afterNotifiedOptions.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_no_notify_options() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
        assertFalse(NotificationPredicate.afterNotifiedOptions.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_notify_options_is_both() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertFalse(NotificationPredicate.afterNotifiedOptions.test(caseData));
    }
}
