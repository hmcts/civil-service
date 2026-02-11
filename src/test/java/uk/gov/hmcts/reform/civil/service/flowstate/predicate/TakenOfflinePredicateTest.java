package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TakenOfflinePredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_bySystem_when_taken_offline_date_exists() {
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.bySystem.test(caseData));
    }

    @Test
    void should_return_false_for_bySystem_when_taken_offline_date_does_not_exist() {
        when(caseData.getTakenOfflineDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.bySystem.test(caseData));
    }

    @Test
    void should_return_true_for_byStaff_when_taken_offline_by_staff_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.byStaff.test(caseData));
    }

    @Test
    void should_return_false_for_byStaff_when_taken_offline_by_staff_date_does_not_exist() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.byStaff.test(caseData));
    }

    @Test
    void should_return_true_for_sdoNotSuitable_when_suitable_sdo_reason_exists() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        assertTrue(TakenOfflinePredicate.sdoNotSuitable.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotSuitable_when_no_suitable_sdo_reason() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.sdoNotSuitable.test(caseData));
    }

    @Test
    void should_return_true_for_sdoNotDrawn_when_suitable_sdo_reason_exists_and_taken_offline_date_exists_and_by_staff_date_negated() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_no_suitable_sdo_reason() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_taken_offline_date_does_not_exist() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_taken_offline_by_staff_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.byStaff.negate()
                        .and(TakenOfflinePredicate.sdoNotDrawn).test(caseData));
    }

    @Test
    void should_return_true_for_beforeSdo_when_applicant1_has_response_and_no_draw_order_and_no_sdo_reason() {
        when(caseData.getApplicant1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(null);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.beforeSdo.test(caseData));
    }

    @Test
    void should_return_false_for_beforeSdo_when_no_applicant1_response_date() {
        when(caseData.getApplicant1ResponseDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.beforeSdo.test(caseData));
    }

    @Test
    void should_return_false_for_beforeSdo_when_draw_directions_order_required_present() {
        when(caseData.getApplicant1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        assertFalse(TakenOfflinePredicate.beforeSdo.test(caseData));
    }

    @Test
    void should_return_false_for_beforeSdo_when_sdo_not_suitable_reason_present() {
        when(caseData.getApplicant1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        assertFalse(TakenOfflinePredicate.beforeSdo.test(caseData));
    }

    @Test
    void should_return_true_for_afterSdo_when_draw_directions_order_required_and_not_suitable_sdo_negated() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdo_when_draw_directions_order_required() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdo_when_suitable_sdo_reason_exists() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        assertFalse(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_true_for_afterSdoNotSuitable_when_draw_order_not_required_and_reason_present() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(null);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(createReason("reason"));
        assertTrue(TakenOfflinePredicate.afterSdoNotSuitable.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdoNotSuitable_when_draw_order_required() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        assertFalse(TakenOfflinePredicate.afterSdoNotSuitable.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdoNotSuitable_when_reason_missing() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(null);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterSdoNotSuitable.test(caseData));
    }

    @Test
    void should_return_true_for_beforeClaimIssue_when_no_deadlines_or_dates_and_submitted() {
        when(caseData.getClaimNotificationDeadline()).thenReturn(null);
        when(caseData.getClaimNotificationDate()).thenReturn(null);
        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.beforeClaimIssue.test(caseData));
    }

    @Test
    void should_return_false_for_beforeClaimIssue_when_notification_deadline_present() {
        when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
        assertFalse(TakenOfflinePredicate.beforeClaimIssue.test(caseData));
    }

    @Test
    void should_return_false_for_beforeClaimIssue_when_notification_date_present() {
        when(caseData.getClaimNotificationDeadline()).thenReturn(null);
        when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.beforeClaimIssue.test(caseData));
    }

    @Test
    void should_return_false_for_beforeClaimIssue_when_submitted_date_missing() {
        when(caseData.getClaimNotificationDeadline()).thenReturn(null);
        when(caseData.getClaimNotificationDate()).thenReturn(null);
        when(caseData.getSubmittedDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.beforeClaimIssue.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotified_when_claim_notified_and_options_not_both() {
        when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList options = DynamicList.builder()
            .value(DynamicListElement.builder().label("Solicitor 1").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(options);
        assertTrue(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_notify_options_missing() {
        when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_notify_options_both() {
        when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList options = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimOptions()).thenReturn(options);
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_notification_date_missing() {
        when(caseData.getClaimNotificationDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedExtension_when_time_extension_present_and_no_ack_or_response() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedExtension_when_ack_present() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedExtension_when_response_present() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedExtension_when_no_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckExtension_when_not_multi_party_and_r1_ack_and_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckExtension_when_one_v_two_one_legal_rep_and_both_ack_and_extensions() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedAckExtension_when_two_legal_rep_and_missing_r2_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedAckExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckNoResponseExtension_when_not_multi_party_and_r1_ack_no_response_with_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckNoResponseExtension_when_two_legal_rep_both_ack_no_response_with_extensions() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedAckNoResponseExtension_when_r1_has_response() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedAckNoResponseExtension_when_two_legal_rep_missing_r2_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckNoResponseNoExtension_when_not_multi_party_and_r1_ack_no_response_no_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotifiedAckNoResponseNoExtension_when_one_legal_rep_both_ack_no_response_no_extensions() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotifiedAckNoResponseNoExtension_when_r1_has_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterNotifiedOptions_when_notified_and_options_not_both() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Solicitor 1").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertTrue(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_not_notified() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);

        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_no_notify_options() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterNotifiedOptions_when_notify_options_is_both() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both").build())
            .build();
        when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dynamicList);
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotified_when_by_staff_date_exists_and_no_notification_date_and_no_acknowledgement_and_no_response_and_future_notification_deadline() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_by_staff_date_does_not_exist() {
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_notification_date_exists() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_acknowledgement_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_response_date_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_no_future_notification_deadline() {
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimDetailsNotified_when_by_staff_date_exists_and_no_acknowledgement_and_no_response_and_no_time_extension_and_not_multi_party() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_by_staff_date_does_not_exist() {
        assertFalse(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_acknowledgement_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_response_date_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_time_extension_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_claim_dismissed_date_exists() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimDetailsNotified_when_one_v_two_two_legal_rep_and_no_r2_acknowledgement_response_or_time_extension() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(null);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension.test(caseData));
    }

    @Test
    void should_return_true_for_isDefendantNoCOnlineForCaseAfterJBA_when_party_unrepresented_and_by_admission_and_taken_offline_date_exists_and_change_of_representation_exists() {
        JudgmentDetails mockedJudgmentDetails = mock(JudgmentDetails.class);
        when(mockedJudgmentDetails.getType()).thenReturn(JudgmentType.JUDGMENT_BY_ADMISSION);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getActiveJudgment()).thenReturn(mockedJudgmentDetails);
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getChangeOfRepresentation()).thenReturn(ChangeOfRepresentation.builder().build());
        assertTrue(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    @Test
    void should_return_false_for_isDefendantNoCOnlineForCaseAfterJBA_when_party_represented() {
        assertFalse(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    @Test
    void should_return_false_for_isDefendantNoCOnlineForCaseAfterJBA_when_not_by_admission() {
        JudgmentDetails mockedJudgmentDetails = mock(JudgmentDetails.class);
        when(mockedJudgmentDetails.getType()).thenReturn(JudgmentType.DEFAULT_JUDGMENT);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getActiveJudgment()).thenReturn(mockedJudgmentDetails);
        assertFalse(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    @Test
    void should_return_false_for_isDefendantNoCOnlineForCaseAfterJBA_when_taken_offline_date_does_not_exist() {
        JudgmentDetails mockedJudgmentDetails = mock(JudgmentDetails.class);
        when(mockedJudgmentDetails.getType()).thenReturn(JudgmentType.JUDGMENT_BY_ADMISSION);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getActiveJudgment()).thenReturn(mockedJudgmentDetails);
        when(caseData.getTakenOfflineDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    @Test
    void should_return_false_for_isDefendantNoCOnlineForCaseAfterJBA_when_no_change_of_representation() {
        JudgmentDetails mockedJudgmentDetails = mock(JudgmentDetails.class);
        when(mockedJudgmentDetails.getType()).thenReturn(JudgmentType.JUDGMENT_BY_ADMISSION);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getActiveJudgment()).thenReturn(mockedJudgmentDetails);
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getChangeOfRepresentation()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    private ReasonNotSuitableSDO createReason(String reasonText) {
        ReasonNotSuitableSDO reason = new ReasonNotSuitableSDO();
        reason.setInput(reasonText);
        return reason;
    }
}
