package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ResponsesPredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_notificationAcknowledged_when_respondent1_acknowledged() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_notificationAcknowledged_when_one_v_two_two_legal_rep_and_respondent2_acknowledged() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_notificationAcknowledged_when_one_v_two_one_legal_rep_and_respondent2_acknowledged() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_false_for_notificationAcknowledged_when_neither_respondent_acknowledged() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        assertFalse(ResponsesPredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_respondentTimeExtension_when_respondent1_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_true_for_respondentTimeExtension_when_one_v_two_two_legal_rep_and_respondent2_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_false_for_respondentTimeExtension_when_no_respondent_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        assertFalse(ResponsesPredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_true_for_allResponsesReceived_when_respondent1_responded_and_not_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_true_for_allResponsesReceived_when_both_respondents_responded_in_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsesPredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_false_for_allResponsesReceived_when_respondent1_not_responded() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertFalse(ResponsesPredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_false_for_allResponsesReceived_when_only_one_respondent_responded_in_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        assertFalse(ResponsesPredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceived_when_one_v_two_two_legal_rep_and_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsesPredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceived_when_one_v_two_two_legal_rep_and_r2_full_defence_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsesPredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsesPredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        assertFalse(ResponsesPredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_neither_is_full_defence() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsesPredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceived_when_one_v_two_two_legal_rep_and_r1_full_admit_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsesPredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceived_when_one_v_two_two_legal_rep_and_r2_full_admit_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        assertTrue(ResponsesPredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsesPredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertFalse(ResponsesPredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_neither_is_full_admit() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsesPredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_one_v_two_two_legal_rep_and_r1_non_full_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_one_v_two_two_legal_rep_and_r2_non_full_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertTrue(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertFalse(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_neither_has_response() {
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsesPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_isOneVOneResponseFlagSpec_when_flag_exists() {
        when(caseData.getShowResponseOneVOneFlag()).thenReturn(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE);
        assertTrue(ResponsesPredicate.isOneVOneResponseFlagSpec.test(caseData));
    }

    @Test
    void should_return_false_for_isOneVOneResponseFlagSpec_when_flag_does_not_exist() {
        when(caseData.getShowResponseOneVOneFlag()).thenReturn(null);
        assertFalse(ResponsesPredicate.isOneVOneResponseFlagSpec.test(caseData));
    }
}
