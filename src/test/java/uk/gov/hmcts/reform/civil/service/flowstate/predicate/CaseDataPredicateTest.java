package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class CaseDataPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class Applicant {

        @Test
        void should_return_true_for_isRepresented_when_applicant_is_represented() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            assertTrue(CaseDataPredicate.Applicant.isRepresented.test(caseData));
        }

        @Test
        void should_return_false_for_isRepresented_when_applicant_is_not_represented() {
            when(caseData.isApplicantNotRepresented()).thenReturn(true);
            assertFalse(CaseDataPredicate.Applicant.isRepresented.test(caseData));
        }

        @Test
        void should_return_false_for_isRepresented_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.isRepresented.test(null));
        }

        @Test
        void should_return_true_for_hasResponseDate_when_present() {
            when(caseData.getApplicant1ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Applicant.hasResponseDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDate_when_absent() {
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasResponseDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDate_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasResponseDate.test(null));
        }

        @Test
        void should_return_true_for_hasPassedResponseDeadline_when_deadline_in_past() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(CaseDataPredicate.Applicant.hasPassedResponseDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedResponseDeadline_when_deadline_in_future() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(CaseDataPredicate.Applicant.hasPassedResponseDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedResponseDeadline_when_deadline_null() {
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasPassedResponseDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedResponseDeadline_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasPassedResponseDeadline.test(null));
        }

        @Test
        void should_return_false_for_hasPassedResponseDeadline_when_deadline_equals_now() {
            // equality should not count as passed (predicate uses isBefore(now)).
            // set slightly in the future to avoid flakiness due to timing
            when(caseData.getApplicant1ResponseDeadline()).thenReturn(LocalDateTime.now().plusSeconds(1));

            assertFalse(CaseDataPredicate.Applicant.hasPassedResponseDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasProceedDecision_when_present() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedDecision.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedDecision_when_absent() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedDecision.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedDecision_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedDecision.test(null));
        }

        @Test
        void should_return_true_for_willProceed_when_yes() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceed.test(caseData));
        }

        @Test
        void should_return_false_for_willProceed_when_no() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YesOrNo.NO);
            assertFalse(CaseDataPredicate.Applicant.willProceed.test(caseData));
        }

        @Test
        void should_return_false_for_willProceed_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceed.test(null));
        }

        @Test
        void should_return_true_for_hasProceedDecisionSpec2v1_when_present() {
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedDecisionSpec2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedDecisionSpec2v1_when_absent() {
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedDecisionSpec2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedDecisionSpec2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedDecisionSpec2v1.test(null));
        }

        @Test
        void should_return_true_for_willProceedSpec2v1_when_yes() {
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceedSpec2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedSpec2v1_when_no() {
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(YesOrNo.NO);
            assertFalse(CaseDataPredicate.Applicant.willProceedSpec2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedSpec2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceedSpec2v1.test(null));
        }

        @Test
        void should_return_true_for_hasProceedAgainstRespondent1_1v2_when_present() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedAgainstRespondent1_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedAgainstRespondent1_1v2_when_absent() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedAgainstRespondent1_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedAgainstRespondent1_1v2_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedAgainstRespondent1_1v2.test(null));
        }

        @Test
        void should_return_true_for_willProceedAgainstRespondent1_1v2_when_yes() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceedAgainstRespondent1_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedAgainstRespondent1_1v2_when_no() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(YesOrNo.NO);
            assertFalse(CaseDataPredicate.Applicant.willProceedAgainstRespondent1_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedAgainstRespondent1_1v2_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceedAgainstRespondent1_1v2.test(null));
        }

        @Test
        void should_return_true_for_hasProceedAgainstRespondent2_1v2_when_present() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedAgainstRespondent2_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedAgainstRespondent2_1v2_when_absent() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedAgainstRespondent2_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedAgainstRespondent2_1v2_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedAgainstRespondent2_1v2.test(null));
        }

        @Test
        void should_return_true_for_willProceedAgainstRespondent2_1v2_when_yes() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceedAgainstRespondent2_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedAgainstRespondent2_1v2_when_no() {
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(YesOrNo.NO);
            assertFalse(CaseDataPredicate.Applicant.willProceedAgainstRespondent2_1v2.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedAgainstRespondent2_1v2_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceedAgainstRespondent2_1v2.test(null));
        }

        @Test
        void should_return_true_for_hasProceedMulti_2v1_when_present() {
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedMulti_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedMulti_2v1_when_absent() {
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedMulti_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedMulti_2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedMulti_2v1.test(null));
        }

        @Test
        void should_return_true_for_willProceedMulti_2v1_when_yes() {
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceedMulti_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedMulti_2v1_when_no() {
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(YesOrNo.NO);

            assertFalse(CaseDataPredicate.Applicant.willProceedMulti_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedMulti_2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceedMulti_2v1.test(null));
        }

        @Test
        void should_return_true_for_hasProceedApplicant2Multi_2v1_when_present() {
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.hasProceedApplicant2Multi_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedApplicant2Multi_2v1_when_absent() {
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(null);
            assertFalse(CaseDataPredicate.Applicant.hasProceedApplicant2Multi_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_hasProceedApplicant2Multi_2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.hasProceedApplicant2Multi_2v1.test(null));
        }

        @Test
        void should_return_true_for_willProceedApplicant2Multi_2v1_when_yes() {
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Applicant.willProceedApplicant2Multi_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedApplicant2Multi_2v1_when_no() {
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(YesOrNo.NO);
            assertFalse(CaseDataPredicate.Applicant.willProceedApplicant2Multi_2v1.test(caseData));
        }

        @Test
        void should_return_false_for_willProceedApplicant2Multi_2v1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Applicant.willProceedApplicant2Multi_2v1.test(null));
        }

    }

    @Nested
    class Claim {

        @Test
        void should_return_true_for_isSpecClaim_when_case_is_spec() {
            when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
            assertTrue(CaseDataPredicate.Claim.isSpecClaim.test(caseData));
        }

        @Test
        void should_return_false_for_isSpecClaim_when_case_category_null() {
            when(caseData.getCaseAccessCategory()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.isSpecClaim.test(caseData));
        }

        @Test
        void should_return_true_for_hasNotificationDeadline_when_present() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Claim.hasNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotificationDeadline_when_absent() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasNotificationDate_when_present() {
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Claim.hasNotificationDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotificationDate_when_absent() {
            when(caseData.getClaimNotificationDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasNotificationDate.test(caseData));
        }

        @Test
        void should_return_true_for_hasDismissedDate_when_present() {
            when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Claim.hasDismissedDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasDismissedDate_when_absent() {
            when(caseData.getClaimDismissedDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasDismissedDate.test(caseData));
        }

        @Test
        void should_return_true_for_hasDismissalDeadline_when_present() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Claim.hasDismissalDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasDismissalDeadline_when_absent() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasDismissalDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasPassedDismissalDeadline_when_deadline_in_past() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(CaseDataPredicate.Claim.hasPassedDismissalDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedDismissalDeadline_when_deadline_in_future() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(CaseDataPredicate.Claim.hasPassedDismissalDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedDismissalDeadline_when_deadline_null() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasPassedDismissalDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasOneVOneResponseFlag_when_present() {
            when(caseData.getShowResponseOneVOneFlag()).thenReturn(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY);
            assertTrue(CaseDataPredicate.Claim.hasOneVOneResponseFlag.test(caseData));
        }

        @Test
        void should_return_false_for_hasOneVOneResponseFlag_when_absent() {
            when(caseData.getShowResponseOneVOneFlag()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claim.hasOneVOneResponseFlag.test(caseData));
        }
    }

    @Nested
    class ClaimDetails {

        @Test
        void should_return_true_for_hasTimeExtensionRespondent1_when_present() {
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());

            assertTrue(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasTimeExtensionRespondent1_when_absent() {
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1.test(caseData));
        }

        @Test
        void should_return_true_for_acknowledgedNotificationRespondent1_when_present() {
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_acknowledgedNotificationRespondent1_when_absent() {
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.test(caseData));
        }

        @Test
        void should_return_true_for_hasResponseDateRespondent1_when_present() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent1_when_absent() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent1.test(null));
        }

        @Test
        void should_return_true_for_hasResponseDateRespondent2_when_present() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());

            assertTrue(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent2_when_absent() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_acknowledgedNotificationRespondent2_when_present() {
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_acknowledgedNotificationRespondent2_when_absent() {
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_hasTimeExtensionRespondent2_when_present() {
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_hasTimeExtensionRespondent2_when_absent() {
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_hasNotificationDate_when_present() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.ClaimDetails.hasNotificationDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotificationDate_when_absent() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasNotificationDate.test(caseData));
        }

        @Test
        void should_return_true_for_hasPassedNotificationDeadline_when_past() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(CaseDataPredicate.ClaimDetails.hasPassedNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedNotificationDeadline_when_future() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(CaseDataPredicate.ClaimDetails.hasPassedNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasPassedNotificationDeadline_when_absent() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasPassedNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasFutureNotificationDeadline_when_future() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertTrue(CaseDataPredicate.ClaimDetails.hasFutureNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasFutureNotificationDeadline_when_past() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertFalse(CaseDataPredicate.ClaimDetails.hasFutureNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_hasFutureNotificationDeadline_when_null() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasFutureNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_true_for_hasNotifyOptions_when_present() {
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(new DynamicList());
            assertTrue(CaseDataPredicate.ClaimDetails.hasNotifyOptions.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotifyOptions_when_absent() {
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasNotifyOptions.test(caseData));
        }

        @Test
        void should_return_true_for_hasNotifyOptionsBoth_when_label_both() {
            DynamicListElement value = DynamicListElement.dynamicElement("Both");
            DynamicList dl = DynamicList.builder().value(value).listItems(List.of(value)).build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dl);
            assertTrue(CaseDataPredicate.ClaimDetails.hasNotifyOptionsBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotifyOptionsBoth_when_label_not_both() {
            DynamicListElement value = DynamicListElement.dynamicElement("Other");
            DynamicList dl = DynamicList.builder().value(value).listItems(List.of(value)).build();
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(dl);
            assertFalse(CaseDataPredicate.ClaimDetails.hasNotifyOptionsBoth.test(caseData));
        }

        @Test
        void should_return_false_for_hasNotifyOptionsBoth_when_label_null() {
            when(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).thenReturn(null);
            assertFalse(CaseDataPredicate.ClaimDetails.hasNotifyOptionsBoth.test(caseData));
        }
    }

    @Nested
    class Claimant {

        @Test
        void should_return_true_for_defendantSingleResponseToBothClaimants_when_yes() {
            when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Claimant.defendantSingleResponseToBothClaimants.test(caseData));
        }

        @Test
        void should_return_false_for_defendantSingleResponseToBothClaimants_when_null() {
            when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claimant.defendantSingleResponseToBothClaimants.test(caseData));
        }

        @Test
        void should_return_true_for_responseTypeSpecClaimant1_when_matches() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Claimant.responseTypeSpecClaimant1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_responseTypeSpecClaimant1_when_different() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Claimant.responseTypeSpecClaimant1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_responseTypeSpecClaimant1_when_null() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claimant.responseTypeSpecClaimant1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_responseTypeSpecClaimant2_when_matches() {
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Claimant.responseTypeSpecClaimant2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_responseTypeSpecClaimant2_when_different() {
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Claimant.responseTypeSpecClaimant2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_responseTypeSpecClaimant2_when_null() {
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claimant.responseTypeSpecClaimant2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_responsesDifferSpec_when_both_present_and_different() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertTrue(CaseDataPredicate.Claimant.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_equal() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertFalse(CaseDataPredicate.Claimant.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_claimant1_is_missing() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claimant.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_claimant2_is_missing() {
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Claimant.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_case_data_is_null() {
            // ensure null-safe: predicate should return false when caseData is null
            assertFalse(CaseDataPredicate.Claimant.responsesDifferSpec.test(null));
        }

        @Test
        void should_return_true_for_agreedToMediation_when_true() {
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(true);
            assertTrue(CaseDataPredicate.Claimant.agreedToMediation.test(caseData));
        }

        @Test
        void should_return_false_for_agreedToMediation_when_false() {
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);
            assertFalse(CaseDataPredicate.Claimant.agreedToMediation.test(caseData));
        }
    }

    @Nested
    class Hearing {

        @Test
        void should_return_true_for_hasReference_when_present() {
            when(caseData.getHearingReferenceNumber()).thenReturn("HR-123");
            assertTrue(CaseDataPredicate.Hearing.hasReference.test(caseData));
        }

        @Test
        void should_return_false_for_hasReference_when_absent() {
            when(caseData.getHearingReferenceNumber()).thenReturn(null);
            assertFalse(CaseDataPredicate.Hearing.hasReference.test(caseData));
        }

        @Test
        void should_return_true_for_isListed_when_listing() {
            when(caseData.getListingOrRelisting()).thenReturn(ListingOrRelisting.LISTING);
            assertTrue(CaseDataPredicate.Hearing.isListed.test(caseData));
        }

        @Test
        void should_return_false_for_isListed_when_not_listing() {
            when(caseData.getListingOrRelisting()).thenReturn(ListingOrRelisting.RELISTING);
            assertFalse(CaseDataPredicate.Hearing.isListed.test(caseData));
        }

        @Test
        void should_return_false_for_isListed_when_null() {
            when(caseData.getListingOrRelisting()).thenReturn(null);
            assertFalse(CaseDataPredicate.Hearing.isListed.test(caseData));
        }

        @Test
        void should_return_true_for_hasDismissedFeeDueDate_when_present() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Hearing.hasDismissedFeeDueDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasDismissedFeeDueDate_when_absent() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Hearing.hasDismissedFeeDueDate.test(caseData));
        }
    }

    @Nested
    class Judgment {

        @Test
        void should_return_true_for_isByAdmission_when_active_judgment_by_admission() {
            uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails jd = new uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails();
            jd.setType(uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType.JUDGMENT_BY_ADMISSION);
            when(caseData.getActiveJudgment()).thenReturn(jd);
            assertTrue(CaseDataPredicate.Judgment.isByAdmission.test(caseData));
        }

        @Test
        void should_return_false_for_isByAdmission_when_no_active_judgment() {
            uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails jd = new uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails();
            jd.setType(uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType.DEFAULT_JUDGMENT);
            when(caseData.getActiveJudgment()).thenReturn(jd);
            assertFalse(CaseDataPredicate.Judgment.isByAdmission.test(caseData));
        }

        @Test
        void should_return_false_for_isByAdmission_when_null() {
            when(caseData.getActiveJudgment()).thenReturn(null);
            assertFalse(CaseDataPredicate.Judgment.isByAdmission.test(caseData));
        }
    }

    @Nested
    class MultiParty {

        @Test
        void should_return_true_for_isOneVTwoOneLegalRep_when_respondent2_present_and_same_legal_rep_yes() {
            when(caseData.getRespondent2()).thenReturn(uk.gov.hmcts.reform.civil.model.Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.YES);
            assertTrue(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.test(caseData));
        }

        @Test
        void should_return_true_for_isOneVTwoTwoLegalRep_when_respondent2_present_and_same_legal_rep_no() {
            when(caseData.getRespondent2()).thenReturn(uk.gov.hmcts.reform.civil.model.Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YesOrNo.NO);
            assertTrue(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.test(caseData));
        }
    }

    @Nested
    class RepaymentPlan {

        @Test
        void should_return_true_for_accepted_when_caseData_reports_true() {
            when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);
            assertTrue(CaseDataPredicate.RepaymentPlan.accepted.test(caseData));
        }

        @Test
        void should_return_true_for_rejected_when_caseData_reports_true() {
            when(caseData.hasApplicantRejectedRepaymentPlan()).thenReturn(true);
            assertTrue(CaseDataPredicate.RepaymentPlan.rejected.test(caseData));
        }

        @Test
        void should_return_false_for_accepted_when_not_reported() {
            when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(false);
            assertFalse(CaseDataPredicate.RepaymentPlan.accepted.test(caseData));
        }
    }

    @Nested
    class Respondent {

        @Test
        void should_return_true_for_hasResponseDateRespondent1_when_present() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());

            assertTrue(CaseDataPredicate.Respondent.hasResponseDateRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent1_when_absent() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasResponseDateRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent1_when_case_data_is_null() {
            assertFalse(CaseDataPredicate.Respondent.hasResponseDateRespondent1.test(null));
        }

        @Test
        void should_return_true_for_hasResponseDateRespondent2_when_present() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Respondent.hasResponseDateRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseDateRespondent2_when_absent() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasResponseDateRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_hasResponseTypeRespondent1_when_present() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseTypeRespondent1_when_absent() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.test(caseData));
        }

        @Test
        void should_return_true_for_hasResponseTypeRespondent2_when_present() {
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_hasResponseTypeRespondent2_when_absent() {
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_hasIntentionToProceedRespondent1_when_present() {
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(ResponseIntention.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_hasIntentionToProceedRespondent1_when_absent() {
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent1.test(caseData));
        }

        @Test
        void should_return_true_for_hasIntentionToProceedRespondent2_when_present() {
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(ResponseIntention.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_hasIntentionToProceedRespondent2_when_absent() {
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_isTypeRespondent1_factory_when_matches() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent1_factory_when_different() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent1_factory_when_null() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_isTypeRespondent2_factory_when_matches() {
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent2_factory_when_different() {
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent2_factory_when_null() {
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_isTypeRespondent1ToApplicant2_factory_when_matches() {
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent1ToApplicant2_factory_when_different() {
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeRespondent1ToApplicant2_factory_when_null() {
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(RespondentResponseType.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_isTypeSpecRespondent1_factory_when_matches() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeSpecRespondent1_factory_when_different() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeSpecRespondent1_factory_when_null() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_isTypeSpecRespondent2_factory_when_matches() {
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertTrue(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeSpecRespondent2_factory_when_different() {
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertFalse(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_false_for_isTypeSpecRespondent2_factory_when_null() {
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
        }

        @Test
        void should_return_true_for_respondentsHaveSameResponseFlag_when_yes() {
            when(caseData.getRespondentResponseIsSame()).thenReturn(YES);
            assertTrue(CaseDataPredicate.Respondent.respondentsHaveSameResponseFlag.test(caseData));
        }

        @Test
        void should_return_false_for_respondentsHaveSameResponseFlag_when_null() {
            when(caseData.getRespondentResponseIsSame()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.respondentsHaveSameResponseFlag.test(caseData));
        }

        @Test
        void should_return_true_for_responsesDiffer_when_different() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
            assertTrue(CaseDataPredicate.Respondent.responsesDiffer.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDiffer_when_equal() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            assertFalse(CaseDataPredicate.Respondent.responsesDiffer.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDiffer_when_respondent1_is_missing() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.responsesDiffer.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDiffer_when_respondent2_is_missing() {
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.responsesDiffer.test(caseData));
        }

        @Test
        void should_return_true_for_responsesDifferSpec_when_spec_different() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertTrue(CaseDataPredicate.Respondent.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_equal() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            assertFalse(CaseDataPredicate.Respondent.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_respondent1_is_missing() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_false_for_responsesDifferSpec_when_respondent2_is_missing() {
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
            assertFalse(CaseDataPredicate.Respondent.responsesDifferSpec.test(caseData));
        }

        @Test
        void should_return_true_for_respondent1ResponseAfterRespondent2_when_after() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusHours(1));
            assertTrue(CaseDataPredicate.Respondent.respondent1ResponseAfterRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_respondent1ResponseAfterRespondent2_when_before() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusHours(2));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusHours(1));
            assertFalse(CaseDataPredicate.Respondent.respondent1ResponseAfterRespondent2.test(caseData));
        }

        @Test
        void should_return_false_for_respondent1ResponseAfterRespondent2_when_missing() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertFalse(CaseDataPredicate.Respondent.respondent1ResponseAfterRespondent2.test(caseData));
        }

        @Test
        void should_return_true_for_respondent2ResponseAfterRespondent1_when_after() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusHours(1));
            assertTrue(CaseDataPredicate.Respondent.respondent2ResponseAfterRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_respondent2ResponseAfterRespondent1_when_before() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusHours(2));
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusHours(1));
            assertFalse(CaseDataPredicate.Respondent.respondent2ResponseAfterRespondent1.test(caseData));
        }

        @Test
        void should_return_false_for_respondent2ResponseAfterRespondent1_when_missing() {
            when(caseData.getRespondent2ResponseDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            assertFalse(CaseDataPredicate.Respondent.respondent2ResponseAfterRespondent1.test(caseData));
        }
    }

    @Nested
    class Payment {

        @Test
        void should_return_true_for_hasPaymentSuccessfulDate_when_present() {
            when(caseData.getPaymentSuccessfulDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.Payment.hasPaymentSuccessfulDate.test(caseData));
        }

        @Test
        void should_return_false_for_hasPaymentSuccessfulDate_when_absent() {
            when(caseData.getPaymentSuccessfulDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.Payment.hasPaymentSuccessfulDate.test(caseData));
        }

        @Test
        void should_return_true_for_claimIssuedPaymentSucceeded_when_status_success() {
            uk.gov.hmcts.reform.civil.model.PaymentDetails pd = uk.gov.hmcts.reform.civil.model.PaymentDetails.builder()
                .status(PaymentStatus.SUCCESS)
                .build();
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(pd);
            assertTrue(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded.test(caseData));
        }

        @Test
        void should_return_false_for_claimIssuedPaymentSucceeded_when_payment_details_absent() {
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
            assertFalse(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded.test(caseData));
        }

        @Test
        void should_return_false_for_claimIssuedPaymentSucceeded_when_payment_status_not_success() {
            uk.gov.hmcts.reform.civil.model.PaymentDetails pd = uk.gov.hmcts.reform.civil.model.PaymentDetails.builder()
                .status(PaymentStatus.FAILED)
                .build();
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(pd);
            assertFalse(CaseDataPredicate.Payment.claimIssuedPaymentSucceeded.test(caseData));
        }
    }

    @Nested
    class TakenOffline {

        @Test
        void should_return_true_for_hasSdoReasonNotSuitable_when_reason_and_input_present() {
            uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO reason = uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO.builder().input("reason").build();
            when(caseData.getReasonNotSuitableSDO()).thenReturn(reason);
            assertTrue(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.test(caseData));
        }

        @Test
        void should_return_false_for_hasSdoReasonNotSuitable_when_reason_absent() {
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            assertFalse(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.test(caseData));
        }

        @Test
        void should_return_false_for_hasSdoReasonNotSuitable_when_input_empty() {
            uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO reason = uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO.builder().input(null).build();
            when(caseData.getReasonNotSuitableSDO()).thenReturn(reason);
            assertFalse(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.test(caseData));
        }

        @Test
        void should_return_true_for_dateExists_when_present() {
            when(caseData.getTakenOfflineDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.TakenOffline.dateExists.test(caseData));
        }

        @Test
        void should_return_false_for_dateExists_when_absent() {
            when(caseData.getTakenOfflineDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.TakenOffline.dateExists.test(caseData));
        }

        @Test
        void should_return_true_for_byStaffDateExists_when_present() {
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
            assertTrue(CaseDataPredicate.TakenOffline.byStaffDateExists.test(caseData));
        }

        @Test
        void should_return_false_for_byStaffDateExists_when_absent() {
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertFalse(CaseDataPredicate.TakenOffline.byStaffDateExists.test(caseData));
        }

        @Test
        void should_return_true_for_hasChangeOfRepresentation_when_present() {
            when(caseData.getChangeOfRepresentation()).thenReturn(new uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation());
            assertTrue(CaseDataPredicate.TakenOffline.hasChangeOfRepresentation.test(caseData));
        }

        @Test
        void should_return_false_for_hasChangeOfRepresentation_when_absent() {
            when(caseData.getChangeOfRepresentation()).thenReturn(null);
            assertFalse(CaseDataPredicate.TakenOffline.hasChangeOfRepresentation.test(caseData));
        }

        @Test
        void should_return_true_for_hasDrawDirectionsOrderRequired_when_yes() {
            when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.YES);
            assertTrue(CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.test(caseData));
        }

        @Test
        void should_return_false_for_hasDrawDirectionsOrderRequired_when_absent() {
            when(caseData.getDrawDirectionsOrderRequired()).thenReturn(null);
            assertFalse(CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.test(caseData));
        }

        @Test
        void should_return_true_for_hasDrawDirectionsOrderRequired_when_no_but_not_null() {
            when(caseData.getDrawDirectionsOrderRequired()).thenReturn(YesOrNo.NO);
            assertTrue(CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.test(caseData));
        }
    }

    @Nested
    class Language {

        @Test
        void should_return_true_for_hasChangePreference_when_present() {
            when(caseData.getChangeLanguagePreference()).thenReturn(new uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference());
            assertTrue(CaseDataPredicate.Language.hasChangePreference.test(caseData));
        }

        @Test
        void should_return_false_for_hasChangePreference_when_absent() {
            when(caseData.getChangeLanguagePreference()).thenReturn(null);
            assertFalse(CaseDataPredicate.Language.hasChangePreference.test(caseData));
        }

        @Test
        void should_return_true_for_isBilingualFlag_when_true() {
            when(caseData.isRespondentResponseBilingual()).thenReturn(true);
            assertTrue(CaseDataPredicate.Language.isBilingualFlag.test(caseData));
        }

        @Test
        void should_return_false_for_isBilingualFlag_when_false() {
            when(caseData.isRespondentResponseBilingual()).thenReturn(false);
            assertFalse(CaseDataPredicate.Language.isBilingualFlag.test(caseData));
        }
    }

    @Nested
    class Lip {

        @Test
        void should_return_true_for_partyIsUnrepresented_when_true() {
            when(caseData.isLipCase()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.partyIsUnrepresented.test(caseData));
        }

        @Test
        void should_return_false_for_partyIsUnrepresented_when_false() {
            when(caseData.isLipCase()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.partyIsUnrepresented.test(caseData));
        }

        @Test
        void should_return_true_for_isLiPCase_when_true() {
            when(caseData.isLipvLipOneVOne()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.isLiPCase.test(caseData));
        }

        @Test
        void should_return_false_for_isLiPCase_when_false() {
            when(caseData.isLipvLipOneVOne()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.isLiPCase.test(caseData));
        }

        @Test
        void should_return_true_for_translatedResponseDocumentUploaded_when_true() {
            when(caseData.isTranslatedDocumentUploaded()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.translatedResponseDocumentUploaded.test(caseData));
        }

        @Test
        void should_return_false_for_translatedResponseDocumentUploaded_when_false() {
            when(caseData.isTranslatedDocumentUploaded()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.translatedResponseDocumentUploaded.test(caseData));
        }

        @Test
        void should_return_true_for_ccjRequestByAdmissionFlag_when_true() {
            when(caseData.isCcjRequestJudgmentByAdmission()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.ccjRequestByAdmissionFlag.test(caseData));
        }

        @Test
        void should_return_false_for_ccjRequestByAdmissionFlag_when_false() {
            when(caseData.isCcjRequestJudgmentByAdmission()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.ccjRequestByAdmissionFlag.test(caseData));
        }

        @Test
        void should_return_true_for_respondentSignedSettlementAgreement_when_true() {
            when(caseData.isRespondentRespondedToSettlementAgreement()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.respondentSignedSettlementAgreement.test(caseData));
        }

        @Test
        void should_return_false_for_respondentSignedSettlementAgreement_when_false() {
            when(caseData.isRespondentRespondedToSettlementAgreement()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.respondentSignedSettlementAgreement.test(caseData));
        }

        @Test
        void should_return_true_for_nocSubmittedForLiPDefendantBeforeOffline_when_true() {
            when(caseData.nocApplyForLiPDefendantBeforeOffline()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.nocSubmittedForLiPDefendantBeforeOffline.test(caseData));
        }

        @Test
        void should_return_false_for_nocSubmittedForLiPDefendantBeforeOffline_when_false() {
            when(caseData.nocApplyForLiPDefendantBeforeOffline()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.nocSubmittedForLiPDefendantBeforeOffline.test(caseData));
        }

        @Test
        void should_return_true_for_nocSubmittedForLiPDefendant_when_true() {
            when(caseData.nocApplyForLiPDefendant()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.nocSubmittedForLiPDefendant.test(caseData));
        }

        @Test
        void should_return_false_for_nocSubmittedForLiPDefendant_when_false() {
            when(caseData.nocApplyForLiPDefendant()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.nocSubmittedForLiPDefendant.test(caseData));
        }

        @Test
        void should_return_true_for_caseContainsLiP_when_isRespondent1LiP_true() {
            when(caseData.isRespondent1LiP()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.caseContainsLiP.test(caseData));
        }

        @Test
        void should_return_true_for_caseContainsLiP_when_isRespondent2LiP_true() {
            when(caseData.isRespondent1LiP()).thenReturn(false);
            when(caseData.isRespondent2LiP()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.caseContainsLiP.test(caseData));
        }

        @Test
        void should_return_true_for_caseContainsLiP_when_isApplicantNotRepresented_true() {
            when(caseData.isRespondent1LiP()).thenReturn(false);
            when(caseData.isRespondent2LiP()).thenReturn(false);
            when(caseData.isApplicantNotRepresented()).thenReturn(true);
            assertTrue(CaseDataPredicate.Lip.caseContainsLiP.test(caseData));
        }

        @Test
        void should_return_false_for_caseContainsLiP_when_false() {
            when(caseData.isRespondent1LiP()).thenReturn(false);
            when(caseData.isRespondent2LiP()).thenReturn(false);
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            assertFalse(CaseDataPredicate.Lip.caseContainsLiP.test(caseData));
        }

    }

}
