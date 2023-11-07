package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class JudicialReferralUtils {

    private JudicialReferralUtils() {
        //NO-OP
    }

    /**
     * Computes whether the case data should move to judicial referral or not.
     *
     * @param caseData a case data such that defendants rejected the claim, and claimant(s) wants to proceed
     *                 vs all the defendants
     * @return true if and only if the case should move to judicial referral
     */
    public static boolean shouldMoveToJudicialReferral(CaseData caseData) {
        CaseCategory caseCategory = caseData.getCaseAccessCategory();

        if (CaseCategory.SPEC_CLAIM.equals(caseCategory)) {
            MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

            return switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP -> caseData.getApplicant1ProceedWithClaim() == YesOrNo.YES;
                case TWO_V_ONE -> caseData.getApplicant1ProceedWithClaimSpec2v1() == YesOrNo.YES;
            };
        } else {
            AllocatedTrack allocatedTrack =
                getAllocatedTrack(
                    CaseCategory.UNSPEC_CLAIM.equals(caseCategory)
                        ? caseData.getClaimValue().toPounds()
                        : caseData.getTotalClaimAmount(),
                    caseData.getClaimType()
                );
            if (AllocatedTrack.MULTI_CLAIM.equals(allocatedTrack)) {
                return false;
            }
            MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
            return switch (multiPartyScenario) {
                case ONE_V_ONE -> caseData.getApplicant1ProceedWithClaim() == YesOrNo.YES;
                case TWO_V_ONE -> caseData.getApplicant1ProceedWithClaimMultiParty2v1() == YES
                    && caseData.getApplicant2ProceedWithClaimMultiParty2v1() == YES;
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2() == YES
                        && caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2() == YES;
            };
        }
    }
}
