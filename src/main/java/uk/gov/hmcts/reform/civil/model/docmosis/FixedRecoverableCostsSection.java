package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FixedRecoverableCostsSection extends FixedRecoverableCosts {

    private String bandText;

    public FixedRecoverableCostsSection(
        YesOrNo isSubjectToFixedRecoverableCostRegime,
        ComplexityBand band,
        YesOrNo complexityBandingAgreed,
        String reasons,
        Document frcSupportingDocument,
        String bandText
    ) {
        super(isSubjectToFixedRecoverableCostRegime, band, complexityBandingAgreed, reasons, frcSupportingDocument);
        this.bandText = bandText;
    }

    public static FixedRecoverableCostsSection from(FixedRecoverableCosts fixedrecoverableCosts) {
        if (fixedrecoverableCosts == null) {
            return null;
        }
        return new FixedRecoverableCostsSection(
            fixedrecoverableCosts.getIsSubjectToFixedRecoverableCostRegime(),
            fixedrecoverableCosts.getBand(),
            fixedrecoverableCosts.getComplexityBandingAgreed(),
            fixedrecoverableCosts.getReasons(),
            fixedrecoverableCosts.getFrcSupportingDocument(),
            fixedrecoverableCosts.getBand() != null ? fixedrecoverableCosts.getBand().getLabel() : null
        );
    }
}
