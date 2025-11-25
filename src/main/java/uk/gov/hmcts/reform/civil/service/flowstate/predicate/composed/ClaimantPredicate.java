package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@SuppressWarnings("java:S1214")
public interface ClaimantPredicate {

    @BusinessRule(
        group = "Claimant",
        summary = "Before applicant response",
        description = "Determines whether the applicant(s) initial response date exists"
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
        description = "Determines whether the applicant(s) has decided to proceed with the claim in their multi-party scenario"
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
        description = "Determines whether the applicant(s) has decided not to proceed with the claim in their multi-party scenario"
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
