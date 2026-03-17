package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ResponsePredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_notificationAcknowledged_when_respondent1_acknowledged() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_notificationAcknowledged_when_one_v_two_two_legal_rep_and_respondent2_acknowledged() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_notificationAcknowledged_when_one_v_two_one_legal_rep_and_respondent2_acknowledged() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_false_for_notificationAcknowledged_when_neither_respondent_acknowledged() {
        when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
        assertFalse(ResponsePredicate.notificationAcknowledged.test(caseData));
    }

    @Test
    void should_return_true_for_respondentTimeExtension_when_respondent1_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_true_for_respondentTimeExtension_when_one_v_two_two_legal_rep_and_respondent2_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_false_for_respondentTimeExtension_when_no_respondent_has_time_extension() {
        when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
        assertFalse(ResponsePredicate.respondentTimeExtension.test(caseData));
    }

    @Test
    void should_return_true_for_allResponsesReceived_when_respondent1_responded_and_not_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_true_for_allResponsesReceived_when_both_respondents_responded_in_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
        assertTrue(ResponsePredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_false_for_allResponsesReceived_when_respondent1_not_responded() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertFalse(ResponsePredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_false_for_allResponsesReceived_when_only_one_respondent_responded_in_two_legal_rep() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2ResponseDate()).thenReturn(null);
        assertFalse(ResponsePredicate.allResponsesReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceived_when_one_v_two_two_legal_rep_and_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceived_when_one_v_two_two_legal_rep_and_r2_full_defence_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsePredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceived_when_neither_is_full_defence() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceived_when_one_v_two_two_legal_rep_and_r1_full_admit_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceived_when_one_v_two_two_legal_rep_and_r2_full_admit_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        assertTrue(ResponsePredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceived_when_neither_is_full_admit() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_one_v_two_two_legal_rep_and_r1_non_full_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_one_v_two_two_legal_rep_and_r2_non_full_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertTrue(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_not_one_v_two_two_legal_rep() {
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_both_respondents_have_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceived_when_neither_has_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived.test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_one_and_respondent1_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_one_and_respondent1_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_one_and_no_response_date() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertFalse(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_same_response() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.YES);
        assertTrue(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_r2_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_r2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_two_legal_rep_and_both_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_two_two_legal_rep_and_r2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_two_v_one_and_r1_full_defence_and_r1_to_a2_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_two_v_one_and_r1_to_a2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_one_and_spec_claim_and_r1_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_one_and_spec_claim_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_same_response() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.YES);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_r2_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_r2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_both_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_r2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceivedSpec_when_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullDefenceReceivedSpec_when_r2_full_defence_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceivedSpec_when_not_two_legal_reps() {
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceivedSpec_when_both_have_responses() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullDefenceReceivedSpec_when_neither_is_full_defence() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceivedSpec_when_r1_full_admission_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesFullAdmitReceivedSpec_when_r2_full_admission_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertTrue(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceivedSpec_when_not_two_legal_reps() {
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceivedSpec_when_both_have_responses() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesFullAdmitReceivedSpec_when_neither_is_full_admission() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_r1_non_full_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertTrue(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_r2_non_full_and_r1_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertTrue(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_not_two_legal_reps() {
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_both_have_responses() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_r1_full_defence_and_r2_no_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_false_for_awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec_when_neither_has_response() {
        when(caseData.getRespondent2()).thenReturn(new Party());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        assertFalse(ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec.test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_two_v_one_and_defendant_single_response_and_r1_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_defendant_single_response_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_both_claimants_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_c1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_c2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponsePredicate.isType(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }
}
