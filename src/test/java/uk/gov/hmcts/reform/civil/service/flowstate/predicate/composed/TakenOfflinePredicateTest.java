package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
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
    void should_return_true_for_bySystem_when_taken_offline_date_exists_and_no_change_of_representation() {
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getChangeOfRepresentation()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.bySystem.test(caseData));
    }

    @Test
    void should_return_false_for_bySystem_when_taken_offline_date_does_not_exist() {
        when(caseData.getTakenOfflineDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.bySystem.test(caseData));
    }

    @Test
    void should_return_false_for_bySystem_when_change_of_representation_exists() {
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getChangeOfRepresentation()).thenReturn(ChangeOfRepresentation.builder().build());
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
    void should_return_true_for_afterSdo_when_draw_directions_order_required_and_not_suitable_sdo_negated_and_date_exists_and_by_staff_date_negated() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
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
        when(caseData.getReasonNotSuitableSDO()).thenReturn(ReasonNotSuitableSDO.builder().input("reason").build());
        assertFalse(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdo_when_taken_offline_date_does_not_exist() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        when(caseData.getTakenOfflineDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_false_for_afterSdo_when_taken_offline_by_staff_date_exists() {
        when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterSdo.test(caseData));
    }

    @Test
    void should_return_true_for_sdoNotDrawn_when_suitable_sdo_reason_exists_and_taken_offline_date_exists_and_by_staff_date_negated() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(ReasonNotSuitableSDO.builder().input("reason").build());
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_no_suitable_sdo_reason() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_taken_offline_date_does_not_exist() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(ReasonNotSuitableSDO.builder().input("reason").build());
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_false_for_sdoNotDrawn_when_taken_offline_by_staff_date_exists() {
        when(caseData.getReasonNotSuitableSDO()).thenReturn(ReasonNotSuitableSDO.builder().input("reason").build());
        when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimNotified_when_by_staff_date_exists_and_no_notification_date_and_no_acknowledgement_and_no_response_and_future_notification_deadline() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
        assertTrue(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_by_staff_date_does_not_exist() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_notification_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_acknowledgement_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_response_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimNotified_when_no_future_notification_deadline() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
        assertFalse(TakenOfflinePredicate.afterClaimNotified.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimDetailsNotified_when_by_staff_date_exists_and_no_acknowledgement_and_no_response_and_no_time_extension_and_not_multi_party() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_by_staff_date_does_not_exist() {
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_acknowledgement_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_response_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_time_extension_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_false_for_afterClaimDetailsNotified_when_claim_dismissed_date_exists() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());
        assertFalse(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
    }

    @Test
    void should_return_true_for_afterClaimDetailsNotified_when_one_v_two_two_legal_rep_and_no_r2_acknowledgement_response_or_time_extension() {
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getClaimDismissedDate()).thenReturn(null);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
        assertTrue(TakenOfflinePredicate.afterClaimDetailsNotified.test(caseData));
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
}
