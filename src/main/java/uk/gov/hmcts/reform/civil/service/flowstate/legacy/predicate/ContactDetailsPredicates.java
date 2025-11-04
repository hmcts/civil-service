package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Cohesive predicates related to contact details changes within the flow state.
 * Logic copied as-is from transition builders to avoid functional changes.
 */
public final class ContactDetailsPredicates {

    private ContactDetailsPredicates() {
        // Utility class
    }

    /**
     * Predicate that indicates a contact details change path in the flow.
     * Mirrors logic from ClaimIssuedTransitionBuilder.contactDetailsChange.
     */
    public static final Predicate<CaseData> contactDetailsChange = caseData ->
        YesOrNo.NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired());
}
