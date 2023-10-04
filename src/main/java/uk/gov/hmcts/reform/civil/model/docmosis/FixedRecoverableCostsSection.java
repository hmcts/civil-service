package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)

public class FixedRecoverableCostsSection extends FixedRecoverableCosts {

    private final String bandText;

    public static FixedRecoverableCostsSection from(uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts fixedrecoverableCosts) {
        if (fixedrecoverableCosts == null) {
            return null;
        }
        return FixedRecoverableCostsSection.builder()
            .isSubjectToFixedRecoverableCostRegime(fixedrecoverableCosts.getIsSubjectToFixedRecoverableCostRegime())
            .complexityBandingAgreed(fixedrecoverableCosts.getComplexityBandingAgreed())
            .band(fixedrecoverableCosts.getBand())
            .bandText(fixedrecoverableCosts.getBand() != null ? fixedrecoverableCosts.getBand().getLabel() : null)
            .reasons(fixedrecoverableCosts.getReasons())
            .build();
    }
}
