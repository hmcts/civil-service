package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import static uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements.OTHER_SUPPORT;

@Data
@AllArgsConstructor
public class DisabilityRequirement {

    private final String requirement;

    public static final DisabilityRequirement toDisabilityRequirements(final SupportRequirements requirement, String otherSupport) {
        String requirementGenerated = requirement != OTHER_SUPPORT ? requirement.getDisplayedValue() : otherSupport;
        return new DisabilityRequirement(requirementGenerated);
    }
}
