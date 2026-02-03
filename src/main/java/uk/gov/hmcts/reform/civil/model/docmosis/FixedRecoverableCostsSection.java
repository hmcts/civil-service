package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FixedRecoverableCostsSection extends FixedRecoverableCosts {

    private String bandText;

    public static FixedRecoverableCostsSection from(uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts fixedrecoverableCosts) {
        if (fixedrecoverableCosts == null) {
            return null;
        }
        FixedRecoverableCostsSection section = new FixedRecoverableCostsSection();
        section.setIsSubjectToFixedRecoverableCostRegime(fixedrecoverableCosts.getIsSubjectToFixedRecoverableCostRegime());
        section.setComplexityBandingAgreed(fixedrecoverableCosts.getComplexityBandingAgreed());
        section.setBand(fixedrecoverableCosts.getBand());
        section.setBandText(fixedrecoverableCosts.getBand() != null ? fixedrecoverableCosts.getBand().getLabel() : null);
        section.setReasons(fixedrecoverableCosts.getReasons());
        return section;
    }
}
