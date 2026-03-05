package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class MediationPredicateTest {

    @Mock
    private CaseData caseData;

    @Mock
    private CaseDataLiP caseDataLiP;

    @Nested
    class AgreedDeclinedMediation {

        @Test
        void should_return_true_when_claimant_agreed_to_mediation() {
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(true);
            assertTrue(MediationPredicate.agreedToMediation.test(caseData));
        }

        @Test
        void should_return_false_when_claimant_not_agreed_to_mediation() {
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);
            assertFalse(MediationPredicate.agreedToMediation.test(caseData));
        }

        @Test
        void should_return_true_when_claimant_declined_mediation() {
            when(caseData.hasClaimantNotAgreedToFreeMediation()).thenReturn(true);
            assertTrue(MediationPredicate.declinedMediation.test(caseData));
        }

        @Test
        void should_return_false_when_claimant_not_declined_mediation() {
            when(caseData.hasClaimantNotAgreedToFreeMediation()).thenReturn(false);
            assertFalse(MediationPredicate.declinedMediation.test(caseData));
        }
    }

    @Nested
    class CarmEnabled {

        @Test
        void should_return_true_when_any_contact_info_present_applicant1() {
            when(caseData.getApp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
        }

        @Test
        void should_return_true_when_any_contact_info_present_respondent1() {
            when(caseData.getResp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
        }

        @Test
        void should_return_true_when_any_contact_info_present_respondent2() {
            when(caseData.getResp2MediationContactInfo()).thenReturn(new MediationContactInformation());
            assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
        }

        @Test
        void should_return_false_when_no_contact_info_present() {
            when(caseData.getApp1MediationContactInfo()).thenReturn(null);
            when(caseData.getResp1MediationContactInfo()).thenReturn(null);
            when(caseData.getResp2MediationContactInfo()).thenReturn(null);
            assertFalse(MediationPredicate.isCarmEnabledForCase.test(caseData));
        }

        @Test
        void should_return_true_for_LiP_when_any_LiP_CARM_response_present_applicant1() {
            when(caseData.getCaseDataLiP()).thenReturn(caseDataLiP);
            when(caseDataLiP.getApplicant1LiPResponseCarm()).thenReturn(new MediationLiPCarm());
            assertTrue(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
        }

        @Test
        void should_return_true_for_LiP_when_any_LiP_CARM_response_present_respondent1() {
            when(caseData.getCaseDataLiP()).thenReturn(caseDataLiP);
            when(caseDataLiP.getRespondent1MediationLiPResponseCarm()).thenReturn(new MediationLiPCarm());
            assertTrue(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
        }

        @Test
        void should_return_false_for_LiP_when_no_LiP_CARM_response_present() {
            when(caseData.getCaseDataLiP()).thenReturn(caseDataLiP);
            when(caseDataLiP.getApplicant1LiPResponseCarm()).thenReturn(null);
            when(caseDataLiP.getRespondent1MediationLiPResponseCarm()).thenReturn(null);
            assertFalse(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
        }
    }

    @Nested
    class CarmApplicableLR {

        @Test
        void should_return_true_when_case_is_spec_small_claims_with_contact_info_and_r1_represented_and_applicant1_not_unrepresented() {
            when(caseData.getApp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            // Applicant1 not unrepresented -> applicant1Represented != NO (set to YES)
            when(caseData.getApplicant1Represented()).thenReturn(YES);
            assertTrue(MediationPredicate.isCarmApplicableCase.test(caseData));
        }

        @Test
        void should_return_false_when_missing_any_condition_e_g_not_small_claims() {
            when(caseData.getApp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            // Not small claims track
            when(caseData.getResponseClaimTrack()).thenReturn("FAST_CLAIM");
            assertFalse(MediationPredicate.isCarmApplicableCase.test(caseData));
        }
    }

    @Nested
    class CarmApplicableLiP {

        @Test
        void should_return_true_when_spec_small_1v1_and_one_party_unrepresented_and_LiP_CARM_response_present() {
            when(caseData.getCaseDataLiP()).thenReturn(caseDataLiP);
            when(caseDataLiP.getApplicant1LiPResponseCarm()).thenReturn(new MediationLiPCarm());
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            // No respondent2
            when(caseData.getRespondent2()).thenReturn(null);
            // At least one unrepresented (applicant1)
            when(caseData.getApplicant1Represented()).thenReturn(NO);

            assertTrue(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
        }

        @Test
        void should_return_false_when_respondent2_present_or_no_LiP_CARM_response() {
            when(caseData.getCaseDataLiP()).thenReturn(caseDataLiP);
            when(caseDataLiP.getApplicant1LiPResponseCarm()).thenReturn(null);
            when(caseDataLiP.getRespondent1MediationLiPResponseCarm()).thenReturn(null);
            assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
        }
    }

    @Nested
    class CarmMediationRoute {

        @Test
        void should_return_true_when_claimant_will_not_settle_and_not_agreed_to_mediation_and_carm_applicable_and_not_taken_offline() {
            // Claimant will not settle (part admit)
            when(caseData.isClaimantNotSettlePartAdmitClaim()).thenReturn(true);
            // claimant has not agreed to mediation
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);
            // CARM applicable (LR path)
            when(caseData.getApp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getApplicant1Represented()).thenReturn(YES);
            // Not taken offline by staff
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);

            assertTrue(MediationPredicate.isCarmMediation.test(caseData));
        }

        @Test
        void should_return_false_when_taken_offline_by_staff() {
            when(caseData.isClaimantNotSettlePartAdmitClaim()).thenReturn(true);
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);
            when(caseData.getApp1MediationContactInfo()).thenReturn(new MediationContactInformation());
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getApplicant1Represented()).thenReturn(YES);
            // Taken offline blocks
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(java.time.LocalDateTime.now());

            assertFalse(MediationPredicate.isCarmMediation.test(caseData));
        }

        @Test
        void should_return_false_when_claimant_has_agreed_to_mediation() {
            when(caseData.isClaimantNotSettlePartAdmitClaim()).thenReturn(true);
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(true);
            assertFalse(MediationPredicate.isCarmMediation.test(caseData));
        }
    }

    @Nested
    class AllAgreedToLrMediationSpec {

        @Test
        void should_return_true_when_all_conditions_met_in_spec_small_claims() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            // Respondent1 agreed (SPEC)
            when(caseData.getResponseClaimMediationSpecRequired()).thenReturn(YES);
            // No blocking R2 different rep non-agreement scenario
            when(caseData.getRespondent2()).thenReturn(null);
            // Applicant1 agreed (SPEC) -> negate of NOT agreed
            uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec app1Spec =
                new uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec(YES);
            when(caseData.getApplicant1ClaimMediationSpecRequired()).thenReturn(app1Spec);
            // MP applicant agreed (or absent) -> negate of NOT required applicant MP
            uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec mpSpec =
                new uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec(YES);
            when(caseData.getApplicantMPClaimMediationSpecRequired()).thenReturn(mpSpec);
            // General claimant agreed flag must be false
            when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);

            assertTrue(MediationPredicate.allAgreedToLrMediationSpec.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_has_not_agreed_spec() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getResponseClaimTrack()).thenReturn(SMALL_CLAIM.name());
            when(caseData.getResponseClaimMediationSpecRequired()).thenReturn(NO);
            assertFalse(MediationPredicate.allAgreedToLrMediationSpec.test(caseData));
        }
    }

    @Nested
    class UnsuccessfulMediationFlags {

        @Test
        void should_return_true_for_beforeUnsuccessful_when_no_reasons_present() {
            Mediation mediation = new Mediation().setUnsuccessfulMediationReason(null).setMediationUnsuccessfulReasonsMultiSelect(null);
            when(caseData.getMediation()).thenReturn(mediation);
            assertTrue(MediationPredicate.beforeUnsuccessful.test(caseData));
        }

        @Test
        void should_return_false_for_beforeUnsuccessful_when_single_reason_present() {
            Mediation mediation = new Mediation().setUnsuccessfulMediationReason("Some reason");
            when(caseData.getMediation()).thenReturn(mediation);
            assertFalse(MediationPredicate.beforeUnsuccessful.test(caseData));
        }

        @Test
        void should_return_true_for_unsuccessful_when_single_reason_present() {
            Mediation mediation = new Mediation().setUnsuccessfulMediationReason("Some reason");
            when(caseData.getMediation()).thenReturn(mediation);
            assertTrue(MediationPredicate.unsuccessful.test(caseData));
        }

        @Test
        void should_return_true_for_unsuccessful_when_multi_select_has_values() {
            Mediation mediation = new Mediation().setMediationUnsuccessfulReasonsMultiSelect(List.of(
                uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE
            ));
            when(caseData.getMediation()).thenReturn(mediation);
            assertTrue(MediationPredicate.unsuccessful.test(caseData));
        }

        @Test
        void should_return_false_for_unsuccessful_when_no_reasons_present() {
            Mediation mediation = new Mediation().setUnsuccessfulMediationReason(null).setMediationUnsuccessfulReasonsMultiSelect(null);
            when(caseData.getMediation()).thenReturn(mediation);
            assertFalse(MediationPredicate.unsuccessful.test(caseData));
        }
    }
}
