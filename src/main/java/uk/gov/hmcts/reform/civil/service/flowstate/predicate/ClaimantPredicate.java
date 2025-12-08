package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@SuppressWarnings("java:S1214")
public non-sealed interface ClaimantPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Claimant",
        summary = "todo",
        description = "Applicant correspondence address not required (Spec)"
    )
    Predicate<CaseData> correspondenceAddressNotRequired = Applicant.isNotApplicantCorrespondenceAddressRequiredSpec;

    @BusinessRule(
        group = "Claimant",
        summary = "Before applicant response",
        description = "Applicant initial response has not been recorded yet (for UNSPEC with applicant 2, neither applicant has responded)"
    )
    Predicate<CaseData> beforeResponse =
        c -> {
            if (CaseDataPredicate.Claim.isUnspecClaim
                .and(CaseDataPredicate.Applicant.isAddApplicant2).test(c)
            ) {
                return CaseDataPredicate.Applicant.hasResponseDateApplicant1.negate()
                    .and(CaseDataPredicate.Applicant.hasResponseDateApplicant2.negate()).test(c);
            }
            return CaseDataPredicate.Applicant.hasResponseDateApplicant1.negate().test(c);
        };

    @BusinessRule(
        group = "Claimant",
        summary = "Applicant will proceed - full defence flow",
        description = "Applicant has decided to proceed with the claim (SPEC/UNSPEC, 1v1/1v2/2v1). In UNSPEC 1v2, " +
            "proceeding against at least one defendant qualifies; in 2v1, at least one applicant chooses to proceed"
    )
    Predicate<CaseData> fullDefenceProceed =
        c -> {
            if (CaseDataPredicate.Claim.isSpecClaim.test(c)) {
                return getMultiPartyScenario(c) == TWO_V_ONE
                    ? CaseDataPredicate.Applicant.hasProceedDecisionSpec2v1
                    .and(CaseDataPredicate.Applicant.willProceedSpec2v1).test(c)
                    : CaseDataPredicate.Applicant.hasProceedDecision.and(CaseDataPredicate.Applicant.willProceed).test(c);
            } else {
                return switch (getMultiPartyScenario(c)) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                        (CaseDataPredicate.Applicant.hasProceedAgainstRespondent1_1v2
                            .and(CaseDataPredicate.Applicant.willProceedAgainstRespondent1_1v2)
                        )
                            .or(CaseDataPredicate.Applicant.hasProceedAgainstRespondent2_1v2
                                    .and(CaseDataPredicate.Applicant.willProceedAgainstRespondent2_1v2)
                            ).test(c);
                    case ONE_V_ONE ->
                        CaseDataPredicate.Applicant.hasProceedDecision
                            .and(CaseDataPredicate.Applicant.willProceed).test(c);
                    case TWO_V_ONE ->
                        (CaseDataPredicate.Applicant.hasProceedMulti_2v1
                            .and(CaseDataPredicate.Applicant.willProceedMulti_2v1))
                            .or(CaseDataPredicate.Applicant.hasProceedApplicant2Multi_2v1
                                    .and(CaseDataPredicate.Applicant.willProceedApplicant2Multi_2v1))
                            .test(c);
                };
            }
        };

    @BusinessRule(
        group = "Claimant",
        summary = "Applicant will not proceed - full defence flow",
        description = "Applicant has decided not to proceed with the claim. In UNSPEC 1v2, 'not proceed' is recorded " +
            "against both defendants; in 2v1, both applicants record 'not proceed'; in 1v1, a single 'not proceed' decision applies"
    )
    Predicate<CaseData> fullDefenceNotProceed =
        c -> {
            if (CaseDataPredicate.Claim.isSpecClaim.test(c)) {
                return getMultiPartyScenario(c) == TWO_V_ONE
                    ? CaseDataPredicate.Applicant.hasProceedDecisionSpec2v1
                    .and(CaseDataPredicate.Applicant.willProceedSpec2v1.negate()).test(c)
                    : CaseDataPredicate.Applicant.hasProceedDecision
                    .and(CaseDataPredicate.Applicant.willProceed.negate()).test(c);
            } else {
                return switch (getMultiPartyScenario(c)) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                        (CaseDataPredicate.Applicant.hasProceedAgainstRespondent1_1v2
                            .and(CaseDataPredicate.Applicant.willProceedAgainstRespondent1_1v2.negate())
                        ).and(
                            CaseDataPredicate.Applicant.hasProceedAgainstRespondent2_1v2
                                .and(CaseDataPredicate.Applicant.willProceedAgainstRespondent2_1v2.negate())
                        ).test(c);
                    case ONE_V_ONE ->
                        CaseDataPredicate.Applicant.hasProceedDecision
                            .and(CaseDataPredicate.Applicant.willProceed.negate()).test(c);
                    case TWO_V_ONE -> (CaseDataPredicate.Applicant.hasProceedMulti_2v1
                        .and(CaseDataPredicate.Applicant.willProceedMulti_2v1.negate())
                    ).and(
                        CaseDataPredicate.Applicant.hasProceedApplicant2Multi_2v1
                            .and(CaseDataPredicate.Applicant.willProceedApplicant2Multi_2v1.negate())
                    ).test(c);
                };
            }
        };

}
