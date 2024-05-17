package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class FixedRecoverableCosts {

    private YesOrNo isSubjectToFixedRecoverableCostRegime;
    private ComplexityBand band;
    private YesOrNo complexityBandingAgreed;
    private String reasons;
    // below used in intermediate claim FRC
    private Document frcSupportingDocument;
}
